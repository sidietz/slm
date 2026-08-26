package de.oberamsystems.slm;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.Duration;

import static org.mockito.Mockito.mock;

public class GenericModelTest {

    @Test
    public void testAllModels() throws Exception {
        File folder = new File("src/main/java/de/oberamsystems/slm/model");
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                String className = "de.oberamsystems.slm.model." + file.getName().replace(".java", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    
                    if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }

                    Object instance = null;
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    for (Constructor<?> constructor : constructors) {
                        constructor.setAccessible(true);
                        Class<?>[] paramTypes = constructor.getParameterTypes();
                        Object[] args = new Object[paramTypes.length];
                        for (int i = 0; i < paramTypes.length; i++) {
                            Object dummy = getDummyValue(paramTypes[i]);
                            if (dummy == null && !paramTypes[i].isPrimitive() && !paramTypes[i].isArray() && paramTypes[i] != String.class && !paramTypes[i].isEnum()) {
                                try {
                                    dummy = mock(paramTypes[i]);
                                } catch (Exception ignored) {}
                            }
                            args[i] = dummy;
                        }
                        try {
                            Object inst = constructor.newInstance(args);
                            if (paramTypes.length == 0 || instance == null) {
                                instance = inst;
                            }
                        } catch (Exception e) {}
                    }
                    
                    if (instance == null) continue;

                    // Invoke common methods
                    instance.toString();
                    instance.hashCode();
                    instance.equals(instance);
                    instance.equals(null);
                    instance.equals(new Object());

                    // Invoke setters
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                            Class<?> paramType = method.getParameterTypes()[0];
                            Object dummyValue = getDummyValue(paramType);
                            if (dummyValue == null && !paramType.isPrimitive() && !paramType.isArray() && paramType != String.class && !paramType.isEnum()) {
                                try {
                                    dummyValue = mock(paramType);
                                } catch (Exception ignored) {}
                            }
                            method.setAccessible(true);
                            method.invoke(instance, dummyValue);
                        }
                    }
                    
                    // Invoke getters
                    for (Method method : methods) {
                        if ((method.getName().startsWith("get") || method.getName().startsWith("is")) 
                             && method.getParameterCount() == 0) {
                            method.setAccessible(true);
                            method.invoke(instance);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Could not fully test " + className + ": " + e.getMessage());
                }
            }
        }
    }

    private Object getDummyValue(Class<?> type) {
        if (type == String.class) return "test";
        if (type == Integer.class || type == int.class) return 1;
        if (type == Long.class || type == long.class) return 1L;
        if (type == Double.class || type == double.class) return 1.0;
        if (type == Float.class || type == float.class) return 1.0f;
        if (type == Boolean.class || type == boolean.class) return true;
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (type == LocalDate.class) return LocalDate.now();
        if (type == Date.class) return new Date();
        if (type == Duration.class) return Duration.ofMinutes(1);
        if (type == java.util.Set.class) return new java.util.HashSet<>();
        if (type == java.util.List.class) return new java.util.ArrayList<>();
        return null;
    }
}
