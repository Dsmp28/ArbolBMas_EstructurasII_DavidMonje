package org.example;

import java.io.*;
import java.util.*;

public class ArbolBMas {
    int m; //Grado del arbol
    NodoInterno raiz; //Nodo raiz del arbol
    NodoHoja primerHoja; //Primera hoja del arbol

    //Constructor
    public ArbolBMas(int m) {
        this.m = m;
        this.raiz = null;
    }

    public void insertar(String clave, Libro valor) {
        // Verificar si el árbol está vacío
        if (estaVacio()) {
            // Si está vacío, crear la primera hoja con el par clave-valor
            this.primerHoja = new NodoHoja(this.m, new ParDiccionario(clave, valor));
        } else {
            // Encontrar la hoja adecuada para insertar la nueva clave-valor
            NodoHoja hoja = (this.raiz == null) ? this.primerHoja : encontrarNodoHoja(clave);

            // Intentar insertar el nuevo par clave-valor en la hoja
            if (!hoja.insertar(new ParDiccionario(clave, valor))) {
                // Si la hoja está llena, agregar el nuevo par al final y ordenar
                hoja.diccionario[hoja.numPares] = new ParDiccionario(clave, valor);
                hoja.numPares++;
                ordenarDiccionario(hoja.diccionario);

                // Dividir la hoja en dos si está llena
                int puntoMedio = obtenerPuntoMedio();
                ParDiccionario[] mitadDiccionario = dividirDiccionario(hoja, puntoMedio);

                // Si la hoja no tiene padre, crear un nuevo nodo interno como padre
                if (hoja.padre == null) {
                    String[] clavesPadre = new String[this.m];
                    clavesPadre[0] = mitadDiccionario[0].clave;
                    NodoInterno padre = new NodoInterno(this.m, clavesPadre);
                    hoja.padre = padre;
                    padre.anadirPunteroHijo(hoja);
                } else {
                    // Actualizar las claves del padre con la nueva clave
                    String nuevaClavePadre = mitadDiccionario[0].clave;
                    hoja.padre.claves[hoja.padre.getPunteros() - 1] = nuevaClavePadre;
                    Arrays.sort(hoja.padre.claves, 0, hoja.padre.getPunteros());
                }

                // Crear una nueva hoja con la mitad del diccionario dividido
                NodoHoja nuevaHoja = new NodoHoja(this.m, mitadDiccionario, hoja.padre);
                int indicePuntero = hoja.padre.encontrarIndiceDePuntero(hoja) + 1;
                hoja.padre.insertarPunteroHijo(nuevaHoja, indicePuntero);

                // Actualizar los enlaces de los hermanos
                nuevaHoja.hermanoDerecho = hoja.hermanoDerecho;
                if (nuevaHoja.hermanoDerecho != null) {
                    nuevaHoja.hermanoDerecho.hermanoIzquierdo = nuevaHoja;
                }
                hoja.hermanoDerecho = nuevaHoja;
                nuevaHoja.hermanoIzquierdo = hoja;

                // Si la raíz es nula, establecer el nuevo padre como la raíz
                if (this.raiz == null) {
                    this.raiz = hoja.padre;
                } else {
                    // Manejar la actualización de la raíz si es necesario
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
        // Verificar si el árbol está vacío
        if (!estaVacio()) {
            // Encontrar la hoja que contiene la clave a eliminar
            NodoHoja hoja = (this.raiz == null) ? this.primerHoja : encontrarNodoHoja(clave);
            // Buscar el índice del par clave-valor en la hoja
            int indicePar = busquedaBinaria(hoja.diccionario, hoja.numPares, clave);

            if (indicePar >= 0) {
                // Eliminar el par clave-valor de la hoja y reordenar
                hoja.eliminar(indicePar);
                hoja.reordenar();

                // Obtener el nodo interno padre de la hoja
                NodoInterno nodoInterno = hoja.padre;

                // Si el nodo interno contiene la clave eliminada, actualizar la clave en el nodo interno
                if (nodoInterno != null && nodoInterno.contieneClave(clave)) {
                    int indiceClave = Arrays.asList(nodoInterno.claves).indexOf(clave);
                    nodoInterno.claves[indiceClave] = hoja.diccionario[0].clave;
                }

                // Si la raíz contiene la clave eliminada, actualizar la clave en la raíz
                if (this.raiz != null && Arrays.asList(this.raiz.claves).contains(clave) && nodoInterno != this.raiz) {
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
                    //Manejar deficiencia de los nodos internos si es necesario
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
        //Validar si esta vacio
        if (estaVacio()) {
            return null;
        }

        //Encontrar la hoja que contiene la clave
        NodoHoja hoja = (this.raiz == null) ? this.primerHoja : encontrarNodoHoja(clave);

        //Buscar el indice del par clave-valor en la hoja
        ParDiccionario[] dps = hoja.diccionario;
        int indice = busquedaBinaria(dps, hoja.numPares, clave);

        //Retornar el valor si se encuentra, de lo contrario, retornar nulo
        if (indice < 0) {
            return null;
        } else {
            return dps[indice].valor;
        }
    }

    //Busqueda lineal en las hojas para encontrar un libro por su nombre
    //Se crea una lista con todos los resultados
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

    //Actualiza los valores del nodo selecionado
    public void actualizar(String clave, Map<String, String> actualizaciones) {
        //Busca el libro usando la clave
        Libro libro = buscar(clave);

        //Si el libro no es nulo, actualiza los valores
        if (libro != null) {
            if (actualizaciones.containsKey("name")) libro.setNombre(actualizaciones.get("name"));
            if (actualizaciones.containsKey("author")) libro.setAutor(actualizaciones.get("author"));
            if (actualizaciones.containsKey("category")) libro.setCategoria(actualizaciones.get("category"));
            if (actualizaciones.containsKey("price")) libro.setPrecio(Double.parseDouble(actualizaciones.get("price")));
            if (actualizaciones.containsKey("quantity")) libro.setCantidad(Integer.parseInt(actualizaciones.get("quantity")));
        }
    }

    // Procesa un archivo CSV que contiene comandos como INSERT, DELETE, PATCH y SEARCH
    public void procesarCsv(String nombreArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            List<Libro> resultadosTotales = new ArrayList<>();
            String linea;
            // Leer cada línea del archivo CSV
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                // Procesar la línea según el comando (INSERT, DELETE, PATCH, SEARCH)

                if (linea.startsWith("INSERT")) {
                    // Parsear el JSON y crear un objeto Libro
                    String json = linea.substring(linea.indexOf("{"));
                    Libro libro = parsearLibroJson(json);
                    // Insertar el libro en el árbol B+
                    insertar(libro.getIsbn(), libro);
                } else if (linea.startsWith("DELETE")) { //Mismo proceso para todas las funciones
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
                    List<Libro> resultado = buscarPorNombre(datos.get("name"));
                    // Imprimir los resultados de la búsqueda
                    if (resultado != null) {
                        resultadosTotales.addAll(resultado);
                    }
                }
            }
            System.out.println("Procesamiento del archivo CSV completado con exito.\n");

            if (!resultadosTotales.isEmpty()) {
                System.out.println("Resultados de la busqueda:");
                for (Libro libro : resultadosTotales) {
                    System.out.println(libro);
                }
            } else {
                System.out.println("No se encontraron resultados.");
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo CSV: " + e.getMessage());
        }
    }

    private Libro parsearLibroJson(String json) {
        Map<String, String> datos = parsearJson(json);

        // Asegurarse de que todas las categorías estén presentes, si no, asignarlas como null
        String isbn = datos.getOrDefault("isbn", null);
        String nombre = datos.getOrDefault("name", null);
        String autor = datos.getOrDefault("author", null);
        String categoria = datos.getOrDefault("category", null);
        String precioStr = datos.getOrDefault("price", null);
        String cantidadStr = datos.getOrDefault("quantity", null);

        Double precio = precioStr != null ? Double.parseDouble(precioStr) : null;
        Integer cantidad = cantidadStr != null ? Integer.parseInt(cantidadStr) : null;

        return new Libro(isbn, nombre, autor, categoria, precio, cantidad);
    }


    // Parsear el JSON y crear un mapa de clave-valor
    private Map<String, String> parsearJson(String json) {
        // Remover las llaves y comillas externas
        json = json.substring(1, json.length() - 1);

        Map<String, String> mapa = new HashMap<>();
        StringBuilder claveBuilder = new StringBuilder();
        StringBuilder valorBuilder = new StringBuilder();
        boolean dentroDeValor = false;
        boolean esClave = true;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '\"') {
                dentroDeValor = !dentroDeValor;
            } else if (c == ':' && esClave) {
                esClave = false;
            } else if (c == ',' && !dentroDeValor) {
                // Agregar el par clave-valor al mapa
                mapa.put(claveBuilder.toString().trim(), valorBuilder.toString().trim());
                claveBuilder.setLength(0); // Reiniciar el StringBuilder de la clave
                valorBuilder.setLength(0); // Reiniciar el StringBuilder del valor
                esClave = true;
            } else {
                if (esClave) {
                    claveBuilder.append(c);
                } else {
                    valorBuilder.append(c);
                }
            }
        }

        // Agregar el último par clave-valor
        if (claveBuilder.length() > 0 && valorBuilder.length() > 0) {
            mapa.put(claveBuilder.toString().trim(), valorBuilder.toString().trim());
        }

        return mapa;
    }



    // Encontrar el nodo hoja que debe contener la clave
    private NodoHoja encontrarNodoHoja(String clave) {
        if (this.raiz == null) {
            return this.primerHoja;
        }

        String[] claves = this.raiz.claves;
        int i;

        // Recorrer las claves del nodo raíz para encontrar el puntero correcto
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

    // Encontrar el nodo hoja a partir de un nodo interno específico
    private NodoHoja encontrarNodoHoja(NodoInterno nodo, String clave) {
        String[] claves = nodo.claves;
        int i;

        // Recorrer las claves del nodo interno para encontrar el puntero correcto
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

    // Realiza una búsqueda binaria en un array de pares clave-valor
    private int busquedaBinaria(ParDiccionario[] dps, int numPares, String clave) {
        Comparator<ParDiccionario> c;
        c = Comparator.comparing(o -> o.clave);
        return Arrays.binarySearch(dps, 0, numPares, new ParDiccionario(clave, null), c);
    }

    // Verifica si el árbol está vacío
    private boolean estaVacio() {
        return primerHoja == null;
    }

    // Obtiene el punto medio para dividir nodos o diccionarios
    private int obtenerPuntoMedio() {
        return (int) Math.ceil((this.m + 1) / 2.0) - 1;
    }

    // Ordena el diccionario de pares clave-valor en una hoja
    private void ordenarDiccionario(ParDiccionario[] diccionario) {
        Arrays.sort(diccionario, (o1, o2) -> {
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
        });
    }

    // Divide el diccionario de una hoja en dos partes
    private ParDiccionario[] dividirDiccionario(NodoHoja hoja, int puntoMedio) {
        ParDiccionario[] diccionario = hoja.diccionario;
        ParDiccionario[] mitadDiccionario = new ParDiccionario[this.m];

        // Mover la segunda mitad del diccionario a un nuevo array
        for (int i = puntoMedio; i < diccionario.length; i++) {
            mitadDiccionario[i - puntoMedio] = diccionario[i];
            hoja.eliminar(i);
        }

        return mitadDiccionario;
    }

    // Divide un nodo interno en dos nodos internos
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

        // Si no hay padre, se crea una nueva raíz
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
            // Se añade la nueva clave al padre y se inserta el puntero al nuevo nodo
            padre.claves[padre.getPunteros() - 1] = nuevaClavePadre;
            Arrays.sort(padre.claves, 0, padre.getPunteros());

            int indicePuntero = padre.encontrarIndiceDePuntero(nodo) + 1;
            padre.insertarPunteroHijo(hermano, indicePuntero);
            hermano.padre = padre;
        }
    }

    // Divide un array de claves en dos partes
    private String[] dividirClaves(String[] claves, int puntoMedio) {
        String[] mitadClaves = new String[this.m];
        claves[puntoMedio] = null;

        // Mover la segunda mitad de las claves a un nuevo array
        for (int i = puntoMedio + 1; i < claves.length; i++) {
            mitadClaves[i - puntoMedio - 1] = claves[i];
            claves[i] = null;
        }

        return mitadClaves;
    }

    // Divide un array de punteros hijos en dos partes
    private Nodo[] dividirPunterosHijos(NodoInterno nodo, int puntoMedio) {
        Nodo[] punteros = nodo.punterosHijos;
        Nodo[] mitadPunteros = new Nodo[this.m + 1];

        // Mover la segunda mitad de los punteros hijos a un nuevo array
        for (int i = puntoMedio + 1; i < punteros.length; i++) {
            mitadPunteros[i - puntoMedio - 1] = punteros[i];
            nodo.eliminarPuntero(i);
        }

        return mitadPunteros;
    }

    // Encuentra el primer índice nulo en un array de punteros
    private int busquedaLinealNula(Nodo[] punteros) {
        for (int i = 0; i < punteros.length; i++) {
            if (punteros[i] == null) {
                return i;
            }
        }
        return -1;
    }

    // Maneja la deficiencia en una hoja
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
        }
    }


    // Redistribuir elementos desde el hermano izquierdo hacia una hoja deficiente
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

    // Redistribuir elementos desde el hermano derecho hacia una hoja deficiente
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

    // Fusionar dos hojas cuando una de ellas es deficiente
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

    // Maneja la deficiencia en un nodo interno
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


    // Redistribuir elementos desde el hermano izquierdo hacia un nodo interno deficiente
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

        //Sí se eliminó la última clave del hermano izquierdo, unir el puntero al hermano derecho
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
    }

