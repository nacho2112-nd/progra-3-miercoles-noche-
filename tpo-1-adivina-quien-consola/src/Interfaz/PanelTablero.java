package Interfaz;

import Things.Personajes;

import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// La grilla de cartas: muestra que personajes siguen siendo candidatos y cuales quedaron descartados.
public class PanelTablero extends JPanel {

    private final Map<Personajes, TarjetaPersonaje> tarjetas = new LinkedHashMap<>();

    public PanelTablero(List<Personajes> personajes, double escala, int columnas) {
        super(new GridLayout(0, columnas, (int) (8 * escala), (int) (8 * escala)));
        setOpaque(false);
        for (Personajes p : personajes) {
            TarjetaPersonaje t = new TarjetaPersonaje(p, escala);
            tarjetas.put(p, t);
            add(t);
        }
    }

    // Tacha a los que ya no son candidatos y marca en cobre los que se descartaron en el ultimo turno
    public void mostrarCandidatos(Collection<Personajes> candidatos, Collection<Personajes> recienDescartados) {
        for (TarjetaPersonaje t : tarjetas.values()) {
            t.setDescartada(!candidatos.contains(t.getPersonaje()));
            t.setRecienDescartada(recienDescartados != null && recienDescartados.contains(t.getPersonaje()));
        }
    }

    public void marcarSecreto(Personajes secreto) {
        tarjetas.values().forEach(t -> t.setSecreta(t.getPersonaje() == secreto));
    }

    public void marcarSeleccion(Personajes seleccionado) {
        tarjetas.values().forEach(t -> t.setSeleccionada(t.getPersonaje() == seleccionado));
    }

    public void setAlClic(Consumer<Personajes> alClic) {
        tarjetas.values().forEach(t -> t.setAlClic(alClic));
    }
}
