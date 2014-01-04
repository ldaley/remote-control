package groovyx.remote.client;

import groovy.lang.Closure;
import groovyx.remote.util.IoUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InnerClosureClassDefinitionsFinder {

    private final URLClassLoader classLoader;

    public InnerClosureClassDefinitionsFinder(ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalArgumentException("Only URLClassLoaders are supported");
        }

        this.classLoader = ((URLClassLoader) (classLoader));
    }

    @SuppressWarnings("NestedBlockDepth")
    public List<byte[]> find(Class<? extends Closure> clazz) throws IOException {
        List<byte[]> classes = new ArrayList<byte[]>();
        String packageDirPath = toPackageDirPath(clazz);
        String innerClassPrefix = toInnerClassPrefix(clazz);
        String innerClassPrefixWithPackage = packageDirPath + "/" + innerClassPrefix;

        for (URLClassLoader loader : calculateEffectiveClassLoaderHierarchy()) {
            for (URL url : loader.getURLs()) {
                if (!url.getProtocol().equals("file")) {
                    // we only support searching the filesystem right not
                    continue;
                }

                File root;
                try {
                    root = new File(url.toURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

                if (!root.exists()) {
                    // may have been removed, just skip it
                    continue;
                }

                if (root.isDirectory()) {
                    File packageDir = packageDirPath != null ? new File(root, packageDirPath) : root;
                    if (packageDir.exists()) {
                        for (String classFileName : packageDir.list()) {
                            if (classFileName.startsWith(innerClassPrefix) && classFileName.endsWith(".class")) {
                                File file = new File(packageDir, classFileName);
                                classes.add(IoUtil.read(file));
                            }
                        }
                    }
                } else if (root.getName().endsWith(".jar") || root.getName().endsWith(".zip")) {
                    ZipFile jarFile = new ZipFile(root);

                    try {
                        Object packageDir = packageDirPath == null ? root : jarFile.getEntry(packageDirPath);
                        if (packageDir != null) {
                            Enumeration<? extends ZipEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.startsWith(innerClassPrefixWithPackage) && name.endsWith(".class")) {
                                    InputStream inputStream = jarFile.getInputStream(entry);
                                    classes.add(IoUtil.read(inputStream));
                                }
                            }
                        }
                    } finally {
                        jarFile.close();
                    }
                }
            }
        }


        return classes;
    }

    protected List<URLClassLoader> calculateEffectiveClassLoaderHierarchy() {
        List<URLClassLoader> hierarchy = new ArrayList<URLClassLoader>();
        URLClassLoader current = classLoader;
        while (current != null && current instanceof URLClassLoader) {
            hierarchy.add(current);
            current = ((URLClassLoader) (current.getParent()));
        }

        return hierarchy;
    }

    protected String toPackageDirPath(Class clazz) {
        Package var = clazz.getPackage();
        return var == null ? null : var.getName().replace(".", "/");
    }

    protected String toInnerClassPrefix(Class clazz) {
        final Package var = clazz.getPackage();
        final String packageName = (var == null ? null : var.getName());
        String fixedName = clazz.getName().replace("$_$", "$_") + "_";
        if (packageName == null) {
            return fixedName;
        } else {
            return fixedName.substring(packageName.length() + 1);
        }
    }

}
