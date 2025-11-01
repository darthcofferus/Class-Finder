package com.github.darthcofferus.class_finder;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * @version 1.0
 * @author Darth Cofferus
 */
@FunctionalInterface
public interface ActionWithClass {

    /** Creating an instance of a class. A class must have a default constructor with any access modifier. */
    ActionWithClass CREATING_INSTANCE = c -> {
        try {
            Constructor<?> constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    void perform(@NotNull Class<?> c);
}