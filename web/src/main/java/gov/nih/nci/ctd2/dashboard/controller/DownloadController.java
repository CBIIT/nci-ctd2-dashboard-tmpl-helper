package gov.nih.nci.ctd2.dashboard.controller;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/download")
public class DownloadController {
    private static final Log log = LogFactory.getLog(DownloadController.class);

    @Autowired
    @Qualifier("uploadLocation")
    private String uploadLocation = "";

    @Transactional
    @RequestMapping(value = "report", method = { RequestMethod.GET })
    public void downloadReport(@RequestParam("centerId") Integer centerId,
            @RequestParam("templateId") String templateId, HttpServletResponse response) {
        log.debug("centerId=" + centerId);
        log.debug("templateId=" + templateId);

        response.setContentType("text/plain");
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        response.addHeader("Content-Disposition", "attachment; filename=\"validation-report-" + timestamp + ".txt\"");

        Path path = Paths.get(uploadLocation).resolve(centerId.toString()).resolve(templateId)
                .resolve("validation-report.txt");
        try {
            PrintWriter pw = response.getWriter();
            BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
            String line = br.readLine();
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }
            pw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("done");
    }

}
