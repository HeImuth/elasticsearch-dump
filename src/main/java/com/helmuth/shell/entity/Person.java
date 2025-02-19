package com.helmuth.shell.entity;

import com.helmuth.shell.model.Document;
import com.helmuth.shell.model.IndexDocument;

@IndexDocument(indexName = "person")
public class Person extends Document {
    private String name;
    private int age;
    private Long birthdate;

    public Person() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Long birthdate) {
        this.birthdate = birthdate;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", birthdate=" + birthdate +
                '}';
    }
}
