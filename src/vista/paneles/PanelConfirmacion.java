package vista.paneles;

import vista.Navegador;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/**
 * Panel de confirmacion tras agendar una reserva exitosamente.
 */
public class PanelConfirmacion extends JPanel {

    private final Navegador navegador;
    private final JLabel    labelMensaje;

    public PanelConfirmacion(Navegador navegador) {
        this.navegador    = navegador;
        this.labelMensaje = new JLabel(
                "¡Reserva confirmada exitosamente!", SwingConstants.CENTER);
        labelMensaje.setFont(new Font("Arial", Font.BOLD, 20));
        labelMensaje.setForeground(new Color(34, 139, 34));

        setLayout(new BorderLayout());

        JButton btnVolver = new JButton("Volver al inicio");
        btnVolver.addActionListener(e -> navegador.mostrarBienvenida());

        add(labelMensaje, BorderLayout.CENTER);
        add(btnVolver,    BorderLayout.SOUTH);
    }

    /**
     * Personaliza el mensaje de confirmacion.
     * Llamar antes de navegar a este panel.
     *
     * @param mensaje texto a mostrar
     */
    public void setMensaje(String mensaje) {
        labelMensaje.setText(mensaje);
    }
}