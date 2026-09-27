package adivinaquien.ui;

import adivinaquien.modelo.Personaje;

import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** La grilla de cartas. Muestra qué personajes siguen siendo candidatos y cuáles quedaron descartados. */
public class PanelTablero extends JPanel {

    private final Map<Personaje, TarjetaPersonaje> tarjetas = new LinkedHashMap<>();

    public PanelTablero(List<Personaje> personajes, double escala, int columnas) {
        super(new GridLayout(0, columnas, (int) (8 * escala), (int) (8 * escala)));
        setOpaque(false);
        for (Personaje p : personajes) {
            TarjetaPersonaje t = new TarjetaPersonaje(p, escala);
            tarjetas.put(p, t);
            add(t);
        }
    }

    /** Tacha a los que no están entre los candidatos y marca los que se descartaron en el último turno. */
    public void mostrarCandidatos(Set<Personaje> candidatos, Collection<Personaje> recienDescartados) {
        for (TarjetaPersonaje t : tarjetas.values()) {
            t.setDescartada(!candidatos.contains(t.getPersonaje()));
            t.setRecienDescartada(recienDescartados != null && recienDescartados.contains(t.getPersonaje()));
        }
    }

    public void marcarSecreto(Personaje secreto) {
        for (TarjetaPersonaje t : tarjetas.values()) {
            t.setSecreta(t.getPersonaje() == secreto);
        }
    }

    public void marcarSeleccion(Personaje seleccionado) {
        for (TarjetaPersonaje t : tarjetas.values()) {
            t.setSeleccionada(t.getPersonaje() == seleccionado);
        }
    }

    public void setAlClic(Consumer<Personaje> alClic) {
        for (TarjetaPersonaje t : tarjetas.values()) {
            t.setAlClic(alClic);
        }
    }
}
