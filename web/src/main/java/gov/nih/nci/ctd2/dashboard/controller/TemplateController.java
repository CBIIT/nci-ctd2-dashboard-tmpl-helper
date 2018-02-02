package gov.nih.nci.ctd2.dashboard.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import gov.nih.nci.ctd2.dashboard.util.SpreadsheetUtil;

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
            @RequestParam("storyTitle") String storyTitle
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
        template.setObservations("");

        dashboardDao.save(template);

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
        template.setObservations("");
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
            @RequestParam("subjectColumns[]") String[] subjects,
            @RequestParam("subjectClasses[]") String[] subjectClasses,
            @RequestParam("subjectRoles[]") String[] subjectRoles,
            @RequestParam("subjectDescriptions[]") String[] subjectDescriptions,
            @RequestParam("evidenceColumns[]") String[] evidences,
            @RequestParam("evidenceTypes[]") String[] evidenceTypes,
            @RequestParam("valueTypes[]") String[] valueTypes,
            @RequestParam("evidenceDescriptions[]") String[] evidenceDescriptions,
            @RequestParam("observationNumber") Integer observationNumber,
            @RequestParam("observations") String allObservations,
            @RequestParam("summary") String summary
            )
    {
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
    	template.setDisplayName(name);
    	template.setDateLastModified(new Date());
    	template.setDescription(description);
    	template.setProject(project);
    	template.setTier(tier);
        template.setIsStory(isStory);
        template.setStoryTitle(storyTitle);
    	template.setFirstName(firstName);
    	template.setLastName(lastName);
        template.setEmail(email);
        template.setPhone(phone);

        if(subjects.length==1) {
            String x = join(subjectDescriptions);
            subjectDescriptions = new String[]{x};
        }
        if(evidences.length==1) {
            String x = join(evidenceDescriptions);
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

        String[] observations = allObservations.split(",", -1);
        String[] previousObservations = new String[0];
        String p = template.getObservations();
        if(p!=null)
            previousObservations = p.split(",", -1);

        String fileLocation = getFileLocationPerTemplate(template);

        int subjectColumnCount = subjects.length;
        int evidenceColumnCount = evidences.length;
        int columnTagCount = subjectColumnCount + evidenceColumnCount;
        for(int i=0; i<valueTypes.length; i++) {
            if(valueTypes[i].equals("file")) {
                for(int j=0; j<observationNumber; j++) {
                    int index = columnTagCount*j + subjectColumnCount + i;
                    if(index>=observations.length) {
                        System.out.println("ERROR: observation index="+index+">= observation length="+observations.length);
                        continue;
                    }
                    String obv = observations[index];
                    if(obv==null || obv.indexOf("::")<=0) {
                        System.out.println("no new observation content for column#="+i+" observation#="+j+" observation="+obv);
                        if(index<previousObservations.length)observations[index] = previousObservations[index];
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
                        byte[] bytes = DatatypeConverter.parseBase64Binary(obv.substring( obv.indexOf("base64:")+7 ));
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
                    String relativePathAndMimeType = obv.substring(0, obv.indexOf(";base64"));
                    observations[index] = relativePathAndMimeType;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<observations.length-1; i++){
            sb.append(observations[i]).append(",");
        }
        if(observations.length>1);
            sb.append(observations[observations.length-1]);
        template.setObservations(sb.toString());

        template.setSummary(summary);

        dashboardDao.update(template);

        return new ResponseEntity<String>("SubmissionTemplate " + templateId + " UPDATED", HttpStatus.OK);
    }

    private static String join(String[] s) { // join is supported Java 8. this is to make it work for Java 7
        // assume s is not null
        if(s.length==0) return "";
        if(s.length==1) return s[0];
        StringBuffer sb = new StringBuffer(s[0]);
        for(int i=1; i<s.length; i++) sb.append(",").append(s[i]);
        return sb.toString();
    }

    private String[] uploadedFiles(Integer templateId) {
        SubmissionTemplate template = dashboardDao.getEntityById(SubmissionTemplate.class, templateId);
        if(template.getObservations()==null) return new String[0]; // should not happen for correct data
        String[] valueTypes = template.getValueTypes();
        Integer observationNumber = template.getObservationNumber();
        int subjectColumnCount = template.getSubjectColumns().length;
        int evidenceColumnCount = template.getEvidenceColumns().length;
        int columnTagCount = subjectColumnCount + evidenceColumnCount;
        String[] observations = template.getObservations().split(",", -1);
        Set<String> files = new HashSet<String>(); // duplicate entry not allowed in ZIP
        for(int i=0; i<valueTypes.length; i++) {
            if(valueTypes[i].equals("file")) {
                for(int j=0; j<observationNumber; j++) {
                    int index = columnTagCount*j + subjectColumnCount + i;
                    String fileInfo = observations[index];
                    if(fileInfo==null || fileInfo.trim().length()==0) {
                        continue;
                    }
                    // remove mime type
                    int mimeMark = fileInfo.indexOf("::");
                    if(mimeMark>=0) {
                        fileInfo = fileInfo.substring(0, mimeMark);
                    }
                    // ignore possible subdirectory names
                    int sep = fileInfo.lastIndexOf('/');
                    if(sep>=0) fileInfo = fileInfo.substring(sep+1);
                    sep = fileInfo.lastIndexOf('\\');
                    if(sep>0) fileInfo = fileInfo.substring(sep+1);
                    files.add(fileInfo);
                }
            }
        }
        return files.toArray(new String[0]);
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

        HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            SpreadsheetUtil.createMetaDataSheet(workbook, template);
            SpreadsheetUtil.createDataSheet(workbook, template);
        } catch (Exception e) { /* safeguard data-caused exception */
            e.printStackTrace();
        }

        response.setContentType("application/zip");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + ".zip\"");
        response.addHeader("Content-Transfer-Encoding", "binary");

        String submissionName = SpreadsheetUtil.getSubmissionName(template);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
            zipOutputStream.putNextEntry(new ZipEntry("template"+templateId+".xls"));
            zipOutputStream.write(outputStream.toByteArray());
            zipOutputStream.closeEntry();

            String fileLocation = getFileLocationPerTemplate(template);
            String[] files = uploadedFiles(templateId);
            for(String fname : files) {
                Path savedPath = Paths.get(fileLocation + fname);
                if(!savedPath.toFile().exists()) { // this should not happen, but be cautious anyway
                    log.error("ERROR: uploaded file "+savedPath.toFile()+" not found");
                    continue; 
                }
                zipOutputStream.putNextEntry(new ZipEntry( SpreadsheetUtil.getZippedPath(fname, submissionName) ));
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
