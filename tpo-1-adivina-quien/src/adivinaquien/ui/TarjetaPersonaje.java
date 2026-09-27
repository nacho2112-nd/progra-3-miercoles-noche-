package adivinaquien.ui;

import adivinaquien.modelo.ColorPelo;
import adivinaquien.modelo.Genero;
import adivinaquien.modelo.Personaje;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * La carta de un personaje. La cara se dibuja con Graphics2D a partir de sus atributos, así que
 * cada rasgo que se puede preguntar (pelo, pelado, lentes, barba, sombrero) se ve en la carta.
 */
public class TarjetaPersonaje extends JComponent {

    private static final int ANCHO = 104;
    private static final int ALTO = 132;
    private static final Color PIEL = new Color(0xF2C9A0);
    private static final Color PIEL_BORDE = new Color(0xC99466);

    private final Personaje personaje;
    private final double escala;
    private boolean descartada;
    private boolean secreta;
    private boolean seleccionada;
    private boolean recienDescartada;
    private Consumer<Personaje> alClic;

    public TarjetaPersonaje(Personaje personaje, double escala) {
        this.personaje = personaje;
        this.escala = escala;
        setPreferredSize(new Dimension((int) (ANCHO * escala), (int) (ALTO * escala)));
        setToolTipText("#" + personaje.getId() + " " + personaje.getNombre() + " — " + personaje.descripcion());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (alClic != null) {
                    alClic.accept(TarjetaPersonaje.this.personaje);
                }
            }
        });
    }

    public void setAlClic(Consumer<Personaje> alClic) {
        this.alClic = alClic;
        setCursor(alClic == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setDescartada(boolean descartada) { this.descartada = descartada; repaint(); }
    public void setSecreta(boolean secreta) { this.secreta = secreta; repaint(); }
    public void setSeleccionada(boolean seleccionada) { this.seleccionada = seleccionada; repaint(); }
    public void setRecienDescartada(boolean recien) { this.recienDescartada = recien; repaint(); }
    public Personaje getPersonaje() { return personaje; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.scale(escala, escala);

        // Carta
        RoundRectangle2D carta = new RoundRectangle2D.Double(2, 2, ANCHO - 4, ALTO - 4, 14, 14);
        g2.setColor(Estilo.PAPEL);
        g2.fill(carta);
        Color borde = Estilo.BORDE;
        float grosor = 1.2f;
        if (secreta) { borde = Estilo.DORADO; grosor = 3.2f; }
        else if (seleccionada) { borde = Estilo.ACENTO; grosor = 3.2f; }
        else if (recienDescartada) { borde = Estilo.COBRE; grosor = 2.4f; }
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(grosor));
        g2.draw(carta);

        // Fondo del retrato
        g2.setColor(personaje.getGenero() == Genero.MUJER ? new Color(0xF4E9F0) : new Color(0xE7EEF6));
        g2.fill(new RoundRectangle2D.Double(9, 9, ANCHO - 18, 84, 10, 10));

        dibujarCara(g2, ANCHO / 2.0, 54, 21);

        // ID y nombre
        g2.setColor(Estilo.TINTA_SUAVE);
        g2.setFont(Estilo.mono(10f));
        g2.drawString(String.format("#%02d", personaje.getId()), 12, 22);
        g2.setColor(Estilo.TINTA);
        g2.setFont(Estilo.negrita(13f));
        FontMetrics fm = g2.getFontMetrics();
        String nombre = personaje.getNombre();
        g2.drawString(nombre, (int) ((ANCHO - fm.stringWidth(nombre)) / 2.0), 113);

        if (secreta) {
            g2.setColor(Estilo.DORADO);
            g2.setFont(Estilo.negrita(9f));
            g2.drawString("SECRETO", ANCHO - 54, 22);
        }

        if (descartada) {
            g2.setColor(new Color(255, 255, 255, 185));
            g2.fill(carta);
            g2.setColor(new Color(Estilo.COBRE.getRed(), Estilo.COBRE.getGreen(), Estilo.COBRE.getBlue(), 200));
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(24, 28, ANCHO - 24, 82);
            g2.drawLine(ANCHO - 24, 28, 24, 82);
        }
        g2.dispose();
    }

    private void dibujarCara(Graphics2D g2, double cx, double cy, double r) {
        Color pelo = colorDe(personaje.getColorPelo());
        boolean mujer = personaje.getGenero() == Genero.MUJER;

        // Pelo largo por detrás de la cara
        if (!personaje.isPelado() && mujer) {
            g2.setColor(pelo);
            g2.fill(new RoundRectangle2D.Double(cx - r * 1.3, cy - r * 1.2, r * 2.6, r * 2.45, r * 1.2, r * 1.2));
        }

        // Cuello y cara
        g2.setColor(PIEL);
        g2.fill(new RoundRectangle2D.Double(cx - r * 0.35, cy + r * 0.8, r * 0.7, r * 0.7, 4, 4));
        Ellipse2D cara = new Ellipse2D.Double(cx - r, cy - r * 1.1, r * 2, r * 2.2);
        g2.fill(cara);
        g2.setColor(PIEL_BORDE);
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(cara);

        // Pelo corto encima, o el brillo de la pelada
        if (!personaje.isPelado()) {
            g2.setColor(pelo);
            if (mujer) {
                g2.fill(new Arc2D.Double(cx - r * 1.08, cy - r * 1.22, r * 2.16, r * 1.5, 0, 180, Arc2D.CHORD));
            } else {
                g2.fill(new Arc2D.Double(cx - r * 1.04, cy - r * 1.22, r * 2.08, r * 1.2, 0, 180, Arc2D.CHORD));
            }
        } else {
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fill(new Ellipse2D.Double(cx - r * 0.45, cy - r * 0.98, r * 0.6, r * 0.3));
        }

        // Barba
        if (personaje.isBarba()) {
            g2.setColor(personaje.isPelado() ? new Color(0x5A4636) : pelo.darker());
            g2.fill(new Arc2D.Double(cx - r, cy - r * 0.55, r * 2, r * 1.65, 180, 180, Arc2D.CHORD));
        }

        // Ojos
        g2.setColor(Estilo.TINTA);
        g2.fill(new Ellipse2D.Double(cx - r * 0.48, cy - r * 0.2, r * 0.2, r * 0.22));
        g2.fill(new Ellipse2D.Double(cx + r * 0.28, cy - r * 0.2, r * 0.2, r * 0.22));

        // Boca
        g2.setColor(personaje.isBarba() ? new Color(0xF4D6BC) : new Color(0x9C4A3A));
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(cx - r * 0.3, cy + r * 0.2, r * 0.6, r * 0.35, 200, 140, Arc2D.OPEN));

        // Lentes
        if (personaje.isLentes()) {
            g2.setColor(new Color(0x2A2F3A));
            g2.setStroke(new BasicStroke(1.8f));
            g2.draw(new Ellipse2D.Double(cx - r * 0.66, cy - r * 0.36, r * 0.56, r * 0.52));
            g2.draw(new Ellipse2D.Double(cx + r * 0.1, cy - r * 0.36, r * 0.56, r * 0.52));
            g2.drawLine((int) (cx - r * 0.1), (int) (cy - r * 0.12), (int) (cx + r * 0.1), (int) (cy - r * 0.12));
        }

        // Sombrero
        if (personaje.isSombrero()) {
            g2.setColor(new Color(0x3B3230));
            g2.fill(new RoundRectangle2D.Double(cx - r * 1.45, cy - r * 1.05, r * 2.9, r * 0.28, 6, 6));
            g2.fill(new RoundRectangle2D.Double(cx - r * 0.85, cy - r * 1.85, r * 1.7, r * 0.9, 8, 8));
            g2.setColor(Estilo.COBRE);
            g2.fill(new RoundRectangle2D.Double(cx - r * 0.85, cy - r * 1.18, r * 1.7, r * 0.18, 2, 2));
        }
    }

    private static Color colorDe(ColorPelo color) {
        switch (color) {
            case COLORADO: return new Color(0xC4502A);
            case NEGRO: return new Color(0x26211F);
            case AMARILLO: return new Color(0xE7C23C);
            default: return PIEL;
        }
    }
}
