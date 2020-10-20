package mytest.rmi;

import rmi.RMIException;

import java.rmi.Remote;
import java.util.ArrayList;

public interface MyTestInterface {
    void print() throws RMIException;

    int add(int a, int b) throws RMIException;

    void modify(ArrayList<Integer> integers) throws RMIException;
}
