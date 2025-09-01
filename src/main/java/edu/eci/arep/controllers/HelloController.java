package edu.eci.arep.controllers;


import edu.eci.arep.anotation.GetMapping;
import edu.eci.arep.anotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public static String index() {
        return "Greetings from Spring Boot!";
    }
}
