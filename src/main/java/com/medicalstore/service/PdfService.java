package com.medicalstore.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.medicalstore.model.Sale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;
    
    private static final String COMPANY_NAME = "MEDICAL STORE";
    private static final String COMPANY_ADDRESS = "123 Medical Street, Healthcare City";
    private static final String COMPANY_PHONE = "+91 9876543210";
    private static final String COMPANY_EMAIL = "info@medicalstore.com";
    private static final String COMPANY_GSTIN = "29ABCDE1234F1Z5";
    
    /**
     * Generate professional invoice PDF with enterprise branding
     */
    public byte[] generateInvoicePdf(Sale sale) {
        try {
            Context context = new Context();
            context.setVariable("sale", sale);
            context.setVariable("companyName", COMPANY_NAME);
            context.setVariable("companyAddress", COMPANY_ADDRESS);
            context.setVariable("companyPhone", COMPANY_PHONE);
            context.setVariable("companyEmail", COMPANY_EMAIL);
            context.setVariable("companyGstin", COMPANY_GSTIN);
            
            String htmlContent = templateEngine.process("pdf/invoice-template", context);
            return convertHtmlToPdf(htmlContent, "Invoice #INV-" + String.format("%06d", sale.getId()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate daily report PDF
     */
    public byte[] generateDailyReportPdf(String reportDate, Object reportData) {
        try {
            Context context = new Context();
            context.setVariable("reportDate", reportDate);
            context.setVariable("reportData", reportData);
            context.setVariable("companyName", COMPANY_NAME);
            
            String htmlContent = templateEngine.process("pdf/daily-report-template", context);
            return convertHtmlToPdf(htmlContent, "Daily Report - " + reportDate);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate daily report PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate monthly report PDF
     */
    public byte[] generateMonthlyReportPdf(String month, String year, Object reportData) {
        try {
            Context context = new Context();
            context.setVariable("month", month);
            context.setVariable("year", year);
            context.setVariable("reportData", reportData);
            context.setVariable("companyName", COMPANY_NAME);
            
            String htmlContent = templateEngine.process("pdf/monthly-report-template", context);
            return convertHtmlToPdf(htmlContent, "Monthly Report - " + month + " " + year);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate monthly report PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate sales report PDF
     */
    public byte[] generateSalesReportPdf(String reportType, String startDate, String endDate, Object reportData) {
        try {
            Context context = new Context();
            context.setVariable("reportType", reportType);
            context.setVariable("startDate", startDate);
            context.setVariable("endDate", endDate);
            context.setVariable("reportData", reportData);
            context.setVariable("companyName", COMPANY_NAME);
            
            String htmlContent = templateEngine.process("pdf/sales-report-template", context);
            return convertHtmlToPdf(htmlContent, reportType + " Sales Report");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate sales report PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert HTML content to enterprise-level PDF with metadata and branding
     */
    private byte[] convertHtmlToPdf(String htmlContent, String documentTitle) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Create PDF writer with enterprise settings
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            
            // Set document metadata
            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            info.setTitle(documentTitle);
            info.setAuthor(COMPANY_NAME);
            info.setCreator(COMPANY_NAME + " Management System");
            info.setSubject("Generated Document");
            info.setKeywords("Medical Store, Invoice, Report");
            
            // Set page size and margins
            pdfDoc.setDefaultPageSize(PageSize.A4);
            
            // Configure converter properties for better rendering
            ConverterProperties converterProperties = new ConverterProperties();
            converterProperties.setBaseUri("classpath:/templates/");
            
            // Convert HTML to PDF with enterprise styling
            HtmlConverter.convertToPdf(htmlContent, pdfDoc, converterProperties);
            
            pdfDoc.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert HTML to PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a simple text-based PDF (for testing or fallback)
     */
    public byte[] generateSimplePdf(String title, String content) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            
            // Add title
            Paragraph titleParagraph = new Paragraph(title)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(102, 126, 234));
            document.add(titleParagraph);
            
            // Add content
            Paragraph contentParagraph = new Paragraph(content)
                    .setFontSize(12);
            document.add(contentParagraph);
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate simple PDF: " + e.getMessage(), e);
        }
    }
}
