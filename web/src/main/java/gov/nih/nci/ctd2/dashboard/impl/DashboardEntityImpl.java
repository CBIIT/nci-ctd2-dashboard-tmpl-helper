package gov.nih.nci.ctd2.dashboard.impl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Proxy;

import gov.nih.nci.ctd2.dashboard.model.DashboardEntity;

@Entity
@Proxy(proxyClass= DashboardEntity.class)
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicUpdate
@DynamicInsert
@Table(name = "dashboard_entity")
public class DashboardEntityImpl implements DashboardEntity {

    private static final long serialVersionUID = 6953675960325146562L;
	private Integer id;
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.TABLE) /* this was left to be different from CTD^2 dashboard in case of affecting existing data */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardEntityImpl that = (DashboardEntityImpl) o;
        if(this.getId() == null || that.getId() == null)
            return super.equals(o);

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
