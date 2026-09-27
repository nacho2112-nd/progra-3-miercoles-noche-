package adivinaquien.motor;

import adivinaquien.algoritmos.MergeSort;
import adivinaquien.datos.CatalogoPersonajes;
import adivinaquien.datos.ValidadorCatalogo;
import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * La máquina arma el tablero: valida el catálogo, lo ordena con MergeSort por nombre y va
 * agregando los personajes a la lista, que les asigna el ID autoincremental.
 *
 * Costo: O(n) validar + Θ(n log n) ordenar + O(n) agregar = Θ(n log n).
 */
public final class OrganizadorTablero {

    private OrganizadorTablero() {
    }

    public static Tablero organizar() {
        return organizar(CatalogoPersonajes.crear(), null);
    }

    public static Tablero organizar(List<Personaje> catalogo, Consumer<String> traza) {
        List<String> errores = ValidadorCatalogo.validar(catalogo);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException("Catálogo inválido:\n" + String.join("\n", errores));
        }
        List<Personaje> ordenados = new ArrayList<>(catalogo);
        new MergeSort().ordenarConTraza(ordenados, Personaje.POR_NOMBRE, traza);

        Tablero tablero = new Tablero();
        for (Personaje p : ordenados) {
            tablero.agregar(p); // ID = 1, 2, 3, ... en el orden en que se agregan
            if (traza != null) {
                traza.accept("agregar " + p.getNombre() + " -> ID " + p.getId());
            }
        }
        return tablero;
    }
}
