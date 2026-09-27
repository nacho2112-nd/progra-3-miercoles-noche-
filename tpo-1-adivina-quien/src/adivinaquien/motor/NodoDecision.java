package adivinaquien.motor;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;

import java.util.Collections;
import java.util.List;

/**
 * Un nodo del árbol de decisión. Los internos son preguntas (con una rama SÍ y otra NO); las hojas
 * son arriesgues, en el orden en que se harían.
 */
public class NodoDecision {

    private final Filtro filtro;
    private final List<Personaje> candidatos;
    private final NodoDecision si;
    private final NodoDecision no;

    NodoDecision(Filtro filtro, List<Personaje> candidatos, NodoDecision si, NodoDecision no) {
        this.filtro = filtro;
        this.candidatos = candidatos;
        this.si = si;
        this.no = no;
    }

    static NodoDecision hoja(List<Personaje> candidatos) {
        return new NodoDecision(null, candidatos, null, null);
    }

    public boolean esHoja() {
        return filtro == null;
    }

    public Filtro getFiltro() { return filtro; }
    public List<Personaje> getCandidatos() { return Collections.unmodifiableList(candidatos); }
    public NodoDecision getSi() { return si; }
    public NodoDecision getNo() { return no; }

    @Override
    public String toString() {
        if (esHoja()) {
            return candidatos.size() == 1
                    ? "Arriesgar: " + candidatos.get(0)
                    : "Arriesgar entre " + candidatos;
        }
        return filtro.getPregunta() + "   (" + candidatos.size() + " candidatos: "
                + si.candidatos.size() + " SÍ / " + no.candidatos.size() + " NO)";
    }
}
