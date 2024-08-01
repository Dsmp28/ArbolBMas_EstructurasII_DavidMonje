package org.example;

import java.io.*;
import java.util.*;

public class ArbolBMas {
    int m;
    NodoInterno raiz;
    NodoHoja primerHoja;

    public ArbolBMas(int m) {
        this.m = m;
        this.raiz = null;
    }

    public void insertar(String clave, Libro valor) {
        if (estaVacio()) {
            NodoHoja hoja = new NodoHoja(this.m, new ParDiccionario(clave, valor));
            this.primerHoja = hoja;
        } else {
            NodoHoja hoja = (this.raiz == null) ? this.primerHoja : encontrarNodoHoja(clave);
            if (!hoja.insertar(new ParDiccionario(clave, valor))) {
                hoja.diccionario[hoja.numPares] = new ParDiccionario(clave, valor);
                hoja.numPares++;
                ordenarDiccionario(hoja.diccionario);

                int puntoMedio = obtenerPuntoMedio();
                ParDiccionario[] mitadDiccionario = dividirDiccionario(hoja, puntoMedio);

                if (hoja.padre == null) {
                    String[] clavesPadre = new String[this.m];
                    clavesPadre[0] = mitadDiccionario[0].clave;
                    NodoInterno padre = new NodoInterno(this.m, clavesPadre);
                    hoja.padre = padre;
                    padre.anadirPunteroHijo(hoja);
                } else {
                    String nuevaClavePadre = mitadDiccionario[0].clave;
                    hoja.padre.claves[hoja.padre.grado - 1] = nuevaClavePadre;
                    Arrays.sort(hoja.padre.claves, 0, hoja.padre.grado);
                }

                NodoHoja nuevaHoja = new NodoHoja(this.m, mitadDiccionario, hoja.padre);
                int indicePuntero = hoja.padre.encontrarIndiceDePuntero(hoja) + 1;
                hoja.padre.insertarPunteroHijo(nuevaHoja, indicePuntero);

                nuevaHoja.hermanoDerecho = hoja.hermanoDerecho;
                if (nuevaHoja.hermanoDerecho != null) {
                    nuevaHoja.hermanoDerecho.hermanoIzquierdo = nuevaHoja;
                }
                hoja.hermanoDerecho = nuevaHoja;
                nuevaHoja.hermanoIzquierdo = hoja;

                if (this.raiz == null) {
                    this.raiz = hoja.padre;
                } else {
                    NodoInterno nodoInterno = hoja.padre;
                    while (nodoInterno != null) {
                        if (nodoInterno.estaLleno()) {
                            dividirNodoInterno(nodoInterno);
                        } else {
                            break;
                        }
                        nodoInterno = nodoInterno.padre;
                    }
                }
            }
        }
    }

    public void eliminar(String clave) {
        if (estaVacio()) {
            System.err.println("Eliminación Inválida: El árbol B+ está actualmente vacío.");
        } else {
            NodoHoja hoja = (this.raiz == null) ? this.primerHoja : encontrarNodoHoja(clave);
            int indicePar = busquedaBinaria(hoja.diccionario, hoja.numPares, clave);

            if (indicePar < 0) {
                System.err.println("Eliminación Inválida: No se pudo encontrar la clave.");
            } else {
                hoja.eliminar(indicePar);

                if (hoja.estaDeficiente()) {
                    manejarDeficiencia(hoja);
                } else if (this.raiz == null && this.primerHoja.numPares == 0) {
                    this.primerHoja = null;
                } else {
                    ordenarDiccionario(hoja.diccionario);
                }
            }
        }
    }

    public Libro buscar(String clave) {
        if (estaVacio()) {
            return null;
        }

        NodoHoja hoja = (this.raiz == null) ? this.primerHoja : encontrarNodoHoja(clave);
        ParDiccionario[] dps = hoja.diccionario;
        int indice = busquedaBinaria(dps, hoja.numPares, clave);

        if (indice < 0) {
            return null;
        } else {
            return dps[indice].valor;
        }
    }

