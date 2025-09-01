package edu.eci.arep;

import edu.eci.arep.anotation.*;
import edu.eci.arep.context.ApplicationContext;
import edu.eci.arep.controllers.ProductController;
import edu.eci.arep.services.ProductService;
import edu.eci.arep.util.JsonUtil;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import edu.eci.arep.context.ApplicationContext.*;

public class HttpServer {

    private static String localPath = "src/main/resources";
    public static Map<String, Method> methods = new HashMap<>();

    private static void loadComponents(String[] args) {
        ApplicationContext.registerBean(ProductService.class, new ProductService());
        try {
            for (String arg : args) {

                Class<?> c = Class.forName(arg);
                if (c.isAnnotationPresent(RestController.class)) {
                    Object controllerInstance = c.getDeclaredConstructor().newInstance();
                    ApplicationContext.registerBean(c, controllerInstance);
                    String basePath = "";
                    if (c.isAnnotationPresent(RequestMapping.class)) {
                        basePath = c.getAnnotation(RequestMapping.class).value();
                    }
                    Method[] ms = c.getDeclaredMethods();
                    for (Method m : ms) {
                        if (m.isAnnotationPresent(GetMapping.class)) {
                            String mapping = m.getAnnotation(GetMapping.class).value();
                            methods.put(basePath + mapping, m);
                        }
                        if (m.isAnnotationPresent(PostMapping.class)) {
                            String mapping = m.getAnnotation(PostMapping.class).value();
                            methods.put(basePath + mapping, m);
                        }
                        if (m.isAnnotationPresent(DeleteMapping.class)) {
                            String mapping = m.getAnnotation(DeleteMapping.class).value();
                            methods.put(basePath + mapping, m);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Clase no encontrada: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runServer(String[] args) throws IOException {
        loadComponents(args);

        ServerSocket serverSocket = new ServerSocket(35000);
        System.out.println("Servidor iniciado en el puerto 35000...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleRequest(clientSocket);
        }
    }

    public static void handleRequest(Socket clientSocket) {
        try (
                OutputStream rawOut = clientSocket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;
            String path = null;
            String method = null;
            boolean firstLine = true;
            String contentLengthStr = null;
            URI request = null;

            // Leer encabezados HTTP
            while ((inputLine = in.readLine()) != null) {
                if (firstLine) {
                    try {
                        request = new URI(inputLine.split(" ")[1]);
                        path = request.getPath();
                        method = inputLine.split(" ")[0];
                        System.out.println("Method: " + method + " | Path: " + path);
                    } catch (URISyntaxException e) {
                        System.err.println("Invalid URI syntax: " + inputLine);
                    }
                    firstLine = false;
                }
                if (inputLine.startsWith("Content-Length:")) {
                    contentLengthStr = inputLine.split(": ")[1];
                }

                if (inputLine.isEmpty()) {
                    break;
                }
            }

            byte[] responseBytes;

            // Manejo de endpoints REST
            if (methods.containsKey(path)) {
                String bodyString = null;
                if (method.equals("POST")) {

                    if (contentLengthStr != null) {
                        int contentLength = Integer.parseInt(contentLengthStr);
                        char[] body = new char[contentLength];
                        in.read(body, 0, contentLength);
                        bodyString = new String(body);
                    }
                }
                String body = invokeService(request, bodyString);
                System.out.println("salida del invokeService");
                System.out.println(body);

                HttpResponse res = new HttpResponse();
                res.setStatus(200, "OK");
                res.setContentType("application/json");
                res.setBody(body);
                rawOut.write(res.buildResponse().getBytes(StandardCharsets.UTF_8));
                rawOut.flush();

                // responseBytes = buildHttpResponse(body, "application/json");
            } else {
                // Manejo de archivos estáticos
                String filePath = localPath + path;
                if (path.equals("/")) {
                    filePath = localPath + "/index.html";
                }
                File file = new File(filePath);
                if (file.exists() && !file.isDirectory()) {
                    String contentType = getContentType(filePath);
                    byte[] fileData = Files.readAllBytes(file.toPath());

                    HttpResponse res = new HttpResponse();
                    res.setStatus(200, "OK");
                    res.setContentType(contentType);
                    res.setContentLength(fileData.length);

                    rawOut.write(res.buildResponse().getBytes(StandardCharsets.UTF_8));
                    rawOut.flush();
                    rawOut.write(fileData);
                    rawOut.flush();

                    // responseBytes = buildHttpResponse(fileData, contentType);
                } else {
                    String notFound = "<html><body><h1>404 Not Found</h1></body></html>";
                    HttpResponse res = new HttpResponse();

                    res.setStatus(404, "Not Found");
                    res.setContentType("text/html");
                    rawOut.write(res.buildResponse().getBytes(StandardCharsets.UTF_8));
                    rawOut.flush();
                    // responseBytes = buildHttpResponse(notFound.getBytes(), "text/html", 404, "Not
                    // Found");
                }
            }

            // rawOut.write(responseBytes);
            // rawOut.flush();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String invokeService(URI requestUri, String bodyString) throws IOException {

        Method m = methods.get(requestUri.getPath());

        if (m != null) {
            try {
                Map<String, String> queryParams = new HashMap<>();
                String query = requestUri.getQuery();
                if (query != null) {
                    String[] params = query.split("=");
                    queryParams.put(params[0], params[1]);
                }
                Parameter[] parameters = m.getParameters();
                Object[] parameterValues = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter p = parameters[i];

                    RequestParam rp = p.getAnnotation(RequestParam.class);
                    if (rp != null) {
                        String name = rp.value();
                        String value = queryParams.get(name);

                        if (value == null || value.isEmpty()) {
                            value = rp.defaultValue();
                        }

                        parameterValues[i] = value;
                        continue;
                    }

                    RequestBody rb = p.getAnnotation(RequestBody.class);
                    if (rb != null) {
                        Class<?> paramType = p.getType();
                        Object obj = JsonUtil.fromJson(bodyString, paramType);
                        parameterValues[i] = obj;
                        continue;
                    }

                }

                Boolean isStatic = Modifier.isStatic(m.getModifiers());
                Object controllerInstance = null;

                if (!isStatic) {
                    controllerInstance = ApplicationContext.getBean(m.getDeclaringClass());
                }
                Object request = m.invoke(controllerInstance, parameterValues);

                if (request instanceof String) {
                    System.out.println((String) request);
                    return (String) request;
                } else {
                    System.out.println(JsonUtil.toJson(request));
                    return JsonUtil.toJson(request);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "<h1>Error al invocar servicio</h1>";
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm"))
            return "text/html";
        if (path.endsWith(".css"))
            return "text/css";
        if (path.endsWith(".js"))
            return "application/javascript";
        if (path.endsWith(".png"))
            return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
            return "image/jpeg";
        if (path.endsWith(".gif"))
            return "image/gif";
        return "text/plain";
    }

    public static void staticFiles(String path) {
        if (!"/".equals(path)) {
            localPath = path;
            System.out.println("Archivos estáticos servirán desde: " + localPath);
        }
    }

    public static void start(String[] args) {
        try {
            runServer(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
