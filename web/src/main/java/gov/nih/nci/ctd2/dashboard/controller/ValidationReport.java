package gov.nih.nci.ctd2.dashboard.controller;

class ValidationReport {
    final private String title;
    final private int count;
    final private ValidationError[] errors;
    final private String[] files;
    final private String otherError;

    ValidationReport(String title, ValidationError[] errors, String[] files, String otherError) {
        this.title = title;
        this.count = errors.length;
        this.errors = errors;
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
