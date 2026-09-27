package adivinaquien.motor;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Estrategia Greedy de la máquina para elegir qué preguntar.
 *
 * Los 5 elementos:
 * - Candidatos: los filtros todavía no resueltos.
 * - Función de selección: el filtro con mayor descarte garantizado, min(SÍ, NO).
 * - Factibilidad: el filtro tiene que separar algo (descarte > 0).
 * - Objetivo: adivinar al secreto en la menor cantidad de turnos.
 * - Solución: quedan 3 candidatos o menos, y a partir de ahí conviene arriesgar.
 *
 * Nunca vuelve atrás: elige el mejor filtro del turno sin mirar qué conviene preguntar después.
 */
public class EstrategiaGreedy {

    /**
     * Con esta cantidad de candidatos o menos, arriesgar rinde más que preguntar. Arriesgar de a uno
     * cuesta (k + 1) / 2 turnos esperados: 1,5 con k = 2 y 2 con k = 3. Preguntar primero cuesta
     * 2 y 2,33. Con k = 4 empatan en 2,5, pero preguntar tiene mejor peor caso (3 contra 4).
     */
    public static final int UMBRAL_ARRIESGUE = 3;

    /** Función de evaluación: cuenta SÍ y NO de cada filtro no resuelto. O(f · c). */
    public List<EvaluacionFiltro> evaluar(Collection<Personaje> candidatos, BaseConocimiento conocimiento) {
        List<EvaluacionFiltro> evaluaciones = new ArrayList<>();
        for (Filtro filtro : Filtro.values()) {
            if (conocimiento.estaResuelto(filtro)) {
                continue; // la dependencia lógica ya lo resolvió: no se evalúa
            }
            int si = 0;
            for (Personaje p : candidatos) {
                if (filtro.evaluar(p)) {
                    si++;
                }
            }
            evaluaciones.add(new EvaluacionFiltro(filtro, si, candidatos.size() - si));
        }
        return evaluaciones;
    }

    /**
     * Función de selección: el máximo descarte garantizado. En empate gana el primero en el orden
     * del enum (la comparación es estricta). Devuelve null si ningún filtro separa candidatos. O(f).
     */
    public EvaluacionFiltro elegir(List<EvaluacionFiltro> evaluaciones) {
        EvaluacionFiltro mejor = null;
        for (EvaluacionFiltro e : evaluaciones) {
            if (e.esUtil() && (mejor == null || e.getDescarteGarantizado() > mejor.getDescarteGarantizado())) {
                mejor = e;
            }
        }
        return mejor;
    }

    /** Criterio de parada: con 1 candidato se gana seguro; con 2 o 3, arriesgar cuesta menos turnos esperados. */
    public boolean convieneArriesgar(int candidatos) {
        return candidatos <= UMBRAL_ARRIESGUE;
    }
}
