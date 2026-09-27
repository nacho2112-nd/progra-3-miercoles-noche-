package adivinaquien.motor;

import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Todo lo que pasó en un turno. La interfaz lo usa para mostrar el razonamiento de la máquina. */
public class ResultadoTurno {

    public enum Accion { PREGUNTA, ARRIESGUE }

    private final int numero;
    private final String jugador;
    private final Accion accion;
    private final Filtro filtro;
    private final boolean respuesta;
    private final Personaje arriesgado;
    private final List<EvaluacionFiltro> evaluaciones;
    private final List<String> inferencias;
    private final List<Personaje> descartados;
    private final int candidatosAntes;
    private final int candidatosDespues;
    private final String motivo;

    ResultadoTurno(int numero, String jugador, Accion accion, Filtro filtro, boolean respuesta,
                   Personaje arriesgado, List<EvaluacionFiltro> evaluaciones, List<String> inferencias,
                   List<Personaje> descartados, int candidatosAntes, int candidatosDespues, String motivo) {
        this.numero = numero;
        this.jugador = jugador;
        this.accion = accion;
        this.filtro = filtro;
        this.respuesta = respuesta;
        this.arriesgado = arriesgado;
        this.evaluaciones = evaluaciones == null ? new ArrayList<>() : evaluaciones;
        this.inferencias = inferencias == null ? new ArrayList<>() : inferencias;
        this.descartados = descartados;
        this.candidatosAntes = candidatosAntes;
        this.candidatosDespues = candidatosDespues;
        this.motivo = motivo;
    }

    public boolean esAcierto() {
        return accion == Accion.ARRIESGUE && respuesta;
    }

    /** Texto para el panel de traza. Con detalle incluye la tabla de evaluación del Greedy. */
    public String describir(boolean detallado) {
        StringBuilder sb = new StringBuilder();
        sb.append("── Turno ").append(numero).append(" · ").append(jugador).append(" ")
                .append("─".repeat(Math.max(3, 40 - jugador.length()))).append('\n');
        if (detallado) {
            sb.append("Candidatos antes: ").append(candidatosAntes).append('\n');
            if (!evaluaciones.isEmpty()) {
                sb.append("Evaluación Greedy (descarte garantizado = min(SÍ, NO)):\n");
                sb.append(String.format(Locale.ROOT, "  %-24s %4s %4s %9s%n", "Filtro", "SÍ", "NO", "descarte"));
                for (EvaluacionFiltro e : evaluaciones) {
                    boolean elegido = accion == Accion.PREGUNTA && e.getFiltro() == filtro;
                    sb.append(String.format(Locale.ROOT, "  %-24s %4d %4d %9d%s%n",
                            e.getFiltro().getPregunta(), e.getSi(), e.getNo(), e.getDescarteGarantizado(),
                            elegido ? "  <- elegido" : (e.esUtil() ? "" : "  (no separa)")));
                }
            }
            if (motivo != null) {
                sb.append("Decisión: ").append(motivo).append('\n');
            }
        }
        if (accion == Accion.PREGUNTA) {
            sb.append("Pregunta: ").append(filtro.getPregunta()).append("  ->  ").append(respuesta ? "SÍ" : "NO").append('\n');
        } else {
            sb.append("Arriesga: ¿Es ").append(arriesgado.getNombre()).append("?  ->  ")
                    .append(respuesta ? "¡SÍ! Adivinó." : "NO").append('\n');
        }
        if (detallado && !inferencias.isEmpty()) {
            sb.append("Inferencias por dependencia lógica:\n");
            for (String inferencia : inferencias) {
                sb.append("  · ").append(inferencia).append('\n');
            }
        }
        if (!esAcierto()) {
            sb.append("Descartados (").append(descartados.size()).append("): ")
                    .append(descartados.isEmpty() ? "ninguno" : descartados.toString()).append('\n');
            sb.append("Quedan ").append(candidatosDespues).append(" candidatos\n");
        }
        return sb.toString();
    }

    public int getNumero() { return numero; }
    public String getJugador() { return jugador; }
    public Accion getAccion() { return accion; }
    public Filtro getFiltro() { return filtro; }
    public boolean getRespuesta() { return respuesta; }
    public Personaje getArriesgado() { return arriesgado; }
    public List<EvaluacionFiltro> getEvaluaciones() { return evaluaciones; }
    public List<String> getInferencias() { return inferencias; }
    public List<Personaje> getDescartados() { return descartados; }
    public int getCandidatosAntes() { return candidatosAntes; }
    public int getCandidatosDespues() { return candidatosDespues; }
    public String getMotivo() { return motivo; }
}
