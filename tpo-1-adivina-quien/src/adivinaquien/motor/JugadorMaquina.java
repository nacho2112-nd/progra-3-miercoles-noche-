package adivinaquien.motor;

import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * La máquina. Elige su propio secreto al azar y en cada turno decide sola con la EstrategiaGreedy.
 *
 * No guarda ninguna referencia al rival: lo recibe como Oraculo en cada turno, y la interfaz
 * Oraculo no tiene forma de devolver el personaje secreto.
 */
public class JugadorMaquina extends Jugador {

    private final EstrategiaGreedy estrategia = new EstrategiaGreedy();
    private final Random azar;

    /** La máquina elige su personaje al azar. */
    public JugadorMaquina(String nombre, Tablero tablero, Random azar) {
        super(nombre, tablero, tablero.obtenerPorId(1 + azar.nextInt(tablero.tamanio())));
        this.azar = azar;
    }

    /** Con un secreto fijo: para las pruebas y para la partida Máquina vs Máquina reproducible. */
    public JugadorMaquina(String nombre, Tablero tablero, Personaje secreto, Random azar) {
        super(nombre, tablero, secreto);
        this.azar = azar;
    }

    /** Un turno completo: decide si arriesga o qué pregunta, y lo ejecuta. O(f · c). */
    ResultadoTurno jugarTurno(int numeroTurno, Oraculo rival) {
        Set<Personaje> candidatos = getCandidatos();
        int n = candidatos.size();

        if (estrategia.convieneArriesgar(n)) {
            Personaje elegido = elegirAlAzar(candidatos);
            String motivo;
            if (n == 1) {
                motivo = "queda un solo candidato: arriesgar es ganar";
            } else if (n == 2) {
                motivo = "quedan 2 candidatos: arriesgar de a uno cuesta 1,5 turnos esperados; preguntar antes, 2";
            } else {
                motivo = "quedan 3 candidatos: arriesgar de a uno cuesta 2 turnos esperados; preguntar antes, 2,33";
            }
            return arriesgar(numeroTurno, elegido, rival, null, motivo);
        }

        List<EvaluacionFiltro> evaluaciones = estrategia.evaluar(candidatos, conocimientoInterno());
        EvaluacionFiltro mejor = estrategia.elegir(evaluaciones);
        if (mejor == null) {
            // No debería pasar con un catálogo válido (perfiles únicos), pero si pasa no hay nada que preguntar.
            return arriesgar(numeroTurno, elegirAlAzar(candidatos), rival, evaluaciones,
                    "ningún filtro separa a los candidatos: sólo queda arriesgar");
        }
        String motivo = mejor.getFiltro().getPregunta() + " descarta al menos " + mejor.getDescarteGarantizado()
                + " de " + n + " candidatos, conteste lo que conteste";
        return preguntar(numeroTurno, mejor.getFiltro(), rival, evaluaciones, motivo);
    }

    private Personaje elegirAlAzar(Set<Personaje> candidatos) {
        List<Personaje> lista = new ArrayList<>(candidatos);
        return lista.get(azar.nextInt(lista.size()));
    }
}
