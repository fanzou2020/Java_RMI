package naming;
import storage.*;


/**
 * File node in naming server'a directory tree
 * Contains Command Stub and Storage Stub
 */
public class ServerStubs {
    public Storage storageStub;
    public Command commandStub;

    public ServerStubs(Storage storageStub, Command commandStub)
    {
        this.storageStub = storageStub;
        this.commandStub = commandStub;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerStubs that = (ServerStubs) o;

        return storageStub.equals(that.storageStub) && commandStub.equals(that.commandStub);
    }

    @Override
    public int hashCode() {
        int result = storageStub.hashCode();
        result = 31 * result + commandStub.hashCode();

        return result;
    }
}
