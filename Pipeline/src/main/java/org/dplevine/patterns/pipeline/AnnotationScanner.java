package org.dplevine.patterns.pipeline;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class AnnotationScanner {
    public static List<Class<?>> scanClassesWithAnnotation(String packageName, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        List<Class<?>> classes = new Vector<>();

        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                String filePath = resource.getFile();
                classes.addAll(getClassesWithAnnotationInDirectory(packageName, filePath, annotation));
            } else if (resource.getProtocol().equals("jar")) {
                JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
                JarFile jarFile = jarURLConnection.getJarFile();
                classes.addAll(getClassesWithAnnotationInJar(packageName, jarFile, annotation));
            }
        }

        return classes;
    }

    private static List<Class<?>> getClassesWithAnnotationInDirectory(String packageName, String filePath, Class<? extends Annotation> annotation) throws ClassNotFoundException {
        List<Class<?>> classes = new Vector<>();
        File directory = new File(filePath);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        classes.addAll(getClassesWithAnnotationInDirectory(packageName + "." + file.getName(), file.getAbsolutePath(), annotation));
                    } else if (file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotation)) {
                            classes.add(clazz);
                        }
                    }
                }
            }
        }
        return classes;
    }

    private static List<Class<?>> getClassesWithAnnotationInJar(String packageName, JarFile jarFile, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new Vector<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                if (className.startsWith(packageName)) {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotation)) {
                        classes.add(clazz);
                    }
                }
            }
        }
        return classes;
    }
}
