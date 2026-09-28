package Interfaz;

import Juego.Marcador;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;

// La ventana: el menu y una pantalla por modo, que se intercambian con un CardLayout.
public class VentanaPrincipal extends JFrame {

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Marcador marcador = new Marcador();
    private final PanelJugadorVsMaquina panelJvm = new PanelJugadorVsMaquina(this::irAlInicio, marcador);
    private final PanelMaquinaVsMaquina panelMvm = new PanelMaquinaVsMaquina(this::irAlInicio);

    public VentanaPrincipal() {
        super("Adivina Quién");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 780));
        setPreferredSize(new Dimension(1380, 900));
        contenido.add(menu(), "inicio");
        contenido.add(panelJvm, "jvm");
        contenido.add(panelMvm, "mvm");
        setContentPane(contenido);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel menu() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilo.FONDO);
        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        JLabel titulo = Estilo.etiqueta("Adivina Quién", Estilo.titulo(44f), Estilo.TINTA);
        JLabel subtitulo = Estilo.etiqueta("Elegí una opción", Estilo.texto(15f), Estilo.TINTA_SUAVE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        columna.add(titulo);
        columna.add(subtitulo);
        columna.add(Box.createVerticalStrut(28));
        columna.add(boton(BotonPlano.primario("Jugar (2 niveles)"), () -> {
            panelJvm.nuevaPartida();
            cartas.show(contenido, "jvm");
        }));
        columna.add(boton(BotonPlano.primario("Máquina vs Máquina"), () -> {
            panelMvm.nuevaPartida();
            cartas.show(contenido, "mvm");
        }));
        columna.add(boton(BotonPlano.secundario("Marcador"), () ->
                JOptionPane.showMessageDialog(this, marcador.texto(), "Marcador", JOptionPane.INFORMATION_MESSAGE)));
        columna.add(Box.createVerticalStrut(14));
        columna.add(boton(BotonPlano.secundario("Salir"), () -> System.exit(0)));
        fondo.add(columna);
        return fondo;
    }

    private JPanel boton(BotonPlano boton, Runnable accion) {
        boton.setFont(Estilo.negrita(15f));
        boton.setPreferredSize(new Dimension(320, 46));
        boton.addActionListener(e -> accion.run());
        JPanel fila = new JPanel();
        fila.setOpaque(false);
        fila.add(boton);
        fila.setMaximumSize(new Dimension(360, 56));
        fila.setAlignmentX(Component.CENTER_ALIGNMENT);
        return fila;
    }

    private void irAlInicio() {
        panelJvm.detener();
        panelMvm.detener();
        cartas.show(contenido, "inicio");
    }
}
