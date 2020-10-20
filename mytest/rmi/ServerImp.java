package mytest.rmi;

import rmi.RMIException;

import java.util.ArrayList;

public class ServerImp implements MyTestInterface {
    @Override
    public void print() throws RMIException {
        System.out.println("From ServerImp");
    }

    @Override
    public int add(int a, int b) throws RMIException {
        return a + b;
    }

    @Override
    public void modify(ArrayList<Integer> integers) throws RMIException {
        integers.set(0, 100);
        System.out.println("First element in arraylist in server is " + integers.get(0));
    }


}
