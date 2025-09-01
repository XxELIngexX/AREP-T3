package edu.eci.arep;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    
    private int statusCode = 200;
    private String statusText = "OK";
    private Map<String, String> headers = new HashMap<>();
    private String body = "";



    public void setStatus(int code, String text) {
        this.statusCode = code;
        this.statusText = text;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setContentType(String contentType) {
        headers.put("Content-Type", contentType);
    }

    public void setContentLength(int contentLength) {
        headers.put("Content-Length",String.valueOf(contentLength));
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String buildResponse() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");

        if (!headers.containsKey("Content-Length")) {
            headers.put("Content-Length", String.valueOf(body.getBytes().length));
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        response.append("\r\n");
        response.append(body);

        return response.toString();
    }
}
