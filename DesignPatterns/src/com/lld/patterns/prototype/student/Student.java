package com.lld.patterns.prototype.student;

/**
 * Concrete prototype. {@link #clone()} runs inside the class, so it can copy private {@code rollNo}.
 * {@code inHighSchool} is not part of the clone ctor in the note — customize it after cloning.
 */
public class Student implements StudentPrototype {
    int id;
    String name;
    String branch;
    boolean inHighSchool;
    private int rollNo;

    Student() {
    }

    public Student(int id, String name, String branch, int rollNo) {
        this.id = id;
        this.name = name;
        this.branch = branch;
        this.rollNo = rollNo;
    }

    public void setInHighSchool(boolean inHighSchool) {
        this.inHighSchool = inHighSchool;
    }

    @Override
    public StudentPrototype clone() {
        return new Student(id, name, branch, rollNo);
    }

    public void printDetails() {
        System.out.println("=== Student Details ===");
        System.out.print(this + ": ");
        System.out.println("Id: " + id + ", Name: " + name + ", Branch: " + branch
                + ", Roll No: " + rollNo + ", In High School: " + inHighSchool);
    }
}
