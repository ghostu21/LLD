package com.lld.patterns.builder.student;

import java.util.ArrayList;
import java.util.List;

/**
 * Product: built only from {@link StudentBuilder} (package-private constructor).
 */
public class Student {
    int rollNumber;
    int age;
    String name;
    String branch;
    String fatherName;
    String motherName;
    List<String> subjects;
    String mobileNo;
    String emailId;

    Student(StudentBuilder builder) {
        this.rollNumber = builder.rollNumber;
        this.age = builder.age;
        this.name = builder.name;
        this.branch = builder.branch;
        this.fatherName = builder.fatherName;
        this.motherName = builder.motherName;
        this.subjects = builder.subjects == null ? null : new ArrayList<>(builder.subjects);
        this.mobileNo = builder.mobileNo;
        this.emailId = builder.emailId;
    }

    @Override
    public String toString() {
        return " roll number: " + this.rollNumber
                + " age: " + this.age
                + " name: " + this.name
                + " branch: " + this.branch
                + " father name: " + this.fatherName
                + " mother name: " + this.motherName
                + " subjects: " + this.subjects
                + " mobile no: " + this.mobileNo
                + " email id: " + this.emailId;
    }
}
