package naming;

import common.*;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * File node in naming server's directory tree
 */
public class PathNode {
    private Path nodePath;
    private int accessTime;
    private ServerStubs serverStubs;
    private HashSet<ServerStubs> replicaStubs;
    private HashMap<String, PathNode> childNodes;

    /**
     * Constructor of PathNode of a file, given ServerStubs of the file, and it's Path
     * @param nodePath Path of a file
     * @param serverStubs The file's corresponding ServerStubs
     */
    public PathNode(Path nodePath, ServerStubs serverStubs) {
        this.nodePath = nodePath;
        this.accessTime = 0;
        this.serverStubs = serverStubs;
        this.replicaStubs = new HashSet<>();
        this.childNodes = new HashMap<>();
    }


    /**
     * Constructor of PathNode of a directory, given the path of this directory
     * @param nodePath Path of a directory
     */
    public PathNode(Path nodePath) {
        this(nodePath, null);
    }

    /**
     * Is this node represent a file?
     * @return true if this node represents a File.
     */
    public boolean isFile() { return serverStubs != null; }

    /**
     * Getter of Path of this node
     * @return Path of this node
     */
    public Path getPath() { return nodePath; }

    /**
     * Getter of storage server stubs
     * @return ServerStubs
     */
    public ServerStubs getStubs() { return serverStubs; }

    /**
     * Setter of storage server stubs
     * @param serverStubs storage server stubs
     */
    public void setStubs(ServerStubs serverStubs) { this.serverStubs = serverStubs; }

    /**
     * Getters of children nodes
     * @return a Map between children node component String, and its PathNode
     */
    public HashMap<String, PathNode> getChildren() { return childNodes; }

    /**
     * Add a child node
     * @param component name of the child file
     * @param child the PathNode of the child
     * @throws UnsupportedOperationException when this child node already contained in childNodes HashMap
     */
    public void addChild(String component, PathNode child) throws UnsupportedOperationException {
        if (childNodes.containsKey(component))
            throw new UnsupportedOperationException("Unable to add an existing node again");

        childNodes.put(component, child);
    }

    /**
     * Delete child node
     * @param component name of the child file
     * @throws UnsupportedOperationException when this child node not contained in childNodes HashMap
     */
    public void deleteChild(String component) throws UnsupportedOperationException {
        if (!childNodes.containsKey(component))
            throw new UnsupportedOperationException("Unable to delete a non-existing node");

        childNodes.remove(component);
    }

    /**
     * Get PathNode from the given path, based on current PathNode
     * @param path relative path from this PathNode
     * @return PathNode of the given path
     * @throws FileNotFoundException File does not exist for this path.
     */
    public PathNode getNodeByPath(Path path) throws FileNotFoundException {
        PathNode curNode = this;

        for (String component : path) {
            if (!curNode.childNodes.containsKey(component))
                throw new FileNotFoundException("Unable to get node from path");

            curNode = curNode.childNodes.get(component);
        }
        return curNode;
    }

    /**
     * Get all the descendants nodes which refers to a file
     * @return the ArrayList of descendants that refers to a file.
     */
    public ArrayList<PathNode> getDescendants() {
        ArrayList<PathNode> descendants = new ArrayList<>();

        for (PathNode node : childNodes.values()) {
            if (node.isFile())
                descendants.add(node);
            else
                descendants.addAll(node.getDescendants());
        }

        return descendants;
    }


    /**
     * Increase the node's access time
     * @param multiple pre-set value
     * @return true if the access time is beyond the pre-set value multiple and then reset the access time to 0.
     */
    public boolean incAccessTime(int multiple) {
        if (++accessTime > multiple) {
            accessTime = 0;
            return true;
        }
        return false;
    }


    /**
     * Reset the accessTime to 0
     */
    public void resetAccessTime() {
        accessTime = 0;
    }

    public HashSet<ServerStubs> getReplicaStubs() {
        return replicaStubs;
    }

    public void addReplicaStub(ServerStubs serverStubs) {
        // Naming server will ensure the nodes calling
        // this method refers to a file, not a directory
        replicaStubs.add(serverStubs);
    }

    public int getReplicaSize() {
        return replicaStubs.size();
    }

    public void removeReplicaStub(ServerStubs serverStubs) {
        replicaStubs.remove(serverStubs);
    }










}
