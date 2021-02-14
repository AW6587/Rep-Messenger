package com.example.repmessenger;

public class TemplateItems {
    String templateType, templateName, message;

    public TemplateItems(String templateType, String templateName, String message) {
        this.templateType = templateType;
        this.templateName = templateName;
        this.message = message;
    }

    public String getTemplateType() {return templateType;}
    public void setTemplateType(String templateType){this.templateType = templateType;}
    public String getTemplateName() {return templateName;}
    public void setTemplateName(String templateName) {this.templateName = templateName;}
    public String getMessage() {return message; }
    public void setMessage(String message) {this.message = message;}
}
