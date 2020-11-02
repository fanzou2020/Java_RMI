package naming;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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

    private List<ServerStubs> registeredStubs;

    private boolean started = false;

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

        registeredStubs = new ArrayList<>();

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
        if (started) return;

        registrationSkeleton.start();
        serviceSkeleton.start();

        started = true;
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
        if (started) {
            registrationSkeleton.stop();
            serviceSkeleton.stop();

            this.stopped(new Throwable("Stopped by calling stop()"));
        }
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
        if (path == null) throw new NullPointerException("Argument is null");

        if (path.isRoot()) return true;

        PathNode dirNode = root.getNodeByPath(path);

        return !dirNode.isFile();

    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
        if (directory == null) throw new NullPointerException("Argument is null");

        PathNode dirNode;

        if (directory.isRoot()) dirNode = root;
        else dirNode = root.getNodeByPath(directory);

        if (dirNode.isFile()) throw new FileNotFoundException("Directory not found");

        Set<String> listItems = dirNode.getChildren().keySet();
        return listItems.toArray(new String[listItems.size()]);

    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        if (file == null) throw new NullPointerException("Argument is null");

        if (file.isRoot()) return false;

        PathNode parentNode = root.getNodeByPath(file.parent());
        if (parentNode.isFile()) throw new FileNotFoundException("Parent directory does not exist");

        // Path of file already exist
        if (parentNode.getChildren().containsKey(file.last())) return false;

        // create file on naming server and Storage server
        // choose a random Storage server, create file on this sotrage server
        ServerStubs serverStubs = registeredStubs.get(new Random().nextInt(registeredStubs.size()));
        serverStubs.commandStub.create(file);

        // create file on naming serving
        parentNode.addChild(file.last(), new PathNode(file, serverStubs));

        return true;
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        if (directory == null) throw new NullPointerException("Argument is null");

        if (directory.isRoot()) return false;

        PathNode parentNode = root.getNodeByPath(directory.parent());
        if (parentNode.isFile()) throw new FileNotFoundException("Parent directory does not exist");

        // Path of directory already exist
        if (parentNode.getChildren().containsKey(directory.last())) return false;

        // create a directory node in naming server
        parentNode.addChild(directory.last(), new PathNode(directory));

        return true;
    }

    // TODO: delete in naming server
    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        if (file == null) throw new NullPointerException("Argument is null");
        if (file.isRoot()) throw new FileNotFoundException();

        PathNode fileNode = root.getNodeByPath(file);
        if (fileNode.isFile()) {
            return fileNode.getStubs().storageStub;
        } else {
            throw new FileNotFoundException("File does not exist");
        }
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

        registeredStubs.add(serverStubs);

        return filesToDeleted.toArray(new Path[filesToDeleted.size()]);

    }
}
