package org.example;

public class Libro {
    String isbn; // ISBN del libro
    String nombre; // Nombre del libro
    String autor; // Autor del libro
    String categoria; // Categoría del libro
    double precio; // Precio del libro
    int cantidad; // Cantidad disponible del libro

    // Constructor que inicializa todos los atributos del libro
    public Libro(String isbn, String nombre, String autor, String categoria, double precio, int cantidad) {
        this.isbn = isbn;
        this.nombre = nombre;
        this.autor = autor;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    // Getters y setters para cada atributo
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // Método toString para representar el libro en formato JSON
    @Override
    public String toString() {
        return String.format("{\"isbn\":\"%s\",\"name\":\"%s\",\"author\":\"%s\",\"category\":\"%s\",\"price\":\"%.2f\",\"quantity\":\"%d\"}",
                isbn, nombre, autor, categoria, precio, cantidad);
    }
}


