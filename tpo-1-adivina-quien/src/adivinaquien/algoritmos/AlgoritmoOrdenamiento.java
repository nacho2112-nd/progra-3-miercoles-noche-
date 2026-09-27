package adivinaquien.algoritmos;

import java.util.Comparator;
import java.util.List;

/** Contrato común de los cuatro ordenamientos, para poder medirlos con el mismo código. */
public interface AlgoritmoOrdenamiento {

    String getNombre();

    /** Complejidad temporal en el peor caso, para mostrar en la tabla comparativa. */
    String getComplejidad();

    <T> void ordenar(List<T> lista, Comparator<? super T> comparador);
}
