package rmi;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;


public class DynamicProxyHandler<T> implements InvocationHandler {
    private InetSocketAddress address;
    private Skeleton<T> skeleton;

    public DynamicProxyHandler(InetSocketAddress address) {
        this.address = address;
    }

    public DynamicProxyHandler(Skeleton<T> skeleton) {
        this.skeleton = skeleton;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;

//        /*************************** Local method **************************/
        if (method.getName().equals("equals")) {
            if (args[0] == null) { return false; }
            if (skeleton != null) return skeleton.hashCode() == args[0].hashCode();
            return address.hashCode() == args[0].hashCode();
        }

        if (method.getName().equals("hashCode")) {
            if (skeleton != null) return skeleton.hashCode();
            return address.hashCode();
        }

        if (method.getName().equals("toString")) {
            return "toString() method";
        }

        /************************* Remote method **************************/
        /* if skeleton is given, call the method of skeleton,
           if not given, connect to remote server */
        else {
            if (skeleton != null) {
                try {
                    return method.invoke(skeleton.server, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }

            Socket client = new Socket(address.getAddress(), address.getPort());

            // serialize the method name, method argTypes, args and write them to ouputStream
            ObjectOutputStream toServer = new ObjectOutputStream(client.getOutputStream());
            Class<?>[] argsTypes = method.getParameterTypes();
            toServer.writeObject(method.getName());
            toServer.writeObject(argsTypes);
            toServer.writeObject(args);
            toServer.flush();

            // Get result object from server
            ObjectInputStream fromServer = new ObjectInputStream(client.getInputStream());
            Boolean hasException = (Boolean) fromServer.readObject();
            if (hasException) {
                Throwable e = (Throwable) fromServer.readObject();
                throw e;
            } else {
                result = fromServer.readObject();
            }
        }

        return result;
    }
}
