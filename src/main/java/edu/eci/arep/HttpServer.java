package edu.eci.arep;

import edu.eci.arep.anotation.GetMapping;
import edu.eci.arep.anotation.RestController;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    private static String localPath = "src/main/resources";
    public static Map<String, Method> methods = new HashMap<>();

    private static void loadComponents(String[] args) {
        try {
            Class<?> c = Class.forName(args[0]);
            if (c.isAnnotationPresent(RestController.class)) {
                Method[] ms = c.getDeclaredMethods();
                for (Method m : ms) {
                    if (m.isAnnotationPresent(GetMapping.class)) {
                        String mapping = m.getAnnotation(GetMapping.class).value();
                        methods.put(mapping, m);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Clase no encontrada: " + e.getMessage());
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
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
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
                String body = invokeService(request);
                responseBytes = buildHttpResponse(body, "text/html");
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
                    responseBytes = buildHttpResponse(fileData, contentType);
                } else {
                    String notFound = "<html><body><h1>404 Not Found</h1></body></html>";
                    responseBytes = buildHttpResponse(notFound.getBytes(), "text/html", 404, "Not Found");
                }
            }

            rawOut.write(responseBytes);
            rawOut.flush();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String invokeService(URI requestUri) {
        Method m = methods.get(requestUri.getPath());
        if (m != null) {
            try {
                return (String) m.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return "<h1>Error al invocar servicio</h1>";
    }

    private static byte[] buildHttpResponse(String body, String contentType) {
        return buildHttpResponse(body.getBytes(StandardCharsets.UTF_8), contentType, 200, "OK");
    }

    private static byte[] buildHttpResponse(byte[] bodyBytes, String contentType) {
        return buildHttpResponse(bodyBytes, contentType, 200, "OK");
    }

    private static byte[] buildHttpResponse(byte[] bodyBytes, String contentType, int statusCode, String statusText) {
        String header = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n\r\n";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] response = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, response, headerBytes.length, bodyBytes.length);
        return response;
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
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
