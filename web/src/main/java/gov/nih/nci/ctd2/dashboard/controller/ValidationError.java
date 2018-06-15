package gov.nih.nci.ctd2.dashboard.controller;

class ValidationError {
    final private String description;
    final private String detail;

    ValidationError(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }

}