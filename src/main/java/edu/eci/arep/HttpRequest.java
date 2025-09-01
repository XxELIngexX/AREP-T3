package edu.eci.arep;

import java.net.URI;

public class HttpRequest {
    URI requesturi;
    HttpRequest(URI requestUri) {
        // Constructor logic if needed
        requesturi = requestUri;
    }   
    public String getValue(String name){
        String out = requesturi.getQuery().split("=")[1]; //name=jhon

        System.out.println(requesturi.getFragment());

        return out;

    }
}
