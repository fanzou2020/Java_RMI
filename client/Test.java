package client;

import rmi.Skeleton;
import rmi.Stub;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws UnknownHostException, RemoteException {
        /*
        RemotePrintInterface imp = new Print();

        Skeleton<RemotePrintInterface> skeleton = new Skeleton<>(RemotePrintInterface.class, imp);

        RemotePrintInterface stub = Stub.create(RemotePrintInterface.class, skeleton);

        stub.print("Hello world!");

        System.out.println(stub);
         */

        Class<Print> c = Print.class;
        Remote remote = new Print();
        System.out.println(Arrays.asList(c.getInterfaces()).contains(Remote.class));
//        if (c instanceof Remote) {
//
//        }
//        Class<?>[] ifs = c.getInterfaces();
//        for (Class<?> item: ifs) {
//            if (item == RemotePrintInterface.class) {
//                System.out.println(true);
//            }
//        }

    }

}
