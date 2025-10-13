package com.travelbackendapp.travelmanagement.service.impl;

import com.travelbackendapp.travelmanagement.model.entity.ReportRecord;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import com.travelbackendapp.travelmanagement.model.entity.ReviewItem;
import com.travelbackendapp.travelmanagement.repository.ReportsRepository;
import com.travelbackendapp.travelmanagement.repository.ToursRepository;
import com.travelbackendapp.travelmanagement.repository.ReviewsRepository;
import com.travelbackendapp.travelmanagement.service.TravelReportsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of travel reports service for specific business reports
 */
@Singleton
public class TravelReportsServiceImpl implements TravelReportsService {
    
    private static final Logger log = LoggerFactory.getLogger(TravelReportsServiceImpl.class);
    
    private final ReportsRepository reportsRepository;
    private final ToursRepository toursRepository;
    private final ReviewsRepository reviewsRepository;
    
    @Inject
    public TravelReportsServiceImpl(ReportsRepository reportsRepository, ToursRepository toursRepository, ReviewsRepository reviewsRepository) {
        this.reportsRepository = reportsRepository;
        this.toursRepository = toursRepository;
        this.reviewsRepository = reviewsRepository;
    }
    
    // ========== TRAVEL AGENT PERFORMANCE REPORTS ==========
    
    @Override
    public Map<String, Object> generateTravelAgentPerformanceReport(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.info("Generating travel agent performance report for {} period {} year {}", agentEmail, period, year);
            
            // Validate access
            if (!validateAccess("AGENT", agentEmail, requesterRole)) {
                throw new SecurityException("Access denied to agent performance data");
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // Get current period data for comparison with previous period
            
            // Tours processed
            Integer toursProcessed = getToursProcessedByAgent(agentEmail, period, year, requesterRole);
            Integer previousToursProcessed = getToursProcessedByAgentForPreviousPeriod(agentEmail, period, year, requesterRole);
            Double toursProcessedDelta = calculatePercentageDelta(toursProcessed.doubleValue(), previousToursProcessed.doubleValue());
            
            // Feedback metrics
            Double averageFeedback = getAverageFeedbackRate(agentEmail, period, year, requesterRole);
            Double previousAverageFeedback = getAverageFeedbackRateForPreviousPeriod(agentEmail, period, year, requesterRole);
            Double averageFeedbackDelta = calculatePercentageDelta(averageFeedback, previousAverageFeedback);
            
            Integer minimumFeedback = getMinimumFeedbackRate(agentEmail, period, year, requesterRole);
            Double previousMinimumFeedback = getMinimumFeedbackRateForPreviousPeriod(agentEmail, period, year, requesterRole).doubleValue();
            Double minimumFeedbackDelta = calculatePercentageDelta(minimumFeedback.doubleValue(), previousMinimumFeedback);
            
            // Revenue metrics
            Double revenue = getRevenueForFinishedTours(agentEmail, period, year, requesterRole);
            Double previousRevenue = getRevenueForFinishedToursForPreviousPeriod(agentEmail, period, year, requesterRole);
            log.info("Revenue: {} Previous Revenue: {}", revenue, previousRevenue);
            Double revenueDelta = calculatePercentageDelta(revenue, previousRevenue);
            
            // Build report
            report.put("agentEmail", agentEmail);
            report.put("period", period);
            report.put("year", year);
            report.put("toursProcessed", toursProcessed);
            report.put("toursProcessedDelta", toursProcessedDelta);
            report.put("averageFeedbackRate", averageFeedback);
            report.put("averageFeedbackRateDelta", averageFeedbackDelta);
            report.put("minimumFeedbackRate", minimumFeedback);
            report.put("minimumFeedbackRateDelta", minimumFeedbackDelta);
            report.put("revenue", revenue);
            report.put("revenueDelta", revenueDelta);
            report.put("reportGeneratedAt", LocalDateTime.now().toString());
            
            return report;
            
        } catch (Exception e) {
            log.error("Error generating travel agent performance report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate travel agent performance report", e);
        }
    }
    
    @Override
    public Integer getToursProcessedByAgent(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting tours processed by agent: {} for period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0;
            }
            
