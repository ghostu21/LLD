package com.lld.patterns.builder.demo;

import com.lld.patterns.builder.student.EngineeringStudentBuilder;
import com.lld.patterns.builder.student.MBAStudentBuilder;
import com.lld.patterns.builder.student.Student;
import com.lld.patterns.builder.student.StudentRegistrationDirector;

public class BuilderPatternDemo {
    public static void main(String[] args) {
        System.out.println("===== Builder Pattern =====");

        StudentRegistrationDirector enggStudentDirector =
                new StudentRegistrationDirector(new EngineeringStudentBuilder());
        StudentRegistrationDirector mbaStudentDirector =
                new StudentRegistrationDirector(new MBAStudentBuilder());

        Student engineerStudent = enggStudentDirector.createStudent();
        Student mbaStudent = mbaStudentDirector.createStudent();

        System.out.println("===> Student details:" + engineerStudent.toString());
        System.out.println("===> Student details:" + mbaStudent.toString());
    }
}
