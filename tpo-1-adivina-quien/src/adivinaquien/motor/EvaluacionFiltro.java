package adivinaquien.motor;

import adivinaquien.modelo.Filtro;

/**
 * Cuántos candidatos responderían SÍ y cuántos NO a un filtro. Pase lo que pase, se descarta al
 * menos el grupo más chico: ese mínimo es el "descarte garantizado" que maximiza el Greedy.
 */
public class EvaluacionFiltro {

    private final Filtro filtro;
    private final int si;
    private final int no;

    public EvaluacionFiltro(Filtro filtro, int si, int no) {
        this.filtro = filtro;
        this.si = si;
        this.no = no;
    }

    public int getDescarteGarantizado() {
        return Math.min(si, no);
    }

    /** Un filtro que no separa a nadie (todos SÍ o todos NO) no aporta información. */
    public boolean esUtil() {
        return getDescarteGarantizado() > 0;
    }

    public Filtro getFiltro() { return filtro; }
    public int getSi() { return si; }
    public int getNo() { return no; }
}
