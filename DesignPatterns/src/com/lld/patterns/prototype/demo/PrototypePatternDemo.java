package com.lld.patterns.prototype.demo;

import com.lld.patterns.prototype.student.Student;

public class PrototypePatternDemo {
    public static void main(String[] args) {
        System.out.println("======= Prototype Design Pattern ======");

        Student student = new Student(5, "Rita", "CSE", 224);
        student.printDetails();

        Student studentClone = (Student) student.clone();
        studentClone.setInHighSchool(true);
        studentClone.printDetails();

        System.out.println("Same object? " + (student == studentClone));
    }
}
