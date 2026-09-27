package adivinaquien;

import adivinaquien.algoritmos.Benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Corre el benchmark varias veces y reporta la mediana, el mínimo y el máximo de cada fila.
 * Una sola corrida con n = 23 es ruidosa: la mediana es el valor que va a la tabla del documento.
 *
 *   java -cp out adivinaquien.MedicionTiempos
 */
public class MedicionTiempos {

    private static final int CORRIDAS = 7;

    public static void main(String[] args) {
        Map<String, List<Double>> tiempos = new LinkedHashMap<>();
        Map<String, Long> comparaciones = new LinkedHashMap<>();
        for (int i = 0; i < CORRIDAS; i++) {
            for (Benchmark.Resultado r : new Benchmark().medir(23, 1_000, 10_000)) {
                String clave = r.getAlgoritmo() + " | " + r.getN();
                tiempos.computeIfAbsent(clave, k -> new ArrayList<>()).add(r.getMsPromedio());
                comparaciones.put(clave, r.getComparaciones());
            }
        }
        System.out.println("algoritmo | n | mediana ms | mínimo ms | máximo ms | comparaciones  (" + CORRIDAS + " corridas)");
        for (Map.Entry<String, List<Double>> e : tiempos.entrySet()) {
            List<Double> v = e.getValue();
            Collections.sort(v);
            System.out.printf(Locale.ROOT, "%s | %.5f | %.5f | %.5f | %d%n",
                    e.getKey(), v.get(v.size() / 2), v.get(0), v.get(v.size() - 1), comparaciones.get(e.getKey()));
        }
    }
}
