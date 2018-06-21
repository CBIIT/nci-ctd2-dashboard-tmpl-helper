package gov.nih.nci.ctd2.dashboard.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class ValidationReport {
    final private String title;
    final private int count;
    final private ValidationError[] errors;
    final private String[] files;
    final private String otherError;

    private static final Log log = LogFactory.getLog(ValidationReport.class);

    public ValidationReport(String message) {
        this.title = "Generic Validation Report";
        this.count = 0;
        this.errors = new ValidationError[0];
        this.files = new String[0];
        this.otherError = message;
    }

    public ValidationReport(String validationScript, String subjectDataLocation, Path topDir, String[] files) {
        // copy the background data
        /*
         * this is not a very reasonable solution, considering that the background data
         * size is pretty large, but is necessary if we are strictly in not modifying
         * the current validation script.
         */
        Path sourcePath = Paths.get(subjectDataLocation + File.separator + "subject_data");
        Path targetPath = topDir.resolve("subject_data");
        try {
            log.debug("start copying subject data");
            Files.walkFileTree(sourcePath, new CopyFileVisitor(targetPath));
            log.debug("finished copying subject data");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProcessBuilder pb = new ProcessBuilder("python", validationScript, topDir.toString());
        List<ValidationError> errors = new ArrayList<ValidationError>();
        String otherError = "";
        try {
            Process p = pb.start();

            int ret = p.waitFor();
            log.info("exit of Python script: " + ret);

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            log.info("output from Python script: ");
            String output = in.readLine();
            while (output != null) {
                log.info(output);
                output = in.readLine();
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuffer otherMessage = new StringBuffer();
            String error = errorReader.readLine();
            while (error != null) {
                if (error.startsWith("ERROR:")) {
                    int index = error.indexOf("[");
                    if(index<0) {
                        index = error.length();
                    }
                    String description = error.substring("ERROR:".length(), index).trim().replaceAll(":$", "");
                    String errorDetail = error.substring(index);
                    errors.add(new ValidationError("ERROR", description, errorDetail));
                } else if (error.startsWith("WARNING:")) {
                    String description = error.substring("WARNING:".length()).trim().replaceAll(":$", "");
                    errors.add(new ValidationError("WARNING", description, ""));
                } else {
                    otherMessage.append(error).append('\n');
                }
                error = errorReader.readLine();
            }
            otherError = escapeHtml(otherMessage.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) { // TODO this should be removed in the final code
            e.printStackTrace();
        }

        this.title = "Validation Report";
        this.count = errors.size();
        this.errors = errors.toArray(new ValidationError[0]);
        this.files = files;
        this.otherError = otherError;
    }

    public int getCount() {
        return count;
    }

    public String getTitle() {
        return title;
    }

    public ValidationError[] getErrors() {
        return errors;
    }

    public String[] getFiles() {
        return files;
    }

    public String getOtherError() {
        return otherError;
    }
}
