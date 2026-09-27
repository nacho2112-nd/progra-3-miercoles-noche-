package adivinaquien.datos;

import adivinaquien.modelo.ColorPelo;
import adivinaquien.modelo.Genero;
import adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;

import static adivinaquien.modelo.ColorPelo.AMARILLO;
import static adivinaquien.modelo.ColorPelo.COLORADO;
import static adivinaquien.modelo.ColorPelo.NEGRO;
import static adivinaquien.modelo.ColorPelo.NINGUNO;
import static adivinaquien.modelo.Genero.HOMBRE;
import static adivinaquien.modelo.Genero.MUJER;

/**
 * Los 23 personajes, tal como los pide la consigna: agrupados únicamente por género (primero las
 * mujeres, después los hombres) y sin ningún otro orden adentro de cada grupo. Ordenarlos es
 * trabajo de la máquina.
 */
public final class CatalogoPersonajes {

    public static final int CANTIDAD = 23;

    private CatalogoPersonajes() {
    }

    /** Devuelve personajes nuevos en cada llamada, sin ID: cada partida arma su propio tablero. */
    public static List<Personaje> crear() {
        List<Personaje> lista = new ArrayList<>();
        //           nombre        género  pelado  color     lentes barba  sombrero
        lista.add(p("Sofía",      MUJER,  false,  NEGRO,    true,  false, false));
        lista.add(p("Carla",      MUJER,  false,  AMARILLO, false, false, true));
        lista.add(p("Lucía",      MUJER,  false,  COLORADO, true,  false, true));
        lista.add(p("Valentina",  MUJER,  false,  NEGRO,    false, false, false));
        lista.add(p("Ana",        MUJER,  false,  AMARILLO, true,  false, false));
        lista.add(p("Martina",    MUJER,  false,  COLORADO, false, false, false));
        lista.add(p("Paula",      MUJER,  false,  NEGRO,    true,  false, true));
        lista.add(p("Julieta",    MUJER,  false,  AMARILLO, false, false, false));
        lista.add(p("Florencia",  MUJER,  false,  COLORADO, true,  false, false));
        lista.add(p("Elena",      MUJER,  true,   NINGUNO,  true,  false, false));
        lista.add(p("Micaela",    MUJER,  false,  NEGRO,    false, false, true));
        lista.add(p("Tomás",      HOMBRE, false,  NEGRO,    false, true,  false));
        lista.add(p("Diego",      HOMBRE, false,  AMARILLO, true,  false, true));
        lista.add(p("Joaquín",    HOMBRE, true,   NINGUNO,  false, true,  false));
        lista.add(p("Bruno",      HOMBRE, false,  COLORADO, true,  true,  false));
        lista.add(p("Martín",     HOMBRE, false,  NEGRO,    true,  false, false));
        lista.add(p("Hernán",     HOMBRE, true,   NINGUNO,  true,  false, true));
        lista.add(p("Lucas",      HOMBRE, false,  AMARILLO, false, false, false));
        lista.add(p("Ramiro",     HOMBRE, false,  COLORADO, false, false, true));
        lista.add(p("Gustavo",    HOMBRE, true,   NINGUNO,  false, false, false));
        lista.add(p("Federico",   HOMBRE, false,  NEGRO,    false, true,  true));
        lista.add(p("Nicolás",    HOMBRE, false,  AMARILLO, true,  true,  false));
        lista.add(p("Sergio",     HOMBRE, false,  COLORADO, false, true,  false));
        return lista;
    }

    private static Personaje p(String nombre, Genero genero, boolean pelado, ColorPelo color,
                               boolean lentes, boolean barba, boolean sombrero) {
        return new Personaje(nombre, genero, pelado, color, lentes, barba, sombrero);
    }
}
