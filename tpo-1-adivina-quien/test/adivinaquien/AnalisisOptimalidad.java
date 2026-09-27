package adivinaquien;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;
import adivinaquien.motor.ArbolDecision;
import adivinaquien.motor.NodoDecision;
import adivinaquien.motor.OrganizadorTablero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Herramienta de verificación, fuera del juego: compara la estrategia Greedy con la estrategia
 * óptima, calculada por búsqueda exhaustiva con memorización sobre todos los subconjuntos de
 * candidatos (programación dinámica sobre máscaras de bits, O(2^n · (n + f))).
 *
 * Las reglas son las del juego: cada turno es una pregunta o un arriesgue, y un arriesgue
 * fallido descarta al arriesgado.
 *
 *   java -Xmx512m -cp out adivinaquien.AnalisisOptimalidad
 */
public class AnalisisOptimalidad {

    public static void main(String[] args) {
        Tablero tablero = OrganizadorTablero.organizar();
        List<Personaje> todos = new ArrayList<>(tablero.getPersonajes());

        System.out.println("=== Tablero completo (23 personajes) ===");
        comparar(todos, true);

        System.out.println();
        System.out.println("=== Búsqueda de contraejemplos: subconjuntos del tablero real ===");
        buscarContraejemplo(todos, false);
        buscarContraejemplo(todos, true);
    }

    /** Imprime Greedy contra óptimo para un conjunto de candidatos. Devuelve {gPeor, oPeor, gSuma, oSuma}. */
    private static int[] comparar(List<Personaje> candidatos, boolean imprimir) {
        ArbolDecision arbol = ArbolDecision.construir(candidatos);
        int gPeor = arbol.turnosMaximos();
        int gSuma = 0;
        for (Personaje p : candidatos) {
            gSuma += arbol.turnosPara(p);
        }
        Optimo optimo = new Optimo(candidatos);
        int todosBits = (1 << candidatos.size()) - 1;
        int oPeor = optimo.peor(todosBits);
        int oSuma = optimo.suma(todosBits);
        if (imprimir) {
            int n = candidatos.size();
            System.out.printf(Locale.ROOT, "Greedy : peor caso %d turnos, promedio %.3f turnos, altura del árbol %d preguntas%n",
                    gPeor, gSuma / (double) n, arbol.profundidadMaxima());
            System.out.printf(Locale.ROOT, "Óptimo : peor caso %d turnos, promedio %.3f turnos (peor caso y promedio optimizados por separado)%n",
                    oPeor, oSuma / (double) n);
            System.out.printf(Locale.ROOT, "Óptimo sólo preguntando hasta aislar a 1: altura %d preguntas (cota inferior: techo(log2 %d) = %d)%n",
                    optimo.alturaSoloPreguntas(todosBits), n, 32 - Integer.numberOfLeadingZeros(n - 1));
        }
        return new int[]{gPeor, oPeor, gSuma, oSuma};
    }

    /** Muestrea subconjuntos del tablero, de menor a mayor tamaño, hasta encontrar uno donde Greedy pierde. */
    private static void buscarContraejemplo(List<Personaje> todos, boolean porPromedio) {
        Random azar = new Random(7);
        for (int tam = 3; tam <= 12; tam++) {
            for (int intento = 0; intento < 20_000; intento++) {
                List<Personaje> sub = new ArrayList<>(todos);
                Collections.shuffle(sub, azar);
                sub = new ArrayList<>(sub.subList(0, tam));
                sub.sort(Personaje.POR_NOMBRE);
                int[] r = comparar(sub, false);
                boolean pierde = porPromedio ? r[2] > r[3] : r[0] > r[1];
                if (pierde) {
                    System.out.println();
                    System.out.println("Contraejemplo por " + (porPromedio ? "PROMEDIO" : "PEOR CASO") + " con " + tam + " personajes:");
                    for (Personaje p : sub) {
                        System.out.println("  " + p.getNombre() + " — " + p.descripcion());
                    }
                    System.out.printf(Locale.ROOT, "  Greedy: peor %d, promedio %.3f | Óptimo: peor %d, promedio %.3f%n",
                            r[0], r[2] / (double) tam, r[1], r[3] / (double) tam);
                    System.out.println("  Árbol Greedy:");
                    imprimirArbol(ArbolDecision.construir(sub).getRaiz(), "    ", "");
                    System.out.println("  Estrategia óptima (" + (porPromedio ? "promedio" : "peor caso") + "):");
                    new Optimo(sub).imprimir((1 << tam) - 1, porPromedio, "    ", "");
                    return;
                }
            }
        }
        System.out.println("Sin contraejemplo " + (porPromedio ? "por promedio" : "por peor caso") + " en los subconjuntos muestreados");
    }

    private static void imprimirArbol(NodoDecision nodo, String sangria, String rama) {
        System.out.println(sangria + rama + nodo);
        if (!nodo.esHoja()) {
            imprimirArbol(nodo.getSi(), sangria + "  ", "SÍ: ");
            imprimirArbol(nodo.getNo(), sangria + "  ", "NO: ");
        }
    }

