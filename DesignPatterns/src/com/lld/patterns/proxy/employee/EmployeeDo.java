package com.lld.patterns.proxy.employee;

/** Data object for an employee (name used in the note: EmployeeDo). */
public class EmployeeDo {
    private final int id;
    private final String name;

    public EmployeeDo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "EmployeeDo{id=" + id + ", name='" + name + "'}";
    }
}
