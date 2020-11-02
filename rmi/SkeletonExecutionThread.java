package rmi;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class SkeletonExecutionThread<T> extends Thread {
    private Socket client;
    private T server;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public SkeletonExecutionThread(Socket client, T server) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            process();
        }
        catch (IOException e) { }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void process() throws Exception {
        // Read method name and arguments from InputStream
        ois = new ObjectInputStream(client.getInputStream());
        String methodName = (String) ois.readObject();
        Class<?>[] argsTypes = (Class<?>[]) ois.readObject();
        Object[] args = (Object[]) ois.readObject();


        // Execute method
        Boolean hasException = false;
        Throwable exception = null;
        Object result = null;
        Method method = server.getClass().getMethod(methodName, argsTypes);
        try {
            result = method.invoke(server, args);
        } catch (InvocationTargetException e) {
            hasException = true;
            exception = e.getCause();
        }

        // Write result to OutputStream
        oos = new ObjectOutputStream(client.getOutputStream());
        oos.writeObject(hasException);
        if (hasException) {
            oos.writeObject(exception);
        } else {
            oos.writeObject(result);
        }
        oos.flush();
    }
}
