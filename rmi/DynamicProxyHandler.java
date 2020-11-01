package rmi;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.reflect.Proxy.isProxyClass;


public class DynamicProxyHandler<T> implements InvocationHandler, Serializable {
    private InetSocketAddress address;
    private Class<T> c;

    public DynamicProxyHandler(Class<T> c, InetSocketAddress address) {
        if (c == null || address == null) throw new NullPointerException("Arguments are null");

        this.address = address;
        this.c = c;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicProxyHandler<?> that = (DynamicProxyHandler<?>) o;
        return address.equals(that.address) &&
                c.equals(that.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, c);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;

        /*************************** Local method **************************/
        if (method.equals(Object.class.getMethod("equals", Object.class))) {
            Object o = args[0];
            if (o == null) return false;

            if (!isProxyClass(args[0].getClass())) {
                return false;
            }

            DynamicProxyHandler dph = (DynamicProxyHandler) Proxy.getInvocationHandler(o);
            return c.equals(dph.c) && address.equals(dph.address);
        }

        if (method.equals(Object.class.getMethod("hashCode"))) {
            return (address.toString() + c.toString()).hashCode();
        }

        if (method.equals(Object.class.getMethod("toString"))) {
            return "Class: " + c + ", Address: " + address;
        }

        /************************* Remote method **************************/
        Socket client = new Socket(address.getAddress(), address.getPort());
        ObjectOutputStream toServer = null;
        ObjectInputStream fromServer = null;

        try {
            // serialize the method name, method argTypes, args and write them to ouputStream
            toServer = new ObjectOutputStream(client.getOutputStream());
            Class<?>[] argsTypes = method.getParameterTypes();
            toServer.writeObject(method.getName());
            toServer.writeObject(argsTypes);
            toServer.writeObject(args);
            toServer.flush();

            // Get result object from server
            fromServer = new ObjectInputStream(client.getInputStream());
            Boolean hasException = (Boolean) fromServer.readObject();
            if (hasException) {
                Throwable e = (Throwable) fromServer.readObject();
                throw e;
            } else {
                result = fromServer.readObject();
            }
        } finally {
            try {
                if (toServer != null) toServer.close();
                if (fromServer != null) fromServer.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
