package com.travelbackendapp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.travelbackendapp.travelmanagement.di.DaggerAppComponent;
import com.travelbackendapp.travelmanagement.service.TravelReportsService;

import com.travelbackendapp.travelmanagement.model.entity.ReportRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.regions.Region;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@DependsOn(name = "${reports_table}", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "${target_table}", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "${travel_agent_table_name}", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "${reviews_table}", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "${bookings_table}", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "${documents_table}", resourceType = ResourceType.DYNAMODB_TABLE)
@LambdaHandler(
    lambdaName = "travel-reports-handler",
	roleName = "travel-api-handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables({
    @EnvironmentVariable(key = "reports_table", value = "${reports_table}"),
    @EnvironmentVariable(key = "table_name", value = "${target_table}"),
    @EnvironmentVariable(key = "travel_agent_table_name", value = "${travel_agent_table_name}"),
    @EnvironmentVariable(key = "reviews_table", value = "${reviews_table}"),
    @EnvironmentVariable(key = "bookings_table", value = "${bookings_table}"),
    @EnvironmentVariable(key = "documents_table", value = "${documents_table}"),
    @EnvironmentVariable(key = "BOOKING_DOCS_BUCKET", value = "${booking-documents-bucket}"),
    @EnvironmentVariable(key = "region", value = "${region}"),
    @EnvironmentVariable(key = "from_email", value = "${from_email}"),
    @EnvironmentVariable(key = "recipient_email", value = "${recipient_email}")
})
public class ReportsSender implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Inject TravelReportsService travelReportsService;
    @Inject ObjectMapper objectMapper;
    @Inject com.travelbackendapp.travelmanagement.repository.ReportsRepository reportsRepository;
    @Inject com.travelbackendapp.travelmanagement.repository.ToursRepository toursRepository;
    
    public ReportsSender() {
        DaggerAppComponent.create().inject(this);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("ReportsSender invoked with input: " + input);
            
            // Get parameters from input
            String reportType = (String) input.getOrDefault("reportType", "agent");
            String tourId = (String) input.get("tourId");
            String period = (String) input.getOrDefault("period", "MONTHLY");
            Integer year = input.containsKey("year") ? 
                Integer.valueOf(String.valueOf(input.get("year"))) : 
                LocalDateTime.now().getYear();
            Boolean sendEmail = input.containsKey("sendEmail") ? 
                Boolean.valueOf(String.valueOf(input.get("sendEmail"))) : 
                false;
            
            // Get recipient email from environment variable
            String recipientEmail = System.getenv("recipient_email");
            
            System.out.println("Report Type: " + reportType);
            System.out.println("Recipient Email (from env): " + recipientEmail);
            System.out.println("Tour ID: " + tourId);
            System.out.println("Period: " + period);
            System.out.println("Year: " + year);
            System.out.println("Send Email: " + sendEmail);
            
            // Validate recipient email for sending
            if (sendEmail && (recipientEmail == null || recipientEmail.isEmpty())) {
                result.put("statusCode", 400);
                result.put("error", "recipient_email environment variable is not set");
                return result;
            }
            
            // Generate report based on type
            if ("agent".equalsIgnoreCase(reportType)) {
                // Get all report records and find unique agent emails
                List<ReportRecord> allReports = reportsRepository.findAll();
                Set<String> agentEmails = new java.util.HashSet<>();
                
                for (ReportRecord reportRecord : allReports) {
                    if (reportRecord.getAgentEmail() != null && !reportRecord.getAgentEmail().isEmpty()) {
                        agentEmails.add(reportRecord.getAgentEmail());
                    }
                }
                
                System.out.println("Found " + agentEmails.size() + " unique agents in reports");
                
                // Generate report for each agent
                List<Map<String, Object>> agentReports = new java.util.ArrayList<>();
                for (String agentEmail : agentEmails) {
                    try {
                        Map<String, Object> report = travelReportsService.generateTravelAgentPerformanceReport(
                            agentEmail, period, year, "ADMIN"
                        );
                        agentReports.add(report);
                    } catch (Exception e) {
                        System.err.println("Error generating report for agent " + agentEmail + ": " + e.getMessage());
                    }
                }
                
                result.put("statusCode", 200);
                result.put("reportType", "All Agents Performance Report");
                result.put("agentCount", agentReports.size());
                result.put("reports", agentReports);
                
                // Generate HTML content
                String htmlContent = generateAllAgentsHtmlReport(agentReports, period, year);
                result.put("htmlPreview", htmlContent);
                
                // Send email if requested
                if (sendEmail) {
                     htmlContent = generateAllAgentsHtmlReport(agentReports, period, year);
                    boolean emailSent = sendEmail(recipientEmail, "All Travel Agents Performance Report - " + period + " " + year, htmlContent);
                    result.put("emailSent", emailSent);
                    result.put("emailRecipient", recipientEmail);
                }
                
            } else if ("sales".equalsIgnoreCase(reportType)) {
                // Generate sales report for all tours in the period
                // Use a default tourId since the new method gets all tours from reports table
                String defaultTourId = "all-tours";
                
                Map<String, Object> report = travelReportsService.generateSalesStatisticsReport(
                    defaultTourId, period, year, "ADMIN"
                );
                
                result.put("statusCode", 200);
                result.put("reportType", "All Tours Sales Statistics Report");
                result.put("period", period);
                result.put("year", year);
                result.put("report", report);
                
                // Generate HTML content
                String htmlContent = generateAllToursSalesHtmlReport(report, period, year);
                result.put("htmlPreview", htmlContent);
                
                // Send email if requested
                if (sendEmail) {
                    boolean emailSent = sendEmail(recipientEmail, "All Tours Sales Statistics Report - " + period + " " + year, htmlContent);
                    result.put("emailSent", emailSent);
                    result.put("emailRecipient", recipientEmail);
                }
                
            } else if ("both".equalsIgnoreCase(reportType)) {
                // Generate both agent and sales reports
                System.out.println("Generating both agent and sales reports");
                
                // Generate agent reports
                List<ReportRecord> allReports = reportsRepository.findAll();
                Set<String> agentEmails = new java.util.HashSet<>();
                
                for (ReportRecord reportRecord : allReports) {
                    if (reportRecord.getAgentEmail() != null && !reportRecord.getAgentEmail().isEmpty()) {
                        agentEmails.add(reportRecord.getAgentEmail());
                    }
                }
                
                System.out.println("Found " + agentEmails.size() + " unique agents in reports");
                
                // Generate report for each agent
                List<Map<String, Object>> agentReports = new java.util.ArrayList<>();
                for (String agentEmail : agentEmails) {
                    try {
                        Map<String, Object> report = travelReportsService.generateTravelAgentPerformanceReport(
                            agentEmail, period, year, "ADMIN"
                        );
                        agentReports.add(report);
                    } catch (Exception e) {
                        System.err.println("Error generating report for agent " + agentEmail + ": " + e.getMessage());
                    }
                }
                
                // Generate sales report
                String defaultTourId = "all-tours";
                Map<String, Object> salesReport = travelReportsService.generateSalesStatisticsReport(
                    defaultTourId, period, year, "ADMIN"
                );
                
                result.put("statusCode", 200);
                result.put("reportType", "Both Agent Performance and Sales Statistics Reports");
                result.put("period", period);
                result.put("year", year);
                result.put("agentCount", agentReports.size());
                result.put("agentReports", agentReports);
                result.put("salesReport", salesReport);
                
                // Generate HTML content for both reports (reuse existing code)
                String agentHtmlContent = generateAllAgentsHtmlReport(agentReports, period, year);
                String salesHtmlContent = generateAllToursSalesHtmlReport(salesReport, period, year);
                result.put("agentHtmlPreview", agentHtmlContent);
                result.put("salesHtmlPreview", salesHtmlContent);
                
                // Send email if requested (one email with both reports combined)
                if (sendEmail) {
                    // Combine both HTML reports into one email
                    String combinedHtml = agentHtmlContent + "<br><br><hr><br><br>" + salesHtmlContent;
                    boolean emailSent = sendEmail(recipientEmail, "Combined Travel Reports - " + period + " " + year, combinedHtml);
                    result.put("emailSent", emailSent);
                    result.put("emailRecipient", recipientEmail);
                }
                
            } else {
                result.put("statusCode", 400);
                result.put("error", "Invalid reportType. Use 'agent', 'sales', or 'both'");
                return result;
            }
            
            result.put("message", "Report generated successfully");
            
        } catch (Exception e) {
            System.err.println("Error in ReportsSender: " + e.getMessage());
            e.printStackTrace();
            result.put("statusCode", 500);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    private String generateAllAgentsHtmlReport(List<Map<String, Object>> agentReports, String period, Integer year) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<title>All Travel Agents Performance Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append("h1 { color: #333; text-align: center; }");
        html.append(".report-meta { text-align: center; margin-bottom: 20px; color: #666; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 20px auto; background-color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: center; font-size: 12px; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; color: #333; }");
        html.append(".positive { color: #28a745; font-weight: bold; }");
        html.append(".negative { color: #dc3545; font-weight: bold; }");
        html.append(".neutral { color: #666; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<h1>All Travel Agents Performance Report</h1>");
        html.append("<div class=\"report-meta\">");
        html.append("<p><strong>Period:</strong> ").append(period).append(" ").append(year).append("</p>");
        html.append("<p><strong>Generated:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("<p><strong>Total Agents:</strong> ").append(agentReports.size()).append("</p>");
        html.append("</div>");
        
        html.append(generateAllAgentsTable(agentReports, period, year));
        
        html.append("</body></html>");
        
        return html.toString();
    }
    private String generateAllAgentsTable(List<Map<String, Object>> agentReports, String period, Integer year) {
        StringBuilder table = new StringBuilder();
        
        // Calculate period dates
        String[] periodDates = calculatePeriodDates(period, year);
        String periodStart = periodDates[0];
        String periodEnd = periodDates[1];
        
        table.append("<table>");
        table.append("<tr>");
        table.append("<th>TA e-mail</th>");
        table.append("<th>Report period start</th>");
        table.append("<th>Report period end</th>");
        table.append("<th>Tours sold</th>");
        table.append("<th>Delta of tours sold to previous period %</th>");
        table.append("<th>Average Feedback rate for Travel experience (1 to 5)</th>");
        table.append("<th>Minimum Feedback rate for Travel experience (1 to 5)</th>");
        table.append("<th>Delta of Average Feedback rate to previous period %</th>");
        table.append("<th>Revenue for tours finished within reported period (USD)</th>");
        table.append("<th>Delta of revenue to previous period %</th>");
        table.append("</tr>");
        
        // Add a row for each agent
        for (Map<String, Object> reportData : agentReports) {
            String agentEmail = (String) reportData.get("agentEmail");
            Integer toursSold = (Integer) reportData.getOrDefault("toursProcessed", 0);
            Double toursSoldDelta = (Double) reportData.get("toursProcessedDelta");
            Double avgFeedback = (Double) reportData.getOrDefault("averageFeedbackRate", 0.0);
            Integer minFeedback = (Integer) reportData.getOrDefault("minimumFeedbackRate", 0);
            Double avgFeedbackDelta = (Double) reportData.get("minimumFeedbackRateDelta");
            Double revenue = (Double) reportData.getOrDefault("revenue", 0.0);
            Double revenueDelta = (Double) reportData.get("revenueDelta");
            
            table.append("<tr>");
            table.append("<td>").append(agentEmail != null ? agentEmail : "N/A").append("</td>");
            table.append("<td>").append(periodStart).append("</td>");
            table.append("<td>").append(periodEnd).append("</td>");
            table.append("<td>").append(toursSold).append("</td>");
            table.append("<td ").append(getDeltaClass(toursSoldDelta)).append(">").append(formatDelta(toursSoldDelta)).append("</td>");
            table.append("<td>").append(String.format("%.1f", avgFeedback)).append("</td>");
            table.append("<td>").append(minFeedback).append("</td>");
            table.append("<td ").append(getDeltaClass(avgFeedbackDelta)).append(">").append(formatDelta(avgFeedbackDelta)).append("</td>");
            table.append("<td>").append(formatCurrency(revenue)).append("</td>");
            table.append("<td ").append(getDeltaClass(revenueDelta)).append(">").append(formatDelta(revenueDelta)).append("</td>");
            table.append("</tr>");
        }
        
        table.append("</table>");
        return table.toString();
    }
    private String[] calculatePeriodDates(String period, Integer year) {
        String[] dates = new String[2];
        
        if ("WEEKLY".equalsIgnoreCase(period)) {
            // Current week (Monday to Sunday) - match TravelReportsServiceImpl logic
            LocalDateTime now = LocalDateTime.now();
            int dayOfWeek = now.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            LocalDateTime weekStart = now.minusDays(dayOfWeek - 1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime weekEnd = now.plusDays(7 - dayOfWeek).withHour(23).withMinute(59).withSecond(59);
            dates[0] = String.format("%02d.%02d.%d", weekStart.getDayOfMonth(), weekStart.getMonthValue(), weekStart.getYear());
            dates[1] = String.format("%02d.%02d.%d", weekEnd.getDayOfMonth(), weekEnd.getMonthValue(), weekEnd.getYear());
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            int currentMonth = LocalDateTime.now().getMonthValue();
            dates[0] = String.format("%02d.01.%d", currentMonth, year);
            LocalDateTime endOfMonth = LocalDateTime.of(year, currentMonth, 1, 0, 0).plusMonths(1).minusDays(1);
            dates[1] = String.format("%02d.%02d.%d", endOfMonth.getDayOfMonth(), currentMonth, year);
        } else if ("QUARTERLY".equalsIgnoreCase(period)) {
            int currentQuarter = (LocalDateTime.now().getMonthValue() - 1) / 3;
            int startMonth = currentQuarter * 3 + 1;
            int endMonth = startMonth + 2;
            dates[0] = String.format("%02d.01.%d", startMonth, year);
            LocalDateTime endOfQuarter = LocalDateTime.of(year, endMonth, 1, 0, 0).plusMonths(1).minusDays(1);
            dates[1] = String.format("%02d.%02d.%d", endOfQuarter.getDayOfMonth(), endMonth, year);
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            dates[0] = String.format("01.01.%d", year);
            dates[1] = String.format("31.12.%d", year);
        } else {
            dates[0] = "N/A";
            dates[1] = "N/A";
        }
        
        return dates;
    }
    
    private String getDeltaClass(Double delta) {
        if (delta == null) return "class=\"neutral\"";
        if (delta > 0) return "class=\"positive\"";
        if (delta < 0) return "class=\"negative\"";
        return "class=\"neutral\"";
    }
    
    private String formatCurrency(Double value) {
        if (value == null) return "0";
        // Format with space as thousand separator (e.g., "229 120")
        String formatted = String.format("%,.0f", value);
        return formatted.replace(",", " ");
    }
    
    
    private String formatDelta(Object delta) {
        if (delta == null) return "N/A";
        if (delta instanceof Double) {
            Double deltaValue = (Double) delta;
            String sign = deltaValue >= 0 ? "+" : "";
            return sign + String.format("%.1f%%", deltaValue);
        }
        return delta.toString();
    }
    
    private String generateAllToursSalesHtmlReport(Map<String, Object> reportData, String period, Integer year) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<title>All Tours Sales Statistics Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append("h1 { color: #333; text-align: center; }");
        html.append(".report-meta { text-align: center; margin-bottom: 20px; color: #666; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 20px auto; background-color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: center; font-size: 12px; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; color: #333; }");
        html.append(".positive { color: #28a745; font-weight: bold; }");
        html.append(".negative { color: #dc3545; font-weight: bold; }");
        html.append(".neutral { color: #666; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<h1>All Tours Sales Statistics Report</h1>");
        html.append("<div class=\"report-meta\">");
        html.append("<p><strong>Period:</strong> ").append(period).append(" ").append(year).append("</p>");
        html.append("<p><strong>Generated:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("</div>");
        
        // Generate table for all tours
        html.append(generateAllToursSalesTable(reportData, period, year));
        
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String generateAllToursSalesTable(Map<String, Object> reportData, String period, Integer year) {
        StringBuilder table = new StringBuilder();
        
        // Calculate period dates
        String[] periodDates = calculatePeriodDates(period, year);
        String periodStart = periodDates[0];
        String periodEnd = periodDates[1];
        
        // Get all tours processed from the report data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allToursProcessed = (List<Map<String, Object>>) reportData.get("allToursProcessed");
        
        if (allToursProcessed == null || allToursProcessed.isEmpty()) {
            table.append("<p>No tours found for the specified period.</p>");
            return table.toString();
        }
        
        table.append("<table>");
        table.append("<tr>");
        table.append("<th>Tour name</th>");
        table.append("<th>Destination country</th>");
        table.append("<th>Destination city</th>");
        table.append("<th>Report period start</th>");
        table.append("<th>Report period end</th>");
        table.append("<th>Tours sold</th>");
        table.append("<th>Delta of tours sold to previous period %</th>");
        table.append("<th>Average Feedback rate for Travel experience (1 to 5)</th>");
        table.append("<th>Minimum Feedback rate for Travel experience (1 to 5)</th>");
        table.append("<th>Delta of Average Feedback rate to previous period %</th>");
        table.append("<th>Revenue for tours finished within reported period (USD)</th>");
        table.append("<th>Delta of revenue to previous period %</th>");
        table.append("</tr>");
        
        for (Map<String, Object> tour : allToursProcessed) {
            String tourName = (String) tour.getOrDefault("name", "N/A");
            String destination = (String) tour.getOrDefault("destination", "N/A");
            String hotelName = (String) tour.getOrDefault("hotelName", "N/A");
            Integer bookingsCount = (Integer) tour.getOrDefault("bookingsCount", 0);
            Double revenue = (Double) tour.getOrDefault("revenue", 0.0);
            Double averageRating = (Double) tour.getOrDefault("averageRating", 0.0);
            Integer minimumRating = (Integer) tour.getOrDefault("minimumRating", 0);
            
            // Get delta values from tour details
            Double toursSoldDelta = (Double) tour.getOrDefault("toursSoldDelta", 0.0);
            Double averageFeedbackDelta = (Double) tour.getOrDefault("averageFeedbackDelta", 0.0);
            Double revenueDelta = (Double) tour.getOrDefault("revenueDelta", 0.0);
            
            table.append("<tr>");
            table.append("<td>").append(tourName).append("</td>");
            table.append("<td>").append(destination).append("</td>");
            table.append("<td>").append(hotelName).append("</td>");
            table.append("<td>").append(periodStart).append("</td>");
            table.append("<td>").append(periodEnd).append("</td>");
            table.append("<td>").append(bookingsCount).append("</td>");
            table.append("<td ").append(getDeltaClass(toursSoldDelta)).append(">").append(formatDelta(toursSoldDelta)).append("</td>");
            table.append("<td>").append(String.format("%.1f", averageRating)).append("</td>");
            table.append("<td>").append(minimumRating).append("</td>");
            table.append("<td ").append(getDeltaClass(averageFeedbackDelta)).append(">").append(formatDelta(averageFeedbackDelta)).append("</td>");
            table.append("<td>").append(formatCurrency(revenue)).append("</td>");
            table.append("<td ").append(getDeltaClass(revenueDelta)).append(">").append(formatDelta(revenueDelta)).append("</td>");
            table.append("</tr>");
        }
        
        table.append("</table>");
        return table.toString();
    }
    
    private boolean sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            System.out.println("=== SENDING EMAIL ===");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + subject);
            System.out.println("HTML Content Length: " + htmlContent.length());
            System.out.println("From: " + System.getenv("from_email"));
            System.out.println("Region: " + System.getenv("region"));
            
            // Validate environment variables
            String fromEmail = System.getenv("from_email");
            String region = System.getenv("region");
            
            if (fromEmail == null || fromEmail.isEmpty()) {
                System.err.println("ERROR: from_email environment variable is not set!");
                return false;
            }
            
            if (region == null || region.isEmpty()) {
                System.err.println("ERROR: region environment variable is not set!");
                return false;
            }
            
            // Create SES client
            SesClient sesClient = SesClient.builder()
                .region(Region.of(region))
                .build();
            
            // Create the email destination
            Destination destination = Destination.builder()
                .toAddresses(toEmail)
                .build();
            
            // Create the email content
            Content subjectContent = Content.builder()
                .data(subject)
                .charset("UTF-8")
                .build();
            
            Content htmlBodyContent = Content.builder()
                .data(htmlContent)
                .charset("UTF-8")
                .build();
            
            Body body = Body.builder()
                .html(htmlBodyContent)
                .build();
            
            Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();
            
            // Create the send email request
            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(destination)
                .message(message)
                .build();
            
            // Send the email
            SendEmailResponse response = sesClient.sendEmail(sendEmailRequest);
            
            System.out.println("✅ Email sent successfully! Message ID: " + response.messageId());
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

