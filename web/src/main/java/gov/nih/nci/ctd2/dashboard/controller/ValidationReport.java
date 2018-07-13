package gov.nih.nci.ctd2.dashboard.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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

    final private Path topDir;

    private static final Log log = LogFactory.getLog(ValidationReport.class);

    public ValidationReport(String message) {
        this.title = "Generic Validation Report";
        this.count = 0;
        this.errors = new ValidationError[0];
        this.files = new String[0];
        this.otherError = message;

        this.topDir = null;
    }

    public ValidationReport(String validationScript, String subjectDataLocation, Path topDir, String[] files) {

        ProcessBuilder pb = new ProcessBuilder("python", validationScript, topDir.toString(), subjectDataLocation);
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
        }

        this.title = "Validation Report";
        this.count = errors.size();
        this.errors = errors.toArray(new ValidationError[0]);
        this.files = files;
        this.otherError = otherError;

        this.topDir = topDir;
    }

    public void export() {
        try {
            Path reportFile = topDir.resolve("validation-report.txt");
            Files.deleteIfExists(reportFile);
            Files.write(reportFile, this.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(title);
        sb.append("\n\n").append(count).append(" error");
        if(count>1) sb.append('s');
        sb.append(" reported by the validation script:").append('\n');
        sb.append("\ttype\tdescription\tdetail\n------------------------------\n");
        for(int i=0; i< count; i++) {
            sb.append(i+1).append("\t").append(errors[i]).append('\n');
        }
        sb.append("\nOther script error:").append(otherError);
        return sb.toString();
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
