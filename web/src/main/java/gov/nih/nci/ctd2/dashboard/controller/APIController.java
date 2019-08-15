package gov.nih.nci.ctd2.dashboard.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
                checkNull(submissionTemplate);
                templates.add(submissionTemplate);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        JSONSerializer jsonSerializer = new JSONSerializer().transform(new DateTransformer(), Date.class);
        String s = jsonSerializer.deepSerialize(templates);
        log.debug("returning all templates for center " + centerId);
        return new ResponseEntity<String>(s, headers, HttpStatus.OK);
    }

    static private void checkNull(final SubmissionTemplate submissionTemplate) {
        Objects.requireNonNull(submissionTemplate.getSubjectColumns(), "subjectColumns is null");
        Objects.requireNonNull(submissionTemplate.getEvidenceColumns(), "evidenceColumns is null");
        Objects.requireNonNull(submissionTemplate.getEvidenceTypes(), "evidenceTypes is null");
        Objects.requireNonNull(submissionTemplate.getValueTypes(), "valueTypes is null");
        Objects.requireNonNull(submissionTemplate.getEvidenceDescriptions(), "evidenceDescription is null");
        Objects.requireNonNull(submissionTemplate.getObservationNumber(), "observationNumber ia null");
        Objects.requireNonNull(submissionTemplate.getObservations(), "observations ia null");
        Objects.requireNonNull(submissionTemplate.getEvidenceDescriptions(), "subjectColumns ia null");
    }
}
