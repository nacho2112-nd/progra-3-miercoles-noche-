package adivinaquien;

import adivinaquien.ui.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/** Punto de entrada: arranca la interfaz en el hilo de eventos de Swing. */
public class App {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no se puede usar el look del sistema, sigue con el de Java.
        }
        ToolTipManager.sharedInstance().setInitialDelay(250);
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
