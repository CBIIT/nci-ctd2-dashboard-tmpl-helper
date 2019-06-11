package gov.nih.nci.ctd2.dashboard.impl;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Proxy;

import gov.nih.nci.ctd2.dashboard.model.SubmissionCenter;
import gov.nih.nci.ctd2.dashboard.model.SubmissionTemplate;

@Entity
@Proxy(proxyClass = SubmissionTemplate.class)
@Table(name = "submission_template")
public class SubmissionTemplateImpl extends DashboardEntityImpl implements SubmissionTemplate {
    private static final long serialVersionUID = -4224162359280232544L;
    private static Log log = LogFactory.getLog(SubmissionTemplateImpl.class);

    private SubmissionCenter submissionCenter;
    private String description;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String project;
    private Integer tier;
    private Boolean isStory;
    private Date dateLastModified;
    private String[] subjectColumns = new String[0];
    private String[] evidenceColumns = new String[0];
    private String[] subjectClasses = new String[0];
    private String[] subjectRoles = new String[0];
    private String[] subjectDescriptions = new String[0];
    private String[] evidenceTypes = new String[0];
    private String[] valueTypes = new String[0];
    private String[] evidenceDescriptions = new String[0];
    private Integer observationNumber;
    private String[] observations = new String[0];
    private String summary;
    private String storyTitle;
    private String piName;

    private static final int DESCRIPTION_LENGTH = 1024;

    @Column(length = DESCRIPTION_LENGTH)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description != null && description.length() > DESCRIPTION_LENGTH) {
            description = description.substring(0, DESCRIPTION_LENGTH);
            log.warn("description truncated to " + DESCRIPTION_LENGTH);
        }
        this.description = description;
    }

    @ManyToOne(targetEntity = SubmissionCenterImpl.class)
    public SubmissionCenter getSubmissionCenter() {
        return submissionCenter;
    }

    public void setSubmissionCenter(SubmissionCenter submissionCenter) {
        this.submissionCenter = submissionCenter;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }

    @Override
    public Boolean getIsStory() {
        return isStory;
    }

    @Override
    public void setIsStory(Boolean isStory) {
        this.isStory = isStory;
    }

    public Date getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(Date dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(columnDefinition = "mediumblob")
    @Override
    public String[] getSubjectColumns() {
        return subjectColumns;
    }

    @Override
    public void setSubjectColumns(String[] subjects) {
        subjectColumns = subjects;
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getEvidenceColumns() {
        return evidenceColumns;
    }

    @Override
    public void setEvidenceColumns(String[] evidences) {
        evidenceColumns = evidences;
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getSubjectClasses() {
        return subjectClasses;
    }

    @Override
    public void setSubjectClasses(String[] c) {
        subjectClasses = c;
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getSubjectRoles() {
        return subjectRoles;
    }

    @Override
    public void setSubjectRoles(String[] r) {
        subjectRoles = r;
    }

    @Column(columnDefinition = "mediumblob")
    @Override
    public String[] getSubjectDescriptions() {
        return subjectDescriptions;
    }

    @Override
    public void setSubjectDescriptions(String[] d) {
        subjectDescriptions = d;
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getEvidenceTypes() {
        return evidenceTypes;
    }

    @Override
    public void setEvidenceTypes(String[] t) {
        evidenceTypes = t;
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getValueTypes() {
        return valueTypes;
    }

    @Override
    public void setValueTypes(String[] v) {
        valueTypes = v;
    }

    @Column(columnDefinition = "mediumblob")
    @Override
    public String[] getEvidenceDescriptions() {
        return evidenceDescriptions;
    }

    @Override
    public void setEvidenceDescriptions(String[] d) {
        evidenceDescriptions = d;
    }

    @Override
    public Integer getObservationNumber() {
        return observationNumber;
    }

    @Override
    public void setObservationNumber(Integer observationNumber) {
        this.observationNumber = observationNumber;
    }

    @Column(name ="observation_array", columnDefinition = "blob")
    @Override
    public String[] getObservations() {
        return observations;
    }

    @Override
    public void setObservations(String[] d) {
        observations = d;
    }

    @Column(length = 1024)
    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public void setSummary(String s) {
        summary = s;
    }

    @Override
    public String getStoryTitle() {
        return storyTitle;
    }

    @Override
    public void setStoryTitle(String s) {
        storyTitle = s;
    }

    @Override
    public String getPiName() {
        return piName;
    }

    @Override
    public void setPiName(String piName) {
        this.piName = piName;
    }
}
