package com.github.darthcofferus.class_finder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Search for classes in the program.
 * It works with both compiled files and JAR file.
 * @version 1.0
 * @author Darth Cofferus
 */
public class ClassFinder {

    private static final String PATH;

    private static final boolean INSIDE_JAR;

    static {
        String path;
        try {
            // path to compiled classes
            path = ClassFinder.class.getClassLoader().getResources("").nextElement().toURI().getPath();
        } catch (NoSuchElementException e) {
            path = getPathToJar();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        PATH = path;
        INSIDE_JAR = PATH.endsWith(".jar");
    }

    private static String getPathToJar() {
        try {
            return ClassFinder.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ActionWithClass actionWithClass;

    private Predicate<Class<?>> predicate;

    private ZipEntry[] zipEntries;

    /** Find all classes in the program. */
    public void find() {
        find("");
    }

    /**
     * Find all classes of a certain package (including subpackages).
     * @param pkg package name. If you want to check all the packages in the program, pass an empty string.
     */
    public void find(@NotNull String pkg) {
        find(pkg, true);
    }

    /**
     * Find all classes of a certain package.
     * Subpackages can be included or excluded from the search.
     * @param pkg package name. If you want to check all packages in the program,
     *            specify an empty line and enable the search in the subpackages.
     * @param subPackages true: include subpackages, false: exclude subpackages.
     */
    public void find(@NotNull String pkg, boolean subPackages) {
        if (pkg.contains("/")) {
            throw new RuntimeException("The package name cannot contain \"/\"");
        }
        String pkgAsPath = asPath(pkg);
        if (!exists(pkgAsPath)) {
            throw new RuntimeException("The \"" + pkg + "\" package does not exist");
        }
        findClasses(pkgAsPath, subPackages);
    }

    private String asPath(String pkg) {
        return pkg.isEmpty() ? pkg : pkg.replace('.', '/') + '/';
    }

    private boolean exists(String pkgAsPath) {
        if (INSIDE_JAR) {
            cacheZipEntries();
            return pkgAsPath.isEmpty() || Arrays.stream(zipEntries).anyMatch(e -> e.getName().equals(pkgAsPath));
        } else {
            return new File(PATH + pkgAsPath).exists();
        }
    }

    private void cacheZipEntries() {
        if (zipEntries == null) {
            try (ZipFile zipFile = new ZipFile(PATH)) {
                zipEntries = Collections.list(zipFile.entries()).toArray(new ZipEntry[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void findClasses(String pkgAsPath, boolean subPackages) {
        try {
            if (INSIDE_JAR) {
                findClassesInJar(pkgAsPath, subPackages);
            } else {
                findClassesInFiles(pkgAsPath, subPackages);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void findClassesInJar(String pkgAsPath, boolean subPackages) throws Exception {
        for (ZipEntry entry : zipEntries) {
            String fileName = entry.getName();
            String parentDirs = fileName.substring(0, fileName.lastIndexOf('/') + 1);
            if ((subPackages && parentDirs.startsWith(pkgAsPath)) || parentDirs.equals(pkgAsPath)) {
                if (fileName.endsWith(".class")) {
                    performActionWithClass(asFullClassName(fileName));
                }
            }
        }
    }

    private void findClassesInFiles(String pkgAsPath, boolean subPackages) throws Exception {
        File directory = new File(PATH + pkgAsPath);
        for (File file : directory.listFiles()) {
            String fileName = pkgAsPath + file.getName();
            if (file.isDirectory()) {
                if (subPackages) {
                    findClassesInFiles(fileName + "/", true);
                }
            } else if (fileName.endsWith(".class")) {
                performActionWithClass(asFullClassName(fileName));
            }
        }
    }

    private String asFullClassName(String fileName) {
        return fileName.substring(0, fileName.length() - ".class".length()).replace('/', '.');
    }

    private void performActionWithClass(String classFullName) throws Exception {
        Class<?> c;
        try {
            c = Class.forName(classFullName);
        } catch (NoClassDefFoundError e) {
            return;
        }
        if (actionWithClass != null && (predicate == null || predicate.test(c))) {
            actionWithClass.perform(c);
        }
    }

    /**
     * Set an action for the found class.
     * This action will be performed with each class that matches the predicate ({@link #setPredicate(Predicate)}).
     * @param actionWithClass action with class.
     * @return this instance of ClassFinder.
     */
    public ClassFinder setActionWithClass(ActionWithClass actionWithClass) {
        this.actionWithClass = actionWithClass;
        return this;
    }

    /**
     * Set a predicate for the found class.
     * The action ({@link #setActionWithClass(ActionWithClass)})
     * with a class will be performed only if the found class matches the predicate.
     * @param predicate the condition that the class must meet.
     * @return this instance of ClassFinder.
     */
    public ClassFinder setPredicate(Predicate<Class<?>> predicate) {
        this.predicate = predicate;
        return this;
    }

}