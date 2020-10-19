package client;

import java.rmi.RemoteException;

public class Print implements RemotePrintInterface  {
    @Override
    public void print(String what) throws RemoteException {
        System.out.println("From Print class");
        System.out.println(what);
    }
}
