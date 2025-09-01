package edu.eci.arep;
import edu.eci.arep.controllers.*;
public class Main {
    public static void main(String[] args) {
        HttpServer.start(new String[]{"edu.eci.arep.controllers.HelloController", "edu.eci.arep.controllers.GreetingController","edu.eci.arep.controllers.ProductController"});
        //HttpServer.start(args);

    }
}

