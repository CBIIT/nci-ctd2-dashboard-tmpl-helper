package gov.nih.nci.ctd2.dashboard.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.impl.SubmissionTemplateImpl;
import gov.nih.nci.ctd2.dashboard.model.SubmissionCenter;
import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;
import gov.nih.nci.ctd2.dashboard.util.SpreadsheetCreator;

@Controller
@RequestMapping("/template")
public class TemplateController {
    private static final Log log = LogFactory.getLog(TemplateController.class);

    @Autowired
    private DashboardDao dashboardDao;

    @Transactional
    @RequestMapping(value="create", method = {RequestMethod.POST}, headers = "Accept=application/text")
    public 
    ResponseEntity<String>
    createNewSubmissionTemplate( /* the method name has no effect, @RequestMapping value binds this method */
            @RequestParam("centerId") Integer centerId,
            @RequestParam("displayName") String name,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("description") String description,
            @RequestParam("project") String project,
            @RequestParam("tier") Integer tier,
            @RequestParam("isStory") Boolean isStory,
            @RequestParam("storyTitle") String storyTitle,
            @RequestParam("piName") String piName
            )
    {
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

        template.setSubjectColumns(new String[]{""});
        template.setSubjectClasses(new String[]{""});
        template.setSubjectRoles(new String[]{""});
        template.setSubjectDescriptions(new String[]{""});
        template.setEvidenceColumns(new String[]{""});
        template.setEvidenceTypes(new String[]{""});
        template.setValueTypes(new String[]{""});
        template.setEvidenceDescriptions(new String[]{""});

        template.setObservationNumber(0);
        template.setObservations(new String[]{""});

        try {
            dashboardDao.save(template);
        } catch(Exception e) {
            e.printStackTrace();
            log.error("template.getId()="+template.getId());
            log.error(e.getMessage());
            String msg = "The new submission template was not created successfully. ID="+template.getId();
            if(template.getId()==null) {
                msg = "The new submission template to be created does not have a proper ID";
            }
            return new ResponseEntity<String>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    	return new ResponseEntity<String>(template.getId().toString(), HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value="clone", method = {RequestMethod.POST}, headers = "Accept=application/text")
    public 
    ResponseEntity<String>
    cloneSubmissionTemplate(
            @RequestParam("centerId") Integer centerId,
            @RequestParam("templateId") Integer templateId
            )
    {
        SubmissionTemplate existing = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
    	SubmissionTemplate template = new SubmissionTemplateImpl();
    	template.setDisplayName(existing.getDisplayName()+" - clone");
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
        template.setObservations(new String[]{""});
        template.setSummary(existing.getSummary());

    	dashboardDao.save(template);

    	return new ResponseEntity<String>(template.getId().toString(), HttpStatus.OK);
    }

    @Autowired
    @Qualifier("uploadLocation")
    private String uploadLocation = "";

    @Transactional
    @RequestMapping(value="update", method = {RequestMethod.POST}, headers = "Accept=application/text")
    public 
    ResponseEntity<String>
    updateSubmissionTemplate(
            @RequestParam("id") Integer templateId,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("displayName") String name,
            @RequestParam("description") String description,
            @RequestParam("project") String project,
            @RequestParam("tier") Integer tier,
            @RequestParam("isStory") Boolean isStory,
            @RequestParam("storyTitle") String storyTitle,
            @RequestParam("piName") String piName,
            @RequestParam("subjectColumns[]") String[] subjects,
            @RequestParam("subjectClasses[]") String[] subjectClasses,
            @RequestParam("subjectRoles[]") String[] subjectRoles,
            @RequestParam("subjectDescriptions[]") String[] subjectDescriptions,
            @RequestParam("evidenceColumns[]") String[] evidences,
            @RequestParam("evidenceTypes[]") String[] evidenceTypes,
            @RequestParam("valueTypes[]") String[] valueTypes,
            @RequestParam("evidenceDescriptions[]") String[] evidenceDescriptions,
            @RequestParam("observationNumber") Integer observationNumber,
            @RequestParam(value="observations[]", required=false, defaultValue="") String[] observations,
            @RequestParam("summary") String summary,
            HttpServletRequest request
            )
    {
        log.info("update request from "+request.getRemoteAddr());
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

        if(subjects.length==1) {
            String x = String.join(",", subjectDescriptions);
            subjectDescriptions = new String[]{x};
        }
        if(evidences.length==1) {
            String x = String.join(",", evidenceDescriptions);
            evidenceDescriptions = new String[]{x};
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
        if(previousObservations==null)
            previousObservations = new String[0];

        String fileLocation = getFileLocationPerTemplate(template);

        int subjectColumnCount = subjects.length;
        int evidenceColumnCount = evidences.length;
        int columnTagCount = subjectColumnCount + evidenceColumnCount;
        if(observations.length!=observationNumber*columnTagCount) {
            log.error("unmatched observation number "+observations.length);
            return new ResponseEntity<String>("unmatched observation number "+observations.length, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        for(int i=0; i<valueTypes.length; i++) {
            if(valueTypes[i].equals("file")) {
                for(int j=0; j<observationNumber; j++) {
                    int index = columnTagCount*j + subjectColumnCount + i;
                    if(index>=observations.length) {
                        System.out.println("ERROR: observation index="+index+">= observation length="+observations.length);
                        continue;
                    }
                    String obv = observations[index];
                    int base64Mark = obv.indexOf("base64:");
                    if(obv==null || base64Mark<=0) {
                        log.info("no new observation content for column#="+i+" observation#="+j+" observation="+obv);
                        if(index<previousObservations.length) {
                            log.info("keep previous content at index "+index+":"+previousObservations[index]);
                            String previousContent = previousObservations[index].replace("\"", ""); // not expected except for corrupted data
                            observations[index] = previousContent;
                        }
                        continue; // prevent later null pointer exception
                    }
                    File directory = new File(fileLocation);
                    boolean okDirectory = directory.isDirectory();
                    if(!directory.exists()) {
                        okDirectory = directory.mkdirs();
                    }
                    if(!okDirectory) {
                        return new ResponseEntity<String>("SubmissionTemplate " + templateId 
                            + " NOT updated because the subdirectory "+fileLocation+" cannot be created", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    String filename = fileLocation + obv.substring(0, obv.indexOf(":"));
                    FileOutputStream stream = null;
                    try {
                        byte[] bytes = DatatypeConverter.parseBase64Binary(obv.substring( base64Mark + 7 ));
                        stream = new FileOutputStream(filename);
                        stream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(stream!=null)
                            try {
                                stream.close();
                            } catch (IOException e) {
                            }
                    }
                    //new File(previousObservations[index]).delete(); // TODO cannot remove the previous upload safely. it may be used for a different observation
                    String relativePathAndMimeType = obv.substring(0, base64Mark - 1);
                    observations[index] = relativePathAndMimeType;
                }
            }
        }
        template.setObservations(observations);

        template.setSummary(summary);

        try {
            dashboardDao.update(template);
        } catch(Exception e) {
            e.printStackTrace();
            log.error("template.getId()="+template.getId());
            log.error(e.getMessage());
            return new ResponseEntity<String>("The submission template was not updated successfully. ID="+template.getId(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>("SubmissionTemplate " + templateId + " UPDATED", HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value="delete", method = {RequestMethod.POST}, headers = "Accept=application/text")
    public 
    ResponseEntity<String>
    deleteSubmissionTemplate(
            @RequestParam("templateId") Integer templateId
            )
    {
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
        dashboardDao.delete(template);
        return new ResponseEntity<String>("SubmissionTemplate " + templateId + " DELETED", HttpStatus.OK);
    }



    @Transactional
    @RequestMapping(value="download", method = {RequestMethod.POST})
    public void downloadTemplate(
            @RequestParam("template-id") Integer templateId,
            @RequestParam("filename") String filename,
            HttpServletResponse response)
    {
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);

        String fileLocation = getFileLocationPerTemplate(template);
        SpreadsheetCreator creator = new SpreadsheetCreator(template, fileLocation);

        response.setContentType("application/zip");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + ".zip\"");
        response.addHeader("Content-Transfer-Encoding", "binary");

        try {
            byte[] workbookAsByteArray = creator.createWorkbookAsByteArray();

            ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
            zipOutputStream.putNextEntry(new ZipEntry("template"+templateId+".xls"));
            zipOutputStream.write(workbookAsByteArray);
            zipOutputStream.closeEntry();

            Map<String, Path> files = creator.getUploadedFiles();
            for(String fname : files.keySet()) {
                Path savedPath = files.get(fname);
                zipOutputStream.putNextEntry(new ZipEntry( fname ));
                zipOutputStream.write(Files.readAllBytes( savedPath ));
                zipOutputStream.closeEntry();
            }

            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileLocationPerTemplate(SubmissionTemplate template) {
        if(!uploadLocation.endsWith(File.separator)) { // safe-guard the possible missing separator
            uploadLocation = uploadLocation + File.separator;
        }
        return uploadLocation + template.getSubmissionCenter().getId() + File.separator + template.getId() + File.separator;
    }
}
