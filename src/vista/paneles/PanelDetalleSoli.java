package vista.paneles;

import modelo.GestorDatos;
import modelo.entidades.ConstantesHorario;
import modelo.entidades.Solicitud;
import vista.Navegador;
import vista.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Optional;

/**
 * Panel de visualización pormenorizada encargado de exponer los detalles de una solicitud específica.
 *
 * Esta pantalla actúa como el nexo operativo e intermediario obligatorio dentro del flujo principal
 * de la aplicación, cuya secuencia de transiciones se estructura de la siguiente manera:
 * Bienvenida -> [Ver detalle] -> DetalleSolicitud -> [Buscar tutor] -> Busqueda
 *
 * Al cargarse mediante los despachadores del sistema, la clase aísla los datos del estudiante y
 * renderiza una matriz estática en modo de solo lectura que refleja los bloques horarios en los
 * cuales el alumno manifestó tener disponibilidad o interés de asistencia.
 *
 * Esta vista cumple el rol crítico de asegurar la persistencia de la solicitud activa dentro del
 * mediador de pantallas (Navegador), permitiendo que los paneles subsecuentes del flujo (como el
 * calendario de asignación) recuperen la información del estudiante correcto al momento de consolidar
 * los comandos de reserva.
 */
public class PanelDetalleSoli extends JPanel {

    /** Controlador centralizado de rutas de la interfaz encargado de orquestar los cambios de pantallas. */
    private final Navegador navegador;
    /** Componente visual destinado a mostrar el nombre completo del estudiante solicitante. */
    private final JLabel labelNombre;
    /** Componente visual que detalla la carrera de origen y el semestre académico vigente del alumno. */
    private final JLabel labelCarrera;
    /** Componente que expone la dirección de correo institucional del estudiante. */
    private final JLabel labelCorreo;
    /** Etiqueta que resume el núcleo temático o materia requerida en la tutoría. */
    private final JLabel labelAsunto;
    /** Bloque de texto descriptivo reservado para las aclaraciones u observaciones adjuntas del alumno. */
    private final JLabel labelComentario;
    /** Indicador de estado dinámico que sitúa al administrador reflejando el asunto en proceso dentro del encabezado. */
    private final JLabel labelEstadoFlujo;
    /** Matriz bidimensional de etiquetas Swing utilizadas para construir los cuadrantes de la grilla horaria. */
    private final JLabel[][] celdas;
    /** Subpanel intermedio estructurado con un GridLayout que confina las celdas del esquema horario semanal. */
    private final JPanel grillaPanel;
    /** Instancia de la entidad solicitud bajo examen en el panel. */
    private Solicitud solicitudActual;

    /** Rótulos de texto de los días laborales graficados en las columnas de la agenda. */
    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie"};
    /** Etiquetas con las horas de apertura correspondientes a las filas horarias del sistema. */
    private static final String[] BLOQUES = {
            "08:00", "09:30", "11:00", "12:30", "14:00", "15:30"
    };

