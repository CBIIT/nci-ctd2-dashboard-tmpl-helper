package gov.nih.nci.ctd2.dashboard.controller;

class ValidationError {
    final private String type;
    final private String description;
    final private String detail;

    ValidationError(String type, String description, String detail) {
        this.type = type;
        this.description = description;
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }
}
