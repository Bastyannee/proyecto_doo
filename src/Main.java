import vista.VentanaPrincipal;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación.
 *
 * POR QUÉ invokeLater():
 * Swing no es thread-safe. invokeLater() encola la construcción
 * de la UI en el Event Dispatch Thread (EDT), garantizando que
 * todo Swing corra en el hilo correcto desde el inicio.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaPrincipal::new);
    }
}