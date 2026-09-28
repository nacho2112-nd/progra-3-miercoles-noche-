import Interfaz.VentanaPrincipal;
import Juego.AdivinaQuien;
import javax.swing.SwingUtilities;

// Sin argumentos abre la ventana. Con "consola" se juega por consola.
void main(String[] args) {
    if (args.length > 0 && args[0].equals("consola")) {
        new AdivinaQuien().iniciar();
    } else {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
