package io.remotecontrol.groovy.client;

import groovy.lang.Closure;
import io.remotecontrol.RemoteControlException;
import io.remotecontrol.util.IoUtil;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InnerClosureClassDefinitionsFinder {

    private final ClassLoader classLoader;

    public InnerClosureClassDefinitionsFinder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @SuppressWarnings("NestedBlockDepth")
    public List<byte[]> find(Class<? extends Closure> clazz) throws IOException {
        List<byte[]> classes    = new ArrayList<byte[]>();
        String innerClassPrefix = toInnerClassPrefix(clazz);
        String packageDirPath   = toPackageDirPath(clazz);

        // Find, via the class loader, the names of all inner closures of given class and for each,
        // gather the inner class as a byte array
        List<String> classNames = findInnerClosureClassNames(packageDirPath, innerClassPrefix);
        for (String className : classNames) {
            byte[] classBytes = fetchClassBytes(packageDirPath, className);
            if (classBytes != null) {
                classes.add(classBytes);
            }
        }
        return classes;
    }

    protected String toPackageDirPath(Class clazz) {
        Package pkg = clazz.getPackage();
        return pkg == null ? "" : pkg.getName().replace(".", "/");
    }

    protected String toInnerClassPrefix(Class clazz) {
        final Package var = clazz.getPackage();
        final String packageName = (var == null ? null : var.getName());
        String fixedName = clazz.getName().replace("$_$", "$_");
        if (packageName == null) {
            return fixedName;
        } else {
            return fixedName.substring(packageName.length() + 1);
        }
    }

    /**
     * Retrieve the list of class names for any inner classes/closures associated the specified package & class name.
     *
     * @param packageName  Name of the package
     * @param className    Name of the 'outer' class/closure
     * @return List of String objects representing class name(s) (with the '.class' suffix)
     */
    private List<String> findInnerClosureClassNames(String packageName, String className) {
        List<String>   classNames = new ArrayList<String>();
        String         dotClass   = ".class";
        try {
            InputStream    inStream = this.classLoader.getResourceAsStream(packageName.replaceAll("[.]", "/"));
            BufferedReader reader   = new BufferedReader(new InputStreamReader(inStream));
            if ((className != null) && (className.length() > 0)) {
                String line = null;
                String nameWithDotClass = className + dotClass;
                while ((line = reader.readLine()) != null) {
                    if ((! line.equals(nameWithDotClass)) && (line.startsWith(className)) && (line.endsWith(dotClass))) {
                        // An inner class of the named class, but not the named class itself; Add to list to be returned
                        classNames.add(line);
                    }
                }
            }
        }
        catch (Exception x) {
            throw new RemoteControlException("Unable to read from classloader for " + packageName, x);
        }
        return classNames;
    }

    /**
     * Fetch the class for the specified class name.
     *
     * @param packageName  Name of the package
     * @param className    Name of the class
     * @return  Class object representing the named class
     */
    private byte[] fetchClassBytes(String packageName, String className) {
        byte[] classBytes   = null;
        String fullPathName = null;
        try {
            String   fullClassName = packageName.replace("/", ".") + "." + className.substring(0, className.lastIndexOf("."));
            Class<?> clazz         = Class.forName(fullClassName);
            fullPathName           = packageName.replace(".", "/") + "/" + className;
            InputStream stream     = clazz.getClassLoader().getResourceAsStream(fullPathName);
            classBytes = IoUtil.read(stream);
        }
        catch (Exception x) {
            throw new RemoteControlException("Unable to find and/or load class " + fullPathName, x);
        }
        return classBytes;
    }
}
