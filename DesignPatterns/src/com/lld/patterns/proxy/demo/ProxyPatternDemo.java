package com.lld.patterns.proxy.demo;

import com.lld.patterns.proxy.employee.EmployeeDao;
import com.lld.patterns.proxy.employee.EmployeeDaoProxy;
import com.lld.patterns.proxy.employee.EmployeeDo;

/**
 * Client from the LLD note ({@code EmployeeManagement}): talks only to {@link EmployeeDao}.
 */
public class ProxyPatternDemo {
    public static void main(String[] args) {
        System.out.println("===== Proxy Design Pattern =====");

        EmployeeDao userProxy = new EmployeeDaoProxy("USER");
        System.out.println("-- USER --");
        userProxy.getEmployeeInfo(101);
        try {
            userProxy.createEmployee(new EmployeeDo(102, "Sahhil"));
        } catch (RuntimeException e) {
            System.out.println("USER createEmployee: " + e.getMessage());
        }

        System.out.println();
        EmployeeDao adminProxy = new EmployeeDaoProxy("ADMIN");
        System.out.println("-- ADMIN --");
        adminProxy.getEmployeeInfo(101);
        adminProxy.createEmployee(new EmployeeDo(102, "Sahhil"));

        System.out.println();
        EmployeeDao guestProxy = new EmployeeDaoProxy("GUEST");
        System.out.println("-- GUEST --");
        try {
            guestProxy.getEmployeeInfo(101);
        } catch (RuntimeException e) {
            System.out.println("GUEST getEmployeeInfo: " + e.getMessage());
        }
    }
}
