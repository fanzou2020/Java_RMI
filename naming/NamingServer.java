package naming;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

import rmi.*;
import common.*;
import storage.*;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{
    private Skeleton<Service> serviceSkeleton;
    private Skeleton<Registration> registrationSkeleton;
    private PathNode root;   // Root PathNode of of directory tree

    private Set<ServerStubs> registeredStubs;

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
        // Create skeletons for Service Interface and Registration Interface
        serviceSkeleton = new Skeleton<>(Service.class,
                this,
                new InetSocketAddress(NamingStubs.SERVICE_PORT)
        );
        registrationSkeleton = new Skeleton<>(Registration.class,
                this,
                new InetSocketAddress(NamingStubs.REGISTRATION_PORT)
        );

        registeredStubs = new HashSet<>();

        // Create root PathNode
        root = new PathNode(new Path());

    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
        registrationSkeleton.start();
        serviceSkeleton.start();
    }

    /** Stops the naming server.

        <p>
        This method waits for both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
        registrationSkeleton.stop();
        serviceSkeleton.stop();
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following methods are documented in Service.java.
    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
        if (client_stub == null || command_stub == null || files == null)
            throw new NullPointerException("Arguments cannot be null");

        ServerStubs serverStubs = new ServerStubs(client_stub, command_stub);
        if (registeredStubs.contains(serverStubs))
            throw new IllegalStateException("Storage server already registered");

        registeredStubs.add(serverStubs);

        List<Path> filesToDeleted = new ArrayList<>();

        // For each Path of a file, add it to directory tree
        for (Path filePath : files) {
            if (filePath.isRoot()) continue;

            PathNode curNode = root;

            for (String component : filePath) {
                HashMap<String, PathNode> children = curNode.getChildren();
                if (children.containsKey(component)) {
                    // go the child
                    curNode = children.get(component);
                }
                else {
                    // add a new directory
                    curNode.addChild(component, new PathNode(new Path(curNode.getPath(), component)));
                    curNode = curNode.getChildren().get(component);
                }
            }

            // Reach the end of path
            if (curNode.getStubs() == null && curNode.getDescendants().size() == 0) {
                // It has no descendant files, and no corresponding stubs
                curNode.setStubs(serverStubs);

            } else {
                // File already exist in naming server, or shadows a directory
                filesToDeleted.add(curNode.getPath());
            }
        }

        return filesToDeleted.toArray(new Path[filesToDeleted.size()]);

    }
}
