package edu.eci.arep.util;

import java.lang.reflect.*;
import java.util.*;

public class JsonUtil {

    // -------------------------
    // Convierte cualquier objeto a JSON
    public static String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Collection) return collectionToJson((Collection<?>) obj);
        if (obj.getClass().isArray()) return arrayToJson(obj);
        if (obj instanceof Map) return mapToJson((Map<?, ?>) obj);

        return objectToJson(obj);
    }

    private static String collectionToJson(Collection<?> col) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object o : col) {
            if (!first) sb.append(",");
            sb.append(toJson(o));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private static String arrayToJson(Object array) {
        StringBuilder sb = new StringBuilder("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(Array.get(array, i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append(toJson(e.getKey().toString())).append(":").append(toJson(e.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (!first) sb.append(",");
                sb.append(toJson(field.getName())).append(":").append(toJson(value));
                first = false;
            } catch (IllegalAccessException e) {
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }


    public static Object fromJson(String json, Class<?> clazz) {
        if (clazz == String.class) return unquote(json);
        if (clazz == int.class || clazz == Integer.class) return Integer.parseInt(json);
        if (clazz == long.class || clazz == Long.class) return Long.parseLong(json);
        if (clazz == boolean.class || clazz == Boolean.class) return Boolean.parseBoolean(json);
        if (clazz == double.class || clazz == Double.class) return Double.parseDouble(json);
        if (clazz == Map.class) return parseMap(json);
        if (clazz == List.class || clazz.isArray()) return parseList(json, clazz);
        return parseObject(json, clazz);
    }

    private static String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"");
    }

    private static Map<String, Object> parseMap(String json) {
        Map<String, Object> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        if (json.isEmpty()) return map;

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = unquote(kv[0].trim());
                String value = unquote(kv[1].trim());
                map.put(key, value);
            }
        }
        return map;
    }

    private static List<Object> parseList(String json, Class<?> clazz) {
        List<Object> list = new ArrayList<>();
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);
        if (json.isEmpty()) return list;

        String[] elements = json.split(",");
        for (String elem : elements) {
            list.add(unquote(elem.trim()));
        }
        return list;
    }

    private static Object parseObject(String json, Class<?> clazz) {
        try {
            Object obj = clazz.getDeclaredConstructor().newInstance();
            Map<String, Object> map = parseMap(json);
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (map.containsKey(field.getName())) {
                    Object value = map.get(field.getName());
                    if (field.getType() == int.class) field.setInt(obj, Integer.parseInt(value.toString()));
                    else if (field.getType() == boolean.class) field.setBoolean(obj, Boolean.parseBoolean(value.toString()));
                    else field.set(obj, value);
                }
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