    /**
     * Construye un nuevo panel de detalle inicializando los contenedores de datos de texto,
     * estructurando las tarjetas de información y preparando los bloques geométricos de la grilla.
     *
     * @param navegador Instancia global del despachador de vistas del sistema.
     */
    public PanelDetalleSoli(Navegador navegador) {
        this.navegador = navegador;
        this.labelNombre = new JLabel();
        this.labelCarrera = new JLabel();
        this.labelCorreo = new JLabel();
        this.labelAsunto = new JLabel();
        this.labelComentario = new JLabel();
        this.labelEstadoFlujo = new JLabel();
        this.celdas = new JLabel[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.grillaPanel = new JPanel();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
    }

    /**
     * Sincroniza y recompone los controles del panel localizando la solicitud solicitada en el
     * GestorDatos mediante su identificador único.
     *
     * Este método es invocado externamente por el Navegador antes de conmutar la visibilidad de la
     * pantalla, garantizando que las referencias a las entidades internas se encuentren debidamente
     * pobladas y evitando excepciones de puntero nulo en tiempo de ejecución.
     *
     * @param solicitudId Cadena técnica representativa del identificador de la solicitud.
     */
    public void cargarSolicitud(String solicitudId) {
        Optional<Solicitud> resultado =
                GestorDatos.getInstancia().buscarSolicitudPorId(solicitudId);
        if (resultado.isEmpty()) return;

        this.solicitudActual = resultado.get();
        var e = solicitudActual.getEstudiante();

        labelNombre.setText(e.getNombre());
        labelCarrera.setText(e.getCarrera() + " — Semestre " + e.getSemestre());
        labelCorreo.setText(e.getCorreo());
        labelAsunto.setText(solicitudActual.getAsunto());
        labelComentario.setText("<html><i>" +
                (solicitudActual.getComentario().isEmpty()
                        ? "Sin comentarios adicionales."
                        : solicitudActual.getComentario()) + "</i></html>");

        labelEstadoFlujo.setText("Solicitud activa: \"" + solicitudActual.getAsunto() + "\"");

        boolean[][] horario = solicitudActual.getHorarioDeseado();
        for (int d = 0; d < ConstantesHorario.DIAS; d++) {
            for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
                celdas[d][b].setBackground(
                        horario[d][b] ? Tema.DISPONIBLE : Tema.NO_DISPONIBLE);
                celdas[d][b].setText(horario[d][b] ? "Disponible" : "");
            }
        }

        revalidate();
        repaint();
    }

    /**
     * Provee acceso a la instancia de la solicitud que se encuentra actualmente en foco.
     *
     * @return El objeto de la entidad Solicitud vinculada.
     */
    public Solicitud getSolicitudActual() { return solicitudActual; }

