package gov.nih.nci.ctd2.dashboard.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.impl.SubmissionTemplateImpl;
import gov.nih.nci.ctd2.dashboard.model.SubmissionCenter;
import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;

public class SpreadsheetProcessor {
    private static final Log log = LogFactory.getLog(SpreadsheetProcessor.class);

    private final Path xlsFilePath;
    private final SubmissionTemplate template;

    public SpreadsheetProcessor(Path xlsFilePath, final DashboardDao dashboardDao) throws IOException {
        this.xlsFilePath = xlsFilePath;
        template = readTemplateFromXsl(dashboardDao);
    }

    final static String metasheetName = "dashboard-CV-per-template";
    final static String[] headers = { "observation_tier", "template_name", "observation_summary", "story_title",
            "submission_name", "submission_description", "project", "submission_story", "submission_story_rank",
            "submission_center", "principal_investigator" };

    private SubmissionTemplate readTemplateFromXsl(final DashboardDao dashboardDao) throws IOException {
        FileInputStream excelFile = new FileInputStream(xlsFilePath.toFile());
        Workbook workbook = new HSSFWorkbook(excelFile);
        Sheet metadataSheet = workbook.getSheetAt(0);
        String metadataSheetName = metadataSheet.getSheetName();
        Sheet dataSheet = workbook.getSheetAt(1);
        String templateName = dataSheet.getSheetName();

        if (!metasheetName.equals(metadataSheetName)) {
            workbook.close();
            System.out.println("metadata sheet name is " + metadataSheetName + " but it should be " + metasheetName);
            // TODO use more specialized exception
            throw new IOException("metadata sheet name is " + metadataSheetName + " but it should be " + metasheetName);
        }

        int lastRowNumber = metadataSheet.getLastRowNum();
        log.debug("lastRowNumber=" + lastRowNumber);

        Row row0 = metadataSheet.getRow(0);
        Iterator<Cell> iter = row0.cellIterator();
        int index = 0;
        while (iter.hasNext()) {
            Cell cell = iter.next();
            String v = cell.getStringCellValue();
            if (!v.equals(headers[index])) {
                throw new IOException("incorrect header ''" + v + "'. Expected: " + headers[index]);
            }
            index++;
        }

        log.debug("row1:");
        Row row1 = metadataSheet.getRow(1);
        short first1 = row1.getFirstCellNum();
        short last1 = row1.getLastCellNum();
        for (index = first1; index < last1; index++) {
            Cell cell = row1.getCell(index);
            String v = cell.getStringCellValue();
            log.debug(index + " " + v);
        }

        if (!templateName.equals(row1.getCell(1).getStringCellValue())) {
            throw new IOException("incorrect template_name " + row1.getCell(1).getStringCellValue());
        }

        String submissionName = row1.getCell(4).getStringCellValue();
        log.debug("submissionName=" + submissionName);
        log.debug("templateName=" + templateName);
        Date date = null;
        try {
            String dateString = submissionName.substring(0, submissionName.indexOf("-"));
            log.debug("dateString=" + dateString);
            date = new SimpleDateFormat("yyyyMMdd").parse(dateString);
        } catch (ParseException e) {
            throw new IOException("incorrect date portion of submission_name " + submissionName);
        } catch (Exception e) {
            throw new IOException("incorrect submission_name " + submissionName); // too many possibilities
        }

        String summary = row1.getCell(2).getStringCellValue();

        String centerName = row1.getCell(9).getStringCellValue();
        SubmissionCenter submissionCenter = dashboardDao.findSubmissionCenterByName(centerName);

        SubmissionTemplate template = new SubmissionTemplateImpl();
        template.setDisplayName(templateName);
        template.setDateLastModified(date);
        template.setSummary(summary);
        template.setSubmissionCenter(submissionCenter);

        workbook.close();
        return template;
    }

