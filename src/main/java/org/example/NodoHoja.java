package org.example;

import java.util.Arrays;

public class NodoHoja extends Nodo {
    int maxNumPares;
    int minNumPares;
    int numPares;
    NodoHoja hermanoIzquierdo;
    NodoHoja hermanoDerecho;
    ParDiccionario[] diccionario;

    public NodoHoja(int m, ParDiccionario dp) {
        this.maxNumPares = m - 1;
        this.minNumPares = (int) (Math.ceil(m / 2.0) - 1);
        this.diccionario = new ParDiccionario[m];
        this.numPares = 0;
        this.insertar(dp);
    }

    public NodoHoja(int m, ParDiccionario[] dps, NodoInterno padre) {
        this.maxNumPares = m - 1;
        this.minNumPares = (int) (Math.ceil(m / 2.0) - 1);
        this.diccionario = dps;
        this.numPares = busquedaLinealNula(dps);
        this.padre = padre;
    }

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

    public void eliminar(int indice) {
        this.diccionario[indice] = null;
        numPares--;
    }

    public boolean estaLleno() {
        return numPares == maxNumPares;
    }

    public boolean estaDeficiente() {
        return numPares < minNumPares;
    }

    public boolean esPrestable() {
        return numPares > minNumPares;
    }

    public boolean esFusionable() {
        return numPares == minNumPares;
    }

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
        return diccionario.toString();
    }
}
