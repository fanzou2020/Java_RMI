package rmi;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class ExecutionThread<T> extends Thread {
    private Socket client;
    private T server;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public ExecutionThread(Socket client, T server) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process() throws Exception {
        // Read method name and arguments from InputStream
        ois = new ObjectInputStream(client.getInputStream());
        String methodName = (String) ois.readObject();
        Class<?>[] argsTypes = (Class<?>[]) ois.readObject();
        Object[] args = (Object[]) ois.readObject();


        // Execute method
        Method method = server.getClass().getMethod(methodName, argsTypes);
        Object result = method.invoke(server, args);

        // Write result to OutputStream
        oos = new ObjectOutputStream(client.getOutputStream());
        oos.writeObject(result);
        oos.flush();
    }
}
