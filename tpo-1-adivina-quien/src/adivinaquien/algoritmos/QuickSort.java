package adivinaquien.algoritmos;

import java.util.Comparator;
import java.util.List;

/**
 * QuickSort con pivote central (partición de Lomuto). Se implementó para compararlo con MergeSort,
 * no se usa para armar el tablero: su peor caso es O(n²) y no es estable.
 *
 * Recurrencia en el caso promedio T(n) = 2T(n/2) + O(n) => O(n log n);
 * en el peor caso (pivote siempre extremo) T(n) = T(n-1) + O(n) => O(n²).
 */
public class QuickSort implements AlgoritmoOrdenamiento {

    @Override
    public String getNombre() {
        return "QuickSort";
    }

    @Override
    public String getComplejidad() {
        return "O(n log n) prom. / O(n²) peor";
    }

    @Override
    public <T> void ordenar(List<T> lista, Comparator<? super T> comparador) {
        @SuppressWarnings("unchecked")
        T[] datos = (T[]) lista.toArray();
        ordenarRango(datos, 0, datos.length - 1, comparador);
        for (int i = 0; i < datos.length; i++) {
            lista.set(i, datos[i]);
        }
    }

    private <T> void ordenarRango(T[] datos, int inicio, int fin, Comparator<? super T> comparador) {
        if (inicio >= fin) {
            return;
        }
        int posPivote = particionar(datos, inicio, fin, comparador);
        ordenarRango(datos, inicio, posPivote - 1, comparador);
        ordenarRango(datos, posPivote + 1, fin, comparador);
    }

    private <T> int particionar(T[] datos, int inicio, int fin, Comparator<? super T> comparador) {
        intercambiar(datos, (inicio + fin) >>> 1, fin); // el pivote central pasa al final
        T pivote = datos[fin];
        int menores = inicio;
        for (int i = inicio; i < fin; i++) {
            if (comparador.compare(datos[i], pivote) < 0) {
                intercambiar(datos, i, menores++);
            }
        }
        intercambiar(datos, menores, fin);
        return menores;
    }

    private static <T> void intercambiar(T[] datos, int i, int j) {
        T tmp = datos[i];
        datos[i] = datos[j];
        datos[j] = tmp;
    }
}
