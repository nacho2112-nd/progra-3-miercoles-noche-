package adivinaquien.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

/** Menú principal: los dos modos de juego y las dos pantallas que muestran los algoritmos. */
public class PanelInicio extends JPanel {

    public PanelInicio(Consumer<String> irA) {
        super(new GridBagLayout());
        setBackground(Estilo.FONDO);

        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));

        JLabel eyebrow = Estilo.etiqueta("PROGRAMACIÓN III · DISEÑO Y ANÁLISIS DE ALGORITMOS · UADE",
                Estilo.negrita(12f), Estilo.ACENTO);
        JLabel titulo = Estilo.etiqueta("Adivina Quién", Estilo.titulo(46f), Estilo.TINTA);
        JLabel bajada = Estilo.etiqueta("23 personajes, 8 preguntas posibles. La máquina ordena el tablero con MergeSort "
                + "y elige cada pregunta con una estrategia Greedy.", Estilo.texto(15f), Estilo.TINTA_SUAVE);
        for (JLabel l : new JLabel[]{eyebrow, titulo, bajada}) {
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            columna.add(l);
            columna.add(Box.createVerticalStrut(6));
        }
        columna.add(Box.createVerticalStrut(22));

        JPanel opciones = new JPanel(new GridLayout(2, 2, 16, 16));
        opciones.setOpaque(false);
        opciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        opciones.add(opcion("Jugador vs Máquina",
                "Elegí tu personaje y competí contra la máquina. Podés ver su razonamiento turno a turno.",
                () -> irA.accept(VentanaPrincipal.JUGADOR_VS_MAQUINA)));
        opciones.add(opcion("Máquina vs Máquina",
                "Dos máquinas juegan entre sí y se muestra todo su proceso: evaluación de filtros, descartes e inferencias.",
                () -> irA.accept(VentanaPrincipal.MAQUINA_VS_MAQUINA)));
        opciones.add(opcion("Ordenamiento del tablero",
                "Del catálogo agrupado por género a la lista ordenada con ID autoincremental. Traza de MergeSort y tiempos.",
                () -> irA.accept(VentanaPrincipal.ORDENAMIENTO)));
        opciones.add(opcion("Árbol de decisión",
                "El árbol completo que recorre la estrategia Greedy sobre los 23 personajes, con sus estadísticas.",
                () -> irA.accept(VentanaPrincipal.ARBOL)));
        columna.add(opciones);

        add(columna);
    }

    private JPanel opcion(String titulo, String descripcion, Runnable accion) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Estilo.PAPEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE), BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        p.setPreferredSize(new Dimension(440, 176));
        JLabel desc = new JLabel("<html><div style='width:340px'>" + descripcion + "</div></html>");
        desc.setFont(Estilo.texto(13.5f));
        desc.setForeground(Estilo.TINTA_SUAVE);
        p.add(Estilo.etiqueta(titulo, Estilo.titulo(19f), Estilo.TINTA), BorderLayout.NORTH);
        p.add(desc, BorderLayout.CENTER);
        BotonPlano boton = BotonPlano.primario("Abrir");
        boton.addActionListener(e -> accion.run());
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(boton, BorderLayout.WEST);
        p.add(pie, BorderLayout.SOUTH);
        return p;
    }
}
