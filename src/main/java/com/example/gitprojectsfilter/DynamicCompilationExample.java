package com.example.gitprojectsfilter;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicCompilationExample {
    public static void main(String[] args) throws Exception {
        File sourceFile = new File("src/SomeClass.java");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File("src/").toURI().toURL()});
        Class<?> clazz = Class.forName("SomeClass", true, classLoader);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod("printHello");
        method.invoke(instance);
    }
}