    public List<String> createTextFiles() {
        assert template != null;

        Path topDir = xlsFilePath.getParent();

        String templateName = template.getDisplayName();
        Date date = template.getDateLastModified();
        String submissionName = new SimpleDateFormat("yyyyMMdd-").format(date) + templateName;

        List<String> files = new ArrayList<String>();

        try {
            Path perColumn = topDir.resolve("dashboard-CV-per-column.txt");
            Files.deleteIfExists(perColumn);
            String content = perColumnContent();
            Files.write(perColumn, content.getBytes());
            files.add("dashboard-CV-per-column.txt");

            String content2 = perTemplateContent();
            Files.write(topDir.resolve("dashboard-CV-per-template.txt"), content2.getBytes());
            files.add("dashboard-CV-per-template.txt");

            Path dir = topDir.resolve("submissions" + File.separator + submissionName);
            if (!dir.toFile().exists()) {
                Files.createDirectories(dir);
            } else if (!dir.toFile().isDirectory()) {
                log.error(dir + " pre-exists but is not a directory.");
            }
            if (dir.toFile().isDirectory()) {
                StringBuffer filecontent = new StringBuffer();
                for (String submission : template.getObservations()) {
                    filecontent.append(submission).append('\n');
                }
                Path path = dir.resolve(submissionName + ".txt");
                Files.deleteIfExists(path);
                Files.write(path, filecontent.toString().getBytes());
                int pathCount = path.getNameCount();
                assert pathCount >= 3;
                files.add(path.getName(pathCount - 3) + File.separator + path.getName(pathCount - 2) + File.separator
                        + path.getFileName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("finished creating tab-delimited files");
        return files;
    }

    private String perColumnContent() {
        String[] headers = { "id", "template_name", "column_name", "subject", "evidence", "role", "mime_type",
                "numeric_units", "display_text" };
        StringBuffer sb = new StringBuffer();
        sb.append(headers[0]);
        for (int i = 1; i < headers.length; i++) {
            sb.append('\t').append(headers[i]);
        }
        sb.append('\n');

        String templateName = template.getDisplayName();
        // subjects
        String[] columnName = template.getSubjectColumns();
        String[] subjectClass = template.getSubjectClasses();
        String[] subjectRole = template.getSubjectRoles();
        String[] displayText = template.getSubjectDescriptions();
        for (int i = 0; i < template.getSubjectColumns().length; i++) {
            sb.append(i + 1).append('\t').append(templateName).append('\t').append(columnName[i]).append('\t')
                    .append(subjectClass[i]).append('\t').append('\t').append(subjectRole[i]).append('\t').append('\t')
                    .append('\t').append(displayText[i]).append('\n');
        }
        // evidences
        String[] evidenceColumnName = template.getEvidenceColumns();
        String[] evidenceType = template.getEvidenceTypes();
        String[] evidenceRole = template.getValueTypes(); // cautious: confusing naming
        String[] observations = template.getObservations();
        String[] evidenceDescription = template.getEvidenceDescriptions();
        for (int i = 0; i < template.getEvidenceColumns().length; i++) {
            String mimeType = ""; // applicable only for file evidence type
            String numericUnits = ""; // applicable only for numeric evidence type
            if (evidenceRole[i].equals("numeric")) {
                numericUnits = ""; // TODO not implemented
            } else if (evidenceRole[i].equals("file")) {
                String observationData = observations[i + template.getSubjectColumns().length];
                int mimeMark = observationData.indexOf("::data:");
                if (mimeMark > 0) {
                    mimeType = observationData.substring(mimeMark + 7);
                }
            }
            sb.append(template.getSubjectColumns().length + i + 1).append('\t').append(templateName).append('\t')
                    .append(evidenceColumnName[i]).append('\t').append('\t').append(evidenceType[i]).append('\t')
                    .append(evidenceRole[i]).append('\t').append(mimeType).append('\t').append(numericUnits)
                    .append('\t').append(evidenceDescription[i]).append('\n');
        }
        return sb.toString();
    }

    private String perTemplateContent() {
        String[] headers = { "observation_tier", "template_name", "observation_summary", "template_description",
                "submission_name", "submission_description", "project", "submission_story", "submission_story_rank",
                "submission_center", "principal_investigator" };
        StringBuffer sb = new StringBuffer();
        sb.append(headers[0]);
        for (int i = 1; i < headers.length; i++) {
            sb.append('\t').append(headers[i]);
        }
        sb.append('\n');

        Integer tier = template.getTier();
        String templateName = template.getDisplayName();
        Date date = template.getDateLastModified();
        String submissionName = new SimpleDateFormat("yyyyMMdd-").format(date) + templateName;
        String summary = template.getSummary().replace('\n', ' '); // assuming \n is not intended
        String templateDescription = template.getDescription();
        String submissionDescription = template.getStoryTitle(); // Totally confusing name!
        String project = template.getProject();
        Boolean story = template.getIsStory();
        Integer rank = 0; // TODO story rank, not implemented in the spreadsheet
        String center = template.getSubmissionCenter().getDisplayName();
        String pi = ""; // TODO PI, not implemented in the spreadsheet

        sb.append(tier).append('\t').append(templateName).append('\t').append(summary).append('\t')
                .append(templateDescription).append('\t').append(submissionName).append('\t')
                .append(submissionDescription).append('\t').append(project).append('\t').append(story).append('\t')
                .append(rank).append('\t').append(center).append('\t').append(pi).append('\n');
        return sb.toString();
    }
}
