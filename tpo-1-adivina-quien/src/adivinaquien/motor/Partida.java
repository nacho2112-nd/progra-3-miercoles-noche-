package adivinaquien.motor;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controla los turnos. Cada turno es una sola acción: preguntar un filtro o arriesgar un personaje.
 * Si el arriesgue acierta, el jugador gana; si no, el turno pasa al rival.
 *
 * Termina siempre: cada turno descarta al menos un candidato (la máquina sólo pregunta filtros que
 * separan, y un arriesgue fallido descarta al arriesgado), así que en 23 turnos propios como mucho
 * cualquier jugador llega al secreto.
 */
public class Partida {

    private final Tablero tablero;
    private final Jugador jugador1;
    private final Jugador jugador2;
    private final List<ResultadoTurno> historial = new ArrayList<>();
    private Jugador turno;
    private Jugador ganador;

    /** Empieza jugador1. */
    public Partida(Tablero tablero, Jugador jugador1, Jugador jugador2) {
        this.tablero = tablero;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.turno = jugador1;
    }

    public ResultadoTurno jugarTurnoMaquina() {
        exigirEnCurso();
        if (!(turno instanceof JugadorMaquina)) {
            throw new IllegalStateException("Es el turno de " + turno.getNombre());
        }
        return cerrar(((JugadorMaquina) turno).jugarTurno(historial.size() + 1, rival()));
    }

    public ResultadoTurno preguntar(Filtro filtro) {
        exigirTurnoHumano();
        return cerrar(turno.preguntar(historial.size() + 1, filtro, rival(), null, null));
    }

    public ResultadoTurno arriesgar(Personaje candidato) {
        exigirTurnoHumano();
        if (candidato == null || tablero.obtenerPorId(candidato.getId()) != candidato) {
            throw new IllegalArgumentException("Sólo se puede arriesgar un personaje del tablero");
        }
        return cerrar(turno.arriesgar(historial.size() + 1, candidato, rival(), null, null));
    }

    /** El rival se entrega como Oraculo: quien lo recibe sólo puede preguntarle cosas. */
    private Oraculo rival() {
        return turno == jugador1 ? jugador2 : jugador1;
    }

    private ResultadoTurno cerrar(ResultadoTurno resultado) {
        historial.add(resultado);
        if (resultado.esAcierto()) {
            ganador = turno;
        } else {
            turno = (turno == jugador1) ? jugador2 : jugador1;
        }
        return resultado;
    }

    /** Recién al terminar la partida se puede ver el secreto de cada uno. */
    public Personaje revelarSecreto(Jugador jugador) {
        if (!terminada()) {
            throw new IllegalStateException("Los secretos se revelan al terminar la partida");
        }
        return jugador.revelarSecreto();
    }

    private void exigirEnCurso() {
        if (terminada()) {
            throw new IllegalStateException("La partida ya terminó");
        }
    }

    private void exigirTurnoHumano() {
        exigirEnCurso();
        if (!(turno instanceof JugadorHumano)) {
            throw new IllegalStateException("Es el turno de " + turno.getNombre());
        }
    }

    public boolean terminada() { return ganador != null; }
    public Jugador getGanador() { return ganador; }
    public Jugador getTurno() { return turno; }
    public Jugador getJugador1() { return jugador1; }
    public Jugador getJugador2() { return jugador2; }
    public Tablero getTablero() { return tablero; }
    public List<ResultadoTurno> getHistorial() { return Collections.unmodifiableList(historial); }
}
