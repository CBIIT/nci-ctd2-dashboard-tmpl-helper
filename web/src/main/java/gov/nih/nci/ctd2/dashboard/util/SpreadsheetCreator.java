package gov.nih.nci.ctd2.dashboard.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.BorderStyle;

import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;

public class SpreadsheetCreator {
    private static final Log log = LogFactory.getLog(SpreadsheetCreator.class);

    private final SubmissionTemplate template;
    private final String submissionName;
    private final String fileLocation;

    public SpreadsheetCreator(final SubmissionTemplate template, String fileLocation) {
        this.template = template;

        String templateName = template.getDisplayName();
        Date date = template.getDateLastModified();
        submissionName = new SimpleDateFormat("yyyyMMdd-").format(date) + templateName;

        this.fileLocation = fileLocation;
    }

    public byte[] createWorkbookAsByteArray() throws IOException {
        HSSFWorkbook workbook = createWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    private HSSFWorkbook createWorkbook() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            this.createMetaDataSheet(workbook);
            this.createDataSheet(workbook);
        } catch (Exception e) { /* safeguard data-caused exception */
            e.printStackTrace();
        }
        return workbook;
    }

    private void createMetaDataSheet(HSSFWorkbook workbook) {

        HSSFFont headerFont = (HSSFFont) workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontName("Courier New");
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        HSSFSheet sheet = workbook.createSheet("dashboard-CV-per-template");
        HSSFRow rowhead0 = sheet.createRow((short) 0);

        String[] headers = { "observation_tier", "template_name", "observation_summary", "story_title",
                "submission_name", "submission_description", "project", "submission_story", "submission_story_rank",
                "submission_center", "principal_investigator" };

        for (int i = 0; i < headers.length; i++) {
            Cell c = rowhead0.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        String templateName = template.getDisplayName();
        Date date = template.getDateLastModified();

        HSSFRow row0 = sheet.createRow((short) 1);
        row0.createCell(0).setCellValue(template.getTier());
        row0.createCell(1).setCellValue(templateName);
        row0.createCell(2).setCellValue(template.getSummary());
        row0.createCell(3).setCellValue("");
        row0.createCell(4).setCellValue(new SimpleDateFormat("yyyyMMdd-").format(date) + templateName);
        row0.createCell(5).setCellValue(template.getDescription());
        row0.createCell(6).setCellValue(template.getProject());
        row0.createCell(7).setCellValue(template.getIsStory());
        row0.createCell(8).setCellValue(0);
        row0.createCell(9).setCellValue(template.getSubmissionCenter().getDisplayName());
        row0.createCell(10).setCellValue("");

        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }

        HSSFFont font2 = (HSSFFont) workbook.createFont();
        font2.setFontName("Courier New");
        HSSFCellStyle style2 = workbook.createCellStyle();
        style2.setFont(font2);
        row0.setRowStyle(style2);
    }

    private void createDataSheet(HSSFWorkbook workbook) {
        String templateName = template.getDisplayName();
        HSSFSheet sheet = workbook.createSheet(templateName);

        CellStyle header = workbook.createCellStyle();
        header.setBorderBottom(BorderStyle.THIN);

        CellStyle blue = workbook.createCellStyle();
        blue.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
        blue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        blue.setBorderBottom(BorderStyle.HAIR);

        CellStyle green = workbook.createCellStyle();
        green.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        green.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        green.setBorderBottom(BorderStyle.HAIR);

        CellStyle yellow = workbook.createCellStyle();
        yellow.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        yellow.setBorderBottom(BorderStyle.HAIR);

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.setRowStyle(header);
        Cell c = rowhead.createCell(1);
        c.setCellValue("submission_name");
        c.setCellStyle(header);
        c = rowhead.createCell(2);
        c.setCellValue("submission_date");
        c.setCellStyle(header);
        c = rowhead.createCell(3);
        c.setCellValue("template_name");
        c.setCellStyle(header);
        String[] subjects = template.getSubjectColumns();
        for (int i = 0; i < subjects.length; i++) {
            c = rowhead.createCell(i + 4);
            c.setCellValue(subjects[i]);
            c.setCellStyle(header);
        }
        String[] evd = template.getEvidenceColumns();
        for (int i = 0; i < evd.length; i++) {
            c = rowhead.createCell(i + 4 + subjects.length);
            c.setCellValue(evd[i]);
            c.setCellStyle(header);
        }

        HSSFRow row = sheet.createRow((short) 1);
        row.setRowStyle(blue);
        Cell cell = row.createCell(0);
        cell.setCellValue("subject");
        cell.setCellStyle(blue);
        String[] classes = template.getSubjectClasses();
        for (int i = 0; i < classes.length; i++) { // classes should have the same length as subject column
            cell = row.createCell(i + 4);
            cell.setCellValue(classes[i].toLowerCase());
            cell.setCellStyle(blue);
        }

        row = sheet.createRow((short) 2);
        row.setRowStyle(green);
        cell = row.createCell(0);
        cell.setCellValue("evidence");
        cell.setCellStyle(green);
        String[] valueType = template.getValueTypes();
        for (int i = 0; i < valueType.length; i++) { // value types should have the same length as evidence column
            cell = row.createCell(i + 4 + subjects.length);
            cell.setCellValue(valueType[i].toLowerCase());
            cell.setCellStyle(green);
        }

        row = sheet.createRow((short) 3);
        row.setRowStyle(yellow);
        cell = row.createCell(0);
        cell.setCellValue("role");
        cell.setCellStyle(yellow);
        String[] roles = template.getSubjectRoles();
        for (int i = 0; i < roles.length; i++) {
            cell = row.createCell(i + 4);
            cell.setCellValue(roles[i].toLowerCase());
            cell.setCellStyle(yellow);
        }
        String[] evidenceRoles = template.getEvidenceTypes();
        for (int i = 0; i < evidenceRoles.length; i++) {
            cell = row.createCell(i + 4 + roles.length);
            cell.setCellValue(evidenceRoles[i].toLowerCase());
            cell.setCellStyle(yellow);
        }

        HSSFRow mimeTypeRow = sheet.createRow((short) 4);
        mimeTypeRow.setRowStyle(yellow);
        cell = mimeTypeRow.createCell(0);
        cell.setCellValue("mime_type");
        cell.setCellStyle(yellow);

        row = sheet.createRow((short) 5);
        row.setRowStyle(yellow);
        cell = row.createCell(0);
        cell.setCellValue("numeric_units");
        cell.setCellStyle(yellow);

        row = sheet.createRow((short) 6);
        row.setRowStyle(yellow);
        cell = row.createCell(0);
        cell.setCellValue("display_text");
        cell.setCellStyle(yellow);
        String[] displayTexts = template.getSubjectDescriptions();
        for (int i = 0; i < displayTexts.length; i++) {
            cell = row.createCell(i + 4);
            cell.setCellValue(displayTexts[i]);
            cell.setCellStyle(yellow);
        }
        String[] evidenceDescriptions = template.getEvidenceDescriptions();
        for (int i = 0; i < evidenceDescriptions.length; i++) {
            cell = row.createCell(i + 4 + displayTexts.length);
            cell.setCellValue(evidenceDescriptions[i]);
            cell.setCellStyle(yellow);
        }

        Date date = template.getDateLastModified();

        String observations = template.getObservations();
        if (observations == null) { // this should never happen for correct data
            log.error("observtions field is null for template ID " + template.getId());
            return;
            /* At this point, the spreadsheet is not completely populated or formatted, still available nonetheless.*/
        }
        Integer observationNumber = template.getObservationNumber();
        if (observationNumber == null)
            observationNumber = 0; // this should never happen for correct data
        String[] obv = observations.split(",", -1);
        int index = 0;
        files.clear(); // this is used by the zipping process
        for (int i = 0; i < observationNumber; i++) {
            row = sheet.createRow((short) (7 + i));
            cell = row.createCell(1);
            cell.setCellValue(submissionName);
            cell = row.createCell(2);
            cell.setCellValue(new SimpleDateFormat("yyyy.MM.dd").format(date));
            cell = row.createCell(3);
            cell.setCellValue(templateName);
            for (int j = 0; j < subjects.length; j++) {
                cell = row.createCell(j + 4);
                cell.setCellValue(obv[index]);
                index++;
            }
            for (int j = 0; j < evd.length; j++) {
                cell = row.createCell(subjects.length + j + 4);
                String observationData = obv[index];
                if (valueType[j].equalsIgnoreCase("file")) {
                    int mimeMark = observationData.indexOf("::data:");
                    String filename = observationData;
                    if (mimeMark > 0) {
                        Cell mimeTypeRowCell = mimeTypeRow.createCell(subjects.length + j + 4);
                        mimeTypeRowCell.setCellStyle(yellow);
                        mimeTypeRowCell.setCellValue(observationData.substring(mimeMark + 7));

                        filename = observationData.substring(0, mimeMark); // the new code only stores the filname without directories
                    }

                    // ignore possible subdirectory names
                    int sep = filename.lastIndexOf('/');
                    if(sep>=0) filename = filename.substring(sep+1);
                    sep = filename.lastIndexOf('\\');
                    if(sep>=0) filename = filename.substring(sep+1);

                    Path savedPath = Paths.get(fileLocation + filename);
                    if(!savedPath.toFile().exists()) { // this should not happen, but be cautious anyway
                        log.error("ERROR: uploaded file "+savedPath.toFile()+" not found");
                        observationData = "";
                    } else {
                        String zippedPath = getZippedPath(filename);
                        files.put(zippedPath, savedPath);
                        observationData = "./" + zippedPath;
                    }
                }
                cell.setCellValue(observationData);
                index++;
            }
        }

        int totalColumn = 4 + subjects.length + evd.length;
        for (int i = 0; i < totalColumn; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private Map<String, Path> files = new HashMap<String, Path>(); // duplicate entry not allowed in ZIP

    public Map<String, Path> getUploadedFiles() {
        return files;
    }

    private String getZippedPath(String zippedFileName) {
        String pathZipped = "submissions/" + submissionName + "/";
        String lowercase = zippedFileName.toLowerCase();
        boolean hasImageFileExtension = false;
        if (lowercase.endsWith("png") || lowercase.endsWith("jpeg") || lowercase.endsWith("jpg")) {
            hasImageFileExtension = true;
        }
        if (hasImageFileExtension) {
            pathZipped += "images/";
        }
        return pathZipped + zippedFileName;
    }
}
