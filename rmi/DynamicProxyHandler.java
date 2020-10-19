package rmi;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;


public class DynamicProxyHandler<T> implements InvocationHandler {
    private InetSocketAddress address;

    public DynamicProxyHandler(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;

//        /*************************** Local method **************************/
//        // TODO: equals method
//        if (method.getName().equals("equals")) {
//            System.out.println("invoke local equals() method");
//        }
//        // TODO: hashCode()
//        if (method.getName().equals("hashCode")) {
//            System.out.println("invoke local hashCode() method");
//        }
        if (method.getName().equals("toString")) {
            System.out.println("invoke local method toString method");
        }

        /************************* Remote method **************************/
        // TODO: Connect to the server specified by address
        else {
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
            result = fromServer.readObject();
        }


        return result;
    }
}
