package gov.nih.nci.ctd2.dashboard.model;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DashboardFactoryTest {
    @Test
    public void testBeanCreate() {
        DashboardFactory dashboardFactory;

        ApplicationContext appContext =
                new ClassPathXmlApplicationContext("classpath*:META-INF/spring/testApplicationContext.xml");
        dashboardFactory = (DashboardFactory) appContext.getBean("dashboardFactory");

        SubmissionCenter center = dashboardFactory.create(SubmissionCenter.class);
        assertNotNull(center);
        assertEquals(null, center.getId());
        assertNotNull(center.getDisplayName());
    }

}
