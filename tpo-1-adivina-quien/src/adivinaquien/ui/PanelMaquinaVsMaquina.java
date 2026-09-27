package adivinaquien.ui;

import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;
import adivinaquien.motor.Jugador;
import adivinaquien.motor.JugadorMaquina;
import adivinaquien.motor.OrganizadorTablero;
import adivinaquien.motor.Partida;
import adivinaquien.motor.ResultadoTurno;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Random;

/**
 * Modo Máquina vs Máquina. Dos jugadores con la misma estrategia Greedy juegan entre sí y se muestra
 * todo su proceso: la tabla de evaluación de cada filtro, la decisión, la respuesta, las inferencias
 * y los descartes. El espectador ve los dos secretos; las máquinas, no.
 */
public class PanelMaquinaVsMaquina extends JPanel {

    private final BarraSuperior barra = new BarraSuperior("Máquina vs Máquina", "");
    private final Lado ladoA = new Lado();
    private final Lado ladoB = new Lado();
    private final JTextArea traza = Estilo.areaTraza();
    private final BotonPlano botonPaso = BotonPlano.primario("Siguiente turno");
    private final BotonPlano botonAuto = BotonPlano.secundario("Automático");
    private final JSlider velocidad = new JSlider(200, 2500, 1200);
    private final Timer automatico;

    private Partida partida;
    private JugadorMaquina maquinaA;
    private JugadorMaquina maquinaB;

