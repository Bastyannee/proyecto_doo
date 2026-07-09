package vista.paneles;

import modelo.GestorDatos;
import modelo.entidades.Solicitud;
import vista.Navegador;
import vista.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel de bienvenida y vista principal de la administración del sistema.
 *
 * Esta clase representa la pantalla principal que ve el usuario administrador al iniciar
 * la aplicación. Presenta un resumen del estado del sistema y una tabla centralizada que
 * lista todas las solicitudes de tutoría en estado pendiente.
 *
 * El panel actúa como el centro de despacho operativo de la aplicación, permitiendo al
 * administrador:
 * - Visualizar de un vistazo el volumen de carga de solicitudes pendientes mediante un contador dinámico.
 * - Examinar datos clave de los estudiantes (nombre, carrera y cantidad de tiempo solicitado).
 * - Interactuar directamente con cada fila mediante botones personalizados integrados en la tabla para delegar el flujo al visor de detalles.
 * - Acceder rápidamente al módulo histórico de reservas generales a través de la barra de navegación inferior.
 *
 * Al integrarse con los componentes de renderizado y edición de celdas personalizados
 * (BotonRenderer y BotonEditor), el panel mantiene una experiencia de usuario interactiva
 * dentro de la arquitectura basada en tablas de Swing.
 */
public class PanelBienvenida extends JPanel {
    /** Controlador de navegación encargado de realizar las transiciones entre pantallas. */
    private final Navegador navegador;
    /** Modelo de datos estructurado para la tabla de solicitudes. */
    private final DefaultTableModel modeloTabla;
    /** Tabla gráfica de Swing configurada con estilos personalizados para desplegar las filas de datos. */
    private final JTable tabla;
    /** Etiqueta superior derecha encargada de reflejar el total actual de solicitudes pendientes en el sistema. */
    private final JLabel labelConteo;
    /** Caché local que mantiene la lista de solicitudes obtenidas desde el gestor de datos para mapear los índices de la tabla. */
    private List<Solicitud> solicitudesActuales;

    /**
     * Construye un nuevo panel de bienvenida inicializando sus componentes visuales y
     * configurando el gestor de distribución.
     *
     * Durante la construcción se define la estructura lógica del modelo de la tabla, se
     * inyectan las referencias de navegación globales, se estila la cabecera y se realiza
     * la primera carga de datos dinámicos mediante una consulta al singleton del modelo.
     *
     * @param navegador Instancia del navegador de vistas que orquesta los intercambios de paneles.
     */
    public PanelBienvenida(Navegador navegador) {
        this.navegador = navegador;
        this.modeloTabla = new DefaultTableModel(
                new String[]{"ID", "Asunto", "Estudiante", "Carrera", "Bloques pedidos", "Acción"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        this.tabla = crearTabla();
        this.labelConteo = new JLabel();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearPieDePagina(), BorderLayout.SOUTH);

        refrescarSolicitudes();
    }

    /**
     * Consulta el estado actual del GestorDatos, limpia las filas vigentes y reconstruye el
     * contenido de la tabla con las solicitudes pendientes actualizadas.
     *
     * Adicionalmente, evalúa el volumen de elementos devueltos para actualizar el texto
     * informativo de la esquina superior derecha del encabezado, modificando su paleta cromática
     * a un tono neutral si la lista se encuentra vacía o a un tono destacado si existen tareas
     * por procesar.
     */
    public void refrescarSolicitudes() {
        solicitudesActuales = GestorDatos.getInstancia().getSolicitudesPendientes();
        modeloTabla.setRowCount(0);
        for (Solicitud s : solicitudesActuales) {
            modeloTabla.addRow(new Object[]{
                    s.getId(),
                    s.getAsunto(),
                    s.getEstudiante().getNombre(),
                    s.getEstudiante().getCarrera(),
                    s.contarBloquesDeseados() + " bloques",
                    "Ver detalle"
            });
        }
        int n = solicitudesActuales.size();
        labelConteo.setText(n == 0
                ? "No hay solicitudes pendientes"
                : n + " solicitud(es) pendiente(s)");
        labelConteo.setForeground(n == 0 ? Tema.TEXTO_SECUNDARIO : new Color(200, 220, 255));
    }

    /**
     * Diseña y empaqueta el contenedor del borde superior (Zona Norte) del panel principal.
     *
     * Este componente visual contiene los títulos corporativos alineados a la izquierda
     * estructurados mediante un BoxLayout vertical, y el indicador dinámico de conteo de
     * solicitudes posicionado a la derecha aprovechando el esquema perimetral del BorderLayout.
     *
     * @return El contenedor JPanel configurado con la identidad visual del encabezado.
     */
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PRIMARIO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.PRIMARIO);

