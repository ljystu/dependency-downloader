package com.example.gitprojectsfilter.validation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ljystu
 * @author ljystu
 */
public class DynamicProxyExample {
    public static void main(String[] args) {
        MyInterface myInterface = (MyInterface) Proxy.newProxyInstance(
                DynamicProxyExample.class.getClassLoader(),
                new Class[]{MyInterface.class},
                new MyInvocationHandler(new MyInterfaceImpl())
        );

        myInterface.sayHello();
    }
}

interface MyInterface {
    void sayHello();
}

class MyInterfaceImpl implements MyInterface {
    public void sayHello() {
        System.out.println("Hello, world!");
    }
}

class MyInvocationHandler implements InvocationHandler {
    private final Object target;

    public MyInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before method call");
        Object result = method.invoke(target, args);
        System.out.println("After method call");
        return result;
    }
}