    /**
     * Configura la franja superior (Zona Norte) del panel maquetando los títulos corporativos
     * y los rótulos pasivos de seguimiento de la sesión.
     *
     * @return El subpanel JPanel formateado con los colores de identidad del encabezado.
     */
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PRIMARIO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.PRIMARIO);

        JLabel titulo = new JLabel("Detalle de Solicitud");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        labelEstadoFlujo.setFont(Tema.FUENTE_CUERPO);
        labelEstadoFlujo.setForeground(new Color(180, 210, 255));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(labelEstadoFlujo);

        p.add(textos, BorderLayout.WEST);
        return p;
    }

    /**
     * Modela la distribución espacial intermedia (Zona Centro) dividiendo simétricamente el
     * lienzo en dos secciones equivalentes para alojar las tarjetas del perfil y del horario.
     *
     * @return Contenedor intermedio provisto de rejillas proporcionales.
     */
    private JPanel crearCuerpo() {
        JPanel p = new JPanel(new GridLayout(1, 2, Tema.PADDING, 0));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, 0, Tema.PADDING));
        p.add(crearTarjetaEstudiante());
        p.add(crearTarjetaHorario());
        return p;
    }

    /**
     * Compila la tarjeta visual izquierda reuniendo los campos de texto estructurados del
     * alumno y sus observaciones adicionales.
     *
     * @return El JPanel de la tarjeta estallado en formato vertical.
     */
    private JPanel crearTarjetaEstudiante() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Tema.FONDO_TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE, 1, true),
                new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING)));

        agregarCampo(p, "Estudiante", labelNombre);
        agregarCampo(p, "Carrera", labelCarrera);
        agregarCampo(p, "Correo", labelCorreo);
        agregarCampo(p, "Asunto", labelAsunto);
        p.add(Box.createVerticalStrut(8));
        labelComentario.setFont(Tema.FUENTE_CUERPO);
        labelComentario.setForeground(Tema.TEXTO_SECUNDARIO);
        p.add(labelComentario);

        return p;
    }

    /**
     * Rutina utilitaria encargada de estilar e insertar pares jerárquicos de etiquetas de control
     * e información dentro de un contenedor.
     *
     * @param p Contenedor de destino.
     * @param etiqueta Texto explicativo superior que define la naturaleza del campo.
     * @param valor Label dinámico que contiene el valor del atributo a desplegar.
     */
    private void agregarCampo(JPanel p, String etiqueta, JLabel valor) {
        JLabel lbl = new JLabel(etiqueta.toUpperCase());
        lbl.setFont(Tema.FUENTE_PEQUENA);
        lbl.setForeground(Tema.TEXTO_SECUNDARIO);
        valor.setFont(Tema.FUENTE_SUBTITULO);
        valor.setForeground(Tema.TEXTO_PRIMARIO);
        p.add(lbl);
        p.add(valor);
        p.add(Box.createVerticalStrut(10));
    }

    /**
     * Ensambla la tarjeta visual derecha unificando los títulos de sección con la grilla de
     * celdas de disponibilidad solicitadas.
     *
     * @return El subpanel estructurado de la agenda del estudiante.
     */
    private JPanel crearTarjetaHorario() {
        JPanel p = new JPanel(new BorderLayout(0, Tema.PADDING_PEQUEÑO));
        p.setBackground(Tema.FONDO_TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE, 1, true),
                new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING)));

        JLabel titulo = new JLabel("Horarios Deseados por el Estudiante");
        titulo.setFont(Tema.FUENTE_SUBTITULO);
        titulo.setForeground(Tema.TEXTO_PRIMARIO);

        grillaPanel.setLayout(new GridLayout(
                ConstantesHorario.BLOQUES + 1,
                ConstantesHorario.DIAS + 1, 2, 2));
        grillaPanel.setBackground(Tema.FONDO);
        construirGrilla();

        p.add(titulo, BorderLayout.NORTH);
        p.add(grillaPanel, BorderLayout.CENTER);
        return p;
    }

    /**
     * Construye secuencialmente las filas y columnas de la agenda acoplando los encabezados
     * textuales con los labels internos indexados en la matriz.
     */
    private void construirGrilla() {
        grillaPanel.add(celdaEncabezado(""));
        for (String d : DIAS)
            grillaPanel.add(celdaEncabezado(d));

        for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
            grillaPanel.add(celdaEncabezado(BLOQUES[b]));
            for (int d = 0; d < ConstantesHorario.DIAS; d++) {
                JLabel c = new JLabel("", SwingConstants.CENTER);
                c.setOpaque(true);
                c.setBackground(Tema.NO_DISPONIBLE);
                c.setBorder(new LineBorder(Color.WHITE, 1));
                c.setFont(Tema.FUENTE_CUERPO);
                celdas[d][b] = c;
                grillaPanel.add(c);
            }
        }
    }

    /**
     * Modela una etiqueta de control endurecida para cumplir la función de celda de rotulado
     * perimetral en la grilla.
     *
     * @param texto Cadena de caracteres que se imprimirá en el foco de la celda guía.
     * @return El componente JLabel estilizado con la tipografía de cabeceras de tablas.
     */
    private JLabel celdaEncabezado(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(Tema.ENCABEZADO_TABLA);
        l.setForeground(Color.WHITE);
        l.setFont(Tema.FUENTE_PEQUENA);
        l.setBorder(new LineBorder(Color.WHITE, 1));
        return l;
    }

    /**
     * Estructura la barra de control inferior (Zona Sur) disponiendo las rutas de escape y los
     * disparadores del módulo de emparejamiento de tutores.
     *
     * @return El panel horizontal de botones alineado al extremo derecho de la visual.
     */
    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(
                Tema.PADDING_PEQUEÑO, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JButton btnVolver = crearBoton("← Volver", Tema.TEXTO_SECUNDARIO);
        JButton btnBuscar = crearBoton("Buscar tutor →", Tema.PRIMARIO);

        btnVolver.addActionListener(e -> navegador.mostrarBienvenida());
        btnBuscar.addActionListener(e -> {
            if (solicitudActual == null) {
                JOptionPane.showMessageDialog(this,
                        "No hay solicitud cargada. Vuelve al inicio.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            navegador.mostrarBusqueda();
        });

        p.add(btnVolver);
        p.add(btnBuscar);
        return p;
    }

    /**
     * Fabrica un botón gráfico parametrizado inyectándole fuentes tipográficas, colores de fondo
     * e instrucciones reactivas para el puntero del ratón.
     *
     * @param texto Cadena informativa frontal del botón.
     * @param fondo Color cromático base del componente.
     * @return El componente JButton listo para recibir disparadores de eventos.
     */
    private JButton crearBoton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setFont(Tema.FUENTE_BOTON);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(160, Tema.ALTO_BOTON));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}