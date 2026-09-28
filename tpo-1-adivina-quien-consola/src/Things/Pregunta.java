package Things;

// Una pregunta de un turno: "¿categoria = valor?". Arriesgar tambien es una pregunta: "¿Es <nombre>?".
public record Pregunta(String categoria, String valor) implements Comparable<Pregunta> {

    public static Pregunta arriesgue(Personajes p) {
        return new Pregunta("nombre", p.get_Nombre());
    }

    public boolean esArriesgue() {
        return categoria.equals("nombre");
    }

    public boolean aplicaA(Personajes p) {
        return valor.equals(esArriesgue() ? p.get_Nombre() : p.get_Atributo(categoria));
    }

    @Override
    public int compareTo(Pregunta otra) {
        return toString().compareTo(otra.toString());
    }

    @Override
    public String toString() {
        return esArriesgue() ? "¿Es " + valor + "?" : "¿" + categoria + " = " + valor + "?";
    }
}
