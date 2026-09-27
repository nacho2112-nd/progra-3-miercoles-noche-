package adivinaquien.motor;

import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;

/**
 * El jugador humano: elige su personaje desde la interfaz y decide cada turno con los botones.
 * Las respuestas a las preguntas de la máquina las da el Oraculo heredado, a partir del secreto
 * guardado en la clase base: el humano no tiene que contestar a mano, y no puede mentir.
 */
public class JugadorHumano extends Jugador {

    public JugadorHumano(String nombre, Tablero tablero, Personaje secreto) {
        super(nombre, tablero, secreto);
    }
}
