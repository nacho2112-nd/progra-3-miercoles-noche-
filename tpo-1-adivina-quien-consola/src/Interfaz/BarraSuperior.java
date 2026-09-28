package Interfaz;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

// Encabezado de cada pantalla: titulo, una linea de estado y los botones de la derecha.
public class BarraSuperior extends JPanel {

    private final JLabel titulo;
    private final JLabel estado;
    private final JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    public BarraSuperior(String textoTitulo) {
        super(new BorderLayout());
        setBackground(Estilo.PAPEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(10, 18, 10, 14)));
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        titulo = Estilo.etiqueta(textoTitulo, Estilo.titulo(20f), Estilo.TINTA);
        estado = Estilo.etiqueta("", Estilo.texto(13.5f), Estilo.TINTA_SUAVE);
        textos.add(titulo);
        textos.add(estado);
        add(textos, BorderLayout.CENTER);
        botones.setOpaque(false);
        add(botones, BorderLayout.EAST);
    }

    public void agregarBoton(JComponent boton) {
        botones.add(boton);
    }

    public void setTitulo(String texto) {
        titulo.setText(texto);
    }

    public void setEstado(String texto) {
        estado.setText(texto);
    }
}
