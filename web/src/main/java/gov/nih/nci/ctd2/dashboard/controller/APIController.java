package gov.nih.nci.ctd2.dashboard.controller;

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
import gov.nih.nci.ctd2.dashboard.model.DashboardEntity;
import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;
import gov.nih.nci.ctd2.dashboard.util.DateTransformer;
import gov.nih.nci.ctd2.dashboard.util.ImplTransformer;
import gov.nih.nci.ctd2.dashboard.util.WebServiceUtil;

@Controller
@RequestMapping("/api")
public class APIController {
    @Autowired
    private WebServiceUtil webServiceUtil;

    @Autowired
    @Qualifier("maxNumberOfEntities")
    private Integer maxNumberOfEntities = 100;

    public WebServiceUtil getWebServiceUtil() {
        return webServiceUtil;
    }

    public void setWebServiceUtil(WebServiceUtil webServiceUtil) {
        this.webServiceUtil = webServiceUtil;
    }

    public Integer getMaxNumberOfEntities() {
        return maxNumberOfEntities;
    }

    public void setMaxNumberOfEntities(Integer maxNumberOfEntities) {
        this.maxNumberOfEntities = maxNumberOfEntities;
    }

    @Transactional
    @RequestMapping(value = "centers", method = { RequestMethod.GET }, headers = "Accept=application/json")
    public ResponseEntity<String> getCenters() {
        List<? extends DashboardEntity> centers = webServiceUtil.getDashboardEntities("center", null);

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
        List<SubmissionTemplate> templates = webServiceUtil.getTemplates(centerId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        JSONSerializer jsonSerializer = new JSONSerializer().transform(new ImplTransformer(), Class.class)
                .transform(new DateTransformer(), Date.class);
        String s = jsonSerializer.deepSerialize(templates);
        return new ResponseEntity<String>(s, headers, HttpStatus.OK);
    }
}
