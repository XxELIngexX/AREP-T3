package edu.eci.arep.services;

import edu.eci.arep.model.Product;

import java.util.ArrayList;

public class ProductService {

    private ArrayList<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        if (products.isEmpty()) {
            int id = 1;
            product.setId(id);
        } else {
            int id = products.get(products.size() - 1).getId() + 1;
            product.setId(id);
        }
        products.add(product);
        System.out.println("added product " + product);
    }

    public void removeProduct(int id) {
        System.out.println("removing product con id " + id);
        int index = 10000;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                index = i;
                
            }
        }
        products.remove(index);
    }

    public ArrayList<Product> getProducts() {
        ArrayList<Product> salida = products;
        return salida;
    }

    public Product getProduct(int id) {
        return products.get(id);
    }

}
