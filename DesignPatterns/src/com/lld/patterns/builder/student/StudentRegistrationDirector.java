package com.lld.patterns.builder.student;

/**
 * Optional director: fixed recipes for engineering vs MBA registration.
 * {@code instanceof} here is the note's shortcut; a new builder type needs a new branch.
 */
public class StudentRegistrationDirector {
    private final StudentBuilder studentBuilder;

    public StudentRegistrationDirector(StudentBuilder studentBuilder) {
        this.studentBuilder = studentBuilder;
    }

    public Student createStudent() {
        if (studentBuilder instanceof EngineeringStudentBuilder) {
            return createEngineeringStudent();
        }
        if (studentBuilder instanceof MBAStudentBuilder) {
            return createMBAStudent();
        }
        return null;
    }

    private Student createEngineeringStudent() {
        return studentBuilder.setRollNumber(1)
                .setAge(22)
                .setName("John")
                .setFatherName("Paul")
                .setMotherName("Jane")
                .setBranch("Computer Science and Engineering")
                .setSubjects()
                .build();
    }

    private Student createMBAStudent() {
        return studentBuilder.setRollNumber(2)
                .setAge(24)
                .setName("Sarah")
                .setFatherName("Gabriel")
                .setMotherName("Taylor")
                .setBranch("Business Administration")
                .setSubjects()
                .setMobileNo("9876543210")
                .setEmailId("sarahgabriel@iitb.com")
                .build();
    }
}
