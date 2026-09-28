package Functional_Things;

import Things.Personajes;
import java.util.ArrayList;
import java.util.List;

// Los 23 personajes declarados, agrupados SOLO por genero (primero mujeres, despues hombres).
// Ordenarlos y darles ID es trabajo de la maquina: ver JugadorMaquina.armarTablero().
// Filtros de la consigna: genero, calvicie, lentes y pelo colorado/negro/amarillo. Con esos solos
// hay 16 perfiles y no alcanzan para 23 personajes distintos: por eso se suman barba y sombrero.
public class GeneradorPersonajes {

    public static List<Personajes> crear_Lista() {
        List<Personajes> l = new ArrayList<>();
        //      nombre       genero    pelo        lentes barba  sombrero
        l.add(p("Sofía",     "mujer",  "negro",    true,  false, false));
        l.add(p("Carla",     "mujer",  "amarillo", false, false, true));
        l.add(p("Lucía",     "mujer",  "colorado", true,  false, true));
        l.add(p("Valentina", "mujer",  "negro",    false, false, false));
        l.add(p("Ana",       "mujer",  "amarillo", true,  false, false));
        l.add(p("Martina",   "mujer",  "colorado", false, false, false));
        l.add(p("Paula",     "mujer",  "negro",    true,  false, true));
        l.add(p("Julieta",   "mujer",  "amarillo", false, false, false));
        l.add(p("Florencia", "mujer",  "colorado", true,  false, false));
        l.add(p("Elena",     "mujer",  "pelado",   true,  false, false));
        l.add(p("Micaela",   "mujer",  "negro",    false, false, true));
        l.add(p("Tomás",     "hombre", "negro",    false, true,  false));
        l.add(p("Diego",     "hombre", "amarillo", true,  false, true));
        l.add(p("Joaquín",   "hombre", "pelado",   false, true,  false));
        l.add(p("Bruno",     "hombre", "colorado", true,  true,  false));
        l.add(p("Martín",    "hombre", "negro",    true,  false, false));
        l.add(p("Hernán",    "hombre", "pelado",   true,  false, true));
        l.add(p("Lucas",     "hombre", "amarillo", false, false, false));
        l.add(p("Ramiro",    "hombre", "colorado", false, false, true));
        l.add(p("Gustavo",   "hombre", "pelado",   false, false, false));
        l.add(p("Federico",  "hombre", "negro",    false, true,  true));
        l.add(p("Nicolás",   "hombre", "amarillo", true,  true,  false));
        l.add(p("Sergio",    "hombre", "colorado", false, true,  false));
        return l;
    }

    private static Personajes p(String nombre, String genero, String pelo, boolean lentes, boolean barba, boolean sombrero) {
        Personajes p = new Personajes(nombre);
        p.agregar_Atributo("genero", genero);
        p.agregar_Atributo("pelo", pelo);
        if (lentes) p.agregar_Atributo("lentes", "sí");
        if (barba) p.agregar_Atributo("barba", "sí");
        if (sombrero) p.agregar_Atributo("sombrero", "sí");
        return p;
    }
}
