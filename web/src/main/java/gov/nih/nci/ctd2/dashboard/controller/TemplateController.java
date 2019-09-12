package gov.nih.nci.ctd2.dashboard.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;

import flexjson.JSONSerializer;
import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.impl.SubmissionTemplateImpl;
import gov.nih.nci.ctd2.dashboard.model.SubmissionCenter;
import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;
import gov.nih.nci.ctd2.dashboard.util.SpreadsheetCreator;
import gov.nih.nci.ctd2.dashboard.util.TxtFileCreator;

@Controller
@RequestMapping("/template")
public class TemplateController implements ServletContextAware {
    private static final Log log = LogFactory.getLog(TemplateController.class);

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private String pythonCommand = "python";

    @Transactional
    @RequestMapping(value = "create", method = { RequestMethod.POST }, headers = "Accept=application/text")
    public ResponseEntity<String> createNewSubmissionTemplate( /*
                                                                * the method name has no effect, @RequestMapping value
                                                                * binds this method
                                                                */
            @RequestParam("centerId") Integer centerId, @RequestParam("displayName") String name,
            @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
            @RequestParam("email") String email, @RequestParam("phone") String phone,
            @RequestParam("description") String description, @RequestParam("project") String project,
            @RequestParam("tier") Integer tier, @RequestParam("isStory") Boolean isStory,
            @RequestParam("storyTitle") String storyTitle, @RequestParam("piName") String piName) {
        SubmissionTemplate template = new SubmissionTemplateImpl();
        template.setDisplayName(name);
        template.setDateLastModified(new Date());
        SubmissionCenter submissionCenter = dashboardDao.getEntityById(SubmissionCenter.class, centerId);
        template.setSubmissionCenter(submissionCenter);
        template.setDescription(description);
        template.setProject(project);
        template.setTier(tier);
        template.setIsStory(isStory);
        template.setStoryTitle(storyTitle);
        template.setPiName(piName);
        template.setFirstName(firstName);
        template.setLastName(lastName);
        template.setEmail(email);
        template.setPhone(phone);

        template.setSubjectColumns(new String[] { "" });
        template.setSubjectClasses(new String[] { "" });
        template.setSubjectRoles(new String[] { "" });
        template.setSubjectDescriptions(new String[] { "" });
        template.setEvidenceColumns(new String[] { "" });
        template.setEvidenceTypes(new String[] { "" });
        template.setValueTypes(new String[] { "" });
        template.setEvidenceDescriptions(new String[] { "" });

        template.setObservationNumber(0);
        template.setObservations(new String[] { "" });
        template.setSummary("");

        try {
            dashboardDao.save(template);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("template.getId()=" + template.getId());
            log.error(e.getMessage());
            String msg = "The new submission template was not created successfully. ID=" + template.getId();
            if (template.getId() == null) {
                msg = "The new submission template to be created does not have a proper ID";
            }
            return new ResponseEntity<String>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(template.getId().toString(), HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "clone", method = { RequestMethod.POST }, headers = "Accept=application/text")
    public ResponseEntity<String> cloneSubmissionTemplate(@RequestParam("centerId") Integer centerId,
            @RequestParam("templateId") Integer templateId) {
        SubmissionTemplate existing = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
        SubmissionTemplate template = new SubmissionTemplateImpl();
        template.setDisplayName(existing.getDisplayName() + " - clone");
        template.setDateLastModified(new Date());
        SubmissionCenter submissionCenter = dashboardDao.getEntityById(SubmissionCenter.class, centerId);
        template.setSubmissionCenter(submissionCenter);
        template.setDescription(existing.getDescription());
        template.setProject(existing.getProject());
        template.setTier(existing.getTier());
        template.setIsStory(existing.getIsStory());
        template.setStoryTitle(existing.getStoryTitle());
        template.setPiName(existing.getPiName());
        template.setFirstName(existing.getFirstName());
        template.setLastName(existing.getLastName());
        template.setEmail(existing.getEmail());
        template.setPhone(existing.getPhone());

        template.setSubjectColumns(existing.getSubjectColumns());
        template.setSubjectClasses(existing.getSubjectClasses());
        template.setSubjectRoles(existing.getSubjectRoles());
        template.setSubjectDescriptions(existing.getSubjectDescriptions());
        template.setEvidenceColumns(existing.getEvidenceColumns());
        template.setValueTypes(existing.getValueTypes());
        template.setEvidenceTypes(existing.getEvidenceTypes());
        template.setEvidenceDescriptions(existing.getEvidenceDescriptions());
        template.setObservationNumber(0);
        template.setObservations(new String[] { "" });
        template.setSummary(existing.getSummary());

        dashboardDao.save(template);

        return new ResponseEntity<String>(template.getId().toString(), HttpStatus.OK);
    }

    private ServletContext servletContext;

    @Autowired
    @Qualifier("uploadLocation")
    private String uploadLocation = "";

    @Autowired
    @Qualifier("subjectDataLocation")
    private String subjectDataLocation = "";

    @Transactional
    @RequestMapping(value = "update-description", method = { RequestMethod.POST }, headers = "Accept=application/text")
    public ResponseEntity<String> updateSubmissionDescription(@RequestParam("id") Integer templateId,
            @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
            @RequestParam("email") String email, @RequestParam("phone") String phone,
            @RequestParam("displayName") String name, @RequestParam("description") String description,
            @RequestParam("project") String project, @RequestParam("tier") Integer tier,
            @RequestParam("isStory") Boolean isStory, @RequestParam("storyTitle") String storyTitle,
            @RequestParam("piName") String piName, HttpServletRequest request) {
        log.info("update-description request from " + request.getRemoteAddr());
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
        template.setDisplayName(name);
        template.setDateLastModified(new Date());
        template.setDescription(description);
        template.setProject(project);
        template.setTier(tier);
        template.setIsStory(isStory);
        template.setStoryTitle(storyTitle);
        template.setPiName(piName);
        template.setFirstName(firstName);
        template.setLastName(lastName);
        template.setEmail(email);
        template.setPhone(phone);

        try {
            dashboardDao.update(template);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("template.getId()=" + template.getId());
            log.error(e.getMessage());
            return new ResponseEntity<String>(
                    "The submission template description was not updated successfully. ID=" + template.getId(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>("SubmissionTemplate " + templateId + " description UPDATED", HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "update", method = { RequestMethod.POST }, headers = "Accept=application/text")
    public ResponseEntity<String> updateSubmissionTemplate(@RequestParam("id") Integer templateId,
            @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
            @RequestParam("email") String email, @RequestParam("phone") String phone,
            @RequestParam("displayName") String name, @RequestParam("description") String description,
            @RequestParam("project") String project, @RequestParam("tier") Integer tier,
            @RequestParam("isStory") Boolean isStory, @RequestParam("storyTitle") String storyTitle,
            @RequestParam("piName") String piName, @RequestParam("subjectColumns[]") String[] subjects,
            @RequestParam("subjectClasses[]") String[] subjectClasses,
            @RequestParam("subjectRoles[]") String[] subjectRoles,
            @RequestParam("subjectDescriptions[]") String[] subjectDescriptions,
            @RequestParam("evidenceColumns[]") String[] evidences,
            @RequestParam("evidenceTypes[]") String[] evidenceTypes, @RequestParam("valueTypes[]") String[] valueTypes,
            @RequestParam("evidenceDescriptions[]") String[] evidenceDescriptions,
            @RequestParam("observationNumber") Integer observationNumber,
            @RequestParam(value = "observations[]", required = false, defaultValue = "") String[] observations,
            HttpServletRequest request) {
        log.info("update request from " + request.getRemoteAddr());
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
        template.setDisplayName(name);
        template.setDateLastModified(new Date());
        template.setDescription(description);
        template.setProject(project);
        template.setTier(tier);
        template.setIsStory(isStory);
        template.setStoryTitle(storyTitle);
        template.setPiName(piName);
        template.setFirstName(firstName);
        template.setLastName(lastName);
        template.setEmail(email);
        template.setPhone(phone);

        if (subjects.length == 1) {
            String x = String.join(",", subjectDescriptions);
            subjectDescriptions = new String[] { x };
        }
        if (evidences.length == 1) {
            String x = String.join(",", evidenceDescriptions);
            evidenceDescriptions = new String[] { x };
        }

        template.setSubjectColumns(subjects);
        template.setSubjectClasses(subjectClasses);
        template.setSubjectRoles(subjectRoles);
        template.setSubjectDescriptions(subjectDescriptions);
        template.setEvidenceColumns(evidences);
        template.setEvidenceTypes(evidenceTypes);
        template.setValueTypes(valueTypes);
        template.setEvidenceDescriptions(evidenceDescriptions);
        template.setObservationNumber(observationNumber);

        String[] previousObservations = template.getObservations();
        if (previousObservations == null)
            previousObservations = new String[0];

        String fileLocation = getFileLocationPerTemplate(template);

        int subjectColumnCount = subjects.length;
        int evidenceColumnCount = evidences.length;
        int columnTagCount = subjectColumnCount + evidenceColumnCount;
        if (observations.length != observationNumber * columnTagCount) {
            log.error("unmatched observation number " + observations.length);
            return new ResponseEntity<String>("unmatched observation number " + observations.length,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        for (int i = 0; i < valueTypes.length; i++) {
            if (valueTypes[i].equals("file")) {
                for (int j = 0; j < observationNumber; j++) {
                    int index = columnTagCount * j + subjectColumnCount + i;
                    if (index >= observations.length) {
                        log.error(
                                "ERROR: observation index=" + index + ">= observation length=" + observations.length);
                        continue;
                    }
                    String obv = observations[index];
                    int base64Mark = obv.indexOf("base64:");
                    if (obv == null || base64Mark <= 0) {
                        log.info("no new observation content for column#=" + i + " observation#=" + j + " observation="
                                + obv);
                        if (index < previousObservations.length) {
                            log.info("keep previous content at index " + index + ":" + previousObservations[index]);
                            String previousContent = previousObservations[index].replace("\"", ""); // not expected
                                                                                                    // except for
                                                                                                    // corrupted data
                            observations[index] = previousContent;
                        }
                        continue; // prevent later null pointer exception
                    }
                    File directory = new File(fileLocation);
                    boolean okDirectory = directory.isDirectory();
                    if (!directory.exists()) {
                        okDirectory = directory.mkdirs();
                    }
                    if (!okDirectory) {
                        return new ResponseEntity<String>("SubmissionTemplate " + templateId
                                + " NOT updated because the subdirectory " + fileLocation + " cannot be created",
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    String filename = fileLocation + obv.substring(0, obv.indexOf(":"));
                    try (FileOutputStream stream = new FileOutputStream(filename)) {
                        stream.write(DatatypeConverter.parseBase64Binary(obv.substring(base64Mark + 7)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // new File(previousObservations[index]).delete(); // TODO cannot remove the
                    // previous upload safely. it may be used for a different observation
                    String relativePathAndMimeType = obv.substring(0, base64Mark - 1);
                    observations[index] = relativePathAndMimeType;
                }
            }
        }
        log.debug("after processing uploaded files");
        template.setObservations(observations);

        try {
            dashboardDao.update(template);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("template.getId()=" + template.getId());
            log.error(e.getMessage());
            return new ResponseEntity<String>(
                    "The submission template was not updated successfully. ID=" + template.getId(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>("SubmissionTemplate " + templateId + " UPDATED", HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "update-summary", method = { RequestMethod.POST }, headers = "Accept=application/text")
    public ResponseEntity<String> updateObservationSummary(@RequestParam("id") Integer templateId,
            @RequestParam("summary") String summary, HttpServletRequest request) {
        log.info("update request from " + request.getRemoteAddr());
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
        template.setSummary(summary);

        try {
            dashboardDao.update(template);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("template.getId()=" + template.getId());
            log.error(e.getMessage());
            return new ResponseEntity<String>(
                    "The observation summary was not updated successfully. ID=" + template.getId(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.debug("ready to respond OK");
        return new ResponseEntity<String>("SubmissionTemplate " + templateId + " observation summary UPDATED",
                HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "delete", method = { RequestMethod.POST }, headers = "Accept=application/text")
    public ResponseEntity<String> deleteSubmissionTemplate(@RequestParam("templateId") Integer templateId) {
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
        dashboardDao.delete(template);
        return new ResponseEntity<String>("SubmissionTemplate " + templateId + " DELETED", HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value="validate", method = {RequestMethod.GET})
    public ResponseEntity<String> validate(
            @RequestParam("templateId") Integer templateId)
    {
        log.debug("request received for templateId=" + templateId);
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);

        String fileLocation = getFileLocationPerTemplate(template);

        Path topDir = Paths.get(fileLocation); // this may not exist if there is no attachment
        if(!topDir.toFile().exists()) {
            try {
                Files.createDirectory(topDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(!topDir.toFile().isDirectory()) {
            log.error(topDir+" pre-exists but is not a directory.");
        }

        List<String> files = new ArrayList<String>();
        try {
            TxtFileCreator txtFileCreator = new TxtFileCreator(template, topDir);
            files = txtFileCreator.createTextFiles();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(
                "Error in creating txt files: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
        }

        if(log.isDebugEnabled()) { // the Excel file are not needed by the validation script
            SpreadsheetCreator creator = new SpreadsheetCreator(template, fileLocation);
            try {
                byte[] workbookAsByteArray = creator.createWorkbookAsByteArray();
                Files.write(Paths.get(fileLocation+"dashboard-CV-master.xls"), workbookAsByteArray);
                files.add("dashboard-CV-master.xls");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidPathException e) {
                e.printStackTrace();
            }
        }

        // run python script to validate
        String validationScript = servletContext.getRealPath("submissionCheck.py");
        ValidationReport report = new ValidationReport(validationScript, subjectDataLocation, topDir, files.toArray(new String[0]), pythonCommand);
        report.export();
        log.debug("finished running python script");
        JSONSerializer jsonSerializer = new JSONSerializer().exclude("class");
        String response = jsonSerializer.deepSerialize(report);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(response, headers, HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "download", method = { RequestMethod.POST })
    public void downloadTemplate(@RequestParam("template-id") Integer templateId,
            @RequestParam("filename") String filename, HttpServletResponse response) {
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);

        String fileLocation = getFileLocationPerTemplate(template);
        SpreadsheetCreator creator = new SpreadsheetCreator(template, fileLocation);

        response.setContentType("application/zip");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + ".zip\"");
        response.addHeader("Content-Transfer-Encoding", "binary");

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            zipOutputStream.putNextEntry(new ZipEntry("template" + templateId + ".xls"));
            zipOutputStream.write(creator.createWorkbookAsByteArray());
            zipOutputStream.closeEntry();

            Map<String, Path> files = creator.getUploadedFiles();
            for (String fname : files.keySet()) {
                Path savedPath = files.get(fname);
                zipOutputStream.putNextEntry(new ZipEntry(fname));
                zipOutputStream.write(Files.readAllBytes(savedPath));
                zipOutputStream.closeEntry();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileLocationPerTemplate(SubmissionTemplate template) {
        if (!uploadLocation.endsWith(File.separator)) { // safe-guard the possible missing separator
            uploadLocation = uploadLocation + File.separator;
        }
        return uploadLocation + template.getSubmissionCenter().getId() + File.separator + template.getId()
                + File.separator;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
