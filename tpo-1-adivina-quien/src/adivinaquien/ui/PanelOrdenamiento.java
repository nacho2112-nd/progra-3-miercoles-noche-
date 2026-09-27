package adivinaquien.ui;

import adivinaquien.algoritmos.Benchmark;
import adivinaquien.datos.CatalogoPersonajes;
import adivinaquien.modelo.Personaje;
import adivinaquien.modelo.Tablero;
import adivinaquien.motor.OrganizadorTablero;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Muestra cómo la máquina arma el tablero: el catálogo agrupado por género, la traza de MergeSort,
 * el tablero final con IDs autoincrementales y la comparación de tiempos contra los cuadráticos.
 */
public class PanelOrdenamiento extends JPanel {

    private final DefaultTableModel modeloTiempos = new DefaultTableModel(
            new Object[]{"Algoritmo", "Complejidad", "n", "Repeticiones", "ms por ordenamiento", "Comparaciones"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final BotonPlano botonMedir = BotonPlano.primario("Medir tiempos");
    private final JLabel estadoMedicion = Estilo.etiqueta("", Estilo.texto(13f), Estilo.TINTA_SUAVE);
    private final JTabbedPane pestanias = new JTabbedPane();

    public PanelOrdenamiento(Runnable alVolver) {
        super(new BorderLayout());
        setBackground(Estilo.FONDO);
        BarraSuperior barra = new BarraSuperior("Ordenamiento del tablero",
                "La máquina recibe los personajes agrupados sólo por género y los ordena por nombre con MergeSort (Divide y Conquista).");
        BotonPlano volver = BotonPlano.secundario("← Menú");
        volver.addActionListener(e -> alVolver.run());
        barra.agregarBoton(volver);
        add(barra, BorderLayout.NORTH);

        List<Personaje> catalogo = CatalogoPersonajes.crear();
        StringBuilder trazaMerge = new StringBuilder();
        Tablero tablero = OrganizadorTablero.organizar(catalogo, linea -> trazaMerge.append(linea).append('\n'));

        pestanias.setFont(Estilo.negrita(13f));
        pestanias.addTab("Del catálogo al tablero", pestaniaTablas(catalogo, tablero));
        pestanias.addTab("Traza de MergeSort", pestaniaTraza(trazaMerge.toString()));
        pestanias.addTab("Comparación de tiempos", pestaniaTiempos());

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setOpaque(false);
        cuerpo.setBorder(Estilo.margen(14));
        cuerpo.add(pestanias, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel pestaniaTablas(List<Personaje> catalogo, Tablero tablero) {
        DefaultTableModel inicial = new DefaultTableModel(new Object[]{"Posición", "Nombre", "Género"}, 0);
        for (int i = 0; i < catalogo.size(); i++) {
            Personaje p = catalogo.get(i);
            inicial.addRow(new Object[]{i + 1, p.getNombre(), p.getGenero().getEtiqueta()});
        }
        DefaultTableModel ordenado = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Género", "Pelo", "Lentes", "Barba", "Sombrero"}, 0);
        for (Personaje p : tablero.getPersonajes()) {
            ordenado.addRow(new Object[]{p.getId(), p.getNombre(), p.getGenero().getEtiqueta(),
                    p.getColorPelo().getEtiqueta(), siNo(p.isLentes()), siNo(p.isBarba()), siNo(p.isSombrero())});
        }
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setBorder(Estilo.margen(12));
        p.setBackground(Estilo.FONDO);
        p.add(Estilo.seccion("1 · Catálogo inicial: agrupado por género, sin otro orden", Estilo.scroll(tabla(inicial))));
        p.add(Estilo.seccion("2 · Tablero: MergeSort por nombre, ID autoincremental al agregar", Estilo.scroll(tabla(ordenado))));
        return p;
    }

    private JPanel pestaniaTraza(String traza) {
        JTextArea area = Estilo.areaTraza();
        area.setText("MergeSort(catálogo, criterio = nombre sin tildes)\n"
                + "T(n) = 2·T(n/2) + O(n)  =>  Θ(n log n)   ·   n = 23: 22 divisiones, 22 combinaciones\n\n" + traza);
        area.setCaretPosition(0);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(Estilo.margen(12));
        p.setBackground(Estilo.FONDO);
        p.add(Estilo.scroll(area), BorderLayout.CENTER);
        return p;
    }

    private JPanel pestaniaTiempos() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(Estilo.margen(12));
        p.setBackground(Estilo.FONDO);
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        arriba.setOpaque(false);
        botonMedir.addActionListener(e -> medir());
        arriba.add(botonMedir);
        arriba.add(estadoMedicion);
        estadoMedicion.setText("Mide MergeSort, QuickSort, Inserción y Burbujeo sobre los mismos 23 personajes, y sobre 1.000 y 10.000 al azar.");
        p.add(arriba, BorderLayout.NORTH);
        JTable tabla = tabla(modeloTiempos);
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(JLabel.RIGHT);
        for (int c = 2; c < 6; c++) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(derecha);
        }
        p.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        JLabel nota = new JLabel("<html><div style='width:1100px'>Con n = 23 las diferencias son de microsegundos: menos que el ruido del sistema y "
                + "mucho menos de lo que tarda en dibujarse la pantalla. El conteo de comparaciones muestra la tendencia "
                + "(n log n contra n²) que con 10.000 elementos ya sí se ve en el reloj.</div></html>");
        nota.setFont(Estilo.texto(13f));
        nota.setForeground(Estilo.TINTA_SUAVE);
        p.add(nota, BorderLayout.SOUTH);
        return p;
    }

    /** Corre el benchmark en segundo plano para no congelar la ventana. */
    public void medir() {
        botonMedir.setEnabled(false);
        estadoMedicion.setText("Midiendo… (unos segundos)");
        modeloTiempos.setRowCount(0);
        new SwingWorker<List<Benchmark.Resultado>, Void>() {
            @Override
            protected List<Benchmark.Resultado> doInBackground() {
                return new Benchmark().medir(23, 1_000, 10_000);
            }

            @Override
            protected void done() {
                try {
                    mostrarTiempos(get());
                    estadoMedicion.setText("Listo. Tiempo promedio por ordenamiento, después de calentar el JIT.");
                } catch (Exception ex) {
                    estadoMedicion.setText("Error al medir: " + ex.getMessage());
                }
                botonMedir.setEnabled(true);
            }
        }.execute();
    }

    public void mostrarTiempos(List<Benchmark.Resultado> resultados) {
        modeloTiempos.setRowCount(0);
        for (Benchmark.Resultado r : resultados) {
            modeloTiempos.addRow(new Object[]{r.getAlgoritmo(), r.getComplejidad(), String.format(Estilo.ES_AR, "%,d", r.getN()),
                    String.format(Estilo.ES_AR, "%,d", r.getRepeticiones()),
                    String.format(Estilo.ES_AR, "%.5f", r.getMsPromedio()),
                    String.format(Estilo.ES_AR, "%,d", r.getComparaciones())});
        }
    }

    public void mostrarPestania(int indice) {
        pestanias.setSelectedIndex(indice);
    }

    private static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setFont(Estilo.texto(13f));
        t.setRowHeight(24);
        t.getTableHeader().setFont(Estilo.negrita(12.5f));
        t.setGridColor(Estilo.BORDE);
        t.setFillsViewportHeight(true);
        return t;
    }

    private static String siNo(boolean valor) {
        return valor ? "Sí" : "No";
    }
}
