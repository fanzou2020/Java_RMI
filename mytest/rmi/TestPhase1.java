package mytest.rmi;

import rmi.*;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class TestPhase1 {

    private InetSocketAddress address;

    private Skeleton<TestInterface> skeleton;

    public void initialize() throws RMIException {

        address = new InetSocketAddress(8000);

        TestInterface server = new ServerImp();

        skeleton = new Skeleton<TestInterface>(TestInterface.class, server, address);

        skeleton.start();
    }

    protected void perform() throws RMIException, InterruptedException {
        TestInterface stub;
        // create the stub
        stub = Stub.create(TestInterface.class, address);
        System.out.println("stub create success");
        stub.print();
        System.out.println("1 + 2 = " + stub.add(1, 2));
    }

    public static void main(String[] args) throws Exception {
        TestPhase1 tp1 = new TestPhase1();
        tp1.initialize();
        tp1.perform();
    }


}
