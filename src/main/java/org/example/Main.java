package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ArbolBMas arbol = new ArbolBMas(5);
        boolean continuar = true;
        while (continuar) {
            continuar = arbol.menu();
        }
        System.out.println("Fin del programa");
    }
}