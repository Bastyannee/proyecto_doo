package vista;

import modelo.entidades.Estudiante;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import vista.paneles.*;

import javax.swing.JPanel;
import java.awt.CardLayout;

/**
 * Controlador de navegación. Gestiona qué panel se muestra con CardLayout.

 * SOLICITUD ACTIVA:
 * Navegador guarda la solicitud que el admin está procesando actualmente.
 * PanelDetalleSoli la registra al cargar. PanelCalendario la recupera
 * al confirmar la reserva. Así el flujo completo tiene trazabilidad.
 */
public class Navegador extends JPanel {

    public static final String PANEL_BIENVENIDA   = "bienvenida";
    public static final String PANEL_BUSQUEDA     = "busqueda";
    public static final String PANEL_DETALLE_SOLI = "detalleSolicitud";
    public static final String PANEL_CONFIRMACION = "confirmacion";
    public static final String PANEL_CALENDARIO   = "calendario";

    private final CardLayout       cardLayout;
    private final VentanaPrincipal ventana;

    private final PanelBienvenida   panelBienvenida;
    private final PanelBusqueda     panelBusqueda;
    private final PanelDetalleSoli  panelDetalleSoli;
    private final PanelConfirmacion panelConfirmacion;
    private final PanelCalendario   panelCalendario;

    // Solicitud que el admin está procesando actualmente.
    // Se asigna en mostrarDetalleSolicitud() y se usa en confirmarReserva().
    private Solicitud solicitudActiva;

    public Navegador(VentanaPrincipal ventana) {
        this.ventana    = ventana;
        this.cardLayout = new CardLayout();
        setLayout(cardLayout);

        this.panelBienvenida   = new PanelBienvenida(this);
        this.panelBusqueda     = new PanelBusqueda(this);
        this.panelDetalleSoli  = new PanelDetalleSoli(this);
        this.panelConfirmacion = new PanelConfirmacion(this);
        this.panelCalendario   = new PanelCalendario(this);

        add(panelBienvenida,   PANEL_BIENVENIDA);
        add(panelBusqueda,     PANEL_BUSQUEDA);
        add(panelDetalleSoli,  PANEL_DETALLE_SOLI);
        add(panelConfirmacion, PANEL_CONFIRMACION);
        add(panelCalendario,   PANEL_CALENDARIO);

        mostrarPanel(PANEL_BIENVENIDA);
    }

    // =========================================================
    // Navegación
    // =========================================================

    public void mostrarPanel(String nombre) {
        cardLayout.show(this, nombre);
    }

    public void mostrarBienvenida() {
        solicitudActiva = null;
        panelBienvenida.refrescarSolicitudes();
        mostrarPanel(PANEL_BIENVENIDA);
    }

    public void mostrarBusqueda() {
        panelBusqueda.refrescarTutores();
        mostrarPanel(PANEL_BUSQUEDA);
    }

    public void mostrarCalendario() {
        mostrarPanel(PANEL_CALENDARIO);
    }

    /**
     * Navega al detalle de solicitud y guarda la solicitud activa.
     * PanelCalendario la usará al crear la Reserva.
     */
    public void mostrarDetalleSolicitud(String solicitudId) {
        panelDetalleSoli.cargarSolicitud(solicitudId);
        // Guardar referencia para que PanelCalendario pueda usarla
        this.solicitudActiva = panelDetalleSoli.getSolicitudActual();
        mostrarPanel(PANEL_DETALLE_SOLI);
    }

    /**
     * Navega a confirmación con todos los datos de la reserva creada.
     * Firma completa con Solicitud para mostrar el resumen correcto.
     */
    public void mostrarConfirmacion(Tutor tutor, Estudiante estudiante,
                                    Solicitud solicitud, int dia, int bloque) {
        panelConfirmacion.cargarConfirmacion(tutor, estudiante, solicitud, dia, bloque);
        mostrarPanel(PANEL_CONFIRMACION);
    }

    // =========================================================
    // Getters
    // =========================================================

    /**
     * Devuelve la solicitud que el admin está procesando actualmente.
     * Usado por PanelCalendario al crear la Reserva.
     */
    public Solicitud getSolicitudActiva() { return solicitudActiva; }

    public VentanaPrincipal getVentana() { return ventana; }
}