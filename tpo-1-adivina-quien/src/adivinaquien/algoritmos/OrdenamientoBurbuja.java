package adivinaquien.algoritmos;

import java.util.Comparator;
import java.util.List;

/** Burbujeo con corte temprano: si una pasada no intercambia nada, la lista ya está ordenada. O(n²). */
public class OrdenamientoBurbuja implements AlgoritmoOrdenamiento {

    @Override
    public String getNombre() {
        return "Burbujeo";
    }

    @Override
    public String getComplejidad() {
        return "O(n²)";
    }

    @Override
    public <T> void ordenar(List<T> lista, Comparator<? super T> comparador) {
        int n = lista.size();
        boolean huboIntercambio = true;
        for (int pasada = 0; pasada < n - 1 && huboIntercambio; pasada++) {
            huboIntercambio = false;
            for (int j = 0; j < n - 1 - pasada; j++) {
                if (comparador.compare(lista.get(j), lista.get(j + 1)) > 0) {
                    T tmp = lista.get(j);
                    lista.set(j, lista.get(j + 1));
                    lista.set(j + 1, tmp);
                    huboIntercambio = true;
                }
            }
        }
    }
}
