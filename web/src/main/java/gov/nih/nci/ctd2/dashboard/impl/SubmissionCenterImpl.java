package gov.nih.nci.ctd2.dashboard.impl;

import gov.nih.nci.ctd2.dashboard.model.SubmissionCenter;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Proxy(proxyClass= SubmissionCenter.class)
@Table(name = "submission_center")
public class SubmissionCenterImpl extends DashboardEntityImpl implements SubmissionCenter {

    private static final long serialVersionUID = 1L;
    private String piName;

    @Override
    public String getPiName() {
        return piName;
    }

    @Override
    public void setPiName(String piName) {
        this.piName = piName;
    }
}
