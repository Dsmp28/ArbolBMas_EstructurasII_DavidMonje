package org.example;

public class Main {
    public static void main(String[] args) {
        ArbolBMas arbol = new ArbolBMas(5);
        arbol.insertar("01", new Libro("1", "Libro 1", "Autor 1", "Categoria 1", 100.0, 10));
        arbol.insertar("02", new Libro("2", "Libro 2", "Autor 2", "Categoria 2", 200.0, 20));
        arbol.insertar("03", new Libro("3", "Libro 3", "Autor 3", "Categoria 3", 300.0, 30));
        arbol.insertar("04", new Libro("4", "Libro 4", "Autor 4", "Categoria 4", 400.0, 40));
        arbol.insertar("13", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));
        arbol.insertar("12", new Libro("10", "Libro 10", "Autor 10", "Categoria 10", 1000.0, 100));
        arbol.insertar("05", new Libro("5", "Libro 5", "Autor 5", "Categoria 5", 500.0, 50));
        arbol.insertar("06", new Libro("6", "Libro 6", "Autor 6", "Categoria 6", 600.0, 60));
        arbol.insertar("07", new Libro("7", "Libro 7", "Autor 7", "Categoria 7", 700.0, 70));
        arbol.insertar("08", new Libro("8", "Libro 8", "Autor 8", "Categoria 8", 800.0, 80));
        arbol.insertar("09", new Libro("9", "Libro 9", "Autor 9", "Categoria 9", 900.0, 90));
        arbol.insertar("14", new Libro("9", "Libro 9", "Autor 9", "Categoria 9", 900.0, 90));
        arbol.insertar("10", new Libro("10", "Libro 10", "Autor 10", "Categoria 10", 1000.0, 100));
        arbol.insertar("11", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));
        arbol.insertar("15", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));
        arbol.insertar("16", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));
        arbol.insertar("17", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));
        arbol.insertar("18", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));
        arbol.insertar("19", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));
        arbol.insertar("20", new Libro("11", "Libro 11", "Autor 11", "Categoria 11", 1100.0, 110));

        arbol.mostrarArbol();
        System.out.println("-------------------------------------------------");

        arbol.eliminar("01");

        arbol.mostrarArbol();
    }
}