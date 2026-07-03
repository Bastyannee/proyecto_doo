package vista.paneles;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestCalendario {
    public static void main(String[] args) {
        // Es buena practica de arquitectura iniciar la interfaz en su propio hilo
        SwingUtilities.invokeLater(() -> {
            // 1. Creamos la ventana principal (el marco)
            JFrame ventana = new JFrame("Prueba Visual - Panel Calendario de Tomás");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setSize(400, 300); // Ancho y alto inicial
            ventana.setLocationRelativeTo(null); // Centra la ventana en tu pantalla

            // 2. Instanciamos tu panel
            PanelCalendario panel = new PanelCalendario();

            // 3. Metemos el panel dentro de la ventana
            ventana.add(panel);

            // 4. Hacemos visible la magia
            ventana.setVisible(true);
        });
    }
}