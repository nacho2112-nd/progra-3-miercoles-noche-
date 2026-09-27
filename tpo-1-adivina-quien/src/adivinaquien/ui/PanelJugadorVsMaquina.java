package adivinaquien.ui;

import adivinaquien.algoritmos.BusquedaBinaria;
import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;
import adivinaquien.motor.JugadorHumano;
import adivinaquien.motor.JugadorMaquina;
import adivinaquien.motor.OrganizadorTablero;
import adivinaquien.motor.Partida;
import adivinaquien.motor.ResultadoTurno;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Modo Jugador vs Máquina. El humano elige su personaje haciendo clic en una carta; la máquina
 * elige el suyo al azar. Empieza el humano y después se alternan.
 */
public class PanelJugadorVsMaquina extends JPanel {

    private final BarraSuperior barra = new BarraSuperior("Jugador vs Máquina", "");
    private final JPanel zonaTablero = new JPanel(new BorderLayout());
    private final JPanel zonaTuPersonaje = new JPanel(new BorderLayout(12, 0));
    private final JPanel zonaMaquina = new JPanel(new BorderLayout(0, 6));
    private final JLabel estadoMaquina = Estilo.etiqueta("", Estilo.texto(13f), Estilo.TINTA);
    private final Map<Filtro, BotonPlano> botonesFiltro = new EnumMap<>(Filtro.class);
    private final JComboBox<String> comboArriesgue = new JComboBox<>();
    private final BotonPlano botonArriesgar = BotonPlano.primario("Arriesgar");
    private final BotonPlano botonConfirmar = BotonPlano.primario("Jugar con este personaje");
    private final JCheckBox verRazonamiento = new JCheckBox("Mostrar el razonamiento de la máquina", true);
    private final JTextArea traza = Estilo.areaTraza();

    private Tablero tablero;
    private PanelTablero tableroHumano;
    private PanelTablero tableroMaquina;
    private Personaje elegido;
    private JugadorHumano humano;
    private JugadorMaquina maquina;
    private Partida partida;
    private Timer temporizador;
    private int demoraMaquinaMs = 900;
    private boolean mostrarDialogos = true;

