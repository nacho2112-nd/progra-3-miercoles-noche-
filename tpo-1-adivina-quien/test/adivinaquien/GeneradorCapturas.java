package adivinaquien;

import adivinaquien.algoritmos.Benchmark;
import adivinaquien.modelo.Filtro;
import adivinaquien.modelo.Personaje;
import adivinaquien.ui.PanelArbolDecision;
import adivinaquien.ui.PanelInicio;
import adivinaquien.ui.PanelJugadorVsMaquina;
import adivinaquien.ui.PanelMaquinaVsMaquina;
import adivinaquien.ui.PanelOrdenamiento;
import adivinaquien.ui.VentanaPrincipal;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Dibuja cada pantalla en una imagen, sin mostrarla, para usarlas como capturas en el documento.
 *
 *   java -cp out adivinaquien.GeneradorCapturas carpeta-destino
 */
public class GeneradorCapturas {

    private static final int ANCHO = 1380;
    private static final int ALTO = 880;
    private static final double ESCALA = 2.0;

    public static void main(String[] args) throws Exception {
        File destino = new File(args.length > 0 ? args[0] : "capturas");
        destino.mkdirs();
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // El benchmark se corre fuera del hilo de Swing y se imprime: esos números van a la tabla del documento.
        List<Benchmark.Resultado> tiempos = new Benchmark().medir(23, 1_000, 10_000);
        System.out.println("Algoritmo | n | repeticiones | ms por ordenamiento | comparaciones");
        for (Benchmark.Resultado r : tiempos) {
            System.out.printf(Locale.ROOT, "%s | %d | %d | %.6f | %d%n",
                    r.getAlgoritmo(), r.getN(), r.getRepeticiones(), r.getMsPromedio(), r.getComparaciones());
        }

        SwingUtilities.invokeAndWait(() -> {
            try {
                Runnable nada = () -> { };
                new VentanaPrincipal().dispose(); // la ventana completa se construye sin errores
                guardar(new PanelInicio(pantalla -> { }), new File(destino, "01-inicio.png"));

                PanelJugadorVsMaquina jvm = new PanelJugadorVsMaquina(nada);
                jvm.setModoDemostracion(true);
                jvm.nuevaPartida();
                Personaje paula = jvm.getTablero().getPersonajes().stream()
                        .filter(p -> p.getNombre().equals("Paula")).findFirst().orElseThrow();
                jvm.elegir(paula);
                guardar(jvm, new File(destino, "02-jvm-eleccion.png"));
                jvm.confirmar();
                jvm.preguntar(Filtro.ES_MUJER);
                jvm.preguntar(Filtro.USA_LENTES);
                guardar(jvm, new File(destino, "03-jvm-partida.png"));

                PanelMaquinaVsMaquina mvm = new PanelMaquinaVsMaquina(nada);
                mvm.nuevaPartida(11);
                for (int i = 0; i < 5; i++) {
                    mvm.paso();
                }
                guardar(mvm, new File(destino, "04-mvm.png"));

                PanelOrdenamiento orden = new PanelOrdenamiento(nada);
                guardar(orden, new File(destino, "05-orden-tablas.png"));
                orden.mostrarPestania(1);
                guardar(orden, new File(destino, "06-orden-traza.png"));
                orden.mostrarTiempos(tiempos);
                orden.mostrarPestania(2);
                guardar(orden, new File(destino, "07-orden-tiempos.png"));

                guardar(new PanelArbolDecision(nada), new File(destino, "08-arbol.png"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Capturas en " + destino.getAbsolutePath());
        System.exit(0);
    }

    private static void guardar(JComponent panel, File archivo) throws Exception {
        JFrame marco = new JFrame();
        marco.setUndecorated(true);
        marco.setContentPane(panel);
        marco.setSize(ANCHO, ALTO);
        marco.addNotify();
        marco.validate();
        BufferedImage imagen = new BufferedImage((int) (ANCHO * ESCALA), (int) (ALTO * ESCALA), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.scale(ESCALA, ESCALA);
        panel.printAll(g);
        g.dispose();
        ImageIO.write(imagen, "png", archivo);
        marco.setContentPane(new javax.swing.JPanel());
        marco.dispose();
        System.out.println("  " + archivo.getName());
    }
}
