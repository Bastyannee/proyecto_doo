package vista;

import modelo.entidades.Estudiante;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import vista.paneles.*;

import javax.swing.JPanel;
import java.awt.CardLayout;

/**
 * Gestor de navegación centralizado para la interfaz gráfica de usuario.
 *
 * Arquitectura basada en CardLayout:
 * Esta clase actúa como un contenedor especializado de Swing que administra el intercambio
 * dinámico de pantallas (subpaneles) dentro de la ventana principal. Mantiene instancias
 * persistentes de los paneles del sistema para mitigar la sobrecarga por re-instanciación
 * y expone una API limpia para conmutar estados visuales de forma segura.
 *
 * Control de Flujo y Estado Operacional:
 * Además de coordinar la visibilidad de los componentes, el Navegador centraliza la referencia
 * a la entidad Solicitud que se encuentra bajo procesamiento activo (solicitudActiva), permitiendo
 * compartir información contextual crítica entre las diferentes fases del flujo de reserva.
 */
public class Navegador extends JPanel {

    public static final String PANEL_BIENVENIDA = "bienvenida";
    public static final String PANEL_BUSQUEDA = "busqueda";
    public static final String PANEL_DETALLE_SOLI = "detalleSolicitud";
    public static final String PANEL_CONFIRMACION = "confirmacion";
    public static final String PANEL_CALENDARIO = "calendario";
    public static final String PANEL_HISTORIAL = "historial";

    /** Administrador de diseño encargado de la conmutación de tarjetas visuales. */
    private final CardLayout cardLayout;
    /** Referencia al marco principal de la aplicación para coordinación de ventanas. */
    private final VentanaPrincipal ventana;

    private final PanelBienvenida panelBienvenida;
    private final PanelBusqueda panelBusqueda;
    private final PanelDetalleSoli panelDetalleSoli;
    private final PanelConfirmacion panelConfirmacion;
    private final PanelCalendario panelCalendario;
    private final PanelHistorial panelHistorial;

    /** Instancia transaccional de la solicitud que se está gestionando u operando actualmente. */
    private Solicitud solicitudActiva;

    /**
     * Construye e inicializa el gestor de navegación, ensamblando el mapa de tarjetas
     * visuales y estableciendo el estado inicial de la pantalla.
     *
     * @param ventana Referencia a la ventana contenedora principal del sistema.
     */
    public Navegador(VentanaPrincipal ventana) {
        this.ventana = ventana;
        this.cardLayout = new CardLayout();
        setLayout(cardLayout);

        this.panelBienvenida = new PanelBienvenida(this);
        this.panelBusqueda = new PanelBusqueda(this);
        this.panelDetalleSoli = new PanelDetalleSoli(this);
        this.panelConfirmacion = new PanelConfirmacion(this);
        this.panelCalendario = new PanelCalendario(this);
        this.panelHistorial = new PanelHistorial(this);

        add(panelBienvenida, PANEL_BIENVENIDA);
        add(panelBusqueda, PANEL_BUSQUEDA);
        add(panelDetalleSoli, PANEL_DETALLE_SOLI);
        add(panelConfirmacion, PANEL_CONFIRMACION);
        add(panelCalendario, PANEL_CALENDARIO);
        add(panelHistorial, PANEL_HISTORIAL);

        mostrarPanel(PANEL_BIENVENIDA);
    }

    /**
     * Alterna la visibilidad del contenedor hacia la tarjeta especificada mediante su clave identificadora.
     *
     * @param nombre Cadena técnica que empareja con alguna de las constantes públicas de la clase.
     */
    public void mostrarPanel(String nombre) { cardLayout.show(this, nombre); }

    /**
     * Transiciona el entorno hacia el panel de bienvenida, liberando cualquier estado de
     * solicitud previa y forzando la recarga reactiva de los listados pendientes.
     */
    public void mostrarBienvenida() {
        solicitudActiva = null;
        panelBienvenida.refrescarSolicitudes();
        mostrarPanel(PANEL_BIENVENIDA);
    }

    /**
     * Transiciona el entorno hacia el panel de búsqueda y filtrado de tutores,
     * gatillando la actualización automática del catálogo de perfiles disponibles.
     */
    public void mostrarBusqueda() {
        panelBusqueda.refrescarTutores();
        mostrarPanel(PANEL_BUSQUEDA);
    }

    /**
     * Transiciona el entorno hacia el módulo del calendario o agenda horaria global.
     */
    public void mostrarCalendario() {
        mostrarPanel(PANEL_CALENDARIO);
    }

    /**
     * Recupera y carga la información pormenorizada de una solicitud específica en el panel
     * de detalles, actualizando la referencia activa del navegador antes de realizar el despliegue.
     *
     * @param solicitudId Identificador único de la solicitud que se desea auditar.
     */
    public void mostrarDetalleSolicitud(String solicitudId) {
        panelDetalleSoli.cargarSolicitud(solicitudId);
        this.solicitudActiva = panelDetalleSoli.getSolicitudActual();
        mostrarPanel(PANEL_DETALLE_SOLI);
    }

    /**
     * Prepara y proyecta el flujo de confirmación final para una reserva de tutoría,
     * poblando la vista con las entidades involucradas y las coordenadas espacio-temporales acordadas.
     *
     * @param tutor Entidad del docente seleccionado.
     * @param estudiante Entidad del alumno solicitante.
     * @param solicitud Instancia base del trámite o proceso.
     * @param dia Índice de la jornada (0 para Lunes ... 4 para Viernes).
     * @param bloque Índice de la franja horaria (0 para las 08:00 ... 7 para las 17:00).
     */
    public void mostrarConfirmacion(Tutor tutor, Estudiante estudiante, Solicitud solicitud, int dia, int bloque) {
        panelConfirmacion.cargarConfirmacion(tutor, estudiante, solicitud, dia, bloque);
        mostrarPanel(PANEL_CONFIRMACION);
    }

    /**
     * Transiciona el entorno hacia el panel histórico de transacciones y registros pasados,
     * forzando la renovación de las tablas de datos adjuntas.
     */
    public void mostrarHistorial() {
        panelHistorial.refrescarHistorial();
        mostrarPanel(PANEL_HISTORIAL);
    }

    /**
     * Obtiene la solicitud actualmente retenida en el hilo de navegación visual.
     *
     * @return La instancia de la entidad Solicitud bajo proceso, o null si el flujo se encuentra limpio.
     */
    public Solicitud getSolicitudActiva() {
        return solicitudActiva;
    }

    /**
     * Recupera el marco de la ventana principal que aloja a este gestor.
     *
     * @return Instancia de VentanaPrincipal vinculada.
     */
    public VentanaPrincipal getVentana() {
        return ventana;
    }
}