    public PanelJugadorVsMaquina(Runnable alVolver) {
        super(new BorderLayout());
        setBackground(Estilo.FONDO);

        BotonPlano nueva = BotonPlano.secundario("Nueva partida");
        nueva.addActionListener(e -> nuevaPartida());
        BotonPlano volver = BotonPlano.secundario("← Menú");
        volver.addActionListener(e -> {
            detener();
            alVolver.run();
        });
        barra.agregarBoton(nueva);
        barra.agregarBoton(volver);
        botonConfirmar.addActionListener(e -> confirmar());
        add(barra, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(14, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(Estilo.margen(14));

        JPanel izquierda = new JPanel(new BorderLayout(0, 12));
        izquierda.setOpaque(false);
        zonaTablero.setOpaque(false);
        izquierda.add(Estilo.seccion("Tu tablero — candidatos para el personaje de la máquina", zonaTablero), BorderLayout.CENTER);
        JPanel registro = new JPanel(new BorderLayout(0, 4));
        registro.setOpaque(false);
        verRazonamiento.setOpaque(false);
        verRazonamiento.setFont(Estilo.texto(12.5f));
        registro.add(verRazonamiento, BorderLayout.NORTH);
        registro.add(Estilo.scroll(traza), BorderLayout.CENTER);
        JPanel seccionRegistro = Estilo.seccion("Registro de la partida", registro);
        seccionRegistro.setPreferredSize(new Dimension(100, 220));
        izquierda.add(seccionRegistro, BorderLayout.SOUTH);
        cuerpo.add(izquierda, BorderLayout.CENTER);
        cuerpo.add(construirLateral(), BorderLayout.EAST);
        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel construirLateral() {
        JPanel lateral = new JPanel();
        lateral.setOpaque(false);
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setPreferredSize(new Dimension(430, 100));

        zonaTuPersonaje.setOpaque(false);
        lateral.add(Estilo.seccion("Tu personaje", zonaTuPersonaje));
        lateral.add(Box.createVerticalStrut(10));

        JPanel grillaFiltros = new JPanel(new GridLayout(0, 2, 6, 6));
        grillaFiltros.setOpaque(false);
        for (Filtro f : Filtro.values()) {
            BotonPlano b = BotonPlano.secundario(f.getPregunta());
            b.setFont(Estilo.negrita(12.5f));
            b.addActionListener(e -> preguntar(f));
            botonesFiltro.put(f, b);
            grillaFiltros.add(b);
        }
        lateral.add(Estilo.seccion("Preguntar", grillaFiltros));
        lateral.add(Box.createVerticalStrut(10));

        JPanel filaArriesgue = new JPanel(new BorderLayout(8, 0));
        filaArriesgue.setOpaque(false);
        comboArriesgue.setEditable(true);
        comboArriesgue.setFont(Estilo.texto(13f));
        filaArriesgue.add(comboArriesgue, BorderLayout.CENTER);
        botonArriesgar.addActionListener(e -> arriesgar(String.valueOf(comboArriesgue.getEditor().getItem())));
        filaArriesgue.add(botonArriesgar, BorderLayout.EAST);
        lateral.add(Estilo.seccion("Arriesgar (búsqueda binaria por nombre)", filaArriesgue));
        lateral.add(Box.createVerticalStrut(10));

        zonaMaquina.setOpaque(false);
        lateral.add(Estilo.seccion("La máquina — sus candidatos para tu personaje", zonaMaquina));
        return lateral;
    }

    /** Arma un tablero nuevo y pasa a la etapa de elegir personaje. */
    public void nuevaPartida() {
        detener();
        tablero = OrganizadorTablero.organizar();
        elegido = null;
        humano = null;
        maquina = null;
        partida = null;
        traza.setText("");

        tableroHumano = new PanelTablero(tablero.getPersonajes(), 0.88, 6);
        tableroHumano.setAlClic(this::elegir);
        zonaTablero.removeAll();
        zonaTablero.add(tableroHumano, BorderLayout.NORTH);

        tableroMaquina = new PanelTablero(tablero.getPersonajes(), 0.4, 6);
        zonaMaquina.removeAll();
        zonaMaquina.add(estadoMaquina, BorderLayout.NORTH);
        zonaMaquina.add(tableroMaquina, BorderLayout.CENTER);
        estadoMaquina.setText("Todavía no empezó: elige su personaje al azar cuando confirmes el tuyo.");

        mostrarEleccion();
        setControlesActivos(false);
        comboArriesgue.removeAllItems();
        barra.setEstado("Hacé clic en la carta del personaje que querés ser.");
        revalidate();
        repaint();
    }

    public void elegir(Personaje p) {
        if (partida != null) {
            return;
        }
        elegido = p;
        tableroHumano.marcarSeleccion(p);
        mostrarEleccion();
    }

    private void mostrarEleccion() {
        zonaTuPersonaje.removeAll();
        if (elegido == null) {
            zonaTuPersonaje.add(new JLabel("<html>Todavía no elegiste. Hacé clic en una carta del tablero.</html>"),
                    BorderLayout.CENTER);
        } else {
            zonaTuPersonaje.add(new TarjetaPersonaje(elegido, 0.8), BorderLayout.WEST);
            JPanel textos = new JPanel(new GridLayout(0, 1));
            textos.setOpaque(false);
            textos.add(Estilo.etiqueta(elegido.getNombre(), Estilo.titulo(18f), Estilo.TINTA));
            textos.add(Estilo.etiqueta(elegido.descripcion(), Estilo.texto(12.5f), Estilo.TINTA_SUAVE));
            if (partida == null) {
                botonConfirmar.setPreferredSize(new Dimension(220, 34));
                JPanel envoltorio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                envoltorio.setOpaque(false);
                envoltorio.add(botonConfirmar);
                textos.add(envoltorio);
            } else {
                textos.add(Estilo.etiqueta("La máquina no puede ver este dato.", Estilo.texto(12f), Estilo.VERDE));
            }
            zonaTuPersonaje.add(textos, BorderLayout.CENTER);
        }
        zonaTuPersonaje.revalidate();
        zonaTuPersonaje.repaint();
    }

    public void confirmar() {
        if (elegido == null || partida != null) {
            return;
        }
        humano = new JugadorHumano("Vos", tablero, elegido);
        maquina = new JugadorMaquina("Máquina", tablero, new Random());
        partida = new Partida(tablero, humano, maquina);

        tableroHumano.setAlClic(null);
        tableroHumano.marcarSeleccion(null);
        tableroMaquina.marcarSecreto(elegido);
        mostrarEleccion();
        actualizarVistas(null, null);
        traza.append("La máquina armó el tablero (MergeSort por nombre, IDs 1 a 23) y eligió su personaje al azar.\n"
                + "Empezás vos.\n\n");
        setControlesActivos(true);
        barra.setEstado("Tu turno: hacé una pregunta o arriesgá.");
    }

    public void preguntar(Filtro filtro) {
        if (partida == null || partida.terminada() || partida.getTurno() != humano) {
            return;
        }
        despuesDeJugadaHumana(partida.preguntar(filtro));
    }

    public void arriesgar(String nombre) {
        if (partida == null || partida.terminada() || partida.getTurno() != humano) {
            return;
        }
        int indice = BusquedaBinaria.buscarPorNombre(tablero.getPersonajes(), nombre);
        if (indice < 0) {
            if (mostrarDialogos) {
                JOptionPane.showMessageDialog(this, "No hay ningún personaje llamado \"" + nombre + "\".",
                        "Arriesgue inválido", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        despuesDeJugadaHumana(partida.arriesgar(tablero.getPersonajes().get(indice)));
    }

    private void despuesDeJugadaHumana(ResultadoTurno r) {
        traza.append(r.describir(false) + "\n");
        actualizarVistas(r.getDescartados(), null);
        if (partida.terminada()) {
            terminar();
            return;
        }
        setControlesActivos(false);
        barra.setEstado("Turno de la máquina…");
        if (demoraMaquinaMs <= 0) {
            turnoMaquina();
        } else {
            temporizador = new Timer(demoraMaquinaMs, e -> turnoMaquina());
            temporizador.setRepeats(false);
            temporizador.start();
        }
    }

    private void turnoMaquina() {
        ResultadoTurno r = partida.jugarTurnoMaquina();
        traza.append(r.describir(verRazonamiento.isSelected()) + "\n");
        actualizarVistas(null, r.getDescartados());
        if (partida.terminada()) {
            terminar();
        } else {
            setControlesActivos(true);
            barra.setEstado("Tu turno: hacé una pregunta o arriesgá.");
        }
    }

    private void actualizarVistas(List<Personaje> descartadosHumano, List<Personaje> descartadosMaquina) {
        tableroHumano.mostrarCandidatos(humano.getCandidatos(), descartadosHumano);
        tableroMaquina.mostrarCandidatos(maquina.getCandidatos(), descartadosMaquina);
        estadoMaquina.setText("A la máquina le quedan " + maquina.getCandidatos().size()
                + " candidatos. A vos, " + humano.getCandidatos().size() + ".");
        String escrito = String.valueOf(comboArriesgue.getEditor().getItem());
        comboArriesgue.removeAllItems();
        for (Personaje p : humano.getCandidatos()) {
            comboArriesgue.addItem(p.getNombre());
        }
        if (humano.getCandidatos().size() > 1) {
            comboArriesgue.setSelectedItem(escrito.isEmpty() ? null : escrito);
        }
        traza.setCaretPosition(traza.getDocument().getLength());
    }

    private void terminar() {
        setControlesActivos(false);
        Personaje secretoMaquina = partida.revelarSecreto(maquina);
        tableroHumano.marcarSecreto(secretoMaquina);
        boolean ganaste = partida.getGanador() == humano;
        String mensaje = ganaste
                ? "¡Ganaste! El personaje de la máquina era " + secretoMaquina.getNombre() + "."
                : "Ganó la máquina: adivinó que eras " + elegido.getNombre() + ". Su personaje era "
                + secretoMaquina.getNombre() + ".";
        traza.append("════ " + mensaje + " ════\n");
        traza.setCaretPosition(traza.getDocument().getLength());
        barra.setEstado(mensaje);
        if (mostrarDialogos) {
            JOptionPane.showMessageDialog(this, mensaje, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setControlesActivos(boolean activos) {
        for (Map.Entry<Filtro, BotonPlano> e : botonesFiltro.entrySet()) {
            e.getValue().setEnabled(activos && humano != null && !humano.estaResuelto(e.getKey()));
        }
        comboArriesgue.setEnabled(activos);
        botonArriesgar.setEnabled(activos);
    }

    public void detener() {
        if (temporizador != null) {
            temporizador.stop();
        }
    }

    /** Para generar capturas sin esperas ni ventanas emergentes. */
    public void setModoDemostracion(boolean activo) {
        demoraMaquinaMs = activo ? 0 : 900;
        mostrarDialogos = !activo;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setVerRazonamiento(boolean ver) {
        verRazonamiento.setSelected(ver);
    }
}
