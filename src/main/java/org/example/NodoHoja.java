package org.example;

import java.util.Arrays;

public class NodoHoja extends Nodo {
    int maxNumPares; // Máximo número de pares clave-valor que puede tener el nodo hoja
    int minNumPares; // Mínimo número de pares clave-valor que debe tener el nodo hoja
    int numPares; // Número actual de pares clave-valor en el nodo hoja
    NodoHoja hermanoIzquierdo; // Referencia al nodo hoja hermano izquierdo
    NodoHoja hermanoDerecho; // Referencia al nodo hoja hermano derecho
    ParDiccionario[] diccionario; // Arreglo de pares clave-valor

    // Constructor que inicializa el nodo hoja con un par clave-valor
    public NodoHoja(int m, ParDiccionario dp) {
        this.maxNumPares = m - 1;
        this.minNumPares = (int) (Math.ceil(m / 2.0) - 1);
        this.diccionario = new ParDiccionario[m];
        this.numPares = 0;
        this.insertar(dp);
    }

    // Constructor que inicializa el nodo hoja con un arreglo de pares clave-valor y un nodo padre
    public NodoHoja(int m, ParDiccionario[] dps, NodoInterno padre) {
        this.maxNumPares = m - 1;
        this.minNumPares = (int) (Math.ceil(m / 2.0) - 1);
        this.diccionario = dps;
        this.numPares = busquedaLinealNula(dps);
        this.padre = padre;
    }

    // Inserta un par clave-valor en el nodo hoja
    public boolean insertar(ParDiccionario dp) {
        if (this.estaLleno()) {
            return false;
        } else {
            this.diccionario[numPares] = dp;
            numPares++;
            Arrays.sort(this.diccionario, 0, numPares);
            return true;
        }
    }

    // Elimina un par clave-valor en un índice específico
    public void eliminar(int indice) {
        this.diccionario[indice] = null;
        numPares--;
    }

    // Reordena los pares clave-valor del nodo hoja para eliminar espacios nulos
    public void reordenar() {
        for (int i = 0; i < diccionario.length; i++) {
            if (diccionario[i] == null) {
                for (int j = i + 1; j < diccionario.length; j++) {
                    if (diccionario[j] != null) {
                        diccionario[i] = diccionario[j];
                        diccionario[j] = null;
                        break;
                    }
                }
            }
        }
    }

    // Verifica si el nodo hoja está lleno (número máximo de pares clave-valor)
    public boolean estaLleno() {
        return numPares == maxNumPares;
    }

    // Verifica si el nodo hoja está deficiente (menos pares clave-valor que el mínimo requerido)
    public boolean estaDeficiente() {
        return numPares < minNumPares;
    }

    // Busca el primer índice nulo en el arreglo de pares clave-valor
    private int busquedaLinealNula(ParDiccionario[] dps) {
        for (int i = 0; i < dps.length; i++) {
            if (dps[i] == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return Arrays.toString(diccionario);
    }
}
