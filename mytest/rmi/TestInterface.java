package mytest.rmi;

import java.rmi.Remote;

public interface TestInterface extends Remote {
    void print();

    int add(int a, int b);
}
