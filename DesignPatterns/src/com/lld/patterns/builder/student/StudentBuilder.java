package com.lld.patterns.builder.student;

import java.util.List;

/**
 * Abstract builder: fluent setters return {@code this}; {@link #setSubjects()} is type-specific.
 */
public abstract class StudentBuilder {
    int rollNumber;
    int age;
    String name;
    String branch;
    String fatherName;
    String motherName;
    List<String> subjects;
    String mobileNo;
    String emailId;

    public StudentBuilder setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
        return this;
    }

    public StudentBuilder setAge(int age) {
        this.age = age;
        return this;
    }

    public StudentBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public StudentBuilder setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public StudentBuilder setFatherName(String fatherName) {
        this.fatherName = fatherName;
        return this;
    }

    public StudentBuilder setMotherName(String motherName) {
        this.motherName = motherName;
        return this;
    }

    public StudentBuilder setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
        return this;
    }

    public StudentBuilder setEmailId(String emailId) {
        this.emailId = emailId;
        return this;
    }

    public abstract StudentBuilder setSubjects();

    public Student build() {
        return new Student(this);
    }
}
