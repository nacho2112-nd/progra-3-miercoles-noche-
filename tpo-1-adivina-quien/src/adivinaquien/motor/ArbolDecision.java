package adivinaquien.motor;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * El árbol de decisión que recorre la máquina, construido de antemano con la misma estrategia
 * Greedy que usa en el juego.
 *
 * La construcción es Divide y Conquista: el mejor filtro divide a los candidatos en dos grupos,
 * se construye recursivamente el subárbol de cada grupo y el nodo los combina. Cada partida de la
 * máquina es un camino de la raíz a una hoja.
 */
public class ArbolDecision {

    private final NodoDecision raiz;

    private ArbolDecision(NodoDecision raiz) {
        this.raiz = raiz;
    }

    public static ArbolDecision construir(Collection<Personaje> candidatos) {
        return new ArbolDecision(construirNodo(new ArrayList<>(candidatos), new BaseConocimiento(), new EstrategiaGreedy()));
    }

    private static NodoDecision construirNodo(List<Personaje> candidatos, BaseConocimiento conocimiento,
                                              EstrategiaGreedy estrategia) {
        if (estrategia.convieneArriesgar(candidatos.size())) {
            return NodoDecision.hoja(candidatos); // caso base
        }
        EvaluacionFiltro mejor = estrategia.elegir(estrategia.evaluar(candidatos, conocimiento));
        if (mejor == null) {
            return NodoDecision.hoja(candidatos);
        }
        Filtro filtro = mejor.getFiltro();

        // Dividir
        List<Personaje> grupoSi = new ArrayList<>();
        List<Personaje> grupoNo = new ArrayList<>();
        for (Personaje p : candidatos) {
            (filtro.evaluar(p) ? grupoSi : grupoNo).add(p);
        }
        BaseConocimiento sabeSi = conocimiento.copia();
        sabeSi.registrar(filtro, true);
        BaseConocimiento sabeNo = conocimiento.copia();
        sabeNo.registrar(filtro, false);

        // Conquistar y combinar
        return new NodoDecision(filtro, candidatos,
                construirNodo(grupoSi, sabeSi, estrategia),
                construirNodo(grupoNo, sabeNo, estrategia));
    }

    /** Turnos que necesita la máquina para adivinar a este secreto: preguntas + posición en el arriesgue. */
    public int turnosPara(Personaje secreto) {
        NodoDecision nodo = raiz;
        int preguntas = 0;
        while (!nodo.esHoja()) {
            nodo = nodo.getFiltro().evaluar(secreto) ? nodo.getSi() : nodo.getNo();
            preguntas++;
        }
        return preguntas + nodo.getCandidatos().indexOf(secreto) + 1;
    }

    public int turnosMaximos() {
        int maximo = 0;
        for (Personaje p : raiz.getCandidatos()) {
            maximo = Math.max(maximo, turnosPara(p));
        }
        return maximo;
    }

    /** Promedio sobre todos los secretos posibles, igual de probables. */
    public double turnosPromedio() {
        int suma = 0;
        for (Personaje p : raiz.getCandidatos()) {
            suma += turnosPara(p);
        }
        return (double) suma / raiz.getCandidatos().size();
    }

    /** Altura en preguntas: el camino más largo de la raíz a una hoja. */
    public int profundidadMaxima() {
        return profundidad(raiz);
    }

    private static int profundidad(NodoDecision nodo) {
        return nodo.esHoja() ? 0 : 1 + Math.max(profundidad(nodo.getSi()), profundidad(nodo.getNo()));
    }

    public NodoDecision getRaiz() {
        return raiz;
    }
}
