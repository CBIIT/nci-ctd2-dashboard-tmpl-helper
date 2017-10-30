package gov.nih.nci.ctd2.dashboard.impl;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;

import org.hibernate.annotations.Proxy;

import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;
import gov.nih.nci.ctd2.dashboard.model.TemplateObservation;

@Entity
@Proxy(proxyClass = TemplateObservation.class)
@Table(name = "template_observation")
public class TemplateObservationImpl extends DashboardEntityImpl implements TemplateObservation {
    private SubmissionTemplate submissionTemplate;
    private String[] subjectValues;
    private String[] evidenceValues;

    @ManyToOne(targetEntity = SubmissionTemplateImpl.class)
    @Override
    public SubmissionTemplate getSubmissionTemplate() {
        return submissionTemplate;
    }

    @Override
    public void setSubmissionTemplate(SubmissionTemplate submissionTemplate) {
        this.submissionTemplate = submissionTemplate;
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getSubjectValues() {
        return subjectValues;
    }

    @Override
    public void setSubjectValues(String[] subjects) {
        subjectValues = subjects;
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getEvidenceValues() {
        return evidenceValues;
    }

    @Override
    public void setEvidenceValues(String[] evidences) {
        evidenceValues = evidences;
    }
}