            List<ReportRecord> reports = getReportsForPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in period: {} year: {}", agentEmail, period, year);
                return 0;
            }
            
            // Count actual bookings processed (only FINISH events - completed sales)
            long bookingsCount = reports.stream()
                .filter(this::isFinishedBooking)
                .count();
            
            log.debug("Found {} bookings sold by agent: {}", bookingsCount, agentEmail);
            return (int) bookingsCount;
            
        } catch (Exception e) {
            log.error("Error getting tours processed by agent: {} - {}", agentEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to get tours processed by agent", e);
        }
    }
    
    
    @Override
    public Double getAverageFeedbackRate(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting average feedback rate for agent: {} period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0.0;
            }
            
            List<ReportRecord> reports = getReportsForPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in period: {} year: {}", agentEmail, period, year);
                return 0.0;
            }
            
            // Extract tour IDs from finished bookings only
            Set<String> tourIds = reports.stream()
                .filter(this::isFinishedBooking)
                .filter(r -> r != null && r.getTourId() != null)
                .map(ReportRecord::getTourId)
                .collect(Collectors.toSet());
            
            if (tourIds.isEmpty()) {
                log.debug("No finished bookings found for agent: {} in period: {} year: {}", agentEmail, period, year);
                return 0.0;
            }
            
            // Get reviews for all tours associated with this agent's finished bookings
            List<ReviewItem> allReviews = new ArrayList<>();
            for (String tourId : tourIds) {
                try {
                    List<ReviewItem> tourReviews = reviewsRepository.scanByTourId(tourId);
                    if (tourReviews != null && !tourReviews.isEmpty()) {
                        // Filter reviews by the same period
                        LocalDateTime periodStart = getPeriodStart(period, year);
                        LocalDateTime periodEnd = getPeriodEnd(period, year);
                        
                        List<ReviewItem> filteredReviews = tourReviews.stream()
                            .filter(review -> {
                                if (review.getCreatedAt() == null) {
                                    return false;
                                }
                                try {
                                    LocalDateTime reviewDate = parseReviewDate(review.getCreatedAt());
                                    if (reviewDate == null) {
                                        return false;
                                    }
                                    return !reviewDate.isBefore(periodStart) && !reviewDate.isAfter(periodEnd);
                                } catch (Exception e) {
                                    log.warn("Failed to parse review date: {}", review.getCreatedAt(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                        
                        allReviews.addAll(filteredReviews);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get reviews for tour: {} - {}", tourId, e.getMessage());
                }
            }
            
            double average = calculateAverageRating(allReviews);
            log.debug("Average feedback rate for agent: {} is {} (from {} reviews across {} tours)", agentEmail, average, allReviews.size(), tourIds.size());
            return average;
            
        } catch (Exception e) {
            log.error("Error getting average feedback rate for agent: {} - {}", agentEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to get average feedback rate", e);
        }
    }
    
    @Override
    public Integer getMinimumFeedbackRate(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting minimum feedback rate for agent: {} period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0;
            }
            
            List<ReportRecord> reports = getReportsForPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in period: {} year: {}", agentEmail, period, year);
                return 0;
            }
            
            // Extract tour IDs from finished bookings only
            Set<String> tourIds = reports.stream()
                .filter(this::isFinishedBooking)
                .filter(r -> r != null && r.getTourId() != null)
                .map(ReportRecord::getTourId)
                .collect(Collectors.toSet());
            
            if (tourIds.isEmpty()) {
                log.debug("No finished bookings found for agent: {} in period: {} year: {}", agentEmail, period, year);
                return 0;
            }
            
            // Get reviews for all tours associated with this agent's finished bookings
            List<ReviewItem> allReviews = new ArrayList<>();
            for (String tourId : tourIds) {
                try {
                    List<ReviewItem> tourReviews = reviewsRepository.scanByTourId(tourId);
                    if (tourReviews != null && !tourReviews.isEmpty()) {
                        // Filter reviews by the same period
                        LocalDateTime periodStart = getPeriodStart(period, year);
                        LocalDateTime periodEnd = getPeriodEnd(period, year);
                        
                        List<ReviewItem> filteredReviews = tourReviews.stream()
                            .filter(review -> {
                                if (review.getCreatedAt() == null) {
                                    return false;
                                }
                                try {
                                    LocalDateTime reviewDate = parseReviewDate(review.getCreatedAt());
                                    if (reviewDate == null) {
                                        return false;
                                    }
                                    return !reviewDate.isBefore(periodStart) && !reviewDate.isAfter(periodEnd);
                                } catch (Exception e) {
                                    log.warn("Failed to parse review date: {}", review.getCreatedAt(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                        
                        allReviews.addAll(filteredReviews);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get reviews for tour: {} - {}", tourId, e.getMessage());
                }
            }
            
            int minimum = calculateMinimumRating(allReviews);
            log.debug("Minimum feedback rate for agent: {} is {} (from {} reviews across {} tours)", agentEmail, minimum, allReviews.size(), tourIds.size());
            return minimum;
            
        } catch (Exception e) {
            log.error("Error getting minimum feedback rate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get minimum feedback rate", e);
        }
    }
    
    
    @Override
    public Double getRevenueForFinishedTours(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting revenue for finished tours for agent: {} period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0.0;
            }
            
            List<ReportRecord> reports = getReportsForPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in period: {} year: {}", agentEmail, period, year);
                return 0.0;
            }
            
            double revenue = reports.stream()
                .filter(this::isCompletedSale)
                .mapToDouble(ReportRecord::getTotalPrice)
                .sum();
            
            log.debug("Revenue for confirmed and finished tours for agent: {} is {}", agentEmail, revenue);
            return revenue;
            
        } catch (Exception e) {
            log.error("Error getting revenue for finished tours for agent: {} - {}", agentEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to get revenue for finished tours", e);
        }
    }
    
    
    // ========== SALES STATISTICS REPORTS ==========
    
    @Override
    public Map<String, Object> generateSalesStatisticsReport(String tourId, String period, Integer year, String requesterRole) {
        try {
            log.info("Generating sales statistics report for {} period {} year {}", tourId, period, year);
            
            // Validate access
            if (!validateAccess("TOUR", tourId, requesterRole)) {
                throw new SecurityException("Access denied to tour sales data");
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // Tours sold
            Integer toursSold = getToursSoldToResort(tourId, period, year, requesterRole);
            Integer previousToursSold = getToursSoldToResortForPreviousPeriod(tourId, period, year, requesterRole);
            Double toursSoldDelta = calculatePercentageDelta(toursSold.doubleValue(), previousToursSold.doubleValue());
            
            // Feedback metrics
            Double averageFeedback = getAverageFeedbackRateForTour(tourId, period, year, requesterRole);
            log.info("Average feedback rate for tour: {} is {}", tourId, averageFeedback);
            log.info("TEST TEST TEST");
            Integer minimumFeedback = getMinimumFeedbackRateForTour(tourId, period, year, requesterRole);
            log.info("Minimum feedback rate for tour: {} is {}", tourId, minimumFeedback);
            Double previousAverageFeedback = getAverageFeedbackRateForTourForPreviousPeriod(tourId, period, year, requesterRole);
            log.info("Previous average feedback rate for tour: {} is {}", tourId, previousAverageFeedback);
            Double averageFeedbackDelta = calculatePercentageDelta(averageFeedback, previousAverageFeedback);
            log.info("Average feedback rate delta for tour: {} is {}", tourId, averageFeedbackDelta);
            
            // Revenue metrics
            Double revenue = getRevenueForFinishedToursByTour(tourId, period, year, requesterRole);
            Double previousRevenue = getRevenueForFinishedToursByTourForPreviousPeriod(tourId, period, year, requesterRole);
            Double revenueDelta = calculatePercentageDelta(revenue, previousRevenue);
            
            // Get details of all tours processed in this period from the entire reports table
            List<Map<String, Object>> allToursDetails = getAllToursDetailsFromReportsTable(period, year, requesterRole);
            
            // Build report
            report.put("tourId", tourId);
            report.put("period", period);
            report.put("year", year);
            report.put("toursSold", toursSold);
            report.put("toursSoldDelta", toursSoldDelta);
            report.put("averageFeedbackRate", averageFeedback);
            report.put("minimumFeedbackRate", minimumFeedback);
            report.put("averageFeedbackRateDelta", averageFeedbackDelta);
            report.put("revenue", revenue);
            report.put("revenueDelta", revenueDelta);
            report.put("allToursProcessed", allToursDetails);
            report.put("reportGeneratedAt", LocalDateTime.now().toString());
            
            return report;
            
        } catch (Exception e) {
            log.error("Error generating sales statistics report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate sales statistics report", e);
        }
    }
    
    @Override
    public Integer getToursSoldToResort(String tourId, String period, Integer year, String requesterRole) {
        try {
            List<ReportRecord> reports = getReportsForPeriodByTour(tourId, period, year, requesterRole);
            
            // Count actual sales (only FINISH events - completed sales)
            long salesCount = reports.stream()
                .filter(this::isFinishedBooking)
                .count();
            
            return (int) salesCount;
            
        } catch (Exception e) {
            log.error("Error getting tours sold to resort: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get tours sold to resort", e);
        }
    }
    
    
    @Override
    public Double getAverageFeedbackRateForTour(String tourId, String period, Integer year, String requesterRole) {
        try {
            // Get all reviews for the period and filter by tour
            List<ReviewItem> allReviews = getAllReviewsForPeriod(period, year);
            List<ReviewItem> tourReviews = allReviews.stream()
                .filter(review -> tourId.equals(review.getTourId()))
                .collect(Collectors.toList());
            return calculateAverageRating(tourReviews);
            
        } catch (Exception e) {
            log.error("Error getting average feedback rate for tour: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get average feedback rate for tour", e);
        }
    }
    
    @Override
    public Integer getMinimumFeedbackRateForTour(String tourId, String period, Integer year, String requesterRole) {
        try {
            // Get all reviews for the period and filter by tour
            List<ReviewItem> allReviews = getAllReviewsForPeriod(period, year);
            List<ReviewItem> tourReviews = allReviews.stream()
                .filter(review -> tourId.equals(review.getTourId()))
                .collect(Collectors.toList());
            return calculateMinimumRating(tourReviews);
            
        } catch (Exception e) {
            log.error("Error getting minimum feedback rate for tour: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get minimum feedback rate for tour", e);
        }
    }
    
    
    @Override
    public Double getRevenueForFinishedToursByTour(String tourId, String period, Integer year, String requesterRole) {
        try {
            List<ReportRecord> reports = getReportsForPeriodByTour(tourId, period, year, requesterRole);
            
            return reports.stream()
                .filter(this::isCompletedSale)
                .mapToDouble(ReportRecord::getTotalPrice)
                .sum();
            
        } catch (Exception e) {
            log.error("Error getting revenue for finished tours by tour: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get revenue for finished tours by tour", e);
        }
    }
    
    
    // ========== PERIOD COMPARISON UTILITIES ==========
    
    @Override
    public String getPreviousPeriod(String period, Integer year) {
        // For period comparison, we need to calculate the actual previous period
        // This method should return the same period type but for the previous time frame
        
        if ("WEEKLY".equalsIgnoreCase(period)) {
            // Previous week - this is handled by getPreviousPeriodStart/End methods
            return "WEEKLY";
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            // Previous month - this is handled by getPreviousPeriodStart/End methods
            return "MONTHLY";
        } else if ("QUARTERLY".equalsIgnoreCase(period)) {
            // Previous quarter - this is handled by getPreviousPeriodStart/End methods  
            return "QUARTERLY";
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            // Previous year - this is handled by getPreviousPeriodStart/End methods
            return "YEARLY";
        }
        
        // Default to same period if unknown
        return period;
    }
    
    @Override
    public Double calculatePercentageDelta(Double currentValue, Double previousValue) {
        if (previousValue == null || previousValue == 0.0) {
            return currentValue != null && currentValue > 0 ? 100.0 : 0.0;
        }
        
        return ((currentValue - previousValue) / previousValue) * 100.0;
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Check if a report record represents a completed sale (FINISH event with valid price)
     */
    private boolean isCompletedSale(ReportRecord record) {
        return record != null && 
               "FINISH".equals(record.getEventType()) && 
               record.getTotalPrice() != null && 
               record.getTotalPrice() > 0;
    }
    
    /**
     * Check if a report record represents a completed sale (FINISH event only)
     */
    private boolean isFinishedBooking(ReportRecord record) {
        return record != null && "FINISH".equals(record.getEventType());
    }
    
    private List<ReportRecord> getReportsForPeriod(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            // Query all reports for the agent
            List<ReportRecord> allReports = reportsRepository.findByAgentEmail(agentEmail);
            
            // Filter by period and year based on eventTimestamp
            LocalDateTime periodStart = getPeriodStart(period, year);
            LocalDateTime periodEnd = getPeriodEnd(period, year);
            
            List<ReportRecord> filteredReports = allReports.stream()
                .filter(report -> {
                    if (report.getEventTimestamp() == null) {
                        return false;
                    }
                    LocalDateTime eventTime = parseEventTimestamp(report.getEventTimestamp());
                    if (eventTime == null) {
                        return false;
                    }
                    return !eventTime.isBefore(periodStart) && !eventTime.isAfter(periodEnd);
                })
                .collect(Collectors.toList());
            
            log.info("Found {} reports for agent {} in period {} {}", filteredReports.size(), agentEmail, period, year);
            return filteredReports;
        } catch (Exception e) {
            log.error("Error getting reports for period", e);
            return new ArrayList<>();
        }
    }
    
    private List<ReportRecord> getReportsForPreviousPeriod(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            // Query all reports for the agent
            List<ReportRecord> allReports = reportsRepository.findByAgentEmail(agentEmail);
            
            // Filter by previous period and year based on eventTimestamp
            LocalDateTime previousPeriodStart = getPreviousPeriodStart(period, year);
            LocalDateTime previousPeriodEnd = getPreviousPeriodEnd(period, year);
            
            List<ReportRecord> filteredReports = allReports.stream()
                .filter(report -> {
                    if (report.getEventTimestamp() == null) {
                        return false;
                    }
                    LocalDateTime eventTime = parseEventTimestamp(report.getEventTimestamp());
                    if (eventTime == null) {
                        return false;
                    }
                    return !eventTime.isBefore(previousPeriodStart) && !eventTime.isAfter(previousPeriodEnd);
                })
                .collect(Collectors.toList());
            
            log.info("Found {} reports for agent {} in previous period {} {}", filteredReports.size(), agentEmail, period, year);
            return filteredReports;
        } catch (Exception e) {
            log.error("Error getting reports for previous period", e);
            return new ArrayList<>();
        }
    }
    
    private List<ReportRecord> getReportsForPeriodByTour(String tourId, String period, Integer year, String requesterRole) {
        try {
            // Query all reports for the tour
            List<ReportRecord> allReports = reportsRepository.findByTourId(tourId);
            
            // Filter by period and year based on eventTimestamp
            LocalDateTime periodStart = getPeriodStart(period, year);
            LocalDateTime periodEnd = getPeriodEnd(period, year);
            
            List<ReportRecord> filteredReports = allReports.stream()
                .filter(report -> {
                    if (report.getEventTimestamp() == null) {
                        return false;
                    }
                    LocalDateTime eventTime = parseEventTimestamp(report.getEventTimestamp());
                    if (eventTime == null) {
                        return false;
                    }
                    return !eventTime.isBefore(periodStart) && !eventTime.isAfter(periodEnd);
                })
                .collect(Collectors.toList());
            
            log.info("Found {} reports for tour {} in period {} {}", filteredReports.size(), tourId, period, year);
            return filteredReports;
        } catch (Exception e) {
            log.error("Error getting reports for period by tour", e);
            return new ArrayList<>();
        }
    }
    
    
    
    /**
     * Calculate the start date for the previous period
     */
    private LocalDateTime getPreviousPeriodStart(String period, Integer year) {
        if ("WEEKLY".equalsIgnoreCase(period)) {
            // Previous week (Monday to Sunday)
            LocalDateTime now = LocalDateTime.now();
            int dayOfWeek = now.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            return now.minusDays(dayOfWeek + 6).withHour(0).withMinute(0).withSecond(0);
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            // Previous month
            int currentMonth = LocalDateTime.now().getMonthValue();
            int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
            int previousYear = currentMonth == 1 ? year - 1 : year;
            return LocalDateTime.of(previousYear, previousMonth, 1, 0, 0, 0);
        } else if ("QUARTERLY".equalsIgnoreCase(period)) {
            // Previous quarter
            int currentMonth = LocalDateTime.now().getMonthValue();
            int currentQuarter = (currentMonth - 1) / 3;
            int previousQuarter = currentQuarter == 0 ? 3 : currentQuarter - 1;
            int previousQuarterStartMonth = previousQuarter * 3 + 1;
            int previousYear = currentQuarter == 0 ? year - 1 : year;
            return LocalDateTime.of(previousYear, previousQuarterStartMonth, 1, 0, 0, 0);
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            // Previous year
            return LocalDateTime.of(year - 1, 1, 1, 0, 0, 0);
        }
        // Default to previous month
        int currentMonth = LocalDateTime.now().getMonthValue();
        int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
        int previousYear = currentMonth == 1 ? year - 1 : year;
        return LocalDateTime.of(previousYear, previousMonth, 1, 0, 0, 0);
    }
    
    /**
     * Calculate the end date for the previous period
     */
    private LocalDateTime getPreviousPeriodEnd(String period, Integer year) {
        if ("WEEKLY".equalsIgnoreCase(period)) {
            // End of previous week (Sunday)
            LocalDateTime now = LocalDateTime.now();
            int dayOfWeek = now.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            return now.minusDays(dayOfWeek).withHour(23).withMinute(59).withSecond(59);
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            // End of previous month
            int currentMonth = LocalDateTime.now().getMonthValue();
            int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
            int previousYear = currentMonth == 1 ? year - 1 : year;
            return LocalDateTime.of(previousYear, previousMonth, 1, 0, 0, 0)
                .plusMonths(1)
                .minusDays(1)
                .withHour(23).withMinute(59).withSecond(59);
        } else if ("QUARTERLY".equalsIgnoreCase(period)) {
            // End of previous quarter
            int currentMonth = LocalDateTime.now().getMonthValue();
            int currentQuarter = (currentMonth - 1) / 3;
            int previousQuarter = currentQuarter == 0 ? 3 : currentQuarter - 1;
            int previousQuarterEndMonth = previousQuarter * 3 + 3;
            int previousYear = currentQuarter == 0 ? year - 1 : year;
            return LocalDateTime.of(previousYear, previousQuarterEndMonth, 1, 0, 0, 0)
                .plusMonths(1)
                .minusDays(1)
                .withHour(23).withMinute(59).withSecond(59);
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            // End of previous year
            return LocalDateTime.of(year - 1, 12, 31, 23, 59, 59);
        }
        // Default to end of previous month
        int currentMonth = LocalDateTime.now().getMonthValue();
        int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
        int previousYear = currentMonth == 1 ? year - 1 : year;
        return LocalDateTime.of(previousYear, previousMonth, 1, 0, 0, 0)
            .plusMonths(1)
            .minusDays(1)
            .withHour(23).withMinute(59).withSecond(59);
    }
    
    /**
     * Calculate the start date for a given period and year
     */
    private LocalDateTime getPeriodStart(String period, Integer year) {
        if ("WEEKLY".equalsIgnoreCase(period)) {
            // Current week (Monday to Sunday)
            LocalDateTime now = LocalDateTime.now();
            int dayOfWeek = now.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            return now.minusDays(dayOfWeek - 1).withHour(0).withMinute(0).withSecond(0);
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            // Current month
            return LocalDateTime.of(year, LocalDateTime.now().getMonthValue(), 1, 0, 0, 0);
        } else if ("QUARTERLY".equalsIgnoreCase(period)) {
            // Current quarter
            int currentMonth = LocalDateTime.now().getMonthValue();
            int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
            return LocalDateTime.of(year, quarterStartMonth, 1, 0, 0, 0);
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            // Current year
            return LocalDateTime.of(year, 1, 1, 0, 0, 0);
        }
        // Default to current month
        return LocalDateTime.of(year, LocalDateTime.now().getMonthValue(), 1, 0, 0, 0);
    }
    
    /**
     * Calculate the end date for a given period and year
     */
    private LocalDateTime getPeriodEnd(String period, Integer year) {
        if ("WEEKLY".equalsIgnoreCase(period)) {
            // End of current week (Sunday)
            LocalDateTime now = LocalDateTime.now();
            int dayOfWeek = now.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            return now.plusDays(7 - dayOfWeek).withHour(23).withMinute(59).withSecond(59);
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            // End of current month
            int currentMonth = LocalDateTime.now().getMonthValue();
            return LocalDateTime.of(year, currentMonth, 1, 0, 0, 0)
                .plusMonths(1)
                .minusDays(1)
                .withHour(23).withMinute(59).withSecond(59);
        } else if ("QUARTERLY".equalsIgnoreCase(period)) {
            // End of current quarter
            int currentMonth = LocalDateTime.now().getMonthValue();
            int quarterEndMonth = ((currentMonth - 1) / 3) * 3 + 3;
            return LocalDateTime.of(year, quarterEndMonth, 1, 0, 0, 0)
                .plusMonths(1)
                .minusDays(1)
                .withHour(23).withMinute(59).withSecond(59);
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            // End of current year
            return LocalDateTime.of(year, 12, 31, 23, 59, 59);
        }
        // Default to end of current month
        int currentMonth = LocalDateTime.now().getMonthValue();
        return LocalDateTime.of(year, currentMonth, 1, 0, 0, 0)
            .plusMonths(1)
            .minusDays(1)
            .withHour(23).withMinute(59).withSecond(59);
    }
    
    
    /**
     * Parse event timestamp string to LocalDateTime
     */
    private LocalDateTime parseEventTimestamp(String timestampString) {
        try {
            if (timestampString == null || timestampString.trim().isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(timestampString);
        } catch (Exception e) {
            log.warn("Failed to parse event timestamp: {}", timestampString, e);
            return null;
        }
    }
    
    /**
     * Parse review date string to LocalDateTime (handles both date and datetime formats)
     */
    private LocalDateTime parseReviewDate(String dateString) {
        try {
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }
            
            // Try parsing as full datetime first
            try {
                return LocalDateTime.parse(dateString);
            } catch (Exception e1) {
                // If that fails, try parsing as date and convert to start of day
                try {
                    return LocalDate.parse(dateString).atStartOfDay();
                } catch (Exception e2) {
                    log.warn("Failed to parse review date: {} (tried both datetime and date formats)", dateString);
                    return null;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse review date: {}", dateString, e);
            return null;
        }
    }
    
    /**
     * Get tours processed by agent for previous period
     */
    private Integer getToursProcessedByAgentForPreviousPeriod(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting tours processed by agent for previous period: {} for period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0;
            }
            
            List<ReportRecord> reports = getReportsForPreviousPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in previous period: {} year: {}", agentEmail, period, year);
                return 0;
            }
            
            // Count actual bookings processed (only FINISH events - completed sales)
            long bookingsCount = reports.stream()
                .filter(this::isFinishedBooking)
                .count();
            
            log.debug("Found {} bookings sold by agent in previous period: {}", bookingsCount, agentEmail);
            return (int) bookingsCount;
            
        } catch (Exception e) {
            log.error("Error getting tours processed by agent for previous period: {} - {}", agentEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to get tours processed by agent for previous period", e);
        }
    }
    
    /**
     * Get revenue for finished tours for previous period
     */
    private Double getRevenueForFinishedToursForPreviousPeriod(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting revenue for finished tours for previous period for agent: {} period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0.0;
            }
            
            List<ReportRecord> reports = getReportsForPreviousPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in previous period: {} year: {}", agentEmail, period, year);
                return 0.0;
            }
            
            double revenue = reports.stream()
                .filter(this::isCompletedSale)
                .mapToDouble(ReportRecord::getTotalPrice)
                .sum();
            
            log.debug("Revenue for confirmed and finished tours for agent in previous period: {} is {}", agentEmail, revenue);
            return revenue;
            
        } catch (Exception e) {
            log.error("Error getting revenue for finished tours for previous period for agent: {} - {}", agentEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to get revenue for finished tours for previous period", e);
        }
    }
    
    /**
     * Get average feedback rate for previous period
     */
    private Double getAverageFeedbackRateForPreviousPeriod(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting average feedback rate for previous period for agent: {} period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0.0;
            }
            
            List<ReportRecord> reports = getReportsForPreviousPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in previous period: {} year: {}", agentEmail, period, year);
                return 0.0;
            }
            
            // Extract tour IDs from finished bookings only
            Set<String> tourIds = reports.stream()
                .filter(this::isFinishedBooking)
                .filter(r -> r != null && r.getTourId() != null)
                .map(ReportRecord::getTourId)
                .collect(Collectors.toSet());
            
            if (tourIds.isEmpty()) {
                log.debug("No finished bookings found for agent: {} in previous period: {} year: {}", agentEmail, period, year);
                return 0.0;
            }
            
            // Get reviews for all tours associated with this agent's finished bookings in previous period
            List<ReviewItem> allReviews = new ArrayList<>();
            for (String tourId : tourIds) {
                try {
                    List<ReviewItem> tourReviews = reviewsRepository.scanByTourId(tourId);
                    if (tourReviews != null && !tourReviews.isEmpty()) {
                        // Filter reviews by the previous period
                        LocalDateTime previousPeriodStart = getPreviousPeriodStart(period, year);
                        LocalDateTime previousPeriodEnd = getPreviousPeriodEnd(period, year);
                        
                        List<ReviewItem> filteredReviews = tourReviews.stream()
                            .filter(review -> {
                                if (review.getCreatedAt() == null) {
                                    return false;
                                }
                                try {
                                    LocalDateTime reviewDate = parseReviewDate(review.getCreatedAt());
                                    if (reviewDate == null) {
                                        return false;
                                    }
                                    return !reviewDate.isBefore(previousPeriodStart) && !reviewDate.isAfter(previousPeriodEnd);
                                } catch (Exception e) {
                                    log.warn("Failed to parse review date: {}", review.getCreatedAt(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                        
                        allReviews.addAll(filteredReviews);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get reviews for tour: {} - {}", tourId, e.getMessage());
                }
            }
            
            double average = calculateAverageRating(allReviews);
            log.debug("Average feedback rate for agent in previous period: {} is {} (from {} reviews across {} tours)", agentEmail, average, allReviews.size(), tourIds.size());
            return average;
            
        } catch (Exception e) {
            log.error("Error getting average feedback rate for previous period for agent: {} - {}", agentEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to get average feedback rate for previous period", e);
        }
    }
    
    /**
     * Get minimum feedback rate for previous period
     */
    private Integer getMinimumFeedbackRateForPreviousPeriod(String agentEmail, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting minimum feedback rate for previous period for agent: {} period: {} year: {}", agentEmail, period, year);
            
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                log.warn("Agent email is null or empty");
                return 0;
            }
            
            List<ReportRecord> reports = getReportsForPreviousPeriod(agentEmail, period, year, requesterRole);
            
            if (reports == null || reports.isEmpty()) {
                log.debug("No reports found for agent: {} in previous period: {} year: {}", agentEmail, period, year);
                return 0;
            }
            
            // Extract tour IDs from finished bookings only
            Set<String> tourIds = reports.stream()
                .filter(this::isFinishedBooking)
                .filter(r -> r != null && r.getTourId() != null)
                .map(ReportRecord::getTourId)
                .collect(Collectors.toSet());
            
            if (tourIds.isEmpty()) {
                log.debug("No finished bookings found for agent: {} in previous period: {} year: {}", agentEmail, period, year);
                return 0;
            }
            
            // Get reviews for all tours associated with this agent's finished bookings in previous period
            List<ReviewItem> allReviews = new ArrayList<>();
            for (String tourId : tourIds) {
                try {
                    List<ReviewItem> tourReviews = reviewsRepository.scanByTourId(tourId);
                    if (tourReviews != null && !tourReviews.isEmpty()) {
                        // Filter reviews by the previous period
                        LocalDateTime previousPeriodStart = getPreviousPeriodStart(period, year);
                        LocalDateTime previousPeriodEnd = getPreviousPeriodEnd(period, year);
                        
                        List<ReviewItem> filteredReviews = tourReviews.stream()
                            .filter(review -> {
                                if (review.getCreatedAt() == null) {
                                    return false;
                                }
                                try {
                                    LocalDateTime reviewDate = parseReviewDate(review.getCreatedAt());
                                    if (reviewDate == null) {
                                        return false;
                                    }
                                    return !reviewDate.isBefore(previousPeriodStart) && !reviewDate.isAfter(previousPeriodEnd);
                                } catch (Exception e) {
                                    log.warn("Failed to parse review date: {}", review.getCreatedAt(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                        
                        allReviews.addAll(filteredReviews);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get reviews for tour: {} - {}", tourId, e.getMessage());
                }
            }
            
            int minimum = calculateMinimumRating(allReviews);
            log.debug("Minimum feedback rate for agent in previous period: {} is {} (from {} reviews across {} tours)", agentEmail, minimum, allReviews.size(), tourIds.size());
            return minimum;
            
        } catch (Exception e) {
            log.error("Error getting minimum feedback rate for previous period for agent: {} - {}", agentEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to get minimum feedback rate for previous period", e);
        }
    }
    
    /**
     * Get tours sold to resort for previous period
     */
    private Integer getToursSoldToResortForPreviousPeriod(String tourId, String period, Integer year, String requesterRole) {
        try {
            List<ReportRecord> reports = getReportsForPreviousPeriodByTour(tourId, period, year, requesterRole);
            
            // Count actual sales (only FINISH events - completed sales)
            long salesCount = reports.stream()
                .filter(this::isFinishedBooking)
                .count();
            
            return (int) salesCount;
            
        } catch (Exception e) {
            log.error("Error getting tours sold to resort for previous period: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get tours sold to resort for previous period", e);
        }
    }
    
    /**
     * Get average feedback rate for tour for previous period
     */
    private Double getAverageFeedbackRateForTourForPreviousPeriod(String tourId, String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting average feedback rate for tour for previous period: {} period: {} year: {}", tourId, period, year);
            
            // Get all reviews for previous period and filter by tour
            List<ReviewItem> allReviews = getAllReviewsForPreviousPeriod(period, year);
            List<ReviewItem> tourReviews = allReviews.stream()
                .filter(review -> tourId.equals(review.getTourId()))
                .collect(Collectors.toList());
            double rating = calculateAverageRating(tourReviews);
            log.debug("Average rating for tour in previous period: {} is {}", tourId, rating);
            return rating;
            
        } catch (Exception e) {
            log.error("Error getting average feedback rate for tour for previous period: {} - {}", tourId, e.getMessage(), e);
            throw new RuntimeException("Failed to get average feedback rate for tour for previous period", e);
        }
    }
    
    /**
     * Get revenue for finished tours by tour for previous period
     */
    private Double getRevenueForFinishedToursByTourForPreviousPeriod(String tourId, String period, Integer year, String requesterRole) {
        try {
            List<ReportRecord> reports = getReportsForPreviousPeriodByTour(tourId, period, year, requesterRole);
            
            return reports.stream()
                .filter(this::isCompletedSale)
                .mapToDouble(ReportRecord::getTotalPrice)
                .sum();
            
        } catch (Exception e) {
            log.error("Error getting revenue for finished tours by tour for previous period: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get revenue for finished tours by tour for previous period", e);
        }
    }
    
    /**
     * Get reports for previous period by tour
     */
    private List<ReportRecord> getReportsForPreviousPeriodByTour(String tourId, String period, Integer year, String requesterRole) {
        try {
            // Query all reports for the tour
            List<ReportRecord> allReports = reportsRepository.findByTourId(tourId);
            
            // Filter by previous period and year based on eventTimestamp
            LocalDateTime previousPeriodStart = getPreviousPeriodStart(period, year);
            LocalDateTime previousPeriodEnd = getPreviousPeriodEnd(period, year);
            
            List<ReportRecord> filteredReports = allReports.stream()
                .filter(report -> {
                    if (report.getEventTimestamp() == null) {
                        return false;
                    }
                    LocalDateTime eventTime = parseEventTimestamp(report.getEventTimestamp());
                    if (eventTime == null) {
                        return false;
                    }
                    return !eventTime.isBefore(previousPeriodStart) && !eventTime.isAfter(previousPeriodEnd);
                })
                .collect(Collectors.toList());
            
            log.info("Found {} reports for tour {} in previous period {} {}", filteredReports.size(), tourId, period, year);
            return filteredReports;
        } catch (Exception e) {
            log.error("Error getting reports for previous period by tour", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get details of all tours processed in the period from the entire reports table
     */
    private List<Map<String, Object>> getAllToursDetailsFromReportsTable(String period, Integer year, String requesterRole) {
        try {
            log.debug("Getting all tours details from reports table for period: {} year: {}", period, year);
            
            // Get all reports from the entire reports table for the period
            List<ReportRecord> allReports = getAllReportsForPeriod(period, year, requesterRole);
            
            if (allReports == null || allReports.isEmpty()) {
                log.debug("No reports found in period: {} year: {}", period, year);
                return new ArrayList<>();
            }
            
            // Extract unique tour IDs from all reports
            Set<String> uniqueTourIds = allReports.stream()
                .filter(r -> r != null && r.getTourId() != null)
                .map(ReportRecord::getTourId)
                .collect(Collectors.toSet());
            
            List<Map<String, Object>> toursDetails = new ArrayList<>();
            
            // Get details for each unique tour
            for (String tourId : uniqueTourIds) {
                try {
                    Optional<TourItem> tourItem = toursRepository.getById(tourId);
                    if (tourItem.isPresent()) {
                        Map<String, Object> tourDetails = new HashMap<>();
                        TourItem tour = tourItem.get();
                        
                        // Basic tour information
                        tourDetails.put("tourId", tour.getTourId());
                        tourDetails.put("name", tour.getName());
                        tourDetails.put("destination", tour.getDestination());
                        tourDetails.put("rating", tour.getRating());
                        tourDetails.put("reviews", tour.getReviews());
                        tourDetails.put("hotelName", tour.getHotelName());
                        tourDetails.put("accommodation", tour.getAccommodation());
                        tourDetails.put("summary", tour.getSummary());
                        
                        // Get statistics for this tour in the period
                        List<ReportRecord> tourReports = allReports.stream()
                            .filter(r -> tourId.equals(r.getTourId()))
                            .collect(Collectors.toList());
                        
                        // Calculate metrics for this tour
                        long bookingsCount = tourReports.stream()
                            .filter(this::isFinishedBooking)
                            .count();
                        
                        double tourRevenue = tourReports.stream()
                            .filter(this::isCompletedSale)
                            .mapToDouble(ReportRecord::getTotalPrice)
                            .sum();
                        
                        // Get reviews from reviews table for current period (all tours in period)
                        List<ReviewItem> currentPeriodReviews = getAllReviewsForPeriod(period, year);
                        double averageRating = calculateAverageRating(currentPeriodReviews);
                        int minimumRating = calculateMinimumRating(currentPeriodReviews);
                        
                        // Get previous period data for delta calculations
                        List<ReportRecord> previousPeriodReports = getReportsForPreviousPeriodByTour(tourId, period, year, requesterRole);
                        
                        // Calculate previous period metrics
                        long previousBookingsCount = previousPeriodReports.stream()
                            .filter(this::isFinishedBooking)
                            .count();
                        
                        double previousRevenue = previousPeriodReports.stream()
                            .filter(this::isCompletedSale)
                            .mapToDouble(ReportRecord::getTotalPrice)
                            .sum();
                        
                        // Get reviews from reviews table for previous period (all tours in previous period)
                        List<ReviewItem> previousPeriodReviews = getAllReviewsForPreviousPeriod(period, year);
                        double previousAverageRating = calculateAverageRating(previousPeriodReviews);
                        
                        // Calculate deltas
                        Double toursSoldDelta = calculatePercentageDelta((double) bookingsCount, (double) previousBookingsCount);
                        Double revenueDelta = calculatePercentageDelta(tourRevenue, previousRevenue);
                        Double averageFeedbackDelta = calculatePercentageDelta(averageRating, previousAverageRating);
                        
                        // Add calculated metrics
                        tourDetails.put("bookingsCount", (int) bookingsCount);
                        tourDetails.put("revenue", tourRevenue);
                        tourDetails.put("averageRating", averageRating);
                        tourDetails.put("minimumRating", minimumRating);
                        tourDetails.put("totalRatings", tour.getReviews() != null ? tour.getReviews() : 0);
                        tourDetails.put("toursSoldDelta", toursSoldDelta);
                        tourDetails.put("revenueDelta", revenueDelta);
                        tourDetails.put("averageFeedbackDelta", averageFeedbackDelta);
                        
                        toursDetails.add(tourDetails);
                        
                        log.debug("Added tour details for: {} with {} bookings and revenue: {}", 
                                tour.getName(), bookingsCount, tourRevenue);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get details for tour: {} - {}", tourId, e.getMessage());
                }
            }
            
            log.info("Found {} unique tours with details from reports table for period {} {}", toursDetails.size(), period, year);
            return toursDetails;
            
        } catch (Exception e) {
            log.error("Error getting all tours details from reports table for period: {} - {}", period, e.getMessage(), e);
        return new ArrayList<>();
        }
    }
    
    /**
     * Get all reports from the reports table for a specific period
     */
    private List<ReportRecord> getAllReportsForPeriod(String period, Integer year, String requesterRole) {
        try {
            // Get all reports from the reports table
            List<ReportRecord> allReports = reportsRepository.findAll();
            
            if (allReports == null || allReports.isEmpty()) {
                log.debug("No reports found in the reports table");
                return new ArrayList<>();
            }
            
            // Filter by period and year based on eventTimestamp
            LocalDateTime periodStart = getPeriodStart(period, year);
            LocalDateTime periodEnd = getPeriodEnd(period, year);
            
            List<ReportRecord> filteredReports = allReports.stream()
                .filter(report -> {
                    if (report.getEventTimestamp() == null) {
                        return false;
                    }
                    LocalDateTime eventTime = parseEventTimestamp(report.getEventTimestamp());
                    if (eventTime == null) {
                        return false;
                    }
                    return !eventTime.isBefore(periodStart) && !eventTime.isAfter(periodEnd);
                })
                .collect(Collectors.toList());
            
            log.info("Found {} reports from entire reports table for period {} {}", filteredReports.size(), period, year);
            return filteredReports;
        } catch (Exception e) {
            log.error("Error getting all reports for period", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all reports from the reports table for the previous period
     */
    private List<ReportRecord> getAllReportsForPreviousPeriod(String period, Integer year, String requesterRole) {
        try {
            // Get all reports from the reports table
            List<ReportRecord> allReports = reportsRepository.findAll();
            
            if (allReports == null || allReports.isEmpty()) {
                log.debug("No reports found in the reports table");
                return new ArrayList<>();
            }
            
            // Filter by previous period and year based on eventTimestamp
            LocalDateTime previousPeriodStart = getPreviousPeriodStart(period, year);
            LocalDateTime previousPeriodEnd = getPreviousPeriodEnd(period, year);
            
            List<ReportRecord> filteredReports = allReports.stream()
                .filter(report -> {
                    if (report.getEventTimestamp() == null) {
                        return false;
                    }
                    LocalDateTime eventTime = parseEventTimestamp(report.getEventTimestamp());
                    if (eventTime == null) {
                        return false;
                    }
                    return !eventTime.isBefore(previousPeriodStart) && !eventTime.isAfter(previousPeriodEnd);
                })
                .collect(Collectors.toList());
            
            log.info("Found {} reports from entire reports table for previous period {} {}", filteredReports.size(), period, year);
            return filteredReports;
        } catch (Exception e) {
            log.error("Error getting all reports for previous period", e);
            return new ArrayList<>();
        }
    }
    
    private boolean validateAccess(String resourceType, String resourceId, String requesterRole) {
        if ("ADMIN".equals(requesterRole)) {
            return true; // Admins can access all data
        }
        if ("TRAVEL_AGENT".equals(requesterRole)) {
            if ("AGENT".equals(resourceType)) {
                // Agents can only access their own data
                // This would need to be implemented with proper user context
                return true; // Simplified for now
            }
            if ("TOUR".equals(resourceType)) {
                return true; // Agents can access tour data
            }
            return false;
        }
        return false; // Customers cannot access reports
    }
    
    
    /**
     * Get reviews for all tours in a given period
     */
    private List<ReviewItem> getAllReviewsForPeriod(String period, Integer year) {
        try {
            log.debug("Getting all reviews for period: {} year: {}", period, year);
            
            // Get all reports for the period to find which tours had activity
            List<ReportRecord> allReports = getAllReportsForPeriod(period, year, "ADMIN");
            
            if (allReports == null || allReports.isEmpty()) {
                log.debug("No reports found for period: {} year: {}", period, year);
                return new ArrayList<>();
            }
            
            // Extract unique tour IDs from reports
            //filter the reports only those who are finish event
            Set<String> uniqueTourIds = allReports.stream()
                .filter(r -> r != null && r.getTourId() != null)
                .filter(this::isFinishedBooking)
                .map(ReportRecord::getTourId)
                .collect(Collectors.toSet());
            
            List<ReviewItem> allPeriodReviews = new ArrayList<>();
            
            // Get reviews for each tour that had activity in the period
            for (String tourId : uniqueTourIds) {
                try {
                    List<ReviewItem> tourReviews = reviewsRepository.scanByTourId(tourId);
                    if (tourReviews != null && !tourReviews.isEmpty()) {
                        // Filter by period based on createdAt date
                        LocalDateTime periodStart = getPeriodStart(period, year);
                        LocalDateTime periodEnd = getPeriodEnd(period, year);
                        
                        List<ReviewItem> filteredTourReviews = tourReviews.stream()
                            .filter(review -> {
                                if (review.getCreatedAt() == null) {
                                    return false;
                                }
                                try {
                                    LocalDateTime reviewDate = parseReviewDate(review.getCreatedAt());
                                    if (reviewDate == null) {
                                        return false;
                                    }
                                    return !reviewDate.isBefore(periodStart) && !reviewDate.isAfter(periodEnd);
                                } catch (Exception e) {
                                    log.warn("Failed to parse review date: {}", review.getCreatedAt(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                        
                        allPeriodReviews.addAll(filteredTourReviews);
                        log.debug("Found {} reviews for tour {} in period {} {}", filteredTourReviews.size(), tourId, period, year);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get reviews for tour: {} - {}", tourId, e.getMessage());
                }
            }
            
            log.info("Found {} total reviews across all tours in period {} {}", allPeriodReviews.size(), period, year);
            return allPeriodReviews;
            
        } catch (Exception e) {
            log.error("Error getting all reviews for period: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    
    /**
     * Get reviews for all tours in the previous period
     */
    private List<ReviewItem> getAllReviewsForPreviousPeriod(String period, Integer year) {
        try {
            log.debug("Getting all reviews for previous period: {} year: {}", period, year);
            
            // Get all reports for the previous period to find which tours had activity
            List<ReportRecord> allReports = getAllReportsForPreviousPeriod(period, year, "ADMIN");
            
            if (allReports == null || allReports.isEmpty()) {
                log.debug("No reports found for previous period: {} year: {}", period, year);
                return new ArrayList<>();
            }
            
            // Extract unique tour IDs from reports
            Set<String> uniqueTourIds = allReports.stream()
                .filter(r -> r != null && r.getTourId() != null)
                .map(ReportRecord::getTourId)
                .collect(Collectors.toSet());
            
            List<ReviewItem> allPreviousPeriodReviews = new ArrayList<>();
            
            // Get reviews for each tour that had activity in the previous period
            for (String tourId : uniqueTourIds) {
                try {
                    List<ReviewItem> tourReviews = reviewsRepository.scanByTourId(tourId);
                    if (tourReviews != null && !tourReviews.isEmpty()) {
                        // Filter by previous period based on createdAt date
                        LocalDateTime previousPeriodStart = getPreviousPeriodStart(period, year);
                        LocalDateTime previousPeriodEnd = getPreviousPeriodEnd(period, year);
                        
                        List<ReviewItem> filteredTourReviews = tourReviews.stream()
                            .filter(review -> {
                                if (review.getCreatedAt() == null) {
                                    return false;
                                }
                                try {
                                    LocalDateTime reviewDate = parseReviewDate(review.getCreatedAt());
                                    if (reviewDate == null) {
                                        return false;
                                    }
                                    return !reviewDate.isBefore(previousPeriodStart) && !reviewDate.isAfter(previousPeriodEnd);
                                } catch (Exception e) {
                                    log.warn("Failed to parse review date: {}", review.getCreatedAt(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                        
                        allPreviousPeriodReviews.addAll(filteredTourReviews);
                        log.debug("Found {} reviews for tour {} in previous period {} {}", filteredTourReviews.size(), tourId, period, year);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get reviews for tour: {} - {}", tourId, e.getMessage());
                }
            }
            
            log.info("Found {} total reviews across all tours in previous period {} {}", allPreviousPeriodReviews.size(), period, year);
            return allPreviousPeriodReviews;
            
        } catch (Exception e) {
            log.error("Error getting all reviews for previous period: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculate average rating from a list of reviews
     */
    private double calculateAverageRating(List<ReviewItem> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        
        return reviews.stream()
            .filter(review -> review.getRate() != null && review.getRate() > 0)
            .mapToInt(ReviewItem::getRate)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calculate minimum rating from a list of reviews
     */
    private int calculateMinimumRating(List<ReviewItem> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0;
        }
        
        return reviews.stream()
            .filter(review -> review.getRate() != null && review.getRate() > 0)
            .mapToInt(ReviewItem::getRate)
            .min()
            .orElse(0);
    }
}
