package gov.nih.nci.ctd2.dashboard.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import flexjson.JSONSerializer;
import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.model.SubmissionCenter;
import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;
import gov.nih.nci.ctd2.dashboard.util.DateTransformer;

@Controller
@RequestMapping("/api")
public class APIController {
    private static final Log log = LogFactory.getLog(APIController.class);

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    @Qualifier("maxNumberOfEntities")
    private Integer maxNumberOfEntities = 100;

    public Integer getMaxNumberOfEntities() {
        return maxNumberOfEntities;
    }

    public void setMaxNumberOfEntities(Integer maxNumberOfEntities) {
        this.maxNumberOfEntities = maxNumberOfEntities;
    }

    @Transactional
    @RequestMapping(value = "centers", method = { RequestMethod.GET }, headers = "Accept=application/json")
    public ResponseEntity<String> getCenters() {
        List<SubmissionCenter> centers = dashboardDao.findEntities(SubmissionCenter.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        JSONSerializer jsonSerializer = new JSONSerializer().transform(new DateTransformer(), Date.class);
        String s = jsonSerializer.serialize(centers);
        return new ResponseEntity<String>(s, headers, HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "templates/{centerId}", method = { RequestMethod.GET }, headers = "Accept=application/json")
    public ResponseEntity<String> getTemplates(@PathVariable("centerId") Integer centerId) {
        List<SubmissionTemplate> templates = new ArrayList<SubmissionTemplate>();
        SubmissionCenter submissionCenter = dashboardDao.getEntityById(SubmissionCenter.class, centerId);
        for (SubmissionTemplate submissionTemplate : dashboardDao.findEntities(SubmissionTemplate.class)) {
            if (submissionTemplate.getSubmissionCenter().equals(submissionCenter)) {
                forceConsistency(submissionTemplate);
                templates.add(submissionTemplate);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        JSONSerializer jsonSerializer = new JSONSerializer().transform(new DateTransformer(), Date.class);
        String s = jsonSerializer.deepSerialize(templates);
        return new ResponseEntity<String>(s, headers, HttpStatus.OK);
    }

    /*
     * safe-guard the data for the client in case it is inconsistent for some reason
     */
    private void forceConsistency(SubmissionTemplate submissionTemplate) {
        // TODO complete this for all relevant fields
        String[] subjectColumns = submissionTemplate.getSubjectColumns();
        if (subjectColumns == null) {
            subjectColumns = new String[0];
            submissionTemplate.setSubjectColumns(subjectColumns);
        }
        String[] evidenceColumns = submissionTemplate.getEvidenceColumns();
        if (evidenceColumns == null) {
            evidenceColumns = new String[0];
            submissionTemplate.setEvidenceColumns(evidenceColumns);
        }
        String[] evidenceTypes = submissionTemplate.getEvidenceTypes();
        if (evidenceTypes == null) {
            submissionTemplate.setEvidenceTypes(new String[0]);
        }
        String[] valueTypes = submissionTemplate.getValueTypes();
        if (valueTypes == null) {
            submissionTemplate.setValueTypes(new String[0]);
        }
        String[] evidenceDescription = submissionTemplate.getEvidenceDescriptions();
        if (evidenceDescription == null) {
            submissionTemplate.setEvidenceDescriptions(new String[0]);
        }
        Integer observationNumber = submissionTemplate.getObservationNumber();
        if (observationNumber == null) {
            observationNumber = 0;
            submissionTemplate.setObservationNumber(observationNumber);
        }
        String[] observations = submissionTemplate.getObservations();
        String observationString = submissionTemplate.getObservationString();
        if(observations==null && observationString!=null ) {
            int size = observationNumber * (subjectColumns.length + evidenceColumns.length);
            String[] tmp = new String[size];
            Pattern pattern = Pattern.compile("(\".*?\"|[^\",]*)(\\s*,|\\s*$)");
            Matcher matcher = pattern.matcher(observationString);
            int index = 0;
            while (matcher.find()) {
                if(index<size)
                    tmp[index] = matcher.group(1);
                index++;
            }
            submissionTemplate.setObservations(tmp);
            log.info("observations transferred from the old field");
        } else {
            log.info("observations in the new field");
        }
    }
}
