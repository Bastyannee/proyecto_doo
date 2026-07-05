package vista.paneles;

import modelo.GestorDatos;
import modelo.entidades.Solicitud;
import vista.Navegador;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

/**
 * Panel inicial del sistema. Muestra las solicitudes pendientes
 * y permite navegar al detalle de cada una.

 * NOTA PARA EL GRUPO:
 * Esqueleto funcional. Enriquecer con tabla de solicitudes y estilos
 * sin cambiar la firma de refrescarSolicitudes() ni el constructor.
 */
public class PanelBienvenida extends JPanel {

    private final Navegador navegador;
    private final JLabel    labelInfo;

    public PanelBienvenida(Navegador navegador) {
        this.navegador = navegador;
        this.labelInfo = new JLabel("Cargando...", SwingConstants.CENTER);

        setLayout(new BorderLayout());

        JLabel titulo = new JLabel(
                "Panel de Bienvenida — Solicitudes Pendientes",
                SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));

        JButton btnBuscar = new JButton("Buscar Tutores");
        btnBuscar.addActionListener(e -> navegador.mostrarBusqueda());

        add(titulo,    BorderLayout.NORTH);
        add(labelInfo, BorderLayout.CENTER);
        add(btnBuscar, BorderLayout.SOUTH);

        refrescarSolicitudes();
    }

    /**
     * Recarga las solicitudes pendientes desde GestorDatos.
     * Llamado por Navegador cada vez que se vuelve a este panel.
     */
    public void refrescarSolicitudes() {
        List<Solicitud> pendientes =
                GestorDatos.getInstancia().getSolicitudesPendientes();
        labelInfo.setText("Solicitudes pendientes: " + pendientes.size());
    }
}