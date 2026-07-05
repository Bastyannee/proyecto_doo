package vista;

import vista.paneles.PanelBienvenida;
import vista.paneles.PanelBusqueda;
import vista.paneles.PanelCalendario;
import vista.paneles.PanelConfirmacion;
import vista.paneles.PanelDetalleSoli;

import javax.swing.JPanel;
import java.awt.CardLayout;

/**
 * Controlador de navegación de la aplicación.

 * Gestiona que panel se muestra usando CardLayout: todos los paneles
 * existen desde el inicio y solo se ocultan o muestran, nunca se
 * destruyen al cambiar de vista (preservan su estado interno).

 * NOMBRES DE PANELES — constantes publicas:
 * Usar siempre estas constantes, nunca Strings literales:
 *   navegador.mostrarPanel(Navegador.PANEL_BUSQUEDA)
 * Elimina errores de tipeo y facilita refactorizaciones.

 * RELACION CON PROXYTUTOR:
 * Navegador no interactúa con ProxyTutor. Los paneles que
 * necesitan el tutor seleccionado se registran solos como
 * observadores al construirse.
 */
public class Navegador extends JPanel {

    // =========================================================
    // Nombres de paneles — usar siempre estas constantes
    // =========================================================

    public static final String PANEL_BIENVENIDA   = "bienvenida";
    public static final String PANEL_BUSQUEDA     = "busqueda";
    public static final String PANEL_DETALLE_SOLI = "detalleSolicitud";
    public static final String PANEL_CONFIRMACION = "confirmacion";
    public static final String PANEL_CALENDARIO   = "calendario";

    // =========================================================
    // Estado interno
    // =========================================================

    private final CardLayout       cardLayout;
    private final VentanaPrincipal ventana;

    private final PanelBienvenida   panelBienvenida;
    private final PanelBusqueda     panelBusqueda;
    private final PanelDetalleSoli  panelDetalleSoli;
    private final PanelConfirmacion panelConfirmacion;
    private final PanelCalendario   panelCalendario;

    // =========================================================
    // Constructor
    // =========================================================

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
    public void mostrarPanel(String nombrePanel) {
        cardLayout.show(this, nombrePanel);
    }

    public void mostrarDetalleSolicitud(String solicitudId) {
        panelDetalleSoli.cargarSolicitud(solicitudId);
        mostrarPanel(PANEL_DETALLE_SOLI);
    }

    public void mostrarBusqueda() {
        panelBusqueda.refrescarTutores();
        mostrarPanel(PANEL_BUSQUEDA);
    }

    public void mostrarCalendario() {
        mostrarPanel(PANEL_CALENDARIO);
    }

    public void mostrarConfirmacion() {
        mostrarPanel(PANEL_CONFIRMACION);
    }

    public void mostrarBienvenida() {
        panelBienvenida.refrescarSolicitudes();
        mostrarPanel(PANEL_BIENVENIDA);
    }

    public VentanaPrincipal getVentana() { return ventana; }
}