package com.helmuth.shell.model;

public class SampleLocation {
    private String street;
    private String city;
    private String state;
    private String country;
    private String zip;
    private SampleCoordinates coordinates;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public SampleCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(SampleCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "SampleLocation{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", zip='" + zip + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }
}
