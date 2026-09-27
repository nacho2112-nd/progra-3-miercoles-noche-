package adivinaquien.datos;

import adivinaquien.modelo.ColorPelo;
import adivinaquien.modelo.Genero;
import adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Precondiciones del catálogo. Se chequean antes de armar el tablero: si alguna falla, el juego
 * no arranca, porque la estrategia de la máquina da por hecho que todas se cumplen.
 */
public final class ValidadorCatalogo {

    private ValidadorCatalogo() {
    }

    /** Devuelve la lista de errores encontrados; vacía si el catálogo es válido. O(n). */
    public static List<String> validar(List<Personaje> catalogo) {
        List<String> errores = new ArrayList<>();
        if (catalogo.size() != CatalogoPersonajes.CANTIDAD) {
            errores.add("Tiene que haber " + CatalogoPersonajes.CANTIDAD + " personajes y hay " + catalogo.size());
        }

        Set<String> nombres = new HashSet<>();
        Map<Integer, Personaje> perfiles = new HashMap<>();
        boolean empezaronHombres = false;

        for (Personaje p : catalogo) {
            if (!nombres.add(p.getClaveOrden())) {
                errores.add("Nombre repetido: " + p.getNombre());
            }
            // Dependencia lógica 1: pelado <=> sin color de pelo.
            if (p.isPelado() != (p.getColorPelo() == ColorPelo.NINGUNO)) {
                errores.add(p.getNombre() + ": un pelado no tiene color de pelo, y quien no es pelado tiene exactamente uno");
            }
            // Dependencia lógica 2: mujer => sin barba.
            if (p.getGenero() == Genero.MUJER && p.isBarba()) {
                errores.add(p.getNombre() + ": una mujer no puede tener barba");
            }
            // Características distinguibles: dos personajes con el mismo perfil no se pueden separar con ningún filtro.
            Personaje gemelo = perfiles.put(p.perfil(), p);
            if (gemelo != null) {
                errores.add(p.getNombre() + " y " + gemelo.getNombre() + " tienen exactamente las mismas características");
            }
            // Precondición de la consigna: el catálogo llega agrupado por género (mujeres, después hombres).
            if (p.getGenero() == Genero.HOMBRE) {
                empezaronHombres = true;
            } else if (empezaronHombres) {
                errores.add(p.getNombre() + " está fuera del grupo de su género");
            }
        }
        return errores;
    }
}
