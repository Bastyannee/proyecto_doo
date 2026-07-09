package vista.paneles;

import controlador.comandos.ComandoCrearReserva;
import controlador.comandos.HistorialOperaciones;
import controlador.eventos.Observador;
import modelo.entidades.ConstantesHorario;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import vista.Navegador;
import vista.Tema;
import vista.proxy.PerfilSeleccionable;
import vista.proxy.ProxyTutor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

/**
 * Panel de visualización de agenda y gestión de reserva de bloques horarios para tutorías.
 *
 * Esta clase implementa una matriz interactiva que despliega visualmente el calendario semanal
 * de disponibilidad del tutor seleccionado. Su arquitectura sigue un diseño reactivo e
 * introduce desacoplamiento mediante patrones de diseño clave:
 *
 * Patrón Observer: El panel implementa la interfaz Observador y se inscribe directamente al
 * singleton del ProxyTutor durante su instanciación. Cuando el tutor activo muta en el sistema,
 * este panel intercepta la señal de refresco de manera automática.
 *
 * Patrón Proxy: En lugar de operar con acoplamiento fuerte hacia la entidad concreta Tutor, la
 * interfaz interactúa exclusivamente a través del tipo abstracto PerfilSeleccionable. Esto protege
 * al componente visual de accesos estructurales directos a las listas internas de datos.
 *
 * Flujo transaccional de comandos:
 * 1. Al capturar la selección horaria y pulsar la confirmación, se genera una instancia del objeto de control ComandoCrearReserva.
 * 2. Se delega su ejecución al HistorialOperaciones invocando su procesamiento interno mediante encapsulamiento transaccional.
 * 3. Si el estado post-ejecución resulta exitoso, el navegador conmuta la pantalla hacia la vista de confirmación definitiva.
 * 4. Si el motor de reglas de negocio detecta colisiones de horarios, el comando encapsula el fallo y se le notifica al usuario mediante un diálogo informativo.
 */
public class PanelCalendario extends JPanel implements Observador {

    /** Etiquetasabreviadas de los días de la semana representados en las columnas del calendario. */
    private static final String[] DIAS = {
            "Lun", "Mar", "Mié", "Jue", "Vie"
    };
    /** Cadenas de texto representativas de los rangos de tiempo de inicio para cada bloque de clase. */
    private static final String[] BLOQUES = {
            "08:00", "09:30", "11:00", "12:30", "14:00", "15:30"
    };

    /** Despachador encargado de realizar las transiciones de pantalla en la interfaz. */
    private final Navegador  navegador;
    /** Componente de cabecera que expone el nombre y especialidad técnica del tutor en evaluación. */
    private final JLabel     labelNombre;
    /** Mensaje de estado dinámico que asiste al usuario con los costos por hora y las instrucciones de interacción. */
    private final JLabel     labelInfo;
    /** Matriz de etiquetas Swing que constituyen las celdas físicas del bloque horario. */
    private final JLabel[][] celdas;
    /** Panel contenedor secundario estructurado mediante un GridLayout para albergar la matriz del calendario. */
    private final JPanel     grillaPanel;
    /** Control de acción destinado a iniciar el flujo de guardado de la reserva en el sistema. */
    private final JButton    btnConfirmar;

    /** Índice numérico que señala la columna del día de la semana escogido por el operador. */
    private int diaSeleccionado = -1;
    /** Índice numérico que mapea la fila del bloque de tiempo específico seleccionado por el operador. */
    private int bloqueSeleccionado = -1;

