package Jugadores;

import Things.Pregunta;

// Lo unico que un jugador ve de su rival: responde SI o NO sobre su personaje secreto,
// pero no tiene ningun metodo que lo devuelva. Asi la maquina nunca lee la variable del humano.
public interface Oraculo {
    boolean responder(Pregunta pregunta);
}
