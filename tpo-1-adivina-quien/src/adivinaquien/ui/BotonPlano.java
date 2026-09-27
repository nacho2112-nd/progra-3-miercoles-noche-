package adivinaquien.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Botón dibujado a mano, para que se vea igual con cualquier look and feel. */
public class BotonPlano extends JButton {

    private final Color fondo;
    private final Color frente;

    public BotonPlano(String texto, Color fondo, Color frente) {
        super(texto);
        this.fondo = fondo;
        this.frente = frente;
        setFont(Estilo.negrita(13f));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(Math.max(120, getFontMetrics(getFont()).stringWidth(texto) + 32), 36));
    }

    public static BotonPlano primario(String texto) {
        return new BotonPlano(texto, Estilo.ACENTO, Color.WHITE);
    }

    public static BotonPlano secundario(String texto) {
        return new BotonPlano(texto, Estilo.ACENTO_CLARO, Estilo.ACENTO);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color base = isEnabled() ? fondo : new Color(0xE6E8EE);
        if (isEnabled() && getModel().isPressed()) {
            base = base.darker();
        } else if (isEnabled() && getModel().isRollover()) {
            base = mezclar(base, Color.WHITE, 0.15f);
        }
        g2.setColor(base);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.setFont(getFont());
        g2.setColor(isEnabled() ? frente : new Color(0x9AA1AE));
        FontMetrics fm = g2.getFontMetrics();
        String texto = getText();
        int x = (getWidth() - fm.stringWidth(texto)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(texto, x, y);
        g2.dispose();
    }

    private static Color mezclar(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed() * (1 - t) + b.getRed() * t),
                Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
                Math.round(a.getBlue() * (1 - t) + b.getBlue() * t));
    }
}
