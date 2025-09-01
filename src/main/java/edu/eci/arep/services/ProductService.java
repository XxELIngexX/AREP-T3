package edu.eci.arep.services;

import edu.eci.arep.model.Product;

import java.util.ArrayList;

public class ProductService {

    private ArrayList<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        if (products.isEmpty()) {
            int id = 1;
            product.setId(id);
        }else{
            int id = products.get(products.size()-1).getId();
            product.setId(id);
        }
        products.add(product);
        System.out.println("added product " + product);
    }
    public void removeProduct(int id) {
        products.remove(products.get(id));
    }
    public ArrayList<Product> getProducts() {
        ArrayList<Product> salida = products;
        return salida;
    }
    public Product getProduct(int id) {
        return products.get(id);
    }


}
