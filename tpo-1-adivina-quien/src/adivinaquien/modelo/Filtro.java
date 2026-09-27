package adivinaquien.modelo;

/**
 * Las preguntas que se pueden hacer en un turno. Cada filtro es una función booleana sobre un
 * personaje: responderla parte el conjunto de candidatos en dos (los que cumplen y los que no).
 *
 * El orden de declaración importa: es el criterio de desempate de la estrategia Greedy.
 */
public enum Filtro {
    ES_MUJER("¿Es mujer?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.getGenero() == Genero.MUJER;
        }
    },
    ES_PELADO("¿Es pelado?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.isPelado();
        }
    },
    USA_LENTES("¿Usa lentes?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.isLentes();
        }
    },
    PELO_COLORADO("¿Tiene pelo colorado?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.getColorPelo() == ColorPelo.COLORADO;
        }
    },
    PELO_NEGRO("¿Tiene pelo negro?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.getColorPelo() == ColorPelo.NEGRO;
        }
    },
    PELO_AMARILLO("¿Tiene pelo amarillo?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.getColorPelo() == ColorPelo.AMARILLO;
        }
    },
    TIENE_BARBA("¿Tiene barba?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.isBarba();
        }
    },
    USA_SOMBRERO("¿Usa sombrero?") {
        @Override
        public boolean evaluar(Personaje p) {
            return p.isSombrero();
        }
    };

    private final String pregunta;

    Filtro(String pregunta) {
        this.pregunta = pregunta;
    }

    public abstract boolean evaluar(Personaje p);

    public String getPregunta() {
        return pregunta;
    }
}
