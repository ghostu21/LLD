package com.lld.patterns.proxy.employee;

/**
 * Real subject: actual employee operations (DB load / persist in a real system).
 */
public class EmployeeDaoImpl implements EmployeeDao {
    @Override
    public void getEmployeeInfo(int empID) {
        System.out.println("Fetching employee info for ID: " + empID);
    }

    @Override
    public void createEmployee(EmployeeDo obj) {
        System.out.println("Creating employee: " + obj);
    }
}
