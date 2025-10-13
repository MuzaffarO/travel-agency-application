package com.travelbackendapp.travelmanagement.service;

import java.util.Map;

/**
 * Service for generating specific travel agent and sales statistics reports
 */
public interface TravelReportsService {
    
    // ========== TRAVEL AGENT PERFORMANCE REPORTS ==========
    
    /**
     * Generate travel agent performance report for a specific period
     */
    Map<String, Object> generateTravelAgentPerformanceReport(String agentEmail, String period, Integer year, String requesterRole);
    
    /**
     * Get tours processed by travel agent for a period
     */
    Integer getToursProcessedByAgent(String agentEmail, String period, Integer year, String requesterRole);
    
    /**
     * Get average feedback rate for travel agent
     */
    Double getAverageFeedbackRate(String agentEmail, String period, Integer year, String requesterRole);
    
    /**
     * Get minimum feedback rate for travel agent
     */
    Integer getMinimumFeedbackRate(String agentEmail, String period, Integer year, String requesterRole);
    
    /**
     * Get revenue for tours finished by travel agent
     */
    Double getRevenueForFinishedTours(String agentEmail, String period, Integer year, String requesterRole);
    
    // ========== SALES STATISTICS REPORTS ==========
    
    /**
     * Generate sales statistics report for a specific tour/resort
     */
    Map<String, Object> generateSalesStatisticsReport(String tourId, String period, Integer year, String requesterRole);
    
    /**
     * Get tours sold to a specific resort/tour
     */
    Integer getToursSoldToResort(String tourId, String period, Integer year, String requesterRole);
    
    /**
     * Get average feedback rate for a tour/resort
     */
    Double getAverageFeedbackRateForTour(String tourId, String period, Integer year, String requesterRole);
    
    /**
     * Get minimum feedback rate for a tour/resort
     */
    Integer getMinimumFeedbackRateForTour(String tourId, String period, Integer year, String requesterRole);
    
    /**
     * Get revenue for tours finished for a specific tour/resort
     */
    Double getRevenueForFinishedToursByTour(String tourId, String period, Integer year, String requesterRole);
    
    // ========== PERIOD COMPARISON UTILITIES ==========
    
    /**
     * Get previous period for comparison
     */
    String getPreviousPeriod(String period, Integer year);
    
    /**
     * Calculate percentage delta between two values
     */
    Double calculatePercentageDelta(Double currentValue, Double previousValue);
}