    public void actualizar(String clave, Map<String, String> actualizaciones) {
        Libro libro = buscar(clave);
        if (libro != null) {
            if (actualizaciones.containsKey("nombre")) libro.setNombre(actualizaciones.get("nombre"));
            if (actualizaciones.containsKey("autor")) libro.setAutor(actualizaciones.get("autor"));
            if (actualizaciones.containsKey("categoria")) libro.setCategoria(actualizaciones.get("categoria"));
            if (actualizaciones.containsKey("precio")) libro.setPrecio(Double.parseDouble(actualizaciones.get("precio")));
            if (actualizaciones.containsKey("cantidad")) libro.setCantidad(Integer.parseInt(actualizaciones.get("cantidad")));
        }
    }

    public void procesarCsv(String nombreArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("INSERT")) {
                    String json = linea.substring(linea.indexOf("{"));
                    Libro libro = parsearLibroJson(json);
                    insertar(libro.getIsbn(), libro);
                } else if (linea.startsWith("DELETE")) {
                    String json = linea.substring(linea.indexOf("{"));
                    Map<String, String> datos = parsearJson(json);
                    eliminar(datos.get("isbn"));
                } else if (linea.startsWith("PATCH")) {
                    String json = linea.substring(linea.indexOf("{"));
                    Map<String, String> datos = parsearJson(json);
                    String isbn = datos.remove("isbn");
                    actualizar(isbn, datos);
                } else if (linea.startsWith("SEARCH")) {
                    String json = linea.substring(linea.indexOf("{"));
                    Map<String, String> datos = parsearJson(json);
                    Libro resultado = buscar(datos.get("isbn"));
                    if (resultado != null) {
                        System.out.println("Encontrado: " + resultado);
                    } else {
                        System.out.println("No Encontrado");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Libro parsearLibroJson(String json) {
        Map<String, String> datos = parsearJson(json);
        return new Libro(
                datos.get("isbn"),
                datos.get("nombre"),
                datos.get("autor"),
                datos.get("categoria"),
                Double.parseDouble(datos.get("precio")),
                Integer.parseInt(datos.get("cantidad"))
        );
    }

    private Map<String, String> parsearJson(String json) {
        json = json.replaceAll("[{}\"]", "");
        String[] pares = json.split(",");
        Map<String, String> mapa = new HashMap<>();
        for (String par : pares) {
            String[] claveValor = par.split(":");
            mapa.put(claveValor[0].trim(), claveValor[1].trim());
        }
        return mapa;
    }

    private NodoHoja encontrarNodoHoja(String clave) {
        if (this.raiz == null) {
            return this.primerHoja;
        }

        String[] claves = this.raiz.claves;
        int i;

        for (i = 0; i < this.raiz.grado - 1; i++) {
            if (clave.compareTo(claves[i]) < 0) {
                break;
            }
        }

        Nodo hijo = this.raiz.punterosHijos[i];
        if (hijo instanceof NodoHoja) {
            return (NodoHoja) hijo;
        } else {
            return encontrarNodoHoja((NodoInterno) hijo, clave);
        }
    }

    private NodoHoja encontrarNodoHoja(NodoInterno nodo, String clave) {
        String[] claves = nodo.claves;
        int i;

        for (i = 0; i < nodo.grado - 1; i++) {
            if (clave.compareTo(claves[i]) < 0) {
                break;
            }
        }

        Nodo hijo = nodo.punterosHijos[i];
        if (hijo instanceof NodoHoja) {
            return (NodoHoja) hijo;
        } else {
            return encontrarNodoHoja((NodoInterno) hijo, clave);
        }
    }

    private int busquedaBinaria(ParDiccionario[] dps, int numPares, String clave) {
        Comparator<ParDiccionario> c = new Comparator<ParDiccionario>() {
            @Override
            public int compare(ParDiccionario o1, ParDiccionario o2) {
                return o1.clave.compareTo(o2.clave);
            }
        };
        return Arrays.binarySearch(dps, 0, numPares, new ParDiccionario(clave, null), c);
    }

    private boolean estaVacio() {
        return primerHoja == null;
    }

    private int obtenerPuntoMedio() {
        return (int) Math.ceil((this.m + 1) / 2.0) - 1;
    }

    private void ordenarDiccionario(ParDiccionario[] diccionario) {
        Arrays.sort(diccionario, new Comparator<ParDiccionario>() {
            @Override
            public int compare(ParDiccionario o1, ParDiccionario o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2);
            }
        });
    }

    private ParDiccionario[] dividirDiccionario(NodoHoja hoja, int puntoMedio) {
        ParDiccionario[] diccionario = hoja.diccionario;
        ParDiccionario[] mitadDiccionario = new ParDiccionario[this.m];

        for (int i = puntoMedio; i < diccionario.length; i++) {
            mitadDiccionario[i - puntoMedio] = diccionario[i];
            hoja.eliminar(i);
        }

        return mitadDiccionario;
    }

    private void dividirNodoInterno(NodoInterno nodo) {
        NodoInterno padre = nodo.padre;

        int puntoMedio = obtenerPuntoMedio();
        String nuevaClavePadre = nodo.claves[puntoMedio];
        String[] mitadClaves = dividirClaves(nodo.claves, puntoMedio);
        Nodo[] mitadPunteros = dividirPunterosHijos(nodo, puntoMedio);

        nodo.grado = busquedaLinealNula(nodo.punterosHijos);

        NodoInterno hermano = new NodoInterno(this.m, mitadClaves, mitadPunteros);
        for (Nodo puntero : mitadPunteros) {
            if (puntero != null) {
                puntero.padre = hermano;
            }
        }

        hermano.hermanoDerecho = nodo.hermanoDerecho;
        if (hermano.hermanoDerecho != null) {
            hermano.hermanoDerecho.hermanoIzquierdo = hermano;
        }
        nodo.hermanoDerecho = hermano;
        hermano.hermanoIzquierdo = nodo;

        if (padre == null) {
            String[] claves = new String[this.m];
            claves[0] = nuevaClavePadre;
            NodoInterno nuevaRaiz = new NodoInterno(this.m, claves);
            nuevaRaiz.anadirPunteroHijo(nodo);
            nuevaRaiz.anadirPunteroHijo(hermano);
            this.raiz = nuevaRaiz;

            nodo.padre = nuevaRaiz;
            hermano.padre = nuevaRaiz;

        } else {
            padre.claves[padre.grado - 1] = nuevaClavePadre;
            Arrays.sort(padre.claves, 0, padre.grado);

            int indicePuntero = padre.encontrarIndiceDePuntero(nodo) + 1;
            padre.insertarPunteroHijo(hermano, indicePuntero);
            hermano.padre = padre;
        }
    }

    private String[] dividirClaves(String[] claves, int puntoMedio) {
        String[] mitadClaves = new String[this.m];
        claves[puntoMedio] = null;

        for (int i = puntoMedio + 1; i < claves.length; i++) {
            mitadClaves[i - puntoMedio - 1] = claves[i];
            claves[i] = null;
        }

        return mitadClaves;
    }

    private Nodo[] dividirPunterosHijos(NodoInterno nodo, int puntoMedio) {
        Nodo[] punteros = nodo.punterosHijos;
        Nodo[] mitadPunteros = new Nodo[this.m + 1];

        for (int i = puntoMedio + 1; i < punteros.length; i++) {
            mitadPunteros[i - puntoMedio - 1] = punteros[i];
            nodo.eliminarPuntero(i);
        }

        return mitadPunteros;
    }

    private int busquedaLinealNula(Nodo[] punteros) {
        for (int i = 0; i < punteros.length; i++) {
            if (punteros[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private void manejarDeficiencia(NodoHoja hoja) {
    }

    public void mostrarArbol() {
        if (estaVacio()) {
            System.out.println("El árbol está vacío.");
        } else {
            mostrarNodo(raiz, 0);
        }
    }

    private void mostrarNodo(Nodo nodo, int nivel) {
        if (nodo instanceof NodoHoja) {
            NodoHoja hoja = (NodoHoja) nodo;
            imprimirIndentacion(nivel);
            System.out.println("Hoja: " + Arrays.toString(hoja.diccionario));
        } else if (nodo instanceof NodoInterno) {
            NodoInterno interno = (NodoInterno) nodo;
            imprimirIndentacion(nivel);
            System.out.println("Interno: " + Arrays.toString(interno.claves));
            for (int i = 0; i < interno.grado; i++) {
                if (interno.punterosHijos[i] != null) {
                    mostrarNodo(interno.punterosHijos[i], nivel + 1);
                }
            }
        }
    }

    private void imprimirIndentacion(int nivel) {
        for (int i = 0; i < nivel; i++) {
            System.out.print("    ");
        }
    }
}