    /** Estrategia óptima por búsqueda exhaustiva con memoria. Los conjuntos son máscaras de bits. */
    private static class Optimo {
        private final List<Personaje> items;
        private final int[] mascaraFiltro = new int[Filtro.values().length];
        private final byte[] memoPeor;
        private final short[] memoSuma;
        private final byte[] memoAltura;

        Optimo(List<Personaje> items) {
            this.items = items;
            for (Filtro f : Filtro.values()) {
                for (int i = 0; i < items.size(); i++) {
                    if (f.evaluar(items.get(i))) {
                        mascaraFiltro[f.ordinal()] |= 1 << i;
                    }
                }
            }
            memoPeor = new byte[1 << items.size()];
            memoSuma = new short[1 << items.size()];
            memoAltura = new byte[1 << items.size()];
        }

        /** Mínimo, sobre todas las estrategias, de los turnos en el peor secreto. */
        int peor(int s) {
            if (Integer.bitCount(s) == 1) return 1;
            if (memoPeor[s] != 0) return memoPeor[s];
            int mejor = Integer.MAX_VALUE;
            for (int resto = s; resto != 0; resto &= resto - 1) {
                int c = Integer.lowestOneBit(resto);
                mejor = Math.min(mejor, 1 + peor(s & ~c)); // arriesgar c y fallar
            }
            for (int m : mascaraFiltro) {
                int si = s & m, no = s & ~m;
                if (si != 0 && no != 0) {
                    mejor = Math.min(mejor, 1 + Math.max(peor(si), peor(no)));
                }
            }
            memoPeor[s] = (byte) mejor;
            return mejor;
        }

        /** Mínimo de la suma de turnos sobre todos los secretos (equivale a minimizar el promedio). */
        int suma(int s) {
            int n = Integer.bitCount(s);
            if (n == 1) return 1;
            if (memoSuma[s] != 0) return memoSuma[s];
            int mejor = Integer.MAX_VALUE;
            for (int resto = s; resto != 0; resto &= resto - 1) {
                int c = Integer.lowestOneBit(resto);
                mejor = Math.min(mejor, n + suma(s & ~c));
            }
            for (int m : mascaraFiltro) {
                int si = s & m, no = s & ~m;
                if (si != 0 && no != 0) {
                    mejor = Math.min(mejor, n + suma(si) + suma(no));
                }
            }
            memoSuma[s] = (short) mejor;
            return mejor;
        }

        /** Árbol de decisión de altura mínima usando sólo preguntas, hasta aislar a un personaje. */
        int alturaSoloPreguntas(int s) {
            if (Integer.bitCount(s) == 1) return 0;
            if (memoAltura[s] != 0) return memoAltura[s];
            int mejor = 99;
            for (int m : mascaraFiltro) {
                int si = s & m, no = s & ~m;
                if (si != 0 && no != 0) {
                    mejor = Math.min(mejor, 1 + Math.max(alturaSoloPreguntas(si), alturaSoloPreguntas(no)));
                }
            }
            memoAltura[s] = (byte) mejor;
            return mejor;
        }

        /** Reconstruye la estrategia óptima eligiendo, en cada conjunto, una acción que alcanza el óptimo. */
        void imprimir(int s, boolean porPromedio, String sangria, String rama) {
            int n = Integer.bitCount(s);
            if (n == 1) {
                System.out.println(sangria + rama + "Arriesgar: " + items.get(Integer.numberOfTrailingZeros(s)));
                return;
            }
            int objetivo = porPromedio ? suma(s) : peor(s);
            for (Filtro f : Filtro.values()) {
                int m = mascaraFiltro[f.ordinal()];
                int si = s & m, no = s & ~m;
                if (si == 0 || no == 0) continue;
                int valor = porPromedio ? n + suma(si) + suma(no) : 1 + Math.max(peor(si), peor(no));
                if (valor == objetivo) {
                    System.out.println(sangria + rama + f.getPregunta() + "   (" + n + " candidatos: "
                            + Integer.bitCount(si) + " SÍ / " + Integer.bitCount(no) + " NO)");
                    imprimir(si, porPromedio, sangria + "  ", "SÍ: ");
                    imprimir(no, porPromedio, sangria + "  ", "NO: ");
                    return;
                }
            }
            for (int resto = s; resto != 0; resto &= resto - 1) {
                int c = Integer.lowestOneBit(resto);
                int valor = porPromedio ? n + suma(s & ~c) : 1 + peor(s & ~c);
                if (valor == objetivo) {
                    System.out.println(sangria + rama + "Arriesgar: " + items.get(Integer.numberOfTrailingZeros(c)) + " (si falla, sigue)");
                    imprimir(s & ~c, porPromedio, sangria, "");
                    return;
                }
            }
        }
    }
}
