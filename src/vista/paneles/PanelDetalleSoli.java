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
import java.util.Optional;

/**
 * Panel de detalle de una solicitud de tutoría.

 * NOTA PARA MARIA JOSE:
 * cargarSolicitud() es llamado por Navegador antes de mostrar
 * este panel. No cambiar su firma.
 */
public class PanelDetalleSoli extends JPanel {

    private final Navegador navegador;
    private final JLabel    labelDetalle;
    private Solicitud       solicitudActual;

    public PanelDetalleSoli(Navegador navegador) {
        this.navegador    = navegador;
        this.labelDetalle = new JLabel("Seleccione una solicitud", SwingConstants.CENTER);

        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Detalle de Solicitud", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));

        JButton btnAprobar = new JButton("Buscar tutor para esta solicitud");
        btnAprobar.addActionListener(e -> navegador.mostrarBusqueda());

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> navegador.mostrarBienvenida());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnVolver);
        panelBotones.add(btnAprobar);

        add(titulo,        BorderLayout.NORTH);
        add(labelDetalle,  BorderLayout.CENTER);
        add(panelBotones,  BorderLayout.SOUTH);
    }

    /**
     * Carga los datos de la solicitud en el panel.
     * Llamado por Navegador.mostrarDetalleSolicitud(id).
     *
     * @param solicitudId id de la solicitud a mostrar
     */
    public void cargarSolicitud(String solicitudId) {
        Optional<Solicitud> resultado =
                GestorDatos.getInstancia().buscarSolicitudPorId(solicitudId);
        if (resultado.isPresent()) {
            this.solicitudActual = resultado.get();
            labelDetalle.setText("<html>Solicitud: " + solicitudActual.getId()
                    + "<br>Estado: " + solicitudActual.getEstado() + "</html>");
        } else {
            labelDetalle.setText("Solicitud no encontrada: " + solicitudId);
        }
    }

    public Solicitud getSolicitudActual() { return solicitudActual; }
}