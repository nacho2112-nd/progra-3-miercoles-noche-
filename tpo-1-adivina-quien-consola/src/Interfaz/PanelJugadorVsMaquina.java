package Interfaz;

import Functional_Things.GeneradorPersonajes;
import Functional_Things.IndiceAtributos;
import Juego.AdivinaQuien;
import Juego.Marcador;
import Jugadores.Jugador;
import Jugadores.JugadorHumano;
import Jugadores.JugadorMaquina;
import Things.Personajes;
import Things.Pregunta;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Campaña de 2 niveles. El humano elige su personaje con un clic (una sola vez) y juega contra
// la Maquina 1; si le gana, sigue contra la Maquina 2, que arranca con lo que pregunto la 1.
public class PanelJugadorVsMaquina extends JPanel {

    private final Runnable alVolver;
    private final Marcador marcador;
    private final BarraSuperior barra = new BarraSuperior("Jugador vs Máquina");
    private final JPanel zonaTablero = new JPanel(new BorderLayout());
    private final JPanel zonaTuPersonaje = new JPanel(new BorderLayout(12, 0));
    private final JPanel zonaMaquina = new JPanel(new BorderLayout(0, 6));
    private final JPanel grillaPreguntas = new JPanel(new GridLayout(0, 2, 6, 6));
    private final JLabel estadoMaquina = Estilo.etiqueta("", Estilo.texto(13f), Estilo.TINTA);
    private final Map<Pregunta, BotonPlano> botonesPregunta = new LinkedHashMap<>();
    private final JComboBox<String> comboArriesgue = new JComboBox<>();
    private final BotonPlano botonArriesgar = BotonPlano.primario("Arriesgar");
    private final BotonPlano botonConfirmar = BotonPlano.primario("Confirmar");
    private final JCheckBox verRazonamiento = new JCheckBox("Ver qué piensa la máquina", true);
    private final JTextArea traza = Estilo.areaTraza();

    private List<Personajes> tablero;
    private IndiceAtributos indice;
    private PanelTablero tableroHumano, tableroMaquina;
    private Personajes elegido;
    private String nombre;
    private JugadorHumano humano;
    private JugadorMaquina maquina, maquina1;
    private int nivel;
    private Timer temporizador;

