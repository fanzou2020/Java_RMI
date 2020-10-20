package rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SkeletonListeningThread<T> extends Thread {
    private final T server;
    private InetSocketAddress address;
    private ServerSocket ss;
    private boolean stop = false;

    public SkeletonListeningThread(T server, InetSocketAddress address) {
        this.server = server;
        this.address = address;
    }

    @Override
    public void run() {
        try  {
            ss = new ServerSocket(address.getPort());
            System.out.println("Server created successfully, port = " + address.getPort());
            // long running thread, listening for connections
            while (!stop) {
                // server waiting for connections
                Socket client = ss.accept();
                System.out.println("New connection from " + client.getRemoteSocketAddress());

                // create a new thread to handle client's request
                SkeletonExecutionThread<T> et = new SkeletonExecutionThread<T>(client, server);
                et.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopListening() {
        stop = true;
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
