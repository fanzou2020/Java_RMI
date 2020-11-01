package common;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Distributed filesystem paths.
 *
 * <p>
 * Objects of type <code>Path</code> are used by all filesystem interfaces.
 * Path objects are immutable.
 *
 * <p>
 * The string representation of paths is a forward-slash-delimeted sequence of
 * path components. The root directory is represented as a single forward
 * slash.
 *
 * <p>
 * The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
 * not permitted within path components. The forward slash is the delimeter,
 * and the colon is reserved as a delimeter for application use.
 */
public class Path implements Iterable<String>, Serializable {
    /**
     * A List of String represents the components of a Path
     * It is immutable, declared as final.
     */
    private final List<String> components;

    /**
     * Creates a new path which represents the root directory.
     */
    public Path() {
        this.components = new ArrayList<>();
    }

    /**
     * Creates a new path by appending the given component to an existing path.
     *
     * @param path      The existing path.
     * @param component The new component.
     * @throws IllegalArgumentException If <code>component</code> includes the
     *                                  separator, a colon, or
     *                                  <code>component</code> is the empty
     *                                  string.
     */
    public Path(Path path, String component) {
        if (component.contains("/") || component.contains(":") || component.isEmpty()) {
            throw new IllegalArgumentException("Components includes the separator, a colon, or is empty!");
        }
        this.components = new ArrayList<>(path.components);
        this.components.add(component);
    }

    /**
     * Creates a new path from a path string.
     *
     * <p>
     * The string is a sequence of components delimited with forward slashes.
     * Empty components are dropped. The string must begin with a forward
     * slash.
     *
     * @param path The path string.
     * @throws IllegalArgumentException If the path string does not begin with
     *                                  a forward slash, or if the path
     *                                  contains a colon character.
     */
    public Path(String path) {
        if (path.isEmpty() || path.contains(":") || path.charAt(0) != '/') {
            throw new IllegalArgumentException("Path string is empty, or Path string does not begin with a forward slash," +
                    " or path contains a colon character");
        }
        this.components = Arrays.stream(path.split("/"))
                .filter(d->!d.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns an iterator over the components of the path.
     *
     * <p>
     * The iterator cannot be used to modify the path object - the
     * <code>remove</code> method is not supported.
     *
     * @return The iterator.
     */
    @Override
    public Iterator<String> iterator() {
        return new PathIterator(components.iterator());
    }

    /** Private PathIterator which does not support remove method. */
    private static class PathIterator implements Iterator<String> {
        private final Iterator<String> iterator;

        public PathIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() method in iterator not supported");
        }
    }

    /**
     * Lists the paths of all files in a directory tree on the local
     * filesystem.
     *
     * @param directory The root directory of the directory tree.
     * @return An array of relative paths, one for each file in the directory
     * tree.
     * @throws FileNotFoundException    If the root directory does not exist.
     * @throws IllegalArgumentException If <code>directory</code> exists but
     *                                  does not refer to a directory.
     */
    public static Path[] list(File directory) throws FileNotFoundException {
        if (!directory.exists())
            throw new FileNotFoundException("Root directory does not exist");
        if (!directory.isDirectory())
            throw new IllegalArgumentException("Directory exists but does not refer to a directory");

        String rootString = directory.getAbsolutePath();

        List<Path> result = new ArrayList<>();
        listFiles(directory, result, rootString);

        return result.toArray(new Path[result.size()]);
    }

    private static void listFiles(File directory, List<Path> result, String rootString) {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                listFiles(file, result, rootString);
            } else {
                result.add(new Path(file.getAbsolutePath().substring(rootString.length())));
            }
        }
    }


    /**
     * Determines whether the path represents the root directory.
     *
     * @return <code>true</code> if the path does represent the root directory,
     * and <code>false</code> if it does not.
     */
    public boolean isRoot() {
        return components.isEmpty();
    }

    /**
     * Returns the path to the parent of this path.
     *
     * @throws IllegalArgumentException If the path represents the root
     *                                  directory, and therefore has no parent.
     */
    public Path parent() {
        if (this.isRoot()) throw new IllegalArgumentException("Current Path is the root path");

        List<String> parentComponents = new ArrayList<>(this.components);
        parentComponents.remove(this.components.size()-1);  // remove the last component
        StringBuilder parentString = new StringBuilder();
        for (String c : parentComponents) {
            parentString.append("/");
            parentString.append(c);
        }
        return new Path(parentString.toString());
    }

    /**
     * Returns the last component in the path.
     *
     * @throws IllegalArgumentException If the path represents the root
     *                                  directory, and therefore has no last
     *                                  component.
     */
    public String last() {
        if (this.isRoot()) throw new IllegalArgumentException("Current Path is the root path");
        return components.get(components.size()-1);
    }

    /**
     * Determines if the given path is a subpath of this path.
     *
     * <p>
     * The other path is a subpath of this path if is a prefix of this path.
     * Note that by this definition, each path is a subpath of itself.
     *
     * @param other The path to be tested.
     * @return <code>true</code> If and only if the other path is a subpath of
     * this path.
     */
    public boolean isSubpath(Path other) {
        String otherPath = other.toString();
        return this.toString().startsWith(otherPath);
    }

    /**
     * Converts the path to <code>File</code> object.
     *
     * @param root The resulting <code>File</code> object is created relative
     *             to this directory.
     * @return The <code>File</code> object.
     */
    public File toFile(File root) {
        return new File(this.toString());
    }

    /**
     * Compares two paths for equality.
     *
     * <p>
     * Two paths are equal if they share all the same components.
     *
     * @param other The other path.
     * @return <code>true</code> if and only if the two paths are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Path strings = (Path) other;
        return components.equals(strings.components);
    }

    /**
     * Returns the hash code of the path.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(components);
    }

    /**
     * Converts the path to a string.
     *
     * <p>
     * The string may later be used as an argument to the
     * <code>Path(String)</code> constructor.
     *
     * @return The string representation of the path.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isRoot()) return "/";

        for (String s : components) {
            sb.append("/");
            sb.append(s);
        }
        return sb.toString();
    }
}
