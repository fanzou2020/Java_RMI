package mytest.rmi;

public class ServerImp implements TestInterface {
    @Override
    public void print() {
        System.out.println("From ServerImp");
    }

    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