    public PanelMaquinaVsMaquina(Runnable alVolver) {
        super(new BorderLayout());
        setBackground(Estilo.FONDO);

        automatico = new Timer(velocidad.getValue(), e -> paso());
        velocidad.setInverted(true);
        velocidad.setOpaque(false);
        velocidad.setToolTipText("Velocidad del modo automático");
        velocidad.addChangeListener(e -> automatico.setDelay(velocidad.getValue()));

        BotonPlano nueva = BotonPlano.secundario("Nueva partida");
        nueva.addActionListener(e -> nuevaPartida());
        botonPaso.addActionListener(e -> paso());
        botonAuto.addActionListener(e -> alternarAutomatico());
        BotonPlano volver = BotonPlano.secundario("← Menú");
        volver.addActionListener(e -> alVolver.run());
        barra.agregarBoton(Estilo.etiqueta("Velocidad", Estilo.texto(12f), Estilo.TINTA_SUAVE));
        barra.agregarBoton(velocidad);
        barra.agregarBoton(botonAuto);
        barra.agregarBoton(botonPaso);
        barra.agregarBoton(nueva);
        barra.agregarBoton(volver);
        add(barra, BorderLayout.NORTH);

        JPanel tableros = new JPanel(new GridLayout(1, 2, 14, 0));
        tableros.setOpaque(false);
        tableros.setBorder(Estilo.margen(12));
        tableros.add(ladoA.panel);
        tableros.add(ladoB.panel);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableros,
                Estilo.seccion("Proceso de la máquina, turno por turno", Estilo.scroll(traza)));
        division.setResizeWeight(0.72);
        division.setBorder(null);
        division.setOpaque(false);
        add(division, BorderLayout.CENTER);
    }

    public void nuevaPartida() {
        nuevaPartida(new Random().nextLong());
    }

    /** Con semilla, la partida es reproducible (misma elección de secretos y de arriesgues). */
    public void nuevaPartida(long semilla) {
        detener();
        Random azar = new Random(semilla);
        Tablero tablero = OrganizadorTablero.organizar();
        Personaje secretoA = tablero.obtenerPorId(1 + azar.nextInt(tablero.tamanio()));
        Personaje secretoB;
        do {
            secretoB = tablero.obtenerPorId(1 + azar.nextInt(tablero.tamanio()));
        } while (secretoB == secretoA);

        maquinaA = new JugadorMaquina("Máquina A", tablero, secretoA, azar);
        maquinaB = new JugadorMaquina("Máquina B", tablero, secretoB, azar);
        partida = new Partida(tablero, maquinaA, maquinaB);

        ladoA.preparar(tablero, maquinaA, secretoA, secretoB);
        ladoB.preparar(tablero, maquinaB, secretoB, secretoA);
        traza.setText("Tablero ordenado con MergeSort por nombre (IDs 1 a 23).\n"
                + "Máquina A tiene a " + secretoA.getNombre() + "; Máquina B tiene a " + secretoB.getNombre() + ".\n"
                + "Ninguna ve el secreto de la otra: sólo le hace preguntas a través de la interfaz Oraculo.\n"
                + "Empieza la Máquina A.\n\n");
        botonPaso.setEnabled(true);
        botonAuto.setEnabled(true);
        barra.setEstado("Turno 1 · Máquina A. Avanzá turno por turno o usá el modo automático.");
        revalidate();
        repaint();
    }

    public void paso() {
        if (partida == null || partida.terminada()) {
            return;
        }
        Jugador quienJuega = partida.getTurno();
        ResultadoTurno r = partida.jugarTurnoMaquina();
        traza.append(r.describir(true) + "\n");
        traza.setCaretPosition(traza.getDocument().getLength());
        (quienJuega == maquinaA ? ladoA : ladoB).actualizar(r);
        (quienJuega == maquinaA ? ladoB : ladoA).actualizar(null);

        if (partida.terminada()) {
            detener();
            String mensaje = "Ganó " + partida.getGanador().getNombre() + " en el turno " + r.getNumero()
                    + ": adivinó a " + r.getArriesgado().getNombre() + ".";
            traza.append("════ " + mensaje + " ════\n");
            barra.setEstado(mensaje);
            botonPaso.setEnabled(false);
            botonAuto.setEnabled(false);
        } else {
            barra.setEstado("Turno " + (r.getNumero() + 1) + " · " + partida.getTurno().getNombre());
        }
    }

    private void alternarAutomatico() {
        if (automatico.isRunning()) {
            detener();
        } else if (partida != null && !partida.terminada()) {
            automatico.start();
            botonAuto.setText("Pausa");
            botonAuto.repaint();
        }
    }

    public void detener() {
        automatico.stop();
        botonAuto.setText("Automático");
        botonAuto.repaint();
    }

    /** Un lado de la mesa: el tablero de una máquina con sus candidatos sobre el secreto del rival. */
    private static class Lado {
        private final JPanel panel = new JPanel(new BorderLayout(0, 8));
        private final JPanel cabecera = new JPanel(new BorderLayout(10, 0));
        private final JLabel titulo = Estilo.etiqueta("", Estilo.titulo(17f), Estilo.TINTA);
        private final JLabel detalle = Estilo.etiqueta("", Estilo.texto(12.5f), Estilo.TINTA_SUAVE);
        private final JLabel contador = Estilo.etiqueta("", Estilo.negrita(13f), Estilo.ACENTO);
        private PanelTablero tablero;
        private JugadorMaquina maquina;
        private int preguntas;

        Lado() {
            panel.setBackground(Estilo.PAPEL);
            panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(Estilo.BORDE), Estilo.margen(10)));
            cabecera.setOpaque(false);
        }

        void preparar(Tablero t, JugadorMaquina maquina, Personaje propio, Personaje objetivo) {
            this.maquina = maquina;
            this.preguntas = 0;
            panel.removeAll();
            cabecera.removeAll();
            TarjetaPersonaje carta = new TarjetaPersonaje(propio, 0.42);
            carta.setSecreta(true);
            cabecera.add(carta, BorderLayout.WEST);
            JPanel textos = new JPanel(new GridLayout(3, 1));
            textos.setOpaque(false);
            titulo.setText(maquina.getNombre() + " — su personaje: " + propio.getNombre());
            detalle.setText("Busca a " + objetivo.getNombre() + " (marcado en dorado, sólo lo ve el espectador)");
            textos.add(titulo);
            textos.add(detalle);
            textos.add(contador);
            cabecera.add(textos, BorderLayout.CENTER);
            panel.add(cabecera, BorderLayout.NORTH);

            tablero = new PanelTablero(t.getPersonajes(), 0.78, 6);
            tablero.marcarSecreto(objetivo);
            JPanel envoltorio = new JPanel(new BorderLayout());
            envoltorio.setOpaque(false);
            envoltorio.add(tablero, BorderLayout.NORTH);
            panel.add(envoltorio, BorderLayout.CENTER);
            actualizar(null);
        }

        void actualizar(ResultadoTurno r) {
            if (r != null && r.getAccion() == ResultadoTurno.Accion.PREGUNTA) {
                preguntas++;
            }
            tablero.mostrarCandidatos(maquina.getCandidatos(), r == null ? null : r.getDescartados());
            contador.setText("Candidatos: " + maquina.getCandidatos().size() + " de 23   ·   preguntas hechas: " + preguntas);
        }
    }
}
