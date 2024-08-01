package org.example;

public class NodoInterno extends Nodo {
    int maxGrado;
    int minGrado;
    int grado;
    NodoInterno hermanoIzquierdo;
    NodoInterno hermanoDerecho;
    String[] claves;
    Nodo[] punterosHijos;

    public NodoInterno(int m, String[] claves) {
        this.maxGrado = m;
        this.minGrado = (int) Math.ceil(m / 2.0);
        this.grado = 0;
        this.claves = claves;
        this.punterosHijos = new Nodo[this.maxGrado + 1];
    }

    public NodoInterno(int m, String[] claves, Nodo[] punteros) {
        this.maxGrado = m;
        this.minGrado = (int) Math.ceil(m / 2.0);
        this.grado = busquedaLinealNula(punteros);
        this.claves = claves;
        this.punterosHijos = punteros;
    }

    public void anadirPunteroHijo(Nodo puntero) {
        this.punterosHijos[grado] = puntero;
        this.grado++;
    }

    public int encontrarIndiceDePuntero(Nodo puntero) {
        for (int i = 0; i < punterosHijos.length; i++) {
            if (punterosHijos[i] == puntero) {
                return i;
            }
        }
        return -1;
    }

    public void insertarPunteroHijo(Nodo puntero, int indice) {
        for (int i = grado - 1; i >= indice; i--) {
            punterosHijos[i + 1] = punterosHijos[i];
        }
        this.punterosHijos[indice] = puntero;
        this.grado++;
    }

    public boolean estaDeficiente() {
        return this.grado < this.minGrado;
    }

    public boolean esPrestable() {
        return this.grado > this.minGrado;
    }

    public boolean esFusionable() {
        return this.grado == this.minGrado;
    }

    public boolean estaLleno() {
        return this.grado == maxGrado + 1;
    }

    public void eliminarPuntero(int indice) {
        this.punterosHijos[indice] = null;
        this.grado--;
    }

    private int busquedaLinealNula(Nodo[] punteros) {
        for (int i = 0; i < punteros.length; i++) {
            if (punteros[i] == null) {
                return i;
            }
        }
        return -1;
    }

}
