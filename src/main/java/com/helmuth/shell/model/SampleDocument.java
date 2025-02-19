package com.helmuth.shell.model;

import java.util.List;

@IndexDocument(indexName = "sample")
public class SampleDocument extends Document {
    private String message;
    private String phoneNumber;
    private  String phoneVariation;
    private String status;
    private SampleName name;
    private String username;
    private String password;
    private List<String> emails;
    private SampleLocation location;
    private String website;
    private String domain;
    private SampleJob job;
    private SampleCreditCard creditCard;
    private String uuid;
    private String objectId;

    public SampleDocument() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneVariation() {
        return phoneVariation;
    }

    public void setPhoneVariation(String phoneVariation) {
        this.phoneVariation = phoneVariation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SampleName getName() {
        return name;
    }

    public void setName(SampleName name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public SampleLocation getLocation() {
        return location;
    }

    public void setLocation(SampleLocation location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public SampleJob getJob() {
        return job;
    }

    public void setJob(SampleJob job) {
        this.job = job;
    }

    public SampleCreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(SampleCreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "message='" + message + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", phoneVariation='" + phoneVariation + '\'' +
                ", status='" + status + '\'' +
                ", name=" + name +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", emails=" + emails +
                ", location=" + location +
                ", website='" + website + '\'' +
                ", domain='" + domain + '\'' +
                ", job=" + job +
                ", creditCard=" + creditCard +
                ", uuid='" + uuid + '\'' +
                ", objectId='" + objectId + '\'' +
                '}';
    }
}
