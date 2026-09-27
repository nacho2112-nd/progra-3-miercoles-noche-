package adivinaquien.algoritmos;

import adivinaquien.modelo.Personaje;

import java.util.List;

/**
 * Búsqueda binaria recursiva (Divide y Conquista) sobre el tablero ordenado por nombre.
 * La usa el arriesgue: "¿Es Lucía?" se resuelve en O(log n) en vez de recorrer los 23.
 *
 * Recurrencia T(n) = T(n/2) + O(1)  =>  O(log n).
 */
public final class BusquedaBinaria {

    private BusquedaBinaria() {
    }

    /** Precondición: la lista está ordenada con Personaje.POR_NOMBRE. Devuelve el índice o -1. */
    public static int buscarPorNombre(List<Personaje> ordenada, String nombre) {
        return buscar(ordenada, Personaje.normalizar(nombre), 0, ordenada.size() - 1);
    }

    private static int buscar(List<Personaje> ordenada, String clave, int inicio, int fin) {
        if (inicio > fin) {
            return -1; // rango vacío: no está
        }
        int medio = (inicio + fin) >>> 1;
        int comparacion = clave.compareTo(ordenada.get(medio).getClaveOrden());
        if (comparacion == 0) {
            return medio;
        }
        return comparacion < 0
                ? buscar(ordenada, clave, inicio, medio - 1)
                : buscar(ordenada, clave, medio + 1, fin);
    }
}
