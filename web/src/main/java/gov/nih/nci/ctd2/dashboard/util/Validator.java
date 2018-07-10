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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

public class Validator {
    private static final Log log = LogFactory.getLog(Validator.class);

    private final SubmissionTemplate template;
    private final Path topDir;

    public Validator(SubmissionTemplate template, Path topDir) {
        this.template = template;
        this.topDir = topDir;
    }

    public Validator(Path xlsFilePath, final DashboardDao dashboardDao) throws IOException, ValidationException {
        template = readTemplateFromXsl(xlsFilePath, dashboardDao);
        topDir = xlsFilePath.getParent();
    }

    final static String metasheetName = "dashboard-CV-per-template";
    final static String[] headers = { "observation_tier", "template_name", "observation_summary", "story_title",
            "submission_name", "submission_description", "project", "submission_story", "submission_story_rank",
            "submission_center", "principal_investigator" };

    private SubmissionTemplate readTemplateFromXsl(final Path xlsFilePath, final DashboardDao dashboardDao) throws IOException, ValidationException {
        FileInputStream excelFile = new FileInputStream(xlsFilePath.toFile());
        Workbook workbook = new HSSFWorkbook(excelFile);
        Sheet metadataSheet = workbook.getSheetAt(0);
        String metadataSheetName = metadataSheet.getSheetName();
        Sheet dataSheet = workbook.getSheetAt(1);
        String templateName = dataSheet.getSheetName();

        if (!metasheetName.equals(metadataSheetName)) {
            workbook.close();
            throw new ValidationException("metadata sheet name is " + metadataSheetName + " but it should be " + metasheetName);
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
                workbook.close();
                throw new ValidationException("incorrect header ''" + v + "'. Expected: " + headers[index]);
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
            workbook.close();
            throw new ValidationException("incorrect template_name " + row1.getCell(1).getStringCellValue());
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
            workbook.close();
            throw new ValidationException("incorrect date portion of submission_name " + submissionName);
        } catch (Exception e) {
            workbook.close();
            throw new ValidationException("incorrect submission_name " + submissionName); // too many possibilities
        }

        String summary = row1.getCell(2).getStringCellValue();

        String centerName = row1.getCell(9).getStringCellValue();
        SubmissionCenter submissionCenter = dashboardDao.findSubmissionCenterByName(centerName);

        int tier = 0;
        try {
            tier = Integer.parseInt(row1.getCell(0).getStringCellValue());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        boolean isStory = Boolean.parseBoolean(row1.getCell(7).getStringCellValue());

        SubmissionTemplate template = new SubmissionTemplateImpl();
        template.setDisplayName(templateName);
        template.setDateLastModified(date);
        template.setSummary(summary);
        template.setSubmissionCenter(submissionCenter);
        template.setTier(tier);
        template.setIsStory(isStory);

        parseDataSheet(dataSheet, template);

        workbook.close();
        return template;
    }

    private void parseDataSheet(final Sheet dataSheet, SubmissionTemplate template) throws ValidationException {
        int firstRowNumber = dataSheet.getFirstRowNum();
        int lastRowNumber = dataSheet.getLastRowNum();
        short topRow = dataSheet.getTopRow();
        log.debug("lastRowNumber=" + lastRowNumber + ", firstRowNumber=" + firstRowNumber + ", topRow=" + topRow);

        if (lastRowNumber < 6) {
            throw new ValidationException("incorrect number of row: " + (lastRowNumber + 1));
        }

        Row row0 = dataSheet.getRow(topRow);
        if (!"submission_name".equals(row0.getCell(1).getStringCellValue())) {
            throw new ValidationException("incorrect header at column 1: " + row0.getCell(1).getStringCellValue());
        }
        if (!"submission_date".equals(row0.getCell(2).getStringCellValue())) {
            throw new ValidationException("incorrect header at column 2: " + row0.getCell(2).getStringCellValue());
        }
        if (!"template_name".equals(row0.getCell(3).getStringCellValue())) {
            throw new ValidationException("incorrect header at column 3: " + row0.getCell(3).getStringCellValue());
        }

        Row subjectRow = dataSheet.getRow(1);
        assert "subject".equals(subjectRow.getCell(0).getStringCellValue());
        Row evidenceRow = dataSheet.getRow(2);
        assert "evidence".equals(evidenceRow.getCell(0).getStringCellValue());
        Row roleRow = dataSheet.getRow(3);
        assert "role".equals(roleRow.getCell(0).getStringCellValue());
        Row mimeTypeRow = dataSheet.getRow(4);
        assert "mime_type".equals(mimeTypeRow.getCell(0).getStringCellValue());
        Row numericUnitsRow = dataSheet.getRow(5);
        assert "numeric_units".equals(numericUnitsRow.getCell(0).getStringCellValue());
        Row displayTextRow = dataSheet.getRow(6);
        assert "display_text".equals(displayTextRow.getCell(0).getStringCellValue());

        short firstColumn = subjectRow.getFirstCellNum();
        short lastSubjectColumn = subjectRow.getLastCellNum(); // exclusive
        log.debug("subject first column=" + firstColumn + ", last column=" + lastSubjectColumn);
        List<String> subjects = new ArrayList<String>();
        for (int i = 4; i < lastSubjectColumn; i++) {
            String subjectColumnTag = row0.getCell(i).getStringCellValue();
            subjects.add(subjectColumnTag);
        }
        // Assume evidences are all to the right of subjects. May this be not the case?
        firstColumn = evidenceRow.getFirstCellNum();
        short lastEvidenceColumn = evidenceRow.getLastCellNum(); // exclusive
        log.debug("evidence first column=" + firstColumn + ", last column=" + lastEvidenceColumn);
        List<String> evidences = new ArrayList<String>();
        for (int i = lastSubjectColumn; i < lastEvidenceColumn; i++) {
            String evidencColumnTag = row0.getCell(i).getStringCellValue();
            evidences.add(evidencColumnTag);
        }

        int subjectCount = subjects.size();
        template.setSubjectColumns(subjects.toArray(new String[subjectCount]));
        String[] subjectClasses = new String[subjectCount];
        String[] subjectRoles = new String[subjectCount];
        String[] subjectTexts = new String[subjectCount];
        for (int i = 0; i < subjectCount; i++) {
            int col = 4 + i;
            subjectClasses[i] = subjectRow.getCell(col).getStringCellValue();
            subjectRoles[i] = roleRow.getCell(col).getStringCellValue();
            subjectTexts[i] = displayTextRow.getCell(col).getStringCellValue();
        }
        template.setSubjectClasses(subjectClasses);
        template.setSubjectRoles(subjectRoles);
        template.setSubjectDescriptions(subjectTexts);

        int evidenceCount = evidences.size();
        template.setEvidenceColumns(evidences.toArray(new String[evidenceCount]));
        /* BE CAUTIOUS WITH THESE INCONSISTENT NAMINGS */
        String[] evidenceTypes = new String[evidenceCount];
        String[] valueTypes = new String[evidenceCount];
        String[] evidenceDescription = new String[evidenceCount];
        for (int i = 0; i < evidenceCount; i++) {
            int col = lastSubjectColumn + i;
            evidenceTypes[i] = evidenceRow.getCell(col).getStringCellValue();
            valueTypes[i] = roleRow.getCell(col).getStringCellValue();
            evidenceDescription[i] = displayTextRow.getCell(col).getStringCellValue();
        }
        template.setEvidenceTypes(evidenceTypes);
        template.setValueTypes(valueTypes);
        template.setEvidenceDescriptions(evidenceDescription);

        int observationNumber = lastRowNumber - 6;
        template.setObservationNumber(observationNumber);
        String[] observations = new String[observationNumber*(subjectCount + evidenceCount)];
        log.debug("observation array size "+observations.length);
        int observationIndex = 0;

        String templateName = template.getDisplayName();
        for (int i = 7; i <= lastRowNumber; i++) {
            log.debug("row #" + i);
            Row row = dataSheet.getRow(i);
            firstColumn = row.getFirstCellNum();
            short lastColumn = row.getLastCellNum();
            log.debug("first column=" + firstColumn + ", last column=" + lastColumn);

            String submissionName = row.getCell(1).getStringCellValue();
            String submissionDate = row.getCell(2).getStringCellValue();
            String templateName_x = row.getCell(3).getStringCellValue();
            if (!templateName.equals(templateName_x)) {
                throw new ValidationException("incorrect template_name " + templateName_x);
            }
            String submissionName_x = submissionDate.replaceAll("\\.", "") + "-" + templateName;
            if (!submissionName.equals(submissionName_x)) {
                throw new ValidationException("incorrect submission_name " + submissionName_x);
            }
            try {
                Date date = new SimpleDateFormat("yyyy.MM.dd").parse(submissionDate);
                log.debug("submission date is " + date);
            } catch (ParseException e) {
                throw new ValidationException("incorrect submission_date " + submissionDate);
            }

            for (int col = 4; col < lastEvidenceColumn; col++) {
                if(observationIndex>=observations.length) {
                    log.error("observationIndex="+observationIndex);
                    break;
                }
                observations[observationIndex++] = row.getCell(col).getStringCellValue();
            }
        }

        template.setObservations(observations);
        template.setObservationString(null); // this field is for back-compatible only, so make sure it is null.
    }

    public List<String> createTextFiles() {
        assert template != null;
        assert topDir != null;

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
        String pi = pis.get(center);

        sb.append(tier).append('\t').append(templateName).append('\t').append(summary).append('\t')
                .append(templateDescription).append('\t').append(submissionName).append('\t')
                .append(submissionDescription).append('\t').append(project).append('\t')
                .append(story.toString().toUpperCase()).append('\t').append(rank).append('\t').append(center)
                .append('\t').append(pi).append('\n');
        return sb.toString();
    }

    private static Map<String, String> pis = new HashMap<String, String>();

    static {
        pis.put("Broad Institute", "Stuart L. Schreiber, Ph.D.");
        pis.put("Cold Spring Harbor Laboratory", "Scott Powers, Ph.D.");
        pis.put("Columbia University", "Andrea Califano, Ph.D.");
        pis.put("Dana-Farber Cancer Institute", "William C. Hahn, M.D., Ph.D.");
        pis.put("Emory University", "Haian Fu, Ph.D.");
        pis.put("Fred Hutchinson Cancer Research Center (1)", "Christopher Kemp, Ph.D.");
        pis.put("Fred Hutchinson Cancer Research Center (2)", "Martin McIntosh, Ph.D.");
        pis.put("Stanford University", "Calvin J. Kuo, M.D., Ph.D.");
        pis.put("Translational Genomics Research Institute", "Michael E. Berens, Ph.D.");
        pis.put("University of California San Francisco (1)", "Michael McManus, Ph.D.");
        pis.put("University of California San Francisco (2)", "William A. Weiss, M.D., Ph.D.");
        pis.put("University of Texas MD Anderson Cancer Center", "Gordon B. Mills, M.D., Ph.D.");
        pis.put("University of Texas Southwestern Medical Center", "Michael Roth, Ph.D.");
    }
}
