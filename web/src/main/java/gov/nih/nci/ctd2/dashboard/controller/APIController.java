package gov.nih.nci.ctd2.dashboard.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import gov.nih.nci.ctd2.dashboard.util.ImplTransformer;

@Controller
@RequestMapping("/api")
public class APIController {
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
        JSONSerializer jsonSerializer = new JSONSerializer().transform(new ImplTransformer(), Class.class)
                .transform(new DateTransformer(), Date.class);
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
        JSONSerializer jsonSerializer = new JSONSerializer().transform(new ImplTransformer(), Class.class)
                .transform(new DateTransformer(), Date.class);
        String s = jsonSerializer.deepSerialize(templates);
        return new ResponseEntity<String>(s, headers, HttpStatus.OK);
    }

    /* safe-guard the data for the client in case it is inconsistent for some reason */
    private void forceConsistency(SubmissionTemplate submissionTemplate) {
        // TODO complete this for all relevant fields
        String[] subjectColumns = submissionTemplate.getSubjectColumns();
        if(subjectColumns==null) {
            subjectColumns = new String[0];
            submissionTemplate.setSubjectColumns( subjectColumns );
        }
        String[] evidenceColumns = submissionTemplate.getEvidenceColumns();
        if(evidenceColumns==null) {
            evidenceColumns = new String[0];
            submissionTemplate.setEvidenceColumns( evidenceColumns );
        }
        String[] evidenceTypes = submissionTemplate.getEvidenceTypes();
        if(evidenceTypes==null) {
            submissionTemplate.setEvidenceTypes( new String[0] );
        }
        String[] valueTypes = submissionTemplate.getValueTypes();
        if(valueTypes==null) {
            submissionTemplate.setValueTypes( new String[0] );
        }
        String[] evidenceDescription = submissionTemplate.getEvidenceDescriptions();
        if(evidenceDescription==null) {
            submissionTemplate.setEvidenceDescriptions( new String[0] );
        }
        Integer observationNumber = submissionTemplate.getObservationNumber();
        if(observationNumber==null) {
            observationNumber = 0;
            submissionTemplate.setObservationNumber(observationNumber);
        }
        String[] observations = new String[0];
        if(submissionTemplate.getObservations()!=null) {
            observations = submissionTemplate.getObservations().split(",", -1);
        }
        int t = observationNumber*(subjectColumns.length+evidenceColumns.length);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<observations.length; i++) {
            sb.append( observations[i] ).append(",");
        }
        for(int i=observations.length; i<t; i++) { // in case we need more commas
            sb.append(",");
        }
        submissionTemplate.setObservations(sb.toString());
    }
}
