package storage;

import java.io.*;
import java.net.*;

import common.*;
import rmi.*;
import naming.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{
    // Two skeletons
    private Skeleton<Command> commandSkeleton;
    private Skeleton<Storage> storageSkeleton;

    // Root file and its Absolute path
    private File root;
    private String rootString;




    /** Creates a storage server, given a directory on the local filesystem.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root)
    {
        if (root == null) throw new NullPointerException("Argument is null");

        this.root = root;
        this.rootString = root.getAbsolutePath();

        commandSkeleton = new Skeleton<Command>(Command.class, this);
        storageSkeleton = new Skeleton<Storage>(Storage.class, this);

    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
        commandSkeleton.start();
        storageSkeleton.start();
        // create stubs
        Command commandStub = Stub.create(Command.class, commandSkeleton, hostname);
        Storage storageStub = Stub.create(Storage.class, storageSkeleton, hostname);

        // register this storage server to naming server, given Registration stub
        Path[] filesToDelete = naming_server.register(storageStub, commandStub, Path.list(root));

        // Remove Files in filesToDelete
        for (Path file : filesToDelete) {
            if (!delete(file)) throw new RMIException("File deletion failed");
        }

        // delete empty directories
        if (isEmptyDir(root)) return;
        deleteEmptyDirs(root);
    }

    private void deleteEmptyDirs(File cur) {
        if (cur.isDirectory()) {
            // if current directory is empty, delete it
            if (isEmptyDir(cur)) cur.delete();

            // else, recursively delete its children files
            else {
                for (File file : cur.listFiles())
                    deleteEmptyDirs(file);

                if (isEmptyDir(cur)) cur.delete();
            }
        }
    }

    private boolean isEmptyDir(File dir) {
        if (dir.isDirectory()) {
            String[] list = dir.list();
            return (list != null) && list.length == 0;
        } else {
            return false;
        }
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
        throw new UnsupportedOperationException("not implemented");
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following methods are documented in Storage.java.
    @Override
    public synchronized long size(Path file) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public synchronized byte[] read(Path file, long offset, int length)
        throws FileNotFoundException, IOException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public synchronized void write(Path file, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    // The following methods are documented in Command.java.
    @Override
    public synchronized boolean create(Path file)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public synchronized boolean delete(Path path)
    {
        File file = new File(rootString + path.toString());
        return file.delete();
    }
}
