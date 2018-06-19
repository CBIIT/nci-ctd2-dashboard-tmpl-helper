package gov.nih.nci.ctd2.dashboard.controller;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/upload")
public class UploadController {
    private static final Log log = LogFactory.getLog(UploadController.class);

    @Autowired
    @Qualifier("uploadLocation")
    private String uploadLocation = "";

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

        try {

            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path filePath = outDir.resolve(entry.getName());
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

        return new ResponseEntity<String>(filename + " uploaded and unzipped", HttpStatus.OK);
    }
}
