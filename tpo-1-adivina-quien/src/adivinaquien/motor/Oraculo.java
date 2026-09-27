package adivinaquien.motor;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;

/**
 * Lo único que un jugador sabe de su rival. Responde preguntas sobre el personaje secreto, pero
 * no tiene ningún método que lo devuelva: la máquina nunca puede leer la variable del secreto
 * humano, sólo preguntarle cosas.
 */
public interface Oraculo {

    /** SÍ o NO sobre el personaje secreto. */
    boolean responder(Filtro filtro);

    /** El arriesgue: ¿el secreto es este personaje? */
    boolean esTuPersonaje(Personaje candidato);
}
