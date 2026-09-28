package Things;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Personajes {
    private int id;
    private final String nombre;
    // categoria -> valor (ej: "pelo" -> "negro"). El genero es un atributo mas, asi se puede preguntar.
    private final Map<String, String> atributos = new LinkedHashMap<>();

    public Personajes(String nombre) {
        this.nombre = nombre;
    }

    public void agregar_Atributo(String categoria, String valor) {
        atributos.put(categoria, valor);
    }

    public String get_Atributo(String categoria) {
        return atributos.get(categoria);
    }

    public Map<String, String> get_Atributos() {
        return Collections.unmodifiableMap(atributos);
    }

    public int get_ID() {
        return id;
    }

    // Lo usa la maquina al armar el tablero ordenado (ID autoincremental)
    public void set_ID(int id) {
        this.id = id;
    }

    public String get_Nombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " " + atributos;
    }
}
