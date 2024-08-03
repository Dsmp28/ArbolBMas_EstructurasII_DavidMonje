package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ArbolBMas arbol = new ArbolBMas(5);
        arbol.procesarCsv("/Users/david/IdeaProjects/ArbolBMas_EstructurasII_DavidMonje/src/main/resources/datos_10000.csv");
    }
}