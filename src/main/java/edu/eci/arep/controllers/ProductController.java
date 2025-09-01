package edu.eci.arep.controllers;

import edu.eci.arep.anotation.*;
import edu.eci.arep.context.ApplicationContext;
import edu.eci.arep.model.Product;
import edu.eci.arep.services.ProductService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    public ProductController() {
        this.productService = (ProductService) ApplicationContext.getBean(ProductService.class);
    }

    @PostMapping("/add")
    public Product createProduct(@RequestBody Product p) {
        productService.addProduct(p);
        return p;
    }
    @GetMapping("/allProducts")
    public ArrayList<Product> getAllProduct() {
        System.out.println("Getting all products");
        ArrayList<Product> salida = productService.getProducts();
        return salida;
    }
    @DeleteMapping("/delete")
    public void deleteProduct(@RequestParam (value="id") String id) {
        System.out.println("esto llegó: " + id);

        productService.removeProduct(Integer.parseInt(id));
    }

}
