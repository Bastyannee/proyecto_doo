package vista.paneles;

import modelo.entidades.ConstantesHorario;
import modelo.entidades.Estudiante;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import vista.Navegador;
import vista.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Panel de confirmación final que se despliega tras consolidar con éxito una reserva.
 *
 * Esta clase actúa como la pantalla de cierre transaccional del flujo de asignación de tutorías.
 * Presenta un resumen limpio y centralizado montado dentro de una tarjeta visual, detallando
 * los datos clave de la operación: identidades del tutor y del estudiante, la materia, el asunto,
 * junto con la fecha y el bloque horario exacto recuperados desde las constantes del dominio.
 *
 * A nivel estructural, expone opciones de navegación simétricas en su barra inferior para que
 * el administrador decida si desea retornar al panel de bienvenida principal para procesar
 * nuevas solicitudes o saltar directamente al registro histórico general de reservas.
 */
public class PanelConfirmacion extends JPanel {

    /** Coordinador de rutas de la interfaz encargado del intercambio seguro de pantallas. */
    private final Navegador navegador;
    /** Contenedor interno vertical que organiza las filas de datos informativos del resumen. */
    private final JLabel labelResumen;

    /**
     * Construye un nuevo panel de confirmación inicializando la estructura perimetral de la vista
     * e integrando los subpaneles de cabecera, cuerpo central y botonera inferior.
     *
     * @param navegador Instancia global del despachador de vistas del sistema.
     */
    public PanelConfirmacion(Navegador navegador) {
        this.navegador = navegador;
        this.labelResumen = new JLabel();
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
    }

    /**
     * Recombina y limpia los controles del lienzo central para desplegar de forma estructurada
     * los datos específicos de la reserva que acaba de consolidarse.
     *
     * Este método inyecta dinámicamente filas de texto plano dentro de un contenedor alineado,
     * abstrayéndose de etiquetas HTML internas para garantizar un correcto renderizado de las
     * fuentes y márgenes definidos en la identidad visual del sistema.
     *
     * @param tutor Entidad del tutor asignado a la reserva.
     * @param estudiante Entidad del estudiante que solicitó la asistencia.
     * @param solicitud Instancia de la solicitud de origen procesada.
     * @param dia Índice numérico correlativo al día de la semana asignado.
     * @param bloque Índice numérico correlativo al bloque de tiempo reservado.
     */
    public void cargarConfirmacion(Tutor tutor, Estudiante estudiante, Solicitud solicitud, int dia, int bloque) {
        labelResumen.setText(String.format(
                "<html><div style='text-align:center; line-height:1.8'>"
                        + "<b>Tutor:</b> %s<br>"
                        + "<b>Materia:</b> %s<br>"
                        + "<b>Estudiante:</b> %s — %s<br>"
                        + "<b>Asunto:</b> %s<br>"
                        + "<b>Día:</b> %s<br>"
                        + "<b>Horario:</b> %s<br>"
                        + "<b>Tarifa:</b> $%.0f/hora"
                        + "</div></html>",
                tutor.getNombre(), tutor.getMateria(),
                estudiante.getNombre(), estudiante.getCarrera(),
                solicitud.getAsunto(),
                ConstantesHorario.NOMBRES_DIAS[dia],
                ConstantesHorario.NOMBRES_BLOQUES[bloque],
                tutor.getTarifa()));
    }

    /**
     * Diseña la franja informativa superior (Zona Norte) aplicando colores de realce
     * festivos para indicar la culminación exitosa del proceso de guardado.
     *
     * @return El panel contenedor del encabezado con sus fuentes y padding configurados.
     */
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.ACENTO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));
        JLabel t = new JLabel("✓  Reserva confirmada exitosamente");
        t.setFont(Tema.FUENTE_TITULO);
        t.setForeground(Color.WHITE);
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    /**
     * Estructura el espacio central (Zona Centro) centrando geométricamente una tarjeta
     * contenedora por medio del gestor de distribución GridBagLayout.
     *
     * @return Contenedor central con la tarjeta de datos empotrada en el medio del lienzo.
     */
    private JPanel crearCuerpo() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Tema.FONDO);
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Tema.FONDO_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE, 1, true),
                new EmptyBorder(30, 40, 30, 40)));
        tarjeta.setPreferredSize(new Dimension(460, 300));
        labelResumen.setFont(Tema.FUENTE_CUERPO);
        labelResumen.setForeground(Tema.TEXTO_PRIMARIO);
        labelResumen.setHorizontalAlignment(SwingConstants.CENTER);
        tarjeta.add(labelResumen, BorderLayout.CENTER);
        p.add(tarjeta);
        return p;
    }

    /**
     * Construye la barra de utilidades inferior (Zona Sur) disponiendo controles para
     * reincorporarse al menú base o examinar las bitácoras generales.
     *
     * @return Un panel contenedor alineado al centro con los botones de comandos gráficos vinculados.
     */
    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, Tema.PADDING, 0));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(0, 0, Tema.PADDING, 0));

        JButton btnInicio = boton("Volver al inicio", Tema.TEXTO_SECUNDARIO);
        JButton btnHistorial = boton("Ver historial →", Tema.PRIMARIO);

        btnInicio.addActionListener(e -> navegador.mostrarBienvenida());
        btnHistorial.addActionListener(e -> navegador.mostrarHistorial());

        p.add(btnInicio);
        p.add(btnHistorial);
        return p;
    }

    /**
     * Fabrica y parametriza un botón estándar inyectándole fuentes homogéneas y propiedades
     * de cursor reactivas para interacciones fluidas.
     *
     * @param texto Cadena informativa que se grabará en el frente del botón.
     * @param fondo Color cromático que adoptará la superficie del componente.
     * @return El objeto JButton estilizado y listo para recibir escuchadores de eventos.
     */
    private JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setFont(Tema.FUENTE_BOTON);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(170, Tema.ALTO_BOTON));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}