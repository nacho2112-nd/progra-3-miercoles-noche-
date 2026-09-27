package adivinaquien.algoritmos;

import adivinaquien.datos.CatalogoPersonajes;
import adivinaquien.modelo.ColorPelo;
import adivinaquien.modelo.Genero;
import adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Mide el tiempo de los cuatro ordenamientos sobre la misma entrada.
 *
 * Con n = 23 un solo ordenamiento tarda microsegundos, por debajo de la resolución útil del reloj.
 * Por eso cada medición repite el ordenamiento muchas veces sobre copias frescas de la misma
 * lista y divide el tiempo total, después de una tanda de calentamiento para que el JIT compile
 * el código antes de medir.
 */
public class Benchmark {

    /** Una fila de la tabla comparativa. */
    public static class Resultado {
        private final String algoritmo;
        private final String complejidad;
        private final int n;
        private final int repeticiones;
        private final double msPromedio;
        private final long comparaciones;

        public Resultado(String algoritmo, String complejidad, int n, int repeticiones,
                         double msPromedio, long comparaciones) {
            this.algoritmo = algoritmo;
            this.complejidad = complejidad;
            this.n = n;
            this.repeticiones = repeticiones;
            this.msPromedio = msPromedio;
            this.comparaciones = comparaciones;
        }

        public String getAlgoritmo() { return algoritmo; }
        public String getComplejidad() { return complejidad; }
        public int getN() { return n; }
        public int getRepeticiones() { return repeticiones; }
        public double getMsPromedio() { return msPromedio; }
        public long getComparaciones() { return comparaciones; }
    }

    public static List<AlgoritmoOrdenamiento> algoritmos() {
        List<AlgoritmoOrdenamiento> lista = new ArrayList<>();
        lista.add(new MergeSort());
        lista.add(new QuickSort());
        lista.add(new OrdenamientoInsercion());
        lista.add(new OrdenamientoBurbuja());
        return lista;
    }

    /** n = 23 usa el catálogo real (agrupado por género); los tamaños grandes, personajes al azar. */
    public List<Resultado> medir(int... tamanios) {
        List<Resultado> resultados = new ArrayList<>();
        for (int n : tamanios) {
            List<Personaje> entrada = (n == CatalogoPersonajes.CANTIDAD)
                    ? CatalogoPersonajes.crear()
                    : generarAleatorios(n, 2026);
            int repeticiones = repeticionesPara(n);
            for (AlgoritmoOrdenamiento algoritmo : algoritmos()) {
                resultados.add(medirUno(algoritmo, entrada, repeticiones));
            }
        }
        return resultados;
    }

    private Resultado medirUno(AlgoritmoOrdenamiento algoritmo, List<Personaje> entrada, int repeticiones) {
        Comparator<Personaje> criterio = Personaje.POR_NOMBRE;

        // Calentamiento: no se mide.
        for (int i = 0; i < Math.max(1, repeticiones / 4); i++) {
            algoritmo.ordenar(new ArrayList<>(entrada), criterio);
        }

        // Las copias se preparan antes, para no medir el costo de copiar.
        List<List<Personaje>> copias = new ArrayList<>(repeticiones);
        for (int i = 0; i < repeticiones; i++) {
            copias.add(new ArrayList<>(entrada));
        }
        long inicio = System.nanoTime();
        for (List<Personaje> copia : copias) {
            algoritmo.ordenar(copia, criterio);
        }
        long total = System.nanoTime() - inicio;

        // Comparaciones: una corrida aparte con un comparador que cuenta.
        long[] contador = {0};
        algoritmo.ordenar(new ArrayList<>(entrada), (a, b) -> {
            contador[0]++;
            return criterio.compare(a, b);
        });

        double msPromedio = total / 1_000_000.0 / repeticiones;
        return new Resultado(algoritmo.getNombre(), algoritmo.getComplejidad(), entrada.size(),
                repeticiones, msPromedio, contador[0]);
    }

    private static int repeticionesPara(int n) {
        if (n <= 100) return 20_000;
        if (n <= 1_000) return 100;
        return 5;
    }

    /** Personajes válidos con nombres al azar, para ver cómo escala cada algoritmo. */
    public static List<Personaje> generarAleatorios(int n, long semilla) {
        Random azar = new Random(semilla);
        List<Personaje> lista = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            StringBuilder nombre = new StringBuilder();
            int largo = 5 + azar.nextInt(6);
            for (int j = 0; j < largo; j++) {
                nombre.append((char) ('a' + azar.nextInt(26)));
            }
            nombre.append(i); // garantiza nombres únicos
            Genero genero = azar.nextBoolean() ? Genero.MUJER : Genero.HOMBRE;
            boolean pelado = azar.nextInt(6) == 0;
            ColorPelo color = pelado ? ColorPelo.NINGUNO : ColorPelo.values()[azar.nextInt(3)];
            boolean barba = genero == Genero.HOMBRE && azar.nextBoolean();
            lista.add(new Personaje(nombre.toString(), genero, pelado, color,
                    azar.nextBoolean(), barba, azar.nextBoolean()));
        }
        return lista;
    }
}
