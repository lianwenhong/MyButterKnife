package com.lianwenhong.butter_knife;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ButterKnife {
    public static void bind(Object target) {
        String clzName = target.getClass().getName() + "$$ViewBinder";
        try {
            Class<?> aClass = Class.forName(clzName);
            Constructor<?> constructor = aClass.getConstructor(target.getClass());
            constructor.newInstance(target);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
