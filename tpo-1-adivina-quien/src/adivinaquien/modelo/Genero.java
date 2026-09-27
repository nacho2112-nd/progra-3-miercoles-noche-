package adivinaquien.modelo;

/** Género del personaje. Es el único criterio por el que viene agrupado el catálogo inicial. */
public enum Genero {
    MUJER("Mujer"),
    HOMBRE("Hombre");

    private final String etiqueta;

    Genero(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
