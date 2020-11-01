package rmi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/** RMI skeleton

    <p>
    A skeleton encapsulates a multithreaded TCP server. The server's clients are
    intended to be RMI stubs created using the <code>Stub</code> class.

    <p>
    The skeleton class is parametrized by a type variable. This type variable
    should be instantiated with an interface. The skeleton will accept from the
    stub requests for calls to the methods of this interface. It will then
    forward those requests to an object. The object is specified when the
    skeleton is constructed, and must implement the remote interface. Each
    method in the interface should be marked as throwing
    <code>RMIException</code>, in addition to any other exceptions that the user
    desires.

    <p>
    Exceptions may occur at the top level in the listening and service threads.
    The skeleton's response to these exceptions can be customized by deriving
    a class from <code>Skeleton</code> and overriding <code>listen_error</code>
    or <code>service_error</code>.
*/
public class Skeleton<T>
{
    Class<T> c;
    T server;
    InetSocketAddress address;
    SkeletonListeningThread<T> thread;
    boolean started = false;

    /** Creates a <code>Skeleton</code> with no initial server address. The
        address will be determined by the system when <code>start</code> is
        called. Equivalent to using <code>Skeleton(null)</code>.

        <p>
        This constructor is for skeletons that will not be used for
        bootstrapping RMI - those that therefore do not require a well-known
        port.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server)
    {
        /* Ensure that null arguments throw NullPointerException */
        if (c == null || server == null)
            throw new NullPointerException("Arguments cannot be null");

        /* Ensure class rejected, c can only be interface */
        if (!c.isInterface())
            throw new Error("Only interface is accepted");

        /* Ensure non-Remote Interface rejected */
        boolean flag = true;
        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods) {
            if (!Arrays.asList(method.getExceptionTypes()).contains(RMIException.class)) {
                flag = false;
                break;
            }
        }
        if (!flag) throw new Error("Non-Remote Interface");

        this.c = c;
        this.server = server;

    }

    /** Creates a <code>Skeleton</code> with the given initial server address.

        <p>
        This constructor should be used when the port number is significant.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @param address The address at which the skeleton is to run. If
                       <code>null</code>, the address will be chosen by the
                       system when <code>start</code> is called.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
        this(c, server);
        this.address = address;
    }

    /** Called when the listening thread exits.

        <p>
        The listening thread may exit due to a top-level exception, or due to a
        call to <code>stop</code>.

        <p>
        When this method is called, the calling thread owns the lock on the
        <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
        calling <code>start</code> or <code>stop</code> from different threads
        during this call.

        <p>
        The default implementation does nothing.

        @param cause The exception that stopped the skeleton, or
                     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
    }

    /** Called when an exception occurs at the top level in the listening
        thread.

        <p>
        The intent of this method is to allow the user to report exceptions in
        the listening thread to another thread, by a mechanism of the user's
        choosing. The user may also ignore the exceptions. The default
        implementation simply stops the server. The user should not use this
        method to stop the skeleton. The exception will again be provided as the
        argument to <code>stopped</code>, which will be called later.

        @param exception The exception that occurred.
        @return <code>true</code> if the server is to resume accepting
                connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

        <p>
        The default implementation does nothing.

        @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
    }

    /** Starts the skeleton server.

        <p>
        A thread is created to listen for connection requests, and the method
        returns immediately. Additional threads are created when connections are
        accepted. The network address used for the server is determined by which
        constructor was used to create the <code>Skeleton</code> object.

        @throws RMIException When the listening socket cannot be created or
                             bound, when the listening thread cannot be created,
                             or when the server has already been started and has
                             not since stopped.
     */
    public synchronized void start() throws RMIException
    {
        ServerSocket serverSocket;
        if (thread != null && thread.isAlive() || started)
            throw new RMIException("Listening thread already running.");

        try {
            if (address == null) {
                // Computer assign randomly an available port
                serverSocket = new ServerSocket(0);
                address = new InetSocketAddress(serverSocket.getLocalPort());
            }
            else {
                serverSocket = new ServerSocket(address.getPort());
            }
            System.out.println("Server created successfully, port = " + address.getPort());


            // create a listening thread
            thread = new SkeletonListeningThread<T>(server, serverSocket);
            thread.start();
            started = true;

        } catch (IOException e) {
            throw new RMIException("I/O exception, unable to create listening socket", e);
        }


    }

    /** Stops the skeleton server, if it is already running.

        <p>
        The listening thread terminates. Threads created to service connections
        may continue running until their invocations of the <code>service</code>
        method return. The server stops at some later time; the method
        <code>stopped</code> is called at that point. The server may then be
        restarted.
     */
    public synchronized void stop() {
        if (thread.isAlive()) {
            thread.stopListening();
        }
        started = false;
        System.out.println("Server stopped, port = " + address.getPort());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skeleton<?> skeleton = (Skeleton<?>) o;
        return Objects.equals(c, skeleton.c) &&
                Objects.equals(server, skeleton.server) &&
                Objects.equals(address, skeleton.address) &&
                Objects.equals(thread, skeleton.thread);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, server, address, thread);
    }
}
