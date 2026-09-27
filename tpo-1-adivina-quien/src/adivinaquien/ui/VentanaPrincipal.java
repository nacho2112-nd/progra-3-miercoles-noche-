package adivinaquien.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

/** La ventana: una pantalla por modo, que se intercambian con un CardLayout. */
public class VentanaPrincipal extends JFrame {

    static final String INICIO = "inicio";
    static final String JUGADOR_VS_MAQUINA = "jvm";
    static final String MAQUINA_VS_MAQUINA = "mvm";
    static final String ORDENAMIENTO = "orden";
    static final String ARBOL = "arbol";

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final PanelJugadorVsMaquina panelJvm;
    private final PanelMaquinaVsMaquina panelMvm;

    public VentanaPrincipal() {
        super("Adivina Quién — Programación III");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 780));
        setPreferredSize(new Dimension(1380, 900));

        panelJvm = new PanelJugadorVsMaquina(this::irAlInicio);
        panelMvm = new PanelMaquinaVsMaquina(this::irAlInicio);

        contenido.add(new PanelInicio(this::mostrar), INICIO);
        contenido.add(panelJvm, JUGADOR_VS_MAQUINA);
        contenido.add(panelMvm, MAQUINA_VS_MAQUINA);
        contenido.add(new PanelOrdenamiento(this::irAlInicio), ORDENAMIENTO);
        contenido.add(new PanelArbolDecision(this::irAlInicio), ARBOL);
        setContentPane(contenido);
        pack();
        setLocationRelativeTo(null);
    }

    void mostrar(String pantalla) {
        if (JUGADOR_VS_MAQUINA.equals(pantalla)) {
            panelJvm.nuevaPartida();
        } else if (MAQUINA_VS_MAQUINA.equals(pantalla)) {
            panelMvm.nuevaPartida();
        }
        cartas.show(contenido, pantalla);
    }

    private void irAlInicio() {
        panelMvm.detener();
        cartas.show(contenido, INICIO);
    }
}
