package util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.impl.SubmissionCenterImpl;
import gov.nih.nci.ctd2.dashboard.model.SubmissionCenter;

// a simplistic tool to create the initial list of submission centers
public class AdminTool {

    public static void main(String[] args) {

        ApplicationContext appContext = new ClassPathXmlApplicationContext(
                "classpath*:META-INF/spring/applicationContext.xml");
        DashboardDao dashboardDao = (DashboardDao) appContext.getBean("dashboardDao");

        Map<String, String> centerPIs = new HashMap<String, String>();
        centerPIs.put("Broad Institute", "Stuart L. Schreiber, Ph.D.");
        centerPIs.put("Cold Spring Harbor Laboratory", "Scott Powers, Ph.D.");
        centerPIs.put("Columbia University", "Andrea Califano, Ph.D.");
        centerPIs.put("Dana-Farber Cancer Institute", "William C. Hahn, M.D., Ph.D.");
        centerPIs.put("Emory University", "Haian Fu, Ph.D.");
        centerPIs.put("Fred Hutchinson Cancer Research Center (1)", "Christopher Kemp, Ph.D.");
        centerPIs.put("Fred Hutchinson Cancer Research Center (2)", "Martin McIntosh, Ph.D.");
        centerPIs.put("Stanford University", "Calvin J. Kuo, M.D., Ph.D.");
        centerPIs.put("Translational Genomics Research Institute", "Michael E. Berens, Ph.D.");
        centerPIs.put("University of California San Francisco (1)", "Michael McManus, Ph.D.");
        centerPIs.put("University of California San Francisco (2)", "William A. Weiss, M.D., Ph.D.");
        centerPIs.put("University of Texas MD Anderson Cancer Center", "Gordon B. Mills, M.D., Ph.D.");
        centerPIs.put("University of Texas Southwestern Medical Center", "Michael Roth, Ph.D.");
        centerPIs.put("Johns Hopkins University", "Joel S. Bader, Ph.D.");
        centerPIs.put("Oregon Health and Science University", "Brian J. Druker, M.D.");
        centerPIs.put("University of California San Diego", "Pablo Tamayo, Ph.D.");
        for (String c : centerPIs.keySet()) {
            SubmissionCenter center = new SubmissionCenterImpl();
            center.setDisplayName(c);
            dashboardDao.save(center);
        }
        System.out.println("done.");
    }
}