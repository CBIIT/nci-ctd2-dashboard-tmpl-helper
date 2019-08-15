package gov.nih.nci.ctd2.dashboard.dao;

import gov.nih.nci.ctd2.dashboard.model.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

import java.util.Date;

public class DashboardDaoTest {
    private DashboardDao dashboardDao;
    private DashboardFactory dashboardFactory;

    @Before
    public void initiateDao() {
        ApplicationContext appContext = new ClassPathXmlApplicationContext(
                "classpath*:META-INF/spring/testApplicationContext.xml");
        this.dashboardDao = (DashboardDao) appContext.getBean("dashboardDao");
        this.dashboardFactory = (DashboardFactory) appContext.getBean("dashboardFactory");
    }

    @Test
    public void createDaoTest() {
        assertNotNull(dashboardDao);
    }

    @Test
    public void createAndPersistTest() {
        SubmissionCenter center = dashboardFactory.create(SubmissionCenter.class);
        center.setDisplayName("Test Center Name");

        SubmissionTemplate template = dashboardFactory.create(SubmissionTemplate.class);
        template.setDateLastModified(new Date());
        template.setDescription("a test template");
        template.setEmail("email@somewhere.com");
        template.setFirstName("John");
        template.setLastName("Doe");
        template.setIsStory(false);
        template.setObservationNumber(0);
        template.setPhone("");
        template.setPiName("");
        template.setProject("project name");
        template.setStoryTitle("");
        template.setSubmissionCenter(center);
        template.setSummary("observation summary");
        template.setTier(0);

        dashboardDao.save(center);
        dashboardDao.save(template);

        assertNotNull(dashboardDao.getEntityById(SubmissionCenter.class, center.getId()));
        assertNotNull(dashboardDao.getEntityById(SubmissionTemplate.class, template.getId()));
    }
}
