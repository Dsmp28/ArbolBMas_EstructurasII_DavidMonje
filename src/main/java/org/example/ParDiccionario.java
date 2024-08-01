package org.example;

public class ParDiccionario implements Comparable<ParDiccionario> {
    String clave;
    Libro valor;

    public ParDiccionario(String clave, Libro valor) {
        this.clave = clave;
        this.valor = valor;
    }

    @Override
    public int compareTo(ParDiccionario o) {
        return this.clave.compareTo(o.clave);
    }

    @Override
    public String toString() {
        return clave;
    }
}
