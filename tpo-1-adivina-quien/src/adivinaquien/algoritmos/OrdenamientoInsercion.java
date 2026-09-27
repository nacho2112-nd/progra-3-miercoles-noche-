package adivinaquien.algoritmos;

import java.util.Comparator;
import java.util.List;

/** Inserción: el cuadrático de referencia. O(n²) peor caso, O(n) si la lista ya está ordenada. */
public class OrdenamientoInsercion implements AlgoritmoOrdenamiento {

    @Override
    public String getNombre() {
        return "Inserción";
    }

    @Override
    public String getComplejidad() {
        return "O(n²)";
    }

    @Override
    public <T> void ordenar(List<T> lista, Comparator<? super T> comparador) {
        for (int i = 1; i < lista.size(); i++) {
            T actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && comparador.compare(lista.get(j), actual) > 0) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
    }
}
