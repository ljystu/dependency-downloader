package com.example.gitprojectsfilter.validation;

import java.lang.reflect.Method;

public class ReflectionExample {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("java.lang.String");
        Method method = clazz.getMethod("length");
        Object result = method.invoke("Hello, world!");
        System.out.println("Result: " + result);
    }
}
