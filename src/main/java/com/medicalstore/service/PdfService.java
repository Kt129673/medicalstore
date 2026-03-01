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
import com.medicalstore.model.SaleItem;
import lombok.RequiredArgsConstructor;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final VelocityEngine velocityEngine;

    private static final String COMPANY_NAME = "MEDICAL STORE";
    private static final String COMPANY_ADDRESS = "123 Medical Street, Healthcare City";
    private static final String COMPANY_PHONE = "+91 9876543210";
    private static final String COMPANY_EMAIL = "info@medicalstore.com";
    private static final String COMPANY_GSTIN = "29ABCDE1234F1Z5";
    private static final DecimalFormat DF = new DecimalFormat("#,##0.00");

    /**
     * Build a common VelocityContext with all sale data pre-formatted as strings.
     * This eliminates any null-pointer risk in the template layer.
     */
    private VelocityContext buildInvoiceContext(Sale sale) {
        VelocityContext ctx = new VelocityContext();

        // Company info
        ctx.put("companyName", COMPANY_NAME);
        ctx.put("companyAddress", COMPANY_ADDRESS);
        ctx.put("companyPhone", COMPANY_PHONE);
        ctx.put("companyEmail", COMPANY_EMAIL);
        ctx.put("companyGstin", COMPANY_GSTIN);

        // Sale ID
        ctx.put("saleId", sale.getId());
        ctx.put("invoiceNumber", String.format("INV-%06d", sale.getId()));

        // Dates
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter fullFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        ctx.put("saleDate", sale.getSaleDate() != null ? sale.getSaleDate().format(dateFmt) : "N/A");
        ctx.put("saleTime", sale.getSaleDate() != null ? sale.getSaleDate().format(timeFmt) : "N/A");
        ctx.put("generatedDate", LocalDateTime.now().format(fullFmt));

        // Customer info (null-safe)
        if (sale.getCustomer() != null) {
            ctx.put("customerName", safe(sale.getCustomer().getName(), "Walk-in Customer"));
            ctx.put("customerPhone", safe(sale.getCustomer().getPhone(), ""));
            ctx.put("customerAddress", safe(sale.getCustomer().getAddress(), ""));
        } else {
            ctx.put("customerName", "Walk-in Customer");
            ctx.put("customerPhone", "");
            ctx.put("customerAddress", "");
        }

        // Payment method
        ctx.put("paymentMethod", safe(sale.getPaymentMethod(), "Cash"));

        // Line items
        List<Map<String, Object>> items = new ArrayList<>();
        if (sale.getItems() != null) {
            for (SaleItem si : sale.getItems()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("medicineName", si.getMedicine() != null ? safe(si.getMedicine().getName(), "N/A") : "N/A");
                row.put("batchNumber",
                        si.getMedicine() != null ? safe(si.getMedicine().getBatchNumber(), "N/A") : "N/A");
                row.put("category", si.getMedicine() != null ? safe(si.getMedicine().getCategory(), "") : "");
                row.put("quantity", si.getQuantity() != null ? si.getQuantity() : 0);
                row.put("unitPrice", DF.format(si.getUnitPrice() != null ? si.getUnitPrice() : 0));
                row.put("totalPrice", DF.format(si.getTotalPrice() != null ? si.getTotalPrice() : 0));
                items.add(row);
            }
        }
        ctx.put("items", items);

        // Totals
        double subtotal = sale.getTotalAmount() != null ? sale.getTotalAmount() : 0;
        double discPct = sale.getDiscountPercentage() != null ? sale.getDiscountPercentage() : 0;
        double discAmt = sale.getDiscountAmount() != null ? sale.getDiscountAmount() : 0;
        double gstPct = sale.getGstPercentage() != null ? sale.getGstPercentage() : 0;
        double gstAmt = sale.getGstAmount() != null ? sale.getGstAmount() : 0;
        double grandTotal = sale.getFinalAmount() != null ? sale.getFinalAmount() : subtotal;

        ctx.put("subtotal", DF.format(subtotal));
        ctx.put("discountPercentage", discPct);
        ctx.put("discountAmount", DF.format(discAmt));
        ctx.put("gstPercentage", gstPct);
        ctx.put("gstAmount", DF.format(gstAmt));
        ctx.put("grandTotal", DF.format(grandTotal));

        return ctx;
    }

    /**
     * Render a Velocity template to a String.
     */
    private String renderTemplate(String templatePath, VelocityContext ctx) {
        StringWriter writer = new StringWriter();
        velocityEngine.mergeTemplate(templatePath, "UTF-8", ctx, writer);
        return writer.toString();
    }

    /**
     * Generate the HTML string for the web-based invoice page (browser view).
     */
    public String generateInvoiceHtml(Sale sale) {
        VelocityContext ctx = buildInvoiceContext(sale);
        return renderTemplate("velocity/invoice-web.vm", ctx);
    }

    /**
     * Generate professional invoice PDF with enterprise branding.
     */
    public byte[] generateInvoicePdf(Sale sale) {
        try {
            VelocityContext ctx = buildInvoiceContext(sale);
            String htmlContent = renderTemplate("velocity/invoice-template.vm", ctx);
            return convertHtmlToPdf(htmlContent, "Invoice #" + String.format("INV-%06d", sale.getId()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Build a common context for reports
     */
    private VelocityContext buildReportContext() {
        VelocityContext ctx = new VelocityContext();
        ctx.put("companyName", COMPANY_NAME);
        ctx.put("companyAddress", COMPANY_ADDRESS);
        ctx.put("companyPhone", COMPANY_PHONE);
        ctx.put("companyEmail", COMPANY_EMAIL);
        ctx.put("companyGstin", COMPANY_GSTIN);

        DateTimeFormatter fullFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        ctx.put("generatedDate", LocalDateTime.now().format(fullFmt));
        ctx.put("df", DF); // Expose DecimalFormat to templates
        return ctx;
    }

    /**
     * Generate daily report PDF.
     */
    public byte[] generateDailyReportPdf(String reportDate, Object reportData) {
        try {
            VelocityContext ctx = buildReportContext();
            ctx.put("reportDate", reportDate);
            ctx.put("r", reportData);

            String htmlContent = renderTemplate("velocity/daily-report-template.vm", ctx);
            return convertHtmlToPdf(htmlContent, "Daily Report - " + reportDate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate daily report PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate monthly report PDF.
     */
    public byte[] generateMonthlyReportPdf(String month, String year, Object reportData) {
        try {
            VelocityContext ctx = buildReportContext();
            ctx.put("reportMonth", month + " " + year);
            ctx.put("r", reportData);

            String htmlContent = renderTemplate("velocity/monthly-report-template.vm", ctx);
            return convertHtmlToPdf(htmlContent, "Monthly Report - " + month + " " + year);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate monthly report PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate sales report PDF.
     */
    public byte[] generateSalesReportPdf(String reportType, String startDate, String endDate, Object reportData) {
        try {
            VelocityContext ctx = buildReportContext();
            ctx.put("reportType", reportType);
            ctx.put("startDate", startDate);
            ctx.put("endDate", endDate);
            ctx.put("sales", reportData);

            String htmlContent = renderTemplate("velocity/sales-report-template.vm", ctx);
            return convertHtmlToPdf(htmlContent, reportType + " Sales Report");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate sales report PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Convert HTML content to enterprise-level PDF with metadata and branding.
     */
    private byte[] convertHtmlToPdf(String htmlContent, String documentTitle) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);

            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            info.setTitle(documentTitle);
            info.setAuthor(COMPANY_NAME);
            info.setCreator(COMPANY_NAME + " Management System");
            info.setSubject("Generated Document");
            info.setKeywords("Medical Store, Invoice, Report");

            pdfDoc.setDefaultPageSize(PageSize.A4);

            ConverterProperties converterProperties = new ConverterProperties();
            converterProperties.setBaseUri("classpath:/templates/");

            HtmlConverter.convertToPdf(htmlContent, pdfDoc, converterProperties);

            pdfDoc.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert HTML to PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a simple text-based PDF (for testing or fallback).
     */
    public byte[] generateSimplePdf(String title, String content) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            Paragraph titleParagraph = new Paragraph(title)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(102, 126, 234));
            document.add(titleParagraph);

            Paragraph contentParagraph = new Paragraph(content)
                    .setFontSize(12);
            document.add(contentParagraph);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate simple PDF: " + e.getMessage(), e);
        }
    }

    /** Null-safe string helper */
    private String safe(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