    public PanelJugadorVsMaquina(Runnable alVolver, Marcador marcador) {
        super(new BorderLayout());
        this.alVolver = alVolver;
        this.marcador = marcador;
        setBackground(Estilo.FONDO);

        BotonPlano nueva = BotonPlano.secundario("Nueva partida");
        nueva.addActionListener(e -> nuevaPartida());
        BotonPlano volver = BotonPlano.secundario("← Menú");
        volver.addActionListener(e -> alVolver.run());
        barra.agregarBoton(nueva);
        barra.agregarBoton(volver);
        botonConfirmar.addActionListener(e -> confirmar());
        botonArriesgar.addActionListener(e -> arriesgar());
        add(barra, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(14, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(Estilo.margen(14));
        JPanel izquierda = new JPanel(new BorderLayout(0, 12));
        izquierda.setOpaque(false);
        zonaTablero.setOpaque(false);
        izquierda.add(Estilo.seccion("Tu tablero", zonaTablero), BorderLayout.CENTER);
        JPanel registro = new JPanel(new BorderLayout(0, 4));
        registro.setOpaque(false);
        verRazonamiento.setOpaque(false);
        verRazonamiento.setFont(Estilo.texto(12.5f));
        registro.add(verRazonamiento, BorderLayout.NORTH);
        registro.add(Estilo.scroll(traza), BorderLayout.CENTER);
        JPanel seccionRegistro = Estilo.seccion("Historial", registro);
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
        grillaPreguntas.setOpaque(false);
        lateral.add(Estilo.seccion("Preguntas", grillaPreguntas));
        lateral.add(Box.createVerticalStrut(10));
        JPanel filaArriesgue = new JPanel(new BorderLayout(8, 0));
        filaArriesgue.setOpaque(false);
        comboArriesgue.setFont(Estilo.texto(13f));
        filaArriesgue.add(comboArriesgue, BorderLayout.CENTER);
        filaArriesgue.add(botonArriesgar, BorderLayout.EAST);
        lateral.add(Estilo.seccion("Arriesgar", filaArriesgue));
        lateral.add(Box.createVerticalStrut(10));
        zonaMaquina.setOpaque(false);
        lateral.add(Estilo.seccion("La máquina — sus candidatos para tu personaje", zonaMaquina));
        return lateral;
    }

    // Tablero nuevo (la maquina lo ordena con MergeSort) y etapa de elegir personaje
    public void nuevaPartida() {
        detener();
        tablero = JugadorMaquina.armarTablero(GeneradorPersonajes.crear_Lista());
        indice = new IndiceAtributos(tablero);
        elegido = null;
        humano = null;
        maquina = maquina1 = null;
        nivel = 0;
        traza.setText("");
        barra.setTitulo("Jugador vs Máquina");

        botonesPregunta.clear();
        grillaPreguntas.removeAll();
        for (Pregunta p : indice.preguntasPosibles()) {
            BotonPlano b = BotonPlano.secundario(p.toString());
            b.setFont(Estilo.negrita(12.5f));
            b.addActionListener(e -> jugadaHumana(p));
            botonesPregunta.put(p, b);
            grillaPreguntas.add(b);
        }

        tableroHumano = new PanelTablero(tablero, 0.88, 6);
        tableroHumano.setAlClic(this::elegir);
        zonaTablero.removeAll();
        zonaTablero.add(tableroHumano, BorderLayout.NORTH);
        tableroMaquina = new PanelTablero(tablero, 0.4, 6);
        zonaMaquina.removeAll();
        zonaMaquina.add(estadoMaquina, BorderLayout.NORTH);
        zonaMaquina.add(tableroMaquina, BorderLayout.CENTER);
        estadoMaquina.setText("Todavía no empezó: elige su personaje al azar cuando confirmes el tuyo.");

        mostrarEleccion();
        comboArriesgue.removeAllItems();
        setControlesActivos(false);
        barra.setEstado("Hacé clic en la carta del personaje que querés ser. No lo vas a poder cambiar.");
        revalidate();
        repaint();
    }

    private void elegir(Personajes p) {
        if (humano != null) return;
        elegido = p;
        tableroHumano.marcarSeleccion(p);
        mostrarEleccion();
    }

    private void mostrarEleccion() {
        zonaTuPersonaje.removeAll();
        if (elegido == null) {
            zonaTuPersonaje.add(new JLabel("<html>Todavía no elegiste. Hacé clic en una carta del tablero.</html>"));
        } else {
            zonaTuPersonaje.add(new TarjetaPersonaje(elegido, 0.8), BorderLayout.WEST);
            JPanel textos = new JPanel(new GridLayout(0, 1));
            textos.setOpaque(false);
            textos.add(Estilo.etiqueta(elegido.get_Nombre(), Estilo.titulo(18f), Estilo.TINTA));
            textos.add(Estilo.etiqueta(TarjetaPersonaje.descripcion(elegido), Estilo.texto(12.5f), Estilo.TINTA_SUAVE));
            if (humano == null) {
                JPanel envoltorio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                envoltorio.setOpaque(false);
                botonConfirmar.setPreferredSize(new Dimension(220, 34));
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

    private void confirmar() {
        if (elegido == null || humano != null) return;
        String escrito = JOptionPane.showInputDialog(this, "¿Cómo te llamás? (para el marcador)", "Jugador");
        if (escrito == null) return;
        nombre = escrito.replace(";", "").trim().isEmpty() ? "Anónimo" : escrito.replace(";", "").trim();
        humano = new JugadorHumano(nombre, tablero, indice);
        humano.elegirSecreto(List.of(elegido));   // una sola vez para los 2 niveles
        tableroHumano.setAlClic(null);
        tableroHumano.marcarSeleccion(null);
        mostrarEleccion();
        traza.append("La máquina armó el tablero: MergeSort por nombre, IDs 1 a 23.\n");
        empezarNivel(1, List.of());
    }

    private void empezarNivel(int n, List<Jugador.Registro> heredado) {
        nivel = n;
        maquina = new JugadorMaquina(n, tablero, indice, heredado, false);
        if (n == 1) {
            maquina1 = maquina;
            maquina.elegirSecreto(tablero);
        } else {
            maquina.elegirSecreto(AdivinaQuien.sinElDe(maquina1, tablero));
            humano.reiniciar();
        }
        barra.setTitulo("Jugador vs Máquina · Nivel " + n + (n == 1 ? " (pregunta al azar)" : " (greedy)"));
        traza.append("\n══ NIVEL " + n + ": contra la " + maquina.getNombre() + " ══\n");
        if (n == 2) {
            traza.append("Se acuerda de las " + heredado.size() + " preguntas de la Máquina 1: arranca con "
                    + maquina.getCandidatos().size() + " candidatos en vez de " + tablero.size() + ".\n");
        }
        traza.append("Eligió su personaje al azar. Empezás vos.\n\n");
        tableroHumano.marcarSecreto(null);
        tableroMaquina.marcarSecreto(elegido);
        actualizarVistas(null, null);
        setControlesActivos(true);
        barra.setEstado("Tu turno: hacé una pregunta o arriesgá.");
    }

    private void arriesgar() {
        Object nombreElegido = comboArriesgue.getSelectedItem();
        tablero.stream().filter(p -> p.get_Nombre().equals(nombreElegido)).findFirst()
                .ifPresent(p -> jugadaHumana(Pregunta.arriesgue(p)));
    }

    private void jugadaHumana(Pregunta pregunta) {
        if (humano == null || !botonArriesgar.isEnabled()) return;
        List<Personajes> antes = new ArrayList<>(humano.getCandidatos());
        boolean gano = humano.jugar(pregunta, maquina);
        traza.append("Vos: " + pregunta + " → " + (humano.getHistorial().getLast().respuesta() ? "SÍ" : "NO") + "\n");
        antes.removeAll(humano.getCandidatos());
        actualizarVistas(antes, null);
        if (gano) {
            terminarNivel(true);
            return;
        }
        setControlesActivos(false);
        barra.setEstado("Turno de la " + maquina.getNombre() + "…");
        temporizador = new Timer(900, e -> turnoMaquina());
        temporizador.setRepeats(false);
        temporizador.start();
    }

    private void turnoMaquina() {
        List<Personajes> antes = new ArrayList<>(maquina.getCandidatos());
        boolean gano = maquina.jugarTurno(humano);
        Jugador.Registro r = maquina.getHistorial().getLast();
        if (verRazonamiento.isSelected()) traza.append(maquina.getRazonamiento());
        traza.append(maquina.getNombre() + ": " + r.pregunta() + " → " + (r.respuesta() ? "SÍ" : "NO")
                + " (le quedan " + maquina.getCandidatos().size() + ")\n\n");
        antes.removeAll(maquina.getCandidatos());
        actualizarVistas(null, antes);
        if (gano) {
            terminarNivel(false);
        } else {
            setControlesActivos(true);
            barra.setEstado("Tu turno: hacé una pregunta o arriesgá.");
        }
    }

    private void terminarNivel(boolean ganaste) {
        setControlesActivos(false);
        Personajes secretoMaquina = AdivinaQuien.secretoDe(maquina, tablero);
        tableroHumano.marcarSecreto(secretoMaquina);
        if (ganaste && nivel == 1) {
            String mensaje = "¡Le ganaste a la Máquina 1! Era " + secretoMaquina.get_Nombre()
                    + ". Pasás al nivel 2 con el mismo personaje.";
            traza.append("════ " + mensaje + " ════\n");
            JOptionPane.showMessageDialog(this, mensaje, "Nivel 1 superado", JOptionPane.INFORMATION_MESSAGE);
            empezarNivel(2, maquina1.getHistorial());
            return;
        }
        int ganadas = marcador.registrar(nombre, nivel, ganaste);
        String mensaje = (ganaste
                ? "¡Le ganaste a las dos máquinas! Era " + secretoMaquina.get_Nombre() + "."
                : "Ganó la " + maquina.getNombre() + ": adivinó que eras " + elegido.get_Nombre()
                        + ". Su personaje era " + secretoMaquina.get_Nombre() + ".")
                + " " + nombre + " lleva " + ganadas + " partidas ganadas.";
        traza.append("════ " + mensaje + " ════\n");
        traza.setCaretPosition(traza.getDocument().getLength());
        barra.setEstado(mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarVistas(List<Personajes> descartadosHumano, List<Personajes> descartadosMaquina) {
        tableroHumano.mostrarCandidatos(humano.getCandidatos(), descartadosHumano);
        tableroMaquina.mostrarCandidatos(maquina.getCandidatos(), descartadosMaquina);
        estadoMaquina.setText("A la " + maquina.getNombre() + " le quedan " + maquina.getCandidatos().size()
                + " candidatos. A vos, " + humano.getCandidatos().size() + ".");
        comboArriesgue.removeAllItems();
        humano.getCandidatos().forEach(p -> comboArriesgue.addItem(p.get_Nombre()));
        traza.setCaretPosition(traza.getDocument().getLength());
    }

    // Una pregunta se habilita solo si todavia divide a tus candidatos (si no, la respuesta ya se sabe)
    private void setControlesActivos(boolean activos) {
        botonesPregunta.forEach((p, b) -> {
            long si = humano == null ? 0 : humano.getCandidatos().stream().filter(p::aplicaA).count();
            b.setEnabled(activos && si > 0 && si < humano.getCandidatos().size());
        });
        comboArriesgue.setEnabled(activos);
        botonArriesgar.setEnabled(activos);
    }

    public void detener() {
        if (temporizador != null) temporizador.stop();
    }
}
