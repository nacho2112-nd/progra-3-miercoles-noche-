package adivinaquien.modelo;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;

/**
 * Un personaje del tablero. Sus atributos son inmutables; el único dato que cambia es el ID,
 * que no lo decide el catálogo sino el Tablero al agregarlo (autoincremental).
 */
public class Personaje {

    /** Criterio de ordenamiento del tablero: alfabético por nombre, sin distinguir tildes ni mayúsculas. */
    public static final Comparator<Personaje> POR_NOMBRE = Comparator.comparing(Personaje::getClaveOrden);

    private final String nombre;
    private final String claveOrden;
    private final Genero genero;
    private final boolean pelado;
    private final ColorPelo colorPelo;
    private final boolean lentes;
    private final boolean barba;
    private final boolean sombrero;
    private int id; // 0 = todavía no está en un tablero

    public Personaje(String nombre, Genero genero, boolean pelado, ColorPelo colorPelo,
                     boolean lentes, boolean barba, boolean sombrero) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El personaje necesita un nombre");
        }
        this.nombre = nombre.trim();
        this.claveOrden = normalizar(this.nombre);
        this.genero = genero;
        this.pelado = pelado;
        this.colorPelo = colorPelo;
        this.lentes = lentes;
        this.barba = barba;
        this.sombrero = sombrero;
    }

    /** Minúsculas y sin tildes: "Lucía" y "lucia" dan la misma clave. Es la clave de ordenamiento. */
    public static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }

    /** Lo usa sólo el Tablero: el ID se asigna una vez, al agregar el personaje a la lista ordenada. */
    void asignarId(int id) {
        if (this.id != 0) {
            throw new IllegalStateException(nombre + " ya tiene el ID " + this.id);
        }
        this.id = id;
    }

    /** Los 8 filtros como una máscara de bits: dos personajes con la misma máscara serían indistinguibles. */
    public int perfil() {
        int mascara = 0;
        for (Filtro f : Filtro.values()) {
            if (f.evaluar(this)) {
                mascara |= 1 << f.ordinal();
            }
        }
        return mascara;
    }

    public String descripcion() {
        StringBuilder sb = new StringBuilder(genero.getEtiqueta());
        sb.append(pelado ? ", pelado" : ", pelo " + colorPelo.getEtiqueta().toLowerCase(Locale.ROOT));
        if (lentes) sb.append(", lentes");
        if (barba) sb.append(", barba");
        if (sombrero) sb.append(", sombrero");
        return sb.toString();
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getClaveOrden() { return claveOrden; }
    public Genero getGenero() { return genero; }
    public boolean isPelado() { return pelado; }
    public ColorPelo getColorPelo() { return colorPelo; }
    public boolean isLentes() { return lentes; }
    public boolean isBarba() { return barba; }
    public boolean isSombrero() { return sombrero; }

    @Override
    public String toString() {
        return nombre;
    }
}
