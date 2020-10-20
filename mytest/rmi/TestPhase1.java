package mytest.rmi;

import rmi.*;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class TestPhase1 {

    private InetSocketAddress address;

    private Skeleton<MyTestInterface> skeleton;

    public void initialize() throws RMIException {

        address = new InetSocketAddress(8000);

        MyTestInterface server = new ServerImp();

        skeleton = new Skeleton<MyTestInterface>(MyTestInterface.class, server, address);

        skeleton.start();

        Socket socket = new Socket();

        try {
            socket.connect(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void perform() throws RMIException, InterruptedException {
        MyTestInterface stub;
        // create the stub
        stub = Stub.create(MyTestInterface.class, address);
        System.out.println("stub create success");

        stub.print();

        System.out.println("1 + 2 = " + stub.add(1, 2));

        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(1);
        stub.modify(integers);
        System.out.println("First element in arraylist in client is " + integers.get(0));

    }

    public static void main(String[] args) throws Exception {
        TestPhase1 tp1 = new TestPhase1();
        tp1.initialize();
//        tp1.perform();

    }


}
