package adivinaquien.ui;

import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;
import adivinaquien.motor.ArbolDecision;
import adivinaquien.motor.NodoDecision;
import adivinaquien.motor.OrganizadorTablero;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * El árbol de decisión completo que genera la estrategia Greedy sobre los 23 personajes. Cada
 * partida de la máquina recorre un camino de la raíz a una hoja.
 */
public class PanelArbolDecision extends JPanel {

    public PanelArbolDecision(Runnable alVolver) {
        super(new BorderLayout());
        setBackground(Estilo.FONDO);

        Tablero tablero = OrganizadorTablero.organizar();
        ArbolDecision arbol = ArbolDecision.construir(tablero.getPersonajes());

        BarraSuperior barra = new BarraSuperior("Árbol de decisión de la máquina", String.format(Estilo.ES_AR,
                "Altura: %d preguntas · peor caso: %d turnos · promedio: %.2f turnos (contando el arriesgue final)",
                arbol.profundidadMaxima(), arbol.turnosMaximos(), arbol.turnosPromedio()));
        BotonPlano volver = BotonPlano.secundario("← Menú");
        volver.addActionListener(e -> alVolver.run());
        barra.agregarBoton(volver);
        add(barra, BorderLayout.NORTH);

        JTree arbolVisual = new JTree(convertir(arbol.getRaiz(), ""));
        arbolVisual.setFont(Estilo.texto(14f));
        arbolVisual.setRowHeight(24);
        for (int i = 0; i < arbolVisual.getRowCount(); i++) {
            arbolVisual.expandRow(i);
        }

        DefaultTableModel turnos = new DefaultTableModel(new Object[]{"ID", "Secreto", "Turnos"}, 0);
        for (Personaje p : tablero.getPersonajes()) {
            turnos.addRow(new Object[]{p.getId(), p.getNombre(), arbol.turnosPara(p)});
        }
        JTable tabla = new JTable(turnos);
        tabla.setFont(Estilo.texto(13f));
        tabla.setRowHeight(22);
        tabla.getTableHeader().setFont(Estilo.negrita(12.5f));
        tabla.setEnabled(false);

        JLabel explicacion = new JLabel("<html><div style='width:290px'>Cada nodo es la pregunta que elegiría el Greedy con esos candidatos: "
                + "la de mayor <b>descarte garantizado</b>, min(SÍ, NO). La construcción es Divide y Conquista: "
                + "el filtro divide a los candidatos, se arma el subárbol de cada grupo y el nodo los combina. "
                + "Las hojas son arriesgues: con 1 candidato se gana; con 2 o 3, se arriesga de a uno, en orden, hasta acertar.</div></html>");
        explicacion.setFont(Estilo.texto(13f));
        explicacion.setForeground(Estilo.TINTA_SUAVE);

        JPanel derecha = new JPanel(new BorderLayout(0, 10));
        derecha.setOpaque(false);
        derecha.setPreferredSize(new Dimension(360, 100));
        derecha.add(Estilo.seccion("Cómo leerlo", explicacion), BorderLayout.NORTH);
        derecha.add(Estilo.seccion("Turnos que necesita la máquina por secreto", Estilo.scroll(tabla)), BorderLayout.CENTER);

        JPanel cuerpo = new JPanel(new BorderLayout(14, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(Estilo.margen(14));
        cuerpo.add(Estilo.seccion("Árbol completo (23 personajes)", Estilo.scroll(arbolVisual)), BorderLayout.CENTER);
        cuerpo.add(derecha, BorderLayout.EAST);
        add(cuerpo, BorderLayout.CENTER);
    }

    private static DefaultMutableTreeNode convertir(NodoDecision nodo, String rama) {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode(rama + nodo);
        if (!nodo.esHoja()) {
            n.add(convertir(nodo.getSi(), "SÍ → "));
            n.add(convertir(nodo.getNo(), "NO → "));
        }
        return n;
    }
}
