package adivinaquien.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Locale;

/** Colores y tipografías de toda la interfaz, en un solo lugar. */
public final class Estilo {

    public static final Color FONDO = new Color(0xF6F7FA);
    public static final Color PAPEL = Color.WHITE;
    public static final Color TINTA = new Color(0x131720);
    public static final Color TINTA_SUAVE = new Color(0x5B6475);
    public static final Color ACENTO = new Color(0x1E3A8F);
    public static final Color ACENTO_CLARO = new Color(0xE3E9F7);
    public static final Color COBRE = new Color(0xA9541F);
    public static final Color VERDE = new Color(0x0B6B74);
    public static final Color DORADO = new Color(0xC99A1C);
    public static final Color BORDE = new Color(0xD9DDE5);

    /** Formato de números argentino: 20.000 y 0,00190. */
    public static final Locale ES_AR = Locale.forLanguageTag("es-AR");

    private Estilo() {
    }

    public static Font titulo(float tamanio) {
        return new Font("Segoe UI Semibold", Font.PLAIN, 12).deriveFont(tamanio);
    }

    public static Font texto(float tamanio) {
        return new Font("Segoe UI", Font.PLAIN, 12).deriveFont(tamanio);
    }

    public static Font negrita(float tamanio) {
        return new Font("Segoe UI", Font.BOLD, 12).deriveFont(tamanio);
    }

    public static Font mono(float tamanio) {
        return new Font("Consolas", Font.PLAIN, 12).deriveFont(tamanio);
    }

    public static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    /** Recuadro blanco con título chico arriba, para agrupar controles. */
    public static JPanel seccion(String titulo, JComponent contenido) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(PAPEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(8, 10, 10, 10)));
        JLabel t = etiqueta(titulo.toUpperCase(), negrita(11f), TINTA_SUAVE);
        p.add(t, BorderLayout.NORTH);
        p.add(contenido, BorderLayout.CENTER);
        return p;
    }

    public static JTextArea areaTraza() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(mono(12.5f));
        area.setForeground(TINTA);
        area.setBackground(new Color(0xFBFBFD));
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return area;
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }

    public static Border margen(int m) {
        return BorderFactory.createEmptyBorder(m, m, m, m);
    }
}
