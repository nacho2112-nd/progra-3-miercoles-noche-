package Interfaz;

import Functional_Things.GeneradorPersonajes;
import Functional_Things.IndiceAtributos;
import Juego.AdivinaQuien;
import Jugadores.Jugador;
import Jugadores.JugadorMaquina;
import Things.Personajes;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

// Maquina 1 (al azar) contra Maquina 2 (greedy). Se ve todo su proceso: los candidatos, la tabla
// SI/NO del Greedy, la pregunta y los descartes. El espectador ve los dos secretos; las maquinas, no.
public class PanelMaquinaVsMaquina extends JPanel {

    private final BarraSuperior barra = new BarraSuperior("Máquina vs Máquina");
    private final Lado ladoA = new Lado();
    private final Lado ladoB = new Lado();
    private final JTextArea traza = Estilo.areaTraza();
    private final BotonPlano botonPaso = BotonPlano.primario("Siguiente turno");
    private final BotonPlano botonAuto = BotonPlano.secundario("Automático");
    private final JSlider velocidad = new JSlider(200, 2500, 1200);
    private final Timer automatico;

    private JugadorMaquina maquinaA, maquinaB;
    private int turno;
    private boolean terminada;

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
        detener();
        List<Personajes> tablero = JugadorMaquina.armarTablero(GeneradorPersonajes.crear_Lista());
        IndiceAtributos indice = new IndiceAtributos(tablero);
        maquinaA = new JugadorMaquina(1, tablero, indice, List.of(), false);
        maquinaA.elegirSecreto(tablero);
        maquinaB = new JugadorMaquina(2, tablero, indice, List.of(), false);
        maquinaB.elegirSecreto(AdivinaQuien.sinElDe(maquinaA, tablero));
        Personajes secretoA = AdivinaQuien.secretoDe(maquinaA, tablero);
        Personajes secretoB = AdivinaQuien.secretoDe(maquinaB, tablero);
        turno = 1;
        terminada = false;

        ladoA.preparar(tablero, maquinaA, "pregunta al azar", secretoA, secretoB);
        ladoB.preparar(tablero, maquinaB, "greedy", secretoB, secretoA);
        traza.setText("Tablero ordenado con MergeSort por nombre (IDs 1 a 23).\n"
                + "Máquina 1 tiene a " + secretoA.get_Nombre() + "; Máquina 2 tiene a " + secretoB.get_Nombre() + ".\n"
                + "Ninguna ve el secreto de la otra: sólo le pregunta a través de la interfaz Oraculo.\n"
                + "Empieza la Máquina 1.\n\n");
        botonPaso.setEnabled(true);
        botonAuto.setEnabled(true);
        barra.setEstado("Turno 1 · Máquina 1. Avanzá turno por turno o usá el modo automático.");
        revalidate();
        repaint();
    }

    private void paso() {
        if (maquinaA == null || terminada) return;
        JugadorMaquina actual = turno % 2 == 1 ? maquinaA : maquinaB;
        Jugador rival = actual == maquinaA ? maquinaB : maquinaA;
        List<Personajes> antes = new ArrayList<>(actual.getCandidatos());
        boolean gano = actual.jugarTurno(rival);
        Jugador.Registro r = actual.getHistorial().getLast();
        antes.removeAll(actual.getCandidatos());

        traza.append("── Turno " + turno + " · " + actual.getNombre() + "\n" + actual.getRazonamiento()
                + actual.getNombre() + ": " + r.pregunta() + " → " + (r.respuesta() ? "SÍ" : "NO")
                + " (le quedan " + actual.getCandidatos().size() + ")\n\n");
        traza.setCaretPosition(traza.getDocument().getLength());
        (actual == maquinaA ? ladoA : ladoB).actualizar(antes);
        (actual == maquinaA ? ladoB : ladoA).actualizar(null);

        if (gano) {
            terminada = true;
            detener();
            String mensaje = "Ganó la " + actual.getNombre() + " en el turno " + turno
                    + ": adivinó a " + r.pregunta().valor() + ".";
            traza.append("════ " + mensaje + " ════\n");
            barra.setEstado(mensaje);
            botonPaso.setEnabled(false);
            botonAuto.setEnabled(false);
        } else {
            turno++;
            barra.setEstado("Turno " + turno + " · " + (turno % 2 == 1 ? maquinaA : maquinaB).getNombre());
        }
    }

    private void alternarAutomatico() {
        if (automatico.isRunning()) {
            detener();
        } else if (maquinaA != null && !terminada) {
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

    // Un lado de la mesa: el tablero de una maquina con sus candidatos sobre el secreto del rival
    private static class Lado {
        private final JPanel panel = new JPanel(new BorderLayout(0, 8));
        private final JLabel contador = Estilo.etiqueta("", Estilo.negrita(13f), Estilo.ACENTO);
        private PanelTablero tablero;
        private JugadorMaquina maquina;

        Lado() {
            panel.setBackground(Estilo.PAPEL);
            panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.BORDE), Estilo.margen(10)));
        }

        void preparar(List<Personajes> personajes, JugadorMaquina maquina, String estilo, Personajes propio, Personajes objetivo) {
            this.maquina = maquina;
            panel.removeAll();
            JPanel cabecera = new JPanel(new BorderLayout(10, 0));
            cabecera.setOpaque(false);
            TarjetaPersonaje carta = new TarjetaPersonaje(propio, 0.42);
            carta.setSecreta(true);
            cabecera.add(carta, BorderLayout.WEST);
            JPanel textos = new JPanel(new GridLayout(3, 1));
            textos.setOpaque(false);
            textos.add(Estilo.etiqueta(maquina.getNombre() + " (" + estilo + ") — su personaje: " + propio.get_Nombre(),
                    Estilo.titulo(17f), Estilo.TINTA));
            textos.add(Estilo.etiqueta("Busca a " + objetivo.get_Nombre() + " (marcado en dorado, sólo lo ve el espectador)",
                    Estilo.texto(12.5f), Estilo.TINTA_SUAVE));
            textos.add(contador);
            cabecera.add(textos, BorderLayout.CENTER);
            panel.add(cabecera, BorderLayout.NORTH);

            tablero = new PanelTablero(personajes, 0.78, 6);
            tablero.marcarSecreto(objetivo);
            JPanel envoltorio = new JPanel(new BorderLayout());
            envoltorio.setOpaque(false);
            envoltorio.add(tablero, BorderLayout.NORTH);
            panel.add(envoltorio, BorderLayout.CENTER);
            actualizar(null);
        }

        void actualizar(List<Personajes> recienDescartados) {
            tablero.mostrarCandidatos(maquina.getCandidatos(), recienDescartados);
            contador.setText("Candidatos: " + maquina.getCandidatos().size() + " de 23   ·   preguntas hechas: "
                    + maquina.getHistorial().size());
        }
    }
}