    /**
     * Inicializa un nuevo visualizador de calendario enlazando los mediadores de navegación,
     * configurando las estructuras base de la grilla de celdas e inscribiendo la vista en el
     * registro de suscriptores del Proxy.
     *
     * @param navegador Instancia del despachador central de pantallas.
     */
    public PanelCalendario(Navegador navegador) {
        this.navegador = navegador;
        this.labelNombre = new JLabel("Sin tutor seleccionado");
        this.labelInfo = new JLabel(" ");
        this.celdas = new JLabel[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.grillaPanel = new JPanel();

        this.btnConfirmar = new JButton("Confirmar Reserva");

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        ProxyTutor.getInstancia().registrarObservador(this);
    }

    /**
     * Reconstruye dinámicamente los estilos, textos y listeners de la matriz horaria cada vez
     * que el Proxy notifica una variación en el tutor seleccionado.
     *
     * Este método purga las rutinas de eventos MouseListener previas de las celdas disponibles
     * para mitigar fugas de memoria causadas por registros redundantes. Configura además efectos
     * visuales de iluminación (Hover) y restringe la edición sobre los bloques declarados como ocupados.
     *
     * @param perfil Representación abstracta provista por el proxy con los datos del tutor seleccionado.
     */
    @Override
    public void actualizar(PerfilSeleccionable perfil) {
        labelNombre.setText(perfil.getNombre() + "  ·  " + perfil.getMateria());
        labelInfo.setText(String.format(
                "$%.0f/hora  ·  Haz clic en un bloque disponible para seleccionarlo",
                perfil.getTarifa()));

        Solicitud solicitud = navegador.getSolicitudActiva();

        diaSeleccionado = -1;
        bloqueSeleccionado = -1;
        btnConfirmar.setEnabled(false);

        for (int d = 0; d < ConstantesHorario.DIAS; d++) {
            for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
                boolean disponibleTutor = perfil.isDisponible(d, b);
                /**
                 * El bloque solo es agendable si, además de estar libre en la agenda del
                 * tutor, el alumno lo marcó como disponible en su solicitud. De lo contrario
                 * se estaría agendando una clase en un horario donde el alumno no coincide.
                 */
                boolean coincideConAlumno = solicitud == null || solicitud.isBloqueSolicitado(d, b);
                boolean seleccionable = disponibleTutor && coincideConAlumno;

                JLabel celda = celdas[d][b];

                for (var l : celda.getMouseListeners())
                    celda.removeMouseListener(l);

                if (seleccionable) {
                    celda.setBackground(Tema.DISPONIBLE);
                    celda.setForeground(Tema.TEXTO_PRIMARIO);
                    celda.setFont(Tema.FUENTE_CUERPO);
                    celda.setText("Disponible");
                    celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    final int fd = d, fb = b;
                    celda.addMouseListener(new MouseAdapter() {
                        @Override public void mouseClicked(MouseEvent e) {
                            seleccionarBloque(fd, fb);
                        }
                        @Override public void mouseEntered(MouseEvent e) {
                            if (fd != diaSeleccionado || fb != bloqueSeleccionado)
                                celda.setBackground(new Color(100, 200, 100)); // Efecto Hover verde claro
                        }
                        @Override public void mouseExited(MouseEvent e) {
                            if (fd != diaSeleccionado || fb != bloqueSeleccionado)
                                celda.setBackground(Tema.DISPONIBLE);
                        }
                    });
                } else if (disponibleTutor) {
                    // El tutor está libre, pero el alumno no coincide en su horario: se
                    // muestra distinguible del "Ocupado" y no queda habilitado para el clic.
                    celda.setBackground(Tema.FUERA_DE_HORARIO_ALUMNO);
                    celda.setForeground(new Color(150, 120, 40));
                    celda.setFont(Tema.FUENTE_CUERPO.deriveFont(Font.BOLD));
                    celda.setText("No coincide");
                    celda.setCursor(Cursor.getDefaultCursor());
                } else {
                    celda.setBackground(Tema.NO_DISPONIBLE);
                    celda.setForeground(new Color(180, 185, 190));
                    celda.setFont(Tema.FUENTE_CUERPO.deriveFont(Font.BOLD));
                    celda.setText("Ocupado");
                    celda.setCursor(Cursor.getDefaultCursor());
                }
            }
        }
        repaint();
        revalidate();
    }

    /**
     * Alterna de forma segura los estados de selección visual de la grilla, revirtiendo la
     * celda previamente escogida a su tonalidad por defecto y destacando el nuevo bloque.
     *
     * @param dia    Índice de la columna seleccionada.
     * @param bloque Índice de la fila seleccionada.
     */
    private void seleccionarBloque(int dia, int bloque) {
        if (diaSeleccionado >= 0) {
            celdas[diaSeleccionado][bloqueSeleccionado].setBackground(Tema.DISPONIBLE);
            celdas[diaSeleccionado][bloqueSeleccionado].setForeground(Tema.TEXTO_PRIMARIO);
            celdas[diaSeleccionado][bloqueSeleccionado].setText("Disponible");
        }
        diaSeleccionado = dia;
        bloqueSeleccionado = bloque;
        celdas[dia][bloque].setBackground(Tema.PRIMARIO);
        celdas[dia][bloque].setForeground(Color.WHITE);
        celdas[dia][bloque].setText(DIAS[dia] + " " + BLOQUES[bloque]);
        btnConfirmar.setEnabled(true);
    }

