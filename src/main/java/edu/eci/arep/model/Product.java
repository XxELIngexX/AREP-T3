package edu.eci.arep.model;

public class Product {
    private int id;
    private String nombre;
    private String description;
    private String precio;
    private String imagen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product() {
    }
    public Product(String nombre, String description, String precio, String imagen) {
        this.nombre = nombre;
        this.description = description;
        this.precio = precio;
        this.imagen = imagen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }


    public String toString() {
        return "Product{" +
                "nombre='" + nombre + '\'' +
                ", description='" + description + '\'' +
                ", price=" + precio +
                ", url='" + imagen + '\'' +
                '}';
    }
}
