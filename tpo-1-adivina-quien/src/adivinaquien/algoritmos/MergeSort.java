package adivinaquien.algoritmos;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * MergeSort: el ordenamiento elegido para armar el tablero (Divide y Conquista).
 *
 * - Dividir: partir el rango por la mitad.
 * - Conquistar: ordenar cada mitad recursivamente (caso base: 0 o 1 elemento).
 * - Combinar: intercalar las dos mitades ordenadas en O(n).
 *
 * Recurrencia T(n) = 2T(n/2) + O(n)  =>  Θ(n log n) en todos los casos (teorema maestro, caso 2).
 * Es estable y usa O(n) de memoria auxiliar.
 */
public class MergeSort implements AlgoritmoOrdenamiento {

    @Override
    public String getNombre() {
        return "MergeSort";
    }

    @Override
    public String getComplejidad() {
        return "Θ(n log n)";
    }

    @Override
    public <T> void ordenar(List<T> lista, Comparator<? super T> comparador) {
        ordenarConTraza(lista, comparador, null);
    }

    /** Igual que ordenar, pero informa cada división y cada combinación (para verlo en pantalla). */
    public <T> void ordenarConTraza(List<T> lista, Comparator<? super T> comparador, Consumer<String> traza) {
        @SuppressWarnings("unchecked")
        T[] datos = (T[]) lista.toArray();
        @SuppressWarnings("unchecked")
        T[] auxiliar = (T[]) new Object[datos.length];
        ordenarRango(datos, auxiliar, 0, datos.length - 1, comparador, traza, 0);
        for (int i = 0; i < datos.length; i++) {
            lista.set(i, datos[i]);
        }
    }

    private <T> void ordenarRango(T[] datos, T[] auxiliar, int inicio, int fin,
                                  Comparator<? super T> comparador, Consumer<String> traza, int nivel) {
        if (inicio >= fin) {
            return; // caso base: 0 o 1 elemento ya está ordenado
        }
        int medio = (inicio + fin) >>> 1;
        if (traza != null) {
            traza.accept(sangria(nivel) + "dividir [" + inicio + ".." + fin + "] en ["
                    + inicio + ".." + medio + "] y [" + (medio + 1) + ".." + fin + "]");
        }
        ordenarRango(datos, auxiliar, inicio, medio, comparador, traza, nivel + 1);
        ordenarRango(datos, auxiliar, medio + 1, fin, comparador, traza, nivel + 1);
        combinar(datos, auxiliar, inicio, medio, fin, comparador);
        if (traza != null) {
            traza.accept(sangria(nivel) + "combinar [" + inicio + ".." + fin + "] -> "
                    + Arrays.toString(Arrays.copyOfRange(datos, inicio, fin + 1)));
        }
    }

    /** Intercala datos[inicio..medio] y datos[medio+1..fin], que ya están ordenados. O(fin - inicio). */
    private <T> void combinar(T[] datos, T[] auxiliar, int inicio, int medio, int fin,
                              Comparator<? super T> comparador) {
        System.arraycopy(datos, inicio, auxiliar, inicio, fin - inicio + 1);
        int izq = inicio;
        int der = medio + 1;
        for (int k = inicio; k <= fin; k++) {
            if (izq > medio) {
                datos[k] = auxiliar[der++];
            } else if (der > fin) {
                datos[k] = auxiliar[izq++];
            } else if (comparador.compare(auxiliar[der], auxiliar[izq]) < 0) {
                datos[k] = auxiliar[der++]; // sólo si es estrictamente menor: así es estable
            } else {
                datos[k] = auxiliar[izq++];
            }
        }
    }

    private static String sangria(int nivel) {
        return "  ".repeat(nivel);
    }
}
