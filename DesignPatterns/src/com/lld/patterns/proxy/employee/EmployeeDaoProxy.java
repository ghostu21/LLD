package com.lld.patterns.proxy.employee;

/**
 * Protection proxy: same interface as the real DAO; checks role before forwarding.
 * ADMIN may read and create. USER may only read.
 */
public class EmployeeDaoProxy implements EmployeeDao {
    private final EmployeeDao empDaoObj;
    private final String clientRole;

    public EmployeeDaoProxy(String clientRole) {
        this.empDaoObj = new EmployeeDaoImpl();
        this.clientRole = clientRole;
    }

    @Override
    public void getEmployeeInfo(int empID) {
        if (clientRole.equals("ADMIN") || clientRole.equals("USER")) {
            empDaoObj.getEmployeeInfo(empID);
        } else {
            throw new RuntimeException("Access Denied");
        }
    }

    @Override
    public void createEmployee(EmployeeDo obj) {
        if (clientRole.equals("ADMIN")) {
            empDaoObj.createEmployee(obj);
        } else {
            throw new RuntimeException("Access Denied");
        }
    }
}
