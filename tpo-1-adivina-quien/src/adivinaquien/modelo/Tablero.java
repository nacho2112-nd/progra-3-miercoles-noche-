package adivinaquien.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La lista ordenada de personajes que comparten los dos jugadores.
 *
 * Invariante: los personajes están ordenados por nombre y el personaje en la posición i tiene el
 * ID i + 1. El ID es autoincremental: se asigna al agregar, nunca antes.
 */
public class Tablero {

    private final List<Personaje> personajes = new ArrayList<>();
    private int contadorId = 0;

    /**
     * Agrega un personaje al final y le asigna el siguiente ID.
     * Precondición: el personaje no puede ir antes que el último (la lista ya tiene que llegar ordenada).
     */
    public void agregar(Personaje p) {
        if (!personajes.isEmpty()) {
            Personaje ultimo = personajes.get(personajes.size() - 1);
            if (Personaje.POR_NOMBRE.compare(ultimo, p) > 0) {
                throw new IllegalArgumentException(p + " rompe el orden del tablero (va antes que " + ultimo + ")");
            }
        }
        contadorId++;
        p.asignarId(contadorId);
        personajes.add(p);
    }

    /** Acceso directo por ID en O(1): el ID es la posición + 1. */
    public Personaje obtenerPorId(int id) {
        if (id < 1 || id > personajes.size()) {
            throw new IllegalArgumentException("No existe el personaje con ID " + id);
        }
        return personajes.get(id - 1);
    }

    public List<Personaje> getPersonajes() {
        return Collections.unmodifiableList(personajes);
    }

    public int tamanio() {
        return personajes.size();
    }
}
