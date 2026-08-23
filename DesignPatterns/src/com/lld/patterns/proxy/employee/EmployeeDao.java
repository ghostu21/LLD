package com.lld.patterns.proxy.employee;

/**
 * Subject: shared API so the client can talk to either the real DAO or the proxy.
 */
public interface EmployeeDao {
    void getEmployeeInfo(int empID);

    void createEmployee(EmployeeDo obj);
}
