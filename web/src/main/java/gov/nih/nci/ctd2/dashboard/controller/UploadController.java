package gov.nih.nci.ctd2.dashboard.controller;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import flexjson.JSONSerializer;
import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.util.Validator;
import gov.nih.nci.ctd2.dashboard.util.ValidationException;

@Controller
@RequestMapping("/upload")
public class UploadController {
    private static final Log log = LogFactory.getLog(UploadController.class);

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    @Qualifier("uploadLocation")
    private String uploadLocation = "";

    @Autowired
    @Qualifier("subjectDataLocation")
    private String subjectDataLocation = "";

    @Transactional
    @RequestMapping(value = "zip", method = { RequestMethod.POST }, headers = "Accept=application/text")
    public ResponseEntity<String> uploadZip(@RequestParam("filename") String filename,
            @RequestParam("filecontent") String filecontent, @RequestParam("centerId") Integer centerId) {

        if (!uploadLocation.endsWith(File.separator)) { // safe-guard the possible missing separator
            uploadLocation = uploadLocation + File.separator;
        }
        String directory = uploadLocation + centerId + File.separator + "unzipped" + File.separator + filename
                + File.separator;
        Path outDir = Paths.get(directory);
        byte[] bytes = DatatypeConverter
                .parseBase64Binary(filecontent.substring(filecontent.indexOf("base64,") + "base64,".length()));
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(bytes));
        ZipEntry entry;

        Path excelFilePath = null;
        try {

            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path filePath = outDir.resolve(entry.getName());
                if (entry.getName().endsWith(".xls")) {
                    excelFilePath = filePath; // it is supposed to have one such file
                }
                final File file = filePath.toFile();
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    byte[] buffer = new byte[1024];
                    OutputStream outputStream = new FileOutputStream(file);
                    int len = 0;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.close();
                }
            }

            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }

        ValidationReport report = null;
        try {
            report = validate(excelFilePath);
        } catch(ValidationException e) {
            e.printStackTrace();
            report = new ValidationReport("file " + filename + ":"+e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
            report = new ValidationReport("Unexpected exception: " + filename + " "+e.getMessage());
        }

        log.info(filename + " uploaded and unzipped");

        JSONSerializer jsonSerializer = new JSONSerializer().exclude("class");
        String response = jsonSerializer.deepSerialize(report);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(response, headers, HttpStatus.OK);
    }

    private ValidationReport validate(Path excelFilePath) throws IOException, ValidationException {
        if (!excelFilePath.toFile().exists()) {
            log.error("expected file " + excelFilePath.toFile() + " not existing");
            return new ValidationReport("expected file " + excelFilePath.toFile() + " not existing");
        }

        Validator validator = new Validator(excelFilePath, dashboardDao);
        /*
         * The tab-delimited text files are created put in the directory structure
         * overlapping the original Excel file and uploaded attachment. This is
         * confusing but follows the existing convention how these files are handled.
         */
        Path topDir = excelFilePath.getParent();
        if (!topDir.toFile().exists()) {
            try {
                Files.createDirectory(topDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!topDir.toFile().isDirectory()) {
            log.error(topDir + " pre-exists but is not a directory.");
        }

        // create the text package
        List<String> files = validator.createTextFiles();

        // run python script to validate
        String validationScript = servletContext.getRealPath("submissionCheck.py");
        ValidationReport report = new ValidationReport(validationScript, subjectDataLocation, topDir,
                files.toArray(new String[0]));
        return report;
    }
}
