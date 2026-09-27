package adivinaquien.modelo;

/**
 * Los tres colores de pelo de la consigna. NINGUNO es exclusivo de los personajes pelados:
 * esa es una de las dependencias lógicas del juego (pelado implica que no hay color).
 */
public enum ColorPelo {
    COLORADO("Colorado"),
    NEGRO("Negro"),
    AMARILLO("Amarillo"),
    NINGUNO("Sin pelo");

    private final String etiqueta;

    ColorPelo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
