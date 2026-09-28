package Interfaz;

import Things.Personajes;

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

// La carta de un personaje. La cara se dibuja con Graphics2D a partir de sus atributos,
// asi cada rasgo que se puede preguntar (pelo, pelado, lentes, barba, sombrero) se ve en la carta.
public class TarjetaPersonaje extends JComponent {

    private static final int ANCHO = 104;
    private static final int ALTO = 132;
    private static final Color PIEL = new Color(0xF2C9A0);
    private static final Color PIEL_BORDE = new Color(0xC99466);

    private final Personajes personaje;
    private final double escala;
    private boolean descartada, secreta, seleccionada, recienDescartada;
    private Consumer<Personajes> alClic;

    public TarjetaPersonaje(Personajes personaje, double escala) {
        this.personaje = personaje;
        this.escala = escala;
        setPreferredSize(new Dimension((int) (ANCHO * escala), (int) (ALTO * escala)));
        setToolTipText("#" + personaje.get_ID() + " " + personaje.get_Nombre() + " — " + descripcion(personaje));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (alClic != null) alClic.accept(TarjetaPersonaje.this.personaje);
            }
        });
    }

    // "mujer, pelo negro, lentes"
    public static String descripcion(Personajes p) {
        StringBuilder sb = new StringBuilder(p.get_Atributo("genero"));
        String pelo = p.get_Atributo("pelo");
        sb.append(pelo.equals("pelado") ? ", pelado" : ", pelo " + pelo);
        for (String extra : new String[] {"lentes", "barba", "sombrero"}) {
            if (tiene(p, extra)) sb.append(", ").append(extra);
        }
        return sb.toString();
    }

    private static boolean tiene(Personajes p, String categoria) {
        return p.get_Atributo(categoria) != null;
    }

    public void setAlClic(Consumer<Personajes> alClic) {
        this.alClic = alClic;
        setCursor(alClic == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setDescartada(boolean v) { descartada = v; repaint(); }
    public void setSecreta(boolean v) { secreta = v; repaint(); }
    public void setSeleccionada(boolean v) { seleccionada = v; repaint(); }
    public void setRecienDescartada(boolean v) { recienDescartada = v; repaint(); }
    public Personajes getPersonaje() { return personaje; }

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
        boolean mujer = "mujer".equals(personaje.get_Atributo("genero"));
        g2.setColor(mujer ? new Color(0xF4E9F0) : new Color(0xE7EEF6));
        g2.fill(new RoundRectangle2D.Double(9, 9, ANCHO - 18, 84, 10, 10));
        dibujarCara(g2, ANCHO / 2.0, 54, 21, mujer);

        // ID y nombre
        g2.setColor(Estilo.TINTA_SUAVE);
        g2.setFont(Estilo.mono(10f));
        g2.drawString(String.format("#%02d", personaje.get_ID()), 12, 22);
        g2.setColor(Estilo.TINTA);
        g2.setFont(Estilo.negrita(13f));
        FontMetrics fm = g2.getFontMetrics();
        String nombre = personaje.get_Nombre();
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

    private void dibujarCara(Graphics2D g2, double cx, double cy, double r, boolean mujer) {
        boolean pelado = "pelado".equals(personaje.get_Atributo("pelo"));
        boolean barba = tiene(personaje, "barba");
        Color pelo = switch (personaje.get_Atributo("pelo")) {
            case "colorado" -> new Color(0xC4502A);
            case "negro" -> new Color(0x26211F);
            case "amarillo" -> new Color(0xE7C23C);
            default -> PIEL;
        };

        // Pelo largo por detras de la cara
        if (!pelado && mujer) {
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
        if (!pelado) {
            g2.setColor(pelo);
            double alto = mujer ? 1.5 : 1.2;
            g2.fill(new Arc2D.Double(cx - r * 1.06, cy - r * 1.22, r * 2.12, r * alto, 0, 180, Arc2D.CHORD));
        } else {
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fill(new Ellipse2D.Double(cx - r * 0.45, cy - r * 0.98, r * 0.6, r * 0.3));
        }

        // Barba
        if (barba) {
            g2.setColor(pelado ? new Color(0x5A4636) : pelo.darker());
            g2.fill(new Arc2D.Double(cx - r, cy - r * 0.55, r * 2, r * 1.65, 180, 180, Arc2D.CHORD));
        }

        // Ojos
        g2.setColor(Estilo.TINTA);
        g2.fill(new Ellipse2D.Double(cx - r * 0.48, cy - r * 0.2, r * 0.2, r * 0.22));
        g2.fill(new Ellipse2D.Double(cx + r * 0.28, cy - r * 0.2, r * 0.2, r * 0.22));

        // Boca
        g2.setColor(barba ? new Color(0xF4D6BC) : new Color(0x9C4A3A));
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(cx - r * 0.3, cy + r * 0.2, r * 0.6, r * 0.35, 200, 140, Arc2D.OPEN));

        // Lentes
        if (tiene(personaje, "lentes")) {
            g2.setColor(new Color(0x2A2F3A));
            g2.setStroke(new BasicStroke(1.8f));
            g2.draw(new Ellipse2D.Double(cx - r * 0.66, cy - r * 0.36, r * 0.56, r * 0.52));
            g2.draw(new Ellipse2D.Double(cx + r * 0.1, cy - r * 0.36, r * 0.56, r * 0.52));
            g2.drawLine((int) (cx - r * 0.1), (int) (cy - r * 0.12), (int) (cx + r * 0.1), (int) (cy - r * 0.12));
        }

        // Sombrero
        if (tiene(personaje, "sombrero")) {
            g2.setColor(new Color(0x3B3230));
            g2.fill(new RoundRectangle2D.Double(cx - r * 1.45, cy - r * 1.05, r * 2.9, r * 0.28, 6, 6));
            g2.fill(new RoundRectangle2D.Double(cx - r * 0.85, cy - r * 1.85, r * 1.7, r * 0.9, 8, 8));
            g2.setColor(Estilo.COBRE);
            g2.fill(new RoundRectangle2D.Double(cx - r * 0.85, cy - r * 1.18, r * 1.7, r * 0.18, 2, 2));
        }
    }
}
