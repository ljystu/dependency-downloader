package com.example.gitprojectsfilter.validation;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderExample {
    public static void main(String[] args) throws Exception {
        File file = new File("/Users/ljystu/Desktop/GitProjectsFilter/src/main/java/com/example/gitprojectsfilter/testjava.java");
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
        Class<?> clazz = loader.loadClass("com.example.gitprojectsfilter.testjava");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod("printHello");
        method.invoke(instance);
    }
}
