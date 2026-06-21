package com.nector.userservice.util;

import java.awt.Color;

import com.nector.userservice.dto.KpiAssignmentResponse;
import com.nector.userservice.dto.KpiResultResponse;
import com.nector.userservice.enums.KPIStatus;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class KpiExportUtil {

    private static final String[] ASSIGNMENT_HEADERS = {
        "ID", "Employee ID", "Employee Name", "Designation", "Role",
        "KPI Code", "KPI Name", "Target Value", "Achieved Value",
        "Weightage (%)", "Score (%)", "Weighted Score", "Status",
        "Start Date", "End Date"
    };

    private static final String[] RESULT_HEADERS = {
        "ID", "Employee ID", "Employee Name", "Month", "Year",
        "Total Score", "Grade", "Grade Meaning", "Generated At"
    };

    public byte[] exportAssignmentsToExcel(List<KpiAssignmentResponse> assignments) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("KPI Assignments");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < ASSIGNMENT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(ASSIGNMENT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;
            
            for (KpiAssignmentResponse assignment : assignments) {
                Row row = sheet.createRow(rowNum++);
                populateAssignmentRow(row, assignment, dataStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < ASSIGNMENT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportResultsToExcel(List<KpiResultResponse> results) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("KPI Results");
            
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < RESULT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(RESULT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }
            
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;
            
            for (KpiResultResponse result : results) {
                Row row = sheet.createRow(rowNum++);
                populateResultRow(row, result, dataStyle);
            }
            
            for (int i = 0; i < RESULT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportAssignmentsToPdf(List<KpiAssignmentResponse> assignments) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("KPI Assignments Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Create table
            PdfPTable table = new PdfPTable(ASSIGNMENT_HEADERS.length);
            table.setWidthPercentage(100);
            
            // Add headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String header : ASSIGNMENT_HEADERS) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }
            
            // Add data
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            for (KpiAssignmentResponse assignment : assignments) {
                addAssignmentCells(table, assignment, dataFont);
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    public byte[] exportResultsToPdf(List<KpiResultResponse> results) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("KPI Results Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            PdfPTable table = new PdfPTable(RESULT_HEADERS.length);
            table.setWidthPercentage(100);
            
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String header : RESULT_HEADERS) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }
            
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            for (KpiResultResponse result : results) {
                addResultCells(table, result, dataFont);
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void populateAssignmentRow(Row row, KpiAssignmentResponse assignment, CellStyle style) {
        int cellNum = 0;
        createCell(row, cellNum++, assignment.getId(), style);
        createCell(row, cellNum++, assignment.getEmployeeId(), style);
        createCell(row, cellNum++, assignment.getEmployeeName(), style);
        createCell(row, cellNum++, assignment.getDesignation(), style);
        createCell(row, cellNum++, assignment.getRoleName(), style);
        createCell(row, cellNum++, assignment.getKpiCode(), style);
        createCell(row, cellNum++, assignment.getKpiName(), style);
        createCell(row, cellNum++, assignment.getTargetValue(), style);
        createCell(row, cellNum++, assignment.getAchievedValue(), style);
        createCell(row, cellNum++, assignment.getWeightage(), style);
        createCell(row, cellNum++, assignment.getScorePercentage(), style);
        createCell(row, cellNum++, assignment.getWeightedScore(), style);
        createCell(row, cellNum++, assignment.getStatus() != null ? assignment.getStatus().name() : "", style);
        createCell(row, cellNum++, assignment.getStartDate(), style);
        createCell(row, cellNum, assignment.getEndDate(), style);
    }

    private void populateResultRow(Row row, KpiResultResponse result, CellStyle style) {
        int cellNum = 0;
        createCell(row, cellNum++, result.getId(), style);
        createCell(row, cellNum++, result.getEmployeeId(), style);
        createCell(row, cellNum++, result.getEmployeeName(), style);
        createCell(row, cellNum++, result.getMonth(), style);
        createCell(row, cellNum++, result.getYear(), style);
        createCell(row, cellNum++, result.getTotalScore(), style);
        createCell(row, cellNum++, result.getFinalGrade() != null ? result.getFinalGrade().getGrade() : "", style);
        createCell(row, cellNum++, result.getGradeMeaning(), style);
        createCell(row, cellNum, result.getGeneratedAt(), style);
    }

    private void createCell(Row row, int cellNum, Object value, CellStyle style) {
        Cell cell = row.createCell(cellNum);
        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        }
        cell.setCellStyle(style);
    }

    private void addAssignmentCells(PdfPTable table, KpiAssignmentResponse assignment, Font font) {
        table.addCell(new Phrase(String.valueOf(assignment.getId()), font));
        table.addCell(new Phrase(String.valueOf(assignment.getEmployeeId()), font));
        table.addCell(new Phrase(assignment.getEmployeeName(), font));
        table.addCell(new Phrase(assignment.getDesignation(), font));
        table.addCell(new Phrase(assignment.getRoleName(), font));
        table.addCell(new Phrase(assignment.getKpiCode(), font));
        table.addCell(new Phrase(assignment.getKpiName(), font));
        table.addCell(new Phrase(String.valueOf(assignment.getTargetValue()), font));
        table.addCell(new Phrase(String.valueOf(assignment.getAchievedValue()), font));
        table.addCell(new Phrase(String.valueOf(assignment.getWeightage()), font));
        table.addCell(new Phrase(String.valueOf(assignment.getScorePercentage()), font));
        table.addCell(new Phrase(String.valueOf(assignment.getWeightedScore()), font));
        table.addCell(new Phrase(assignment.getStatus() != null ? assignment.getStatus().name() : "", font));
        table.addCell(new Phrase(String.valueOf(assignment.getStartDate()), font));
        table.addCell(new Phrase(String.valueOf(assignment.getEndDate()), font));
    }

    private void addResultCells(PdfPTable table, KpiResultResponse result, Font font) {
        table.addCell(new Phrase(String.valueOf(result.getId()), font));
        table.addCell(new Phrase(String.valueOf(result.getEmployeeId()), font));
        table.addCell(new Phrase(result.getEmployeeName(), font));
        table.addCell(new Phrase(String.valueOf(result.getMonth()), font));
        table.addCell(new Phrase(String.valueOf(result.getYear()), font));
        table.addCell(new Phrase(String.valueOf(result.getTotalScore()), font));
        table.addCell(new Phrase(result.getFinalGrade() != null ? result.getFinalGrade().getGrade() : "", font));
        table.addCell(new Phrase(result.getGradeMeaning(), font));
        table.addCell(new Phrase(String.valueOf(result.getGeneratedAt()), font));
    }
}
