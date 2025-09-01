package edu.eci.arep.context;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {

    // Aquí guardamos las instancias de los "beans"
    private static final Map<Class<?>, Object> beans = new HashMap<>();

    // Obtiene o crea un bean de la clase solicitada
    public static Object getBean(Class<?> clazz) {
        try {
            // Si ya existe la instancia, la devolvemos
            if (beans.containsKey(clazz)) {
                return beans.get(clazz);
            }
            // Si no, la creamos y la guardamos
            Object instance = clazz.getDeclaredConstructor().newInstance();
            beans.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el bean de " + clazz.getName(), e);
        }
    }

    // Permite registrar manualmente un bean (opcional)
    public static void registerBean(Class<?> clazz, Object instance) {
        beans.put(clazz, instance);
    }
}
