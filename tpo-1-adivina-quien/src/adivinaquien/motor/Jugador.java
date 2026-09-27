package adivinaquien.motor;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lo común a los dos tipos de jugador: un personaje secreto, el conjunto de candidatos que todavía
 * pueden ser el secreto del rival, y lo que ya sabe de ese secreto.
 *
 * El secreto es private y no tiene getter. Hacia afuera, el jugador sólo se ofrece como Oraculo:
 * se le pueden hacer preguntas, pero no se le puede leer el personaje.
 */
public abstract class Jugador implements Oraculo {

    private final String nombre;
    private final Personaje secreto;
    private final Set<Personaje> candidatos;
    private final BaseConocimiento conocimiento = new BaseConocimiento();

    protected Jugador(String nombre, Tablero tablero, Personaje secreto) {
        if (secreto == null || secreto.getId() == 0 || tablero.obtenerPorId(secreto.getId()) != secreto) {
            throw new IllegalArgumentException("El personaje secreto tiene que ser uno del tablero");
        }
        this.nombre = nombre;
        this.secreto = secreto;
        this.candidatos = new LinkedHashSet<>(tablero.getPersonajes()); // conserva el orden por ID
    }

    @Override
    public final boolean responder(Filtro filtro) {
        return filtro.evaluar(secreto);
    }

    @Override
    public final boolean esTuPersonaje(Personaje candidato) {
        return candidato == secreto;
    }

    /** Sólo la Partida lo usa, y sólo cuando terminó (ver Partida.revelarSecreto). */
    Personaje revelarSecreto() {
        return secreto;
    }

    /** Hace una pregunta al rival y descarta los candidatos que no coinciden con la respuesta. O(c). */
    ResultadoTurno preguntar(int numeroTurno, Filtro filtro, Oraculo rival,
                             List<EvaluacionFiltro> evaluaciones, String motivo) {
        if (conocimiento.estaResuelto(filtro)) {
            throw new IllegalArgumentException(filtro.getPregunta() + " ya está resuelta");
        }
        int antes = candidatos.size();
        boolean respuesta = rival.responder(filtro);
        List<String> inferencias = conocimiento.registrar(filtro, respuesta);

        List<Personaje> descartados = new ArrayList<>();
        Iterator<Personaje> it = candidatos.iterator();
        while (it.hasNext()) {
            Personaje p = it.next();
            if (filtro.evaluar(p) != respuesta) {
                descartados.add(p);
                it.remove();
            }
        }
        verificarInvariante();
        return new ResultadoTurno(numeroTurno, nombre, ResultadoTurno.Accion.PREGUNTA, filtro, respuesta, null,
                evaluaciones, inferencias, descartados, antes, candidatos.size(), motivo);
    }

    /** Arriesga un personaje. Si falla, se pierde el turno y ese personaje queda descartado. */
    ResultadoTurno arriesgar(int numeroTurno, Personaje candidato, Oraculo rival,
                             List<EvaluacionFiltro> evaluaciones, String motivo) {
        int antes = candidatos.size();
        boolean acierto = rival.esTuPersonaje(candidato);
        List<Personaje> descartados = new ArrayList<>();
        if (!acierto && candidatos.remove(candidato)) {
            descartados.add(candidato);
        }
        verificarInvariante();
        return new ResultadoTurno(numeroTurno, nombre, ResultadoTurno.Accion.ARRIESGUE, null, acierto, candidato,
                evaluaciones, null, descartados, antes, candidatos.size(), motivo);
    }

    /** Invariante: el secreto del rival siempre está entre los candidatos, así que nunca quedan vacíos. */
    private void verificarInvariante() {
        if (candidatos.isEmpty()) {
            throw new IllegalStateException(nombre + " se quedó sin candidatos: hubo respuestas inconsistentes");
        }
    }

    public String getNombre() {
        return nombre;
    }

    public Set<Personaje> getCandidatos() {
        return Collections.unmodifiableSet(candidatos);
    }

    public boolean estaResuelto(Filtro filtro) {
        return conocimiento.estaResuelto(filtro);
    }

    /** Copia defensiva: quien la pide no puede alterar lo que el jugador sabe. */
    public BaseConocimiento getConocimiento() {
        return conocimiento.copia();
    }

    protected BaseConocimiento conocimientoInterno() {
        return conocimiento;
    }
}