    /**
     * Extrae las entidades contextuales activas e intenta compilar la transacción de reserva
     * a través del subsistema de comandos.
     *
     * El método valida la integridad del estado del programa antes de disparar el comando,
     * previniendo ejecuciones sin un tutor asignado, sin una solicitud en foco o sin una
     * selección horaria válida en la matriz gráfica.
     */
    private void confirmarReserva() {
        Tutor tutor = ProxyTutor.getInstancia().getTutorActual();
        Solicitud solicitud = navegador.getSolicitudActiva();

        if (tutor == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay tutor seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (solicitud == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay solicitud activa. Vuelve al inicio y selecciona una.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (diaSeleccionado < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un bloque horario antes de confirmar.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ComandoCrearReserva comando = new ComandoCrearReserva(
                solicitud,
                tutor,
                LocalDate.now(),
                diaSeleccionado,
                bloqueSeleccionado
        );

        HistorialOperaciones.getInstancia().ejecutar(comando);

        if (comando.fueExitosa()) {
            navegador.mostrarConfirmacion(
                    tutor,
                    solicitud.getEstudiante(),
                    solicitud,
                    diaSeleccionado,
                    bloqueSeleccionado
            );
        } else {
            JOptionPane.showMessageDialog(this,
                    comando.getMensajeError(),
                    "Conflicto de horario",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Diseña y empaqueta el subpanel superior (Zona Norte) encargado de albergar las
     * etiquetas de identificación del perfil en consulta.
     *
     * @return El panel contenedor del encabezado con sus fuentes y márgenes configurados.
     */
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PRIMARIO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));
        labelNombre.setFont(Tema.FUENTE_TITULO);
        labelNombre.setForeground(Color.WHITE);
        labelInfo.setFont(Tema.FUENTE_CUERPO);
        labelInfo.setForeground(new Color(200, 220, 255));
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.PRIMARIO);
        textos.add(labelNombre);
        textos.add(labelInfo);
        p.add(textos, BorderLayout.WEST);
        return p;
    }

    /**
     * Prepara el entorno intermedio (Zona Centro) que confina la grilla física de botones de la agenda.
     *
     * @return Un contenedor intermedio configurado con márgenes externos adecuados.
     */
    private JPanel crearCuerpo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, 0, Tema.PADDING));
        grillaPanel.setLayout(new GridLayout(
                ConstantesHorario.BLOQUES + 1,
                ConstantesHorario.DIAS + 1, 3, 3));
        grillaPanel.setBackground(Tema.FONDO);
        construirGrilla();
        p.add(grillaPanel, BorderLayout.CENTER);
        return p;
    }

    /**
     * Ensambla secuencialmente los componentes de cabecera y celdas de datos en el
     * contenedor cuadriculado, respetando las dimensiones algebraicas provistas por las constantes del modelo.
     */
    private void construirGrilla() {
        grillaPanel.add(celdaEncabezado(""));
        for (String d : DIAS) grillaPanel.add(celdaEncabezado(d));
        for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
            grillaPanel.add(celdaEncabezado(BLOQUES[b]));
            for (int d = 0; d < ConstantesHorario.DIAS; d++) {
                JLabel c = new JLabel("", SwingConstants.CENTER);
                c.setOpaque(true);
                c.setBackground(Tema.NO_DISPONIBLE);
                c.setBorder(new LineBorder(Color.WHITE, 2, true));
                c.setFont(Tema.FUENTE_CUERPO);
                c.setPreferredSize(new Dimension(0, 52));
                celdas[d][b] = c;
                grillaPanel.add(c);
            }
        }
    }

    /**
     * Fabrica una etiqueta formateada y pre-estilizada destinada a actuar como rotulado estático
     * de fila o columna dentro del calendario.
     *
     * @param texto Cadena informativa que se va a imprimir en la celda guía.
     * @return El componente JLabel configurado con colores de contraste para cabeceras.
     */
    private JLabel celdaEncabezado(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(Tema.ENCABEZADO_TABLA);
        l.setForeground(Color.WHITE);
        l.setFont(Tema.FUENTE_SUBTITULO);
        l.setBorder(new LineBorder(Color.WHITE, 1));
        return l;
    }

    /**
     * Construye la barra operativa del extremo inferior (Zona Sur) organizando los
     * flujos de navegación inversa y el botón definitivo de reserva.
     *
     * @return Un panel contenedor alineado a la derecha con los botones de control de navegación.
     */
    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(
                Tema.PADDING_PEQUEÑO, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JButton btnVolver = new JButton("← Volver");
        btnVolver.setFont(Tema.FUENTE_BOTON);
        btnVolver.setBackground(Tema.TEXTO_SECUNDARIO);
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setPreferredSize(new Dimension(130, Tema.ALTO_BOTON));
        btnVolver.addActionListener(e -> navegador.mostrarBusqueda());

        btnConfirmar.setFont(Tema.FUENTE_BOTON);
        btnConfirmar.setBackground(Tema.PRIMARIO);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBorderPainted(false);
        btnConfirmar.setPreferredSize(new Dimension(190, Tema.ALTO_BOTON));
        btnConfirmar.setEnabled(false);
        btnConfirmar.addActionListener(e -> confirmarReserva());

        p.add(btnVolver);
        p.add(btnConfirmar);
        return p;
    }
}