        JLabel titulo = new JLabel("Panel de Administración");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Selecciona una solicitud para asignarle un tutor");
        sub.setFont(Tema.FUENTE_CUERPO);
        sub.setForeground(new Color(180, 205, 255));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(sub);

        labelConteo.setFont(Tema.FUENTE_CUERPO);
        p.add(textos, BorderLayout.WEST);
        p.add(labelConteo, BorderLayout.EAST);
        return p;
    }

    /**
     * Construye la barra de estado y utilidades inferior (Zona Sur) del panel.
     *
     * Integra un mensaje pasivo con instrucciones operativas dirigidas al administrador y un
     * botón de acción rápida que invoca de forma segura al mediador de la interfaz para
     * transicionar hacia la pantalla del registro histórico global de reservas de clases.
     *
     * @return El panel contenedor inferior con márgenes compactos y botones vinculados.
     */
    private JPanel crearPieDePagina() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(
                Tema.PADDING_PEQUEÑO, Tema.PADDING, Tema.PADDING_PEQUEÑO, Tema.PADDING));

        JLabel instruccion = new JLabel(
                "Haz clic en \"Ver detalle\" para revisar una solicitud y asignarle un tutor.");
        instruccion.setFont(Tema.FUENTE_PEQUENA);
        instruccion.setForeground(Tema.TEXTO_SECUNDARIO);

        JButton btnHistorial = new JButton("Ver historial de reservas →");
        btnHistorial.setFont(Tema.FUENTE_BOTON);
        btnHistorial.setBackground(Tema.PRIMARIO);
        btnHistorial.setForeground(Color.WHITE);
        btnHistorial.setFocusPainted(false);
        btnHistorial.setBorderPainted(false);
        btnHistorial.setPreferredSize(new Dimension(230, 34));
        btnHistorial.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHistorial.addActionListener(e -> navegador.mostrarHistorial());

        p.add(instruccion, BorderLayout.WEST);
        p.add(btnHistorial, BorderLayout.EAST);
        return p;
    }

    /**
     * Instancia la tabla del panel parametrizando sus dimensiones de celda, fuentes y
     * anchos de columna preferentes.
     *
     * Este método oculta intencionalmente la primera columna (índice 0) que resguarda el
     * identificador numérico interno del objeto (ID de la solicitud) forzando sus dimensiones
     * máximas y mínimas a cero píxeles. Esto permite conservar el ID accesible programáticamente
     * a nivel de modelo para las consultas del Editor sin exponerlo en la visualización directa del usuario.
     *
     * @return Objeto JTable configurado y enlazado con sus respectivos renderizadores y editores de celda en la columna de acción.
     */
    private JTable crearTabla() {
        JTable t = new JTable(modeloTabla);
        t.setFont(Tema.FUENTE_CUERPO);
        t.setRowHeight(44);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(230, 238, 255));
        t.setBackground(Tema.FONDO_TARJETA);
        t.getTableHeader().setFont(Tema.FUENTE_SUBTITULO);
        t.getTableHeader().setBackground(Tema.ENCABEZADO_TABLA);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setPreferredSize(new Dimension(0, 42));
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(1).setPreferredWidth(200);
        t.getColumnModel().getColumn(2).setPreferredWidth(160);
        t.getColumnModel().getColumn(3).setPreferredWidth(160);
        t.getColumnModel().getColumn(4).setPreferredWidth(110);
        t.getColumnModel().getColumn(5).setPreferredWidth(110);
        t.getColumnModel().getColumn(5).setCellRenderer(new BotonRenderer());
        t.getColumnModel().getColumn(5).setCellEditor(
                new BotonEditor(new JCheckBox(), navegador));
        return t;
    }

    /**
     * Renderizador estático encargado de dibujar un botón gráfico uniforme dentro de las
     * celdas de la columna de operaciones.
     *
     * Dado que Swing requiere un componente físico para pintar los píxeles de una región de
     * celda, esta clase recicla una única instancia interna de un JButton pre-estilizado
     * según las directrices de diseño corporativas, evitando la sobrecarga de instanciar
     * múltiples botones por fila en pantallas extensas.
     */
    static class BotonRenderer implements TableCellRenderer {
        private final JButton boton = new JButton("Ver detalle");
        /**
         * Inicializa la estructura del botón molde asignándole las tipografías y el cursor hand típicos de interacciones.
         */
        BotonRenderer() {
            boton.setFont(Tema.FUENTE_BOTON);
            boton.setBackground(Tema.PRIMARIO);
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        /**
         * Devuelve el componente configurado listo para ser renderizado por el motor gráfico de la JTable.
         *
         * @param t Tabla de procedencia.
         * @param v Valor asignado en la celda del modelo de datos.
         * @param s Estado de selección de la fila actual.
         * @param f Estado de enfoque de la celda específica.
         * @param r Índice de la fila que se está procesando.
         * @param c Índice de la columna que se está procesando.
         * @return El objeto botón listo para ser pintado.
         */
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return boton; }
    }

    /**
     * Editor interactivo encargado de interceptar los clics del usuario sobre el botón de
     * la celda y gatillar el cambio de vista correspondiente.
     *
     * A diferencia del Renderizador, esta clase gestiona activamente eventos de captura de
     * ratón. Al detectar la interacción, interrumpe el estado editable latente de la celda,
     * rescata el identificador único oculto (ID en la columna cero) de la fila clickeada y
     * direcciona la solicitud hacia el método de visualización pormenorizada del Navegador.
     */
    static class BotonEditor extends DefaultCellEditor {
        /** Botón gráfico activo encargado de desplegarse mientras la celda está en modo de edición interactiva. */
        private final JButton boton;
        /** ID técnico de la solicitud vinculada a la fila donde se activó la edición por última vez. */
        private String solicitudId;
        /** Copia del objeto mediador de pantallas para despachar las redirecciones. */
        private final Navegador navegador;

        /**
         * Construye el editor de celdas inicializando la lógica de escucha del componente interactivo.
         *
         * @param cb Casilla de verificación dummy requerida por el constructor por defecto de Swing.
         * @param navegador Objeto navegador utilizado para procesar los ruteos de interfaces.
         */
        BotonEditor(JCheckBox cb, Navegador navegador) {
            super(cb);
            this.navegador = navegador;
            this.boton = new JButton("Ver detalle");
            boton.setFont(Tema.FUENTE_BOTON);
            boton.setBackground(Tema.PRIMARIO_OSCURO);
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.addActionListener(e -> {
                fireEditingStopped();
                navegador.mostrarDetalleSolicitud(solicitudId);
            });
        }

        /**
         * Prepara el componente interactivo extrayendo de forma segura los valores de la
         * fila bajo edición antes de ceder el control al puntero.
         *
         * @param t Tabla de procedencia.
         * @param v Valor guardado en el modelo de celdas.
         * @param s Estado de selección de la celda.
         * @param r Índice de la fila seleccionada por el clic.
         * @param c Índice de la columna seleccionada por el clic.
         * @return El botón interactivo activo en la interfaz durante el evento.
         */
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            solicitudId = (String) t.getValueAt(r, 0);
            return boton;
        }

        /**
         * Devuelve el valor que se mantendrá asignado a la celda una vez que el proceso de edición concluya.
         *
         * @return Texto genérico plano de control.
         */
        @Override public Object getCellEditorValue() { return "Ver detalle"; }
    }
}