    // Redistribuir elementos desde el hermano derecho hacia un nodo interno deficiente
    private void redistribuirDesdeDerechoInterno(NodoInterno nodo, NodoInterno hermanoDerecho) {
        int indicePadre = nodo.padre.encontrarIndiceDePuntero(nodo);
        nodo.claves[nodo.getPunteros()] = nodo.padre.claves[indicePadre];
        hermanoDerecho.punterosHijos[0].padre = nodo;
        nodo.punterosHijos[nodo.getPunteros() + 1] = hermanoDerecho.punterosHijos[0];
        nodo.reordenarClaves();
        nodo.reordenarPunteros();

        // Actualizar el nodo padre
        nodo.padre.claves[indicePadre] = hermanoDerecho.claves[0];
        nodo.padre.reordenarClaves();

        // Eliminar la clave y puntero en el hermano derecho
        hermanoDerecho.claves[0] = null;
        hermanoDerecho.punterosHijos[0] = null;
        hermanoDerecho.reordenarClaves();
        hermanoDerecho.reordenarPunteros();
    }

    // Fusionar dos nodos internos cuando uno de ellos es deficiente
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

    // Fusionar un nodo interno con su padre cuando no hay hermanos disponibles
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

    // Elimina el puntero de un nodo padre que apuntaba a un nodo hijo que se ha fusionado
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
        } else if (padre != raiz && padre.estaDeficiente() && padre.padre == null) {
            this.raiz = (NodoInterno) padre.punterosHijos[0];
            this.raiz.padre = null;
        }
    }

    // Muestra el árbol B+ en la consola
    public void mostrarArbol() {
        if (estaVacio()) {
            System.out.println("El árbol está vacío.");
        } else {
            mostrarNodo(raiz, 0);
        }
    }

    public boolean menu() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("");
            System.out.println("1. Importar archivo CSV ");
            System.out.println("2. Salir");
            System.out.print("Ingrese la opción deseada (Ej: 1): ");
            int opcion = scanner.nextInt();
            switch (opcion) {
                case 1:
                    System.out.println("");
                    System.out.print("Ingrese la direccion del archivo CSV: ");
                    String nombreArchivo = scanner.next();
                    procesarCsv(nombreArchivo);
                    return true;
                case 2:
                    return false;
                default:
                    System.out.println("Opción inválida. Inténtelo de nuevo.");
                    return true;
            }
        } catch (Exception e) {
            System.out.println("Ocurrio un error, por favor vuelva a intentarlo ");
            return true;
        }
    }

    // Muestra un nodo específico del árbol en la consola, con indentación para los niveles
    private void mostrarNodo(Nodo nodo, int nivel) {
        if (nodo instanceof NodoHoja hoja) {
            imprimirIndentacion(nivel);
            System.out.println("Hoja: " + Arrays.toString(hoja.diccionario));
        } else if (nodo instanceof NodoInterno interno) {
            imprimirIndentacion(nivel);
            System.out.println("Interno: " + Arrays.toString(interno.claves));
            for (int i = 0; i < interno.getPunteros(); i++) {
                if (interno.punterosHijos[i] != null) {
                    mostrarNodo(interno.punterosHijos[i], nivel + 1);
                }
            }
        } else if (this.raiz == null && this.primerHoja != null) {
            imprimirIndentacion(nivel);
            System.out.println("Hoja: " + Arrays.toString(this.primerHoja.diccionario));
        }
    }

    // Imprime espacios de indentación para la visualización del árbol en la consola
    private void imprimirIndentacion(int nivel) {
        for (int i = 0; i < nivel; i++) {
            System.out.print("    ");
        }
    }
}
