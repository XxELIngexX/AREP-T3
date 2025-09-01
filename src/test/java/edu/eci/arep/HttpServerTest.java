package edu.eci.arep;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.eci.arep.controllers.ProductController;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;

public class HttpServerTest {

    @Test
    public void testHandleGetRequest() throws IOException {
        // Simular una petición GET a /layouts/menu.html
        String fakeRequest = "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayInputStream input = new ByteArrayInputStream(fakeRequest.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Socket falso
        Socket fakeSocket = new FakeSocket(input, output);

        // Llamar al método que maneja la solicitud
        HttpServer.handleRequest(fakeSocket);

        String response = output.toString();
        System.out.println(response);
        assertTrue(response.contains("HTTP/1.1 200 OK")); // Verifica que responde OK
    }

    @Test
    public void testHandlePostRequest() throws IOException {
    // JSON del producto
    String jsonBody = "{ \"nombre\":\"Control\", \"precio\":\"300000\", \"imagen\":\"https://i.pinimg.com/originals/3c/41/72/3c4172c4a6ba0a4b8aef662f8197e059.png\" }";

    String fakeRequest = "POST /product/add HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Content-Type: application/json\r\n" +
            "Content-Length: " + jsonBody.getBytes().length + "\r\n" +
            "\r\n" +
            jsonBody;

    ByteArrayInputStream input = new ByteArrayInputStream(fakeRequest.getBytes());
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    Socket fakeSocket = new FakeSocket(input, output);

    HttpServerTestHelper.loadTestComponents(ProductController.class);

    HttpServer.handleRequest(fakeSocket);

    String response = output.toString();
    System.out.println(response);

    assertTrue(response.contains("HTTP/1.1 200 OK"));
    assertTrue(response.contains("\"nombre\":\"Control\""));
}

    @Test
    public void testHandleDeleteRequest() throws IOException {
        // 1. Crear producto con POST
        String jsonBody = "{ \"nombre\":\"Control\", \"precio\":\"300000\", \"imagen\":\"https://i.pinimg.com/originals/3c/41/72/3c4172c4a6ba0a4b8aef662f8197e059.png\" }";
        String postRequest = "POST /product/add HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonBody.getBytes().length + "\r\n" +
                "\r\n" +
                jsonBody;

        ByteArrayInputStream postInput = new ByteArrayInputStream(postRequest.getBytes());
        ByteArrayOutputStream postOutput = new ByteArrayOutputStream();
        Socket postSocket = new FakeSocket(postInput, postOutput);

        HttpServerTestHelper.loadTestComponents(ProductController.class);
        HttpServer.handleRequest(postSocket);

        // 2. Eliminar producto (ejemplo con query param id=1)
        String deleteRequest = "DELETE /product/delete?id=1 HTTP/1.1\r\n" +
                "Host: localhost\r\n\r\n";

        ByteArrayInputStream deleteInput = new ByteArrayInputStream(deleteRequest.getBytes());
        ByteArrayOutputStream deleteOutput = new ByteArrayOutputStream();
        Socket deleteSocket = new FakeSocket(deleteInput, deleteOutput);

        HttpServer.handleRequest(deleteSocket);

        String deleteResponse = deleteOutput.toString();
        System.out.println(deleteResponse);

        assertTrue(deleteResponse.contains("HTTP/1.1 200 OK"));

        // 3. Consultar productos para verificar que ya no está
        String getRequest = "GET /product/allProducts HTTP/1.1\r\nHost: localhost\r\n\r\n";

        ByteArrayInputStream getInput = new ByteArrayInputStream(getRequest.getBytes());
        ByteArrayOutputStream getOutput = new ByteArrayOutputStream();
        Socket getSocket = new FakeSocket(getInput, getOutput);

        HttpServer.handleRequest(getSocket);

        String getResponse = getOutput.toString();
        System.out.println(getResponse);

        // Verificar que ya no aparece el producto eliminado
        assertFalse(getResponse.contains("\"nombre\":\"Control\""));
    }

    // Clase Socket falsa para simular conexiones
    static class FakeSocket extends Socket {
        private final InputStream input;
        private final OutputStream output;

        FakeSocket(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public InputStream getInputStream() {
            return input;
        }

        @Override
        public OutputStream getOutputStream() {
            return output;
        }

        @Override
        public synchronized void close() {
            // No hacer nada
        }
    }

    public static class HttpServerTestHelper {
        public static void loadTestComponents(Class<?>... controllers) {
            String[] args = Arrays.stream(controllers)
                    .map(Class::getName)
                    .toArray(String[]::new);
            try {
                Method load = HttpServer.class.getDeclaredMethod("loadComponents", String[].class);
                load.setAccessible(true);
                load.invoke(null, (Object) args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}