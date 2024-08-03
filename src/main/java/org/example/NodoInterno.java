package org.example;

public class NodoInterno extends Nodo {
    int maxGrado; // Máximo número de punteros hijos que puede tener el nodo
    int minGrado; // Mínimo número de punteros hijos que debe tener el nodo
    int grado; // Número actual de punteros hijos
    NodoInterno hermanoIzquierdo; // Referencia al nodo hermano izquierdo
    NodoInterno hermanoDerecho; // Referencia al nodo hermano derecho
    String[] claves; // Arreglo de claves del nodo
    Nodo[] punterosHijos; // Arreglo de punteros a los hijos del nodo

    // Constructor que inicializa el nodo con un máximo de claves y punteros
    public NodoInterno(int m, String[] claves) {
        this.maxGrado = m;
        this.minGrado = (int) Math.ceil(m / 2.0);
        this.grado = 0;
        this.claves = claves;
        this.punterosHijos = new Nodo[this.maxGrado + 1];
    }

    // Constructor que inicializa el nodo con claves y punteros dados
    public NodoInterno(int m, String[] claves, Nodo[] punteros) {
        this.maxGrado = m;
        this.minGrado = (int) Math.ceil(m / 2.0);
        this.grado = busquedaLinealNula(punteros);
        this.claves = claves;
        this.punterosHijos = punteros;
    }

    // Añade un puntero hijo al nodo
    public void anadirPunteroHijo(Nodo puntero) {
        this.punterosHijos[grado] = puntero;
        this.grado++;
    }

    // Devuelve el número total de punteros hijos no nulos
    public int getPunteros() {
        int total = 0;
        for (Nodo punterosHijo : punterosHijos) {
            if (punterosHijo != null) {
                total++;
            }
        }
        return total;
    }

    // Encuentra el índice de un puntero hijo en el arreglo de punteros
    public int encontrarIndiceDePuntero(Nodo puntero) {
        for (int i = 0; i < punterosHijos.length; i++) {
            if (punterosHijos[i] == puntero) {
                return i;
            }
        }
        return -1;
    }

    // Verifica si el nodo contiene una clave específica
    public boolean contieneClave(String clave) {
        for (String c : claves) {
            if (c != null && c.equals(clave)) {
                return true;
            }
        }
        return false;
    }

    // Inserta un puntero hijo en un índice específico
    public void insertarPunteroHijo(Nodo puntero, int indice) {
        for (int i = grado - 1; i >= indice; i--) {
            punterosHijos[i + 1] = punterosHijos[i];
        }
        this.punterosHijos[indice] = puntero;
        this.grado++;
    }

    // Verifica si el nodo está deficiente (menos punteros hijos que el mínimo requerido)
    public boolean estaDeficiente() {
        return getPunteros() < this.minGrado;
    }

    // Verifica si el nodo tiene más punteros hijos que el mínimo requerido
    public boolean esPrestable() {
        return getPunteros() > this.minGrado;
    }

    // Verifica si el nodo está lleno (número máximo de punteros hijos)
    public boolean estaLleno() {
        return getPunteros() == maxGrado + 1;
    }

    // Elimina un puntero hijo en un índice específico
    public void eliminarPuntero(int indice) {
        this.punterosHijos[indice] = null;
        this.grado--;
    }

    // Reordena las claves del nodo para eliminar espacios nulos
    public void reordenarClaves() {
        for (int i = 0; i < claves.length; i++) {
            if (claves[i] == null) {
                for (int j = i + 1; j < claves.length; j++) {
                    if (claves[j] != null) {
                        claves[i] = claves[j];
                        claves[j] = null;
                        break;
                    }
                }
            }
        }
    }

    // Reordena los punteros hijos del nodo para eliminar espacios nulos
    public void reordenarPunteros() {
        for (int i = 0; i < punterosHijos.length; i++) {
            if (punterosHijos[i] == null) {
                for (int j = i + 1; j < punterosHijos.length; j++) {
                    if (punterosHijos[j] != null) {
                        punterosHijos[i] = punterosHijos[j];
                        punterosHijos[j] = null;
                        break;
                    }
                }
            }
        }
    }

    // Devuelve el número actual de claves no nulas en el nodo
    public int getClavesActuales() {
        int total = 0;
        for (String clave : claves) {
            if (clave != null) {
                total++;
            }
        }
        return total;
    }

    // Busca el primer índice nulo en el arreglo de punteros hijos
    private int busquedaLinealNula(Nodo[] punteros) {
        for (int i = 0; i < punteros.length; i++) {
            if (punteros[i] == null) {
                return i;
            }
        }
        return -1;
    }
}
