package com.helmuth.shell.model;

public class SampleJob {
    private String title;
    private String descriptor;
    private String area;
    private String type;
    private String company;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "SampleJob{" +
                "title='" + title + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", area='" + area + '\'' +
                ", type='" + type + '\'' +
                ", company='" + company + '\'' +
                '}';
    }
}
