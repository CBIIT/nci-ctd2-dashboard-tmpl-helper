package gov.nih.nci.ctd2.dashboard.impl;

import java.util.Date;
import java.util.Objects;

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
    private String[] evidenceMimeTypes = new String[0];
    private Integer observationNumber;
    private String[] observations = new String[0];
    private String summary;
    private String storyTitle;
    private String piName;

    private static final int DESCRIPTION_LENGTH = 1024;
    private static final int PROJECT_LENGTH = 1024;
    private static final int SUMMARY_LENGTH = 1024;
    private static final int STORY_TITLE_LENGTH = 1024;
    private static final int PI_LENGTH = 64;

    @Column(length = DESCRIPTION_LENGTH)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_LENGTH) {
            description = description.substring(0, DESCRIPTION_LENGTH);
            log.warn("description truncated to " + DESCRIPTION_LENGTH);
        }
        this.description = Objects.requireNonNull(description);
    }

    @ManyToOne(targetEntity = SubmissionCenterImpl.class)
    public SubmissionCenter getSubmissionCenter() {
        return submissionCenter;
    }

    public void setSubmissionCenter(SubmissionCenter submissionCenter) {
        this.submissionCenter = Objects.requireNonNull(submissionCenter);
    }

    @Column(length = PROJECT_LENGTH)
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        if (project != null && project.length() > PROJECT_LENGTH) {
            project = project.substring(0, PROJECT_LENGTH);
            log.warn("project truncated to " + PROJECT_LENGTH);
        }
        this.project = Objects.requireNonNull(project);
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = Objects.requireNonNull(tier);
    }

    @Override
    public Boolean getIsStory() {
        return isStory;
    }

    @Override
    public void setIsStory(Boolean isStory) {
        this.isStory = Objects.requireNonNull(isStory);
    }

    public Date getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(Date dateLastModified) {
        this.dateLastModified = Objects.requireNonNull(dateLastModified);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = Objects.requireNonNull(lastName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = Objects.requireNonNull(phone);
    }

    @Column(columnDefinition = "mediumblob")
    @Override
    public String[] getSubjectColumns() {
        return subjectColumns;
    }

    @Override
    public void setSubjectColumns(String[] subjects) {
        subjectColumns = Objects.requireNonNull(subjects);
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getEvidenceColumns() {
        return evidenceColumns;
    }

    @Override
    public void setEvidenceColumns(String[] evidences) {
        evidenceColumns = Objects.requireNonNull(evidences);
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getSubjectClasses() {
        return subjectClasses;
    }

    @Override
    public void setSubjectClasses(String[] c) {
        subjectClasses = Objects.requireNonNull(c);
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getSubjectRoles() {
        return subjectRoles;
    }

    @Override
    public void setSubjectRoles(String[] r) {
        subjectRoles = Objects.requireNonNull(r);
    }

    @Column(columnDefinition = "mediumblob")
    @Override
    public String[] getSubjectDescriptions() {
        return subjectDescriptions;
    }

    @Override
    public void setSubjectDescriptions(String[] d) {
        subjectDescriptions = Objects.requireNonNull(d);
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getEvidenceTypes() {
        return evidenceTypes;
    }

    @Override
    public void setEvidenceTypes(String[] t) {
        evidenceTypes = Objects.requireNonNull(t);
    }

    @Column(columnDefinition = "blob")
    @Override
    public String[] getValueTypes() {
        return valueTypes;
    }

    @Override
    public void setValueTypes(String[] v) {
        valueTypes = Objects.requireNonNull(v);
    }

    @Column(columnDefinition = "mediumblob")
    @Override
    public String[] getEvidenceDescriptions() {
        return evidenceDescriptions;
    }

    @Override
    public void setEvidenceDescriptions(String[] d) {
        evidenceDescriptions = Objects.requireNonNull(d);
    }

    @Override
    public Integer getObservationNumber() {
        return observationNumber;
    }

    @Override
    public void setObservationNumber(Integer observationNumber) {
        this.observationNumber = Objects.requireNonNull(observationNumber);
    }

    @Column(name = "observation_array", columnDefinition = "blob")
    @Override
    public String[] getObservations() {
        return observations;
    }

    @Override
    public void setObservations(String[] d) {
        observations = Objects.requireNonNull(d);
    }

    @Override
    @Column(length = SUMMARY_LENGTH)
    public String getSummary() {
        return summary;
    }

    @Override
    public void setSummary(String s) {
        if(s == null) s = ""; // necessary only for existing unchecked data
        if (s != null && s.length() > SUMMARY_LENGTH) {
            s = s.substring(0, SUMMARY_LENGTH);
            log.warn("summary truncated to " + SUMMARY_LENGTH);
        }
        summary = Objects.requireNonNull(s);
    }

    @Override
    @Column(length = STORY_TITLE_LENGTH)
    public String getStoryTitle() {
        return storyTitle;
    }

    @Override
    public void setStoryTitle(String s) {
        if (s != null && s.length() > STORY_TITLE_LENGTH) {
            s = s.substring(0, STORY_TITLE_LENGTH);
            log.warn("Story title truncated to " + STORY_TITLE_LENGTH);
        }
        storyTitle = Objects.requireNonNull(s);
    }

    @Override
    @Column(length = PI_LENGTH)
    public String getPiName() {
        return piName;
    }

    @Override
    public void setPiName(String piName) {
        if(piName == null) piName = ""; // necessary only for existing unchecked data
        if (piName != null && piName.length() > PI_LENGTH) {
            piName = piName.substring(0, PI_LENGTH);
            log.warn("PI name truncated to " + PI_LENGTH);
        }
        this.piName = Objects.requireNonNull(piName);
    }

    @Override
    public String[] getEvidenceMimeTypes() {
        return evidenceMimeTypes;
    }

    @Override
    public void setEvidenceMimeTypes(String[] evidenceMimeTypes) {
        this.evidenceMimeTypes = evidenceMimeTypes;
    }
}
