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
                    hoja.padre.claves[hoja.padre.getPunteros() - 1] = nuevaClavePadre;
                    Arrays.sort(hoja.padre.claves, 0, hoja.padre.getPunteros());
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
                hoja.reordenar();

                NodoInterno nodoInterno = hoja.padre;
                if (nodoInterno != null && nodoInterno.contieneClave(clave)) {
                    int indiceClave = Arrays.asList(nodoInterno.claves).indexOf(clave);
                    nodoInterno.claves[indiceClave] = hoja.diccionario[0].clave;
                }

                // Validación para ajustar la raíz si la clave eliminada era una clave de la raíz
                if (this.raiz != null && Arrays.asList(this.raiz.claves).contains(clave)) {
                    int indiceRaiz = Arrays.asList(this.raiz.claves).indexOf(clave);

                    // Buscar el valor más pequeño en el subárbol derecho
                    Nodo nodoDerecho = this.raiz.punterosHijos[indiceRaiz + 1];
                    while (nodoDerecho instanceof NodoInterno) {
                        nodoDerecho = ((NodoInterno) nodoDerecho).punterosHijos[0];
                    }
                    NodoHoja nodoMinimoDerecho = (NodoHoja) nodoDerecho;
                    String nuevoValorRaiz = nodoMinimoDerecho.diccionario[0].clave;

                    // Reemplazar el valor de la raíz con el valor más pequeño del subárbol derecho
                    this.raiz.claves[indiceRaiz] = nuevoValorRaiz;
                }

                // Manejar deficiencia si es necesario
                if (hoja.estaDeficiente()) {
                    manejarDeficiencia(hoja);
                } else if (this.raiz == null && this.primerHoja.numPares == 0) {
                    this.primerHoja = null;
                } else {
                    hoja.reordenar();
                    ordenarDiccionario(hoja.diccionario);
                    if (hoja.padre != null) {
                        while (nodoInterno != null) {
                            if (nodoInterno.estaDeficiente()) {
                                manejarDeficiencia(nodoInterno);
                            } else {
                                break;
                            }
                            nodoInterno = nodoInterno.padre;
                        }
                    }
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
    public List<Libro> buscarPorNombre(String nombre) {
        List<Libro> resultados = new ArrayList<>();
        NodoHoja hojaActual = this.primerHoja;

        while (hojaActual != null) {
            for (int i = 0; i < hojaActual.numPares; i++) {
                if (hojaActual.diccionario[i] != null && hojaActual.diccionario[i].valor.getNombre().equalsIgnoreCase(nombre)) {
                    resultados.add(hojaActual.diccionario[i].valor);
                }
            }
            hojaActual = hojaActual.hermanoDerecho;
        }

        return resultados;
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
                    List<Libro> resultado = buscarPorNombre(datos.get("nombre"));
                    if (resultado != null) {
                        System.out.println();
                        System.out.println("Encontrado: ");
                        for (Libro libro : resultado) {
                            System.out.println(libro.nombre);
                        }
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

        for (i = 0; i < this.raiz.getPunteros() - 1; i++) {
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

        for (i = 0; i < nodo.getPunteros() - 1; i++) {
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
            padre.claves[padre.getPunteros() - 1] = nuevaClavePadre;
            Arrays.sort(padre.claves, 0, padre.getPunteros());

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
        NodoHoja hermanoIzquierdo = hoja.hermanoIzquierdo;
        NodoHoja hermanoDerecho = hoja.hermanoDerecho;

        // Intentar redistribuir con el hermano izquierdo
        if (hermanoIzquierdo != null && hermanoIzquierdo.padre == hoja.padre && hermanoIzquierdo.numPares > obtenerPuntoMedio()) {
            redistribuirDesdeIzquierdo(hoja, hermanoIzquierdo);
        }
        // Intentar redistribuir con el hermano derecho
        else if (hermanoDerecho != null && hermanoDerecho.padre == hoja.padre && hermanoDerecho.numPares > obtenerPuntoMedio()) {
            redistribuirDesdeDerecho(hoja, hermanoDerecho);
        }
        // Si no es posible redistribuir, realizar fusión con el hermano izquierdo o derecho
        else if (hermanoIzquierdo != null && hermanoIzquierdo.padre == hoja.padre) {
            fusionarHojas(hoja, hermanoIzquierdo);
        }
        else if (hermanoDerecho != null && hermanoDerecho.padre == hoja.padre) {
            fusionarHojas(hermanoDerecho, hoja);
        }
        // Si la hoja es la raíz y está vacía, ajustar la raíz
        else if (hoja.padre == null && hoja.numPares == 0) {
            if (hoja.hermanoDerecho != null) {
                this.raiz = encontrarNodoHoja(hoja.hermanoDerecho.diccionario[0].clave).padre;
                this.primerHoja = hoja.hermanoDerecho;
                eliminarPunteroDePadre(hoja);
            } else if (hoja.hermanoIzquierdo != null) {
                this.raiz = encontrarNodoHoja(hoja.hermanoIzquierdo.diccionario[0].clave).padre;
                this.primerHoja = hoja.hermanoIzquierdo;
                eliminarPunteroDePadre(hoja);
            } else {
                this.raiz = null;
                this.primerHoja = null;
            }
        } else {
            // Ajustar la raíz si la hoja deficiente contiene la clave mínima del subárbol derecho
            if (this.raiz != null && Arrays.asList(this.raiz.claves).contains(hoja.diccionario[0].clave)) {
                int indiceRaiz = Arrays.asList(this.raiz.claves).indexOf(hoja.diccionario[0].clave);
                this.raiz.claves[indiceRaiz] = hoja.diccionario[0].clave;
            }
        }
    }



    private void redistribuirDesdeIzquierdo(NodoHoja hoja, NodoHoja hermanoIzquierdo) {
        // Mover la última clave del hermano izquierdo al nodo hoja deficiente
        for (int i = hoja.numPares - 1; i >= 0; i--) {
            hoja.diccionario[i + 1] = hoja.diccionario[i];
        }
        hoja.diccionario[0] = hermanoIzquierdo.diccionario[hermanoIzquierdo.numPares - 1];
        hoja.numPares++;
        hermanoIzquierdo.eliminar(hermanoIzquierdo.numPares - 1);

        // Actualizar clave del padre
        int indiceEnPadre = hoja.padre.encontrarIndiceDePuntero(hoja);
        hoja.padre.claves[indiceEnPadre - 1] = hoja.diccionario[0].clave;
    }

    private void redistribuirDesdeDerecho(NodoHoja hoja, NodoHoja hermanoDerecho) {
        // Mover la primera clave del hermano derecho al nodo hoja deficiente
        hoja.diccionario[hoja.numPares] = hermanoDerecho.diccionario[0];
        hoja.numPares++;
        hermanoDerecho.eliminar(0);

        // Reordenar el diccionario del hermano derecho
        hermanoDerecho.reordenar();

        // Actualizar clave del padre
        int indiceEnPadre = hoja.padre.encontrarIndiceDePuntero(hermanoDerecho);
        hoja.padre.claves[indiceEnPadre - 1] = hermanoDerecho.diccionario[0].clave;
    }

    private void fusionarHojas(NodoHoja hoja, NodoHoja hermano) {
        // Fusionar todas las claves del nodo deficiente en el nodo hermano
        for (int i = 0; i < hoja.numPares; i++) {
            hermano.diccionario[hermano.numPares] = hoja.diccionario[i];
            hermano.numPares++;
        }

        // Actualizar los enlaces de los hermanos
        hermano.hermanoDerecho = hoja.hermanoDerecho;
        if (hermano.hermanoDerecho != null) {
            hermano.hermanoDerecho.hermanoIzquierdo = hermano;
        }

        // Eliminar la referencia al nodo deficiente en el padre
        eliminarPunteroDePadre(hoja);

        // Si la hoja es la raíz y está vacía, ajustar la raíz
        if (hoja.padre == null && hoja.numPares == 0) {
            this.primerHoja = hermano;
            this.raiz = hermano.padre;
        } else {
            // Ajustar la raíz si la hoja fusionada contenía la clave mínima del subárbol derecho
            if (this.raiz != null && Arrays.asList(this.raiz.claves).contains(hoja.diccionario[0].clave)) {
                int indiceRaiz = Arrays.asList(this.raiz.claves).indexOf(hoja.diccionario[0].clave);
                this.raiz.claves[indiceRaiz] = hoja.diccionario[0].clave;
            }
        }
    }



    private void manejarDeficiencia(NodoInterno nodo) {
        NodoInterno hermanoIzquierdo = nodo.hermanoIzquierdo;
        NodoInterno hermanoDerecho = nodo.hermanoDerecho;
        NodoInterno padre = nodo.padre;

        // Intentar redistribuir con el hermano izquierdo
        if (hermanoIzquierdo != null && hermanoIzquierdo.padre == nodo.padre && hermanoIzquierdo.esPrestable()) {
            redistribuirDesdeIzquierdoInterno(nodo, hermanoIzquierdo);
        }
        // Intentar redistribuir con el hermano derecho
        else if (hermanoDerecho != null && hermanoDerecho.padre == nodo.padre && hermanoDerecho.esPrestable()) {
            redistribuirDesdeDerechoInterno(nodo, hermanoDerecho);
        }
        // Si no es posible redistribuir, realizar fusión con el hermano izquierdo o derecho
        else if (hermanoIzquierdo != null && hermanoIzquierdo.padre == nodo.padre) {
            fusionarNodosInternos(nodo, hermanoIzquierdo);
        }
        else if (hermanoDerecho != null && hermanoDerecho.padre == nodo.padre) {
            fusionarNodosInternos(hermanoDerecho, nodo);
        }
        // Si no es posible fusionar con hermanos, considerar fusión con el nodo padre
        else if (padre != null) {
            fusionarConPadre(nodo);
        }
        // Ajustar la raíz si el nodo deficiente contenía la clave mínima del subárbol derecho
        if (this.raiz != null && Arrays.asList(this.raiz.claves).contains(nodo.claves[0])) {
            int indiceRaiz = Arrays.asList(this.raiz.claves).indexOf(nodo.claves[0]);
            this.raiz.claves[indiceRaiz] = nodo.claves[0];
        }
    }



    private void redistribuirDesdeIzquierdoInterno(NodoInterno nodo, NodoInterno hermanoIzquierdo) {
        // Mover la clave más grande del hermano izquierdo al nodo
        for (int i = nodo.getPunteros() - 1; i >= 0; i--) {
            nodo.claves[i + 1] = nodo.claves[i];
            nodo.punterosHijos[i + 1] = nodo.punterosHijos[i];
        }

        int indicePadre = nodo.padre.encontrarIndiceDePuntero(nodo) - 1;
        nodo.claves[0] = nodo.padre.claves[indicePadre];
        nodo.punterosHijos[0] = hermanoIzquierdo.punterosHijos[hermanoIzquierdo.getPunteros() - 1];
        nodo.reordenarClaves();
        nodo.reordenarPunteros();
        nodo.punterosHijos[0].padre = nodo;

        // Actualizar el nodo padre
        nodo.padre.claves[indicePadre] = hermanoIzquierdo.claves[hermanoIzquierdo.getClavesActuales() - 1];

        // Eliminar la clave y puntero en el hermano izquierdo
        hermanoIzquierdo.claves[hermanoIzquierdo.getClavesActuales() - 1] = null;
        hermanoIzquierdo.punterosHijos[hermanoIzquierdo.getPunteros() - 1] = null;
        hermanoIzquierdo.reordenarClaves();

        // Si se eliminó la última clave del hermano izquierdo, unir el puntero al hermano derecho
        if (hermanoIzquierdo.getClavesActuales() == 0) {
            NodoInterno hermanoDerecho = nodo.hermanoDerecho;
            if (hermanoDerecho != null) {
                for (int i = hermanoDerecho.getPunteros(); i > 0; i--) {
                    hermanoDerecho.punterosHijos[i] = hermanoDerecho.punterosHijos[i - 1];
                    hermanoDerecho.claves[i] = hermanoDerecho.claves[i - 1];
                }
                hermanoDerecho.punterosHijos[0] = hermanoIzquierdo.punterosHijos[hermanoIzquierdo.getPunteros()];
            }
        }

        // Si el nodo padre ya no necesita la clave que se movió al hijo, manejar deficiencia
        if (hermanoIzquierdo.getClavesActuales() < hermanoIzquierdo.minGrado) {
            manejarDeficiencia(hermanoIzquierdo);
        }
    }




    private void redistribuirDesdeDerechoInterno(NodoInterno nodo, NodoInterno hermanoDerecho) {
        int indicePadre = nodo.padre.encontrarIndiceDePuntero(nodo);
        nodo.claves[nodo.getPunteros()] = nodo.padre.claves[indicePadre];
        nodo.punterosHijos[nodo.getPunteros() + 1] = hermanoDerecho.punterosHijos[0];

        // Actualizar el nodo padre
        nodo.padre.claves[indicePadre] = hermanoDerecho.claves[0];

        // Eliminar la clave y puntero en el hermano derecho
        hermanoDerecho.eliminarPuntero(0);
        hermanoDerecho.reordenarClaves();
        hermanoDerecho.reordenarPunteros();

        // Si la clave en el nodo padre es la misma que la que se movió, eliminarla
        if (hermanoDerecho.getClavesActuales() < hermanoDerecho.minGrado) {
            manejarDeficiencia(hermanoDerecho);
        }
    }



    private void fusionarNodosInternos(NodoInterno nodo, NodoInterno hermano) {
        int indicePadre = nodo.padre.encontrarIndiceDePuntero(nodo);

        // Mover la clave del padre al hermano para fusionar
        hermano.claves[hermano.getPunteros()] = nodo.padre.claves[indicePadre - 1];
        hermano.grado++;
        nodo.padre.claves[indicePadre - 1] = null;
        hermano.reordenarClaves();
        nodo.padre.reordenarClaves();

        // Mover las claves y punteros del nodo al hermano
        for (int i = 0; i < nodo.getPunteros(); i++) {
            hermano.claves[hermano.getPunteros()] = nodo.claves[i];
            nodo.punterosHijos[i].padre = hermano;
            hermano.punterosHijos[hermano.getPunteros()] = nodo.punterosHijos[i];
            hermano.grado++;
        }
        hermano.reordenarClaves();
        hermano.reordenarPunteros();
        hermano.punterosHijos[hermano.getPunteros()] = nodo.punterosHijos[nodo.getPunteros()];

        // Actualizar los enlaces de los hermanos
        hermano.hermanoDerecho = nodo.hermanoDerecho;
        if (hermano.hermanoDerecho != null) {
            hermano.hermanoDerecho.hermanoIzquierdo = hermano;
        }

        // Eliminar la referencia al nodo en el padre
        nodo.padre.eliminarPuntero(indicePadre);
        nodo.padre.reordenarClaves();
        nodo.padre.reordenarPunteros();

        // Manejar deficiencia en el padre si es necesario
        if (nodo.padre.estaDeficiente() && nodo.padre != this.raiz) {
            if (nodo.padre.padre == null && nodo.padre.getPunteros() == 1) {
                this.raiz = (NodoInterno) nodo.padre.punterosHijos[0];
                this.raiz.padre = null;
            } else {
                manejarDeficiencia(nodo.padre);
            }
        }
    }


    private void fusionarConPadre(NodoInterno nodo) {
        NodoInterno padre = nodo.padre;
        int indicePuntero = padre.encontrarIndiceDePuntero(nodo);

        // Si no hay hermanos disponibles, fusionar con el padre
        for (int i = 0; i < nodo.getPunteros(); i++) {
            padre.claves[i + indicePuntero - 1] = nodo.claves[i];
            padre.punterosHijos[i + indicePuntero] = nodo.punterosHijos[i];
        }

        padre.eliminarPuntero(indicePuntero);
        padre.reordenarClaves();
        padre.reordenarPunteros();

        // Verificar si el padre necesita más fusiones o redistribuciones
        if (padre.estaDeficiente()) {
            manejarDeficiencia(padre);
        }
    }

    private void eliminarPunteroDePadre(Nodo nodo) {
        NodoInterno padre = nodo.padre;
        int indicePuntero = padre.encontrarIndiceDePuntero(nodo);

        // Eliminar el puntero en el índice correspondiente
        padre.eliminarPuntero(indicePuntero);
        padre.reordenarPunteros();// Reordenar las claves después de la eliminación

        // Verificar si la clave en el padre corresponde al valor eliminado del nodo hijo
        if (indicePuntero > 0 && nodo instanceof NodoHoja) {
            String claveEliminada = ((NodoHoja) nodo).diccionario[0].clave;
            if (padre.claves[indicePuntero - 1].equals(claveEliminada)) {
                padre.claves[indicePuntero - 1] = null;
                padre.reordenarClaves();
            }
        }

        // Manejar deficiencia en el nodo padre si es necesario
        if (padre.estaDeficiente() && padre.padre != null) {
            manejarDeficiencia(padre);
        } else if (padre.estaDeficiente() && padre.padre == null) {
            this.raiz = (NodoInterno) padre.punterosHijos[0];
            this.raiz.padre = null;
        }
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
            for (int i = 0; i < interno.getPunteros(); i++) {
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
