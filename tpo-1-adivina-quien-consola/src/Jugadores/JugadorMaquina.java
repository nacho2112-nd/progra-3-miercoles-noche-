package Jugadores;

import Functional_Things.IndiceAtributos;
import Things.Personajes;
import Things.Pregunta;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JugadorMaquina extends Jugador {

    private final int nivel;        // 1 = pregunta al azar, 2 = greedy
    private final boolean verboso;  // modo Maquina vs Maquina: muestra cada decision
    private final Random num_random = new Random();

    // 'heredado' = preguntas que ya se le hicieron al rival. La Maquina 2 arranca con las de la Maquina 1.
    public JugadorMaquina(int nivel, List<Personajes> tablero, IndiceAtributos indice, List<Registro> heredado, boolean verboso) {
        super("Máquina " + nivel, tablero, indice);
        this.nivel = nivel;
        this.verboso = verboso;
        heredado.forEach(r -> aprender(r.pregunta(), r.respuesta()));
    }

    @Override
    protected Personajes elegir(List<Personajes> disponibles) {
        return disponibles.get(num_random.nextInt(disponibles.size()));
    }

    @Override
    protected Pregunta decidir() {
        log("Candidatos: " + candidatos.stream().map(Personajes::get_Nombre).toList());
        return nivel == 1 ? alAzar() : greedy();
    }

    // NIVEL 1: sin heuristica ni memoria. Con 2 o menos candidatos arriesga al azar (puede errar).
    // Si no, pregunta un atributo cualquiera de un candidato cualquiera: puede salir inutil o repetida. O(a)
    private Pregunta alAzar() {
        List<Personajes> lista = new ArrayList<>(candidatos);
        Personajes p = lista.get(num_random.nextInt(lista.size()));
        if (lista.size() <= 2) return Pregunta.arriesgue(p);
        List<Map.Entry<String, String>> atributos = new ArrayList<>(p.get_Atributos().entrySet());
        Map.Entry<String, String> a = atributos.get(num_random.nextInt(atributos.size()));
        log("Pregunto al azar por un atributo de " + p.get_Nombre());
        return new Pregunta(a.getKey(), a.getValue());
    }

    // NIVEL 2: GREEDY. Puntaje de una pregunta = min(SI, NO): los candidatos que descarta seguro,
    // conteste lo que conteste el rival. Elige el mayor. Con 3 o menos arriesga: probar de a uno
    // cuesta (k+1)/2 turnos, o sea 2 con k = 3, contra 2,33 si pregunta primero. O(q·c) por turno.
    private Pregunta greedy() {
        int total = candidatos.size();
        int mejorPuntaje = 0;
        Pregunta mejor = null;
        if (total > 3) {
            for (Pregunta p : indice.preguntasPosibles()) {
                long si = candidatos.stream().filter(indice.buscar(p)::contains).count();
                int puntaje = (int) Math.min(si, total - si);
                if (puntaje == 0) continue; // no divide: la respuesta ya se sabe
                log(p + " SÍ=" + si + " NO=" + (total - si) + " -> descarta seguro " + puntaje);
                if (puntaje > mejorPuntaje) {
                    mejorPuntaje = puntaje;
                    mejor = p;
                }
            }
        }
        return mejor != null ? mejor : Pregunta.arriesgue(candidatos.iterator().next());
    }

    // DIVIDE Y CONQUISTA: la maquina recibe los personajes agrupados por genero, los ordena por
    // nombre con MergeSort y les da un ID autoincremental a medida que los agrega al tablero.
    // T(n) = 2T(n/2) + O(n) => Θ(n log n) (teorema maestro, caso 2)
    public static List<Personajes> armarTablero(List<Personajes> porGenero) {
        List<Personajes> tablero = new ArrayList<>();
        for (Personajes p : mergeSort(porGenero)) {
            tablero.add(p);
            p.set_ID(tablero.size());
        }
        return tablero;
    }

    private static List<Personajes> mergeSort(List<Personajes> lista) {
        if (lista.size() <= 1) return lista;                                  // caso base
        int medio = lista.size() / 2;                                         // dividir
        List<Personajes> izq = mergeSort(lista.subList(0, medio));            // conquistar
        List<Personajes> der = mergeSort(lista.subList(medio, lista.size()));
        List<Personajes> resultado = new ArrayList<>();                       // combinar: O(n)
        int i = 0, j = 0;
        while (i < izq.size() || j < der.size()) {
            boolean tomarIzq = j == der.size()
                    || (i < izq.size() && izq.get(i).get_Nombre().compareTo(der.get(j).get_Nombre()) <= 0);
            resultado.add(tomarIzq ? izq.get(i++) : der.get(j++));
        }
        return resultado;
    }

    private void log(String mensaje) {
        if (verboso) System.out.println("   [" + getNombre() + "] " + mensaje);
    }
}
