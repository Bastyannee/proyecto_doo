package vista.paneles;

import controlador.comandos.HistorialOperaciones;
import modelo.GestorDatos;
import modelo.entidades.Reserva;
import modelo.entidades.Tutor;
import vista.Navegador;
import vista.Tema;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Panel de consulta y gestión retrospectiva que despliega el historial general de reservas.
 *
 * Esta clase expone de manera cronológica o secuencial todas las transiciones operadas en el
 * sistema, empaquetando cada registro dentro de una tarjeta visual. Cada fila incluye la foto
 * recortada en formato circular del tutor asignado, un badge cromático indicador del estado de
 * vigencia de la cita y una botonera contextual de cancelación.
 *
 * Mecánica de cancelación:
 * En alineación estricta con el modelo de dominio (Reserva.java), las cancelaciones directas
 * manipulan el estado interno mutando la propiedad mediante setEstado hacia el valor pasivo
 * CANCELADA, prescindiendo de llamadas abstractas a métodos de destrucción que no se hallan
 * contemplados en el negocio.
 *
 * Transacciones reversibles (Deshacer):
 * Para revertir de forma segura la última acción consolidada, el panel delega el flujo al
 * singleton HistorialOperaciones.deshacerUltimo(). Este mecanismo intercepta la operación,
 * da de baja la reserva y reacondiciona la solicitud de origen al estado PENDIENTE, evitando
 * la necesidad de acoplar rutinas complejas de des-ejecución (unexecute) en las vistas.
 */
public class PanelHistorial extends JPanel {

    /** Coordinador central de rutas empleado para regresar a los menús principales. */
    private final Navegador navegador;
    /** Panel contenedor interno provisto de scroll que agrupa las filas de las tarjetas. */
    private final JPanel panelReservas;
    /** Etiqueta informativa que expone el balance cuantitativo de registros listados. */
    private final JLabel labelConteo;
    /** Disparador de restauración encargado de activar el des-procesamiento del último comando. */
    private final JButton btnDeshacer;

    /**
     * Construye e inicializa el panel de historial configurando las barras de desplazamiento,
     * estructurando el encabezado y gatillando la primera lectura del almacén de datos.
     *
     * @param navegador Instancia global del despachador de vistas del sistema.
     */
    public PanelHistorial(Navegador navegador) {
        this.navegador = navegador;
        this.panelReservas = new JPanel();
        this.labelConteo = new JLabel();
        this.btnDeshacer = new JButton("↩ Deshacer última reserva");

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(new JScrollPane(panelReservas), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        panelReservas.setBackground(Tema.FONDO);
        refrescarHistorial();
    }

    /**
     * Recombina y limpia los elementos visuales del listado barriendo la colección actualizada
     * de reservas guardadas en el GestorDatos.
     *
     * Si la bitácora del sistema se encuentra vacía, inyecta un aviso estático de ausencia de datos.
     * Evalúa además de manera reactiva el estado del stack de comandos para activar o deshabilitar
     * el control gráfico de deshacer.
     */
    public void refrescarHistorial() {
        List<Reserva> reservas = GestorDatos.getInstancia().getReservas();
        panelReservas.removeAll();
        panelReservas.setLayout(new GridLayout(0, 1, 0, Tema.PADDING_PEQUEÑO));
        panelReservas.setBorder(new EmptyBorder(
                Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        if (reservas.isEmpty()) {
            JLabel vacio = new JLabel(
                    "No hay reservas confirmadas todavía.", SwingConstants.CENTER);
            vacio.setFont(Tema.FUENTE_CUERPO);
            vacio.setForeground(Tema.TEXTO_SECUNDARIO);
            panelReservas.setLayout(new BorderLayout());
            panelReservas.add(vacio, BorderLayout.CENTER);
        } else {
            for (Reserva r : reservas) {
                panelReservas.add(crearFilaReserva(r));
            }
        }

        int n = reservas.size();
        labelConteo.setText(n == 0 ? "Sin reservas" : n + " reserva(s) en el historial");
        btnDeshacer.setEnabled(HistorialOperaciones.getInstancia().puedeDeshacer());
        panelReservas.revalidate();
        panelReservas.repaint();
    }

    /**
     * Ensambla una tarjeta visual independiente parametrizada con los atributos de una reserva.
     *
     * @param reserva Instancia de la entidad Reserva que provee el contexto de la fila.
     * @return El subpanel JPanel maquetado con la información de la reserva y sus botones vinculados.
     */
    private JPanel crearFilaReserva(Reserva reserva) {
        JPanel fila = new JPanel(new BorderLayout(Tema.PADDING, 0));
        fila.setBackground(Tema.FONDO_TARJETA);
        fila.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE, 1, true),
                new EmptyBorder(Tema.PADDING_PEQUEÑO, Tema.PADDING,
                        Tema.PADDING_PEQUEÑO, Tema.PADDING)));

        JLabel foto = crearFotoCircular(reserva.getTutor());

        JPanel datos = new JPanel();
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        datos.setBackground(Tema.FONDO_TARJETA);

        JLabel lblTutor = new JLabel(
                reserva.getTutor().getNombre() + "  ·  " + reserva.getTutor().getMateria());
        lblTutor.setFont(Tema.FUENTE_SUBTITULO);
        lblTutor.setForeground(Tema.TEXTO_PRIMARIO);

        JLabel lblEstudiante = new JLabel(
                "Estudiante: " + reserva.getEstudiante().getNombre()
                        + " — " + reserva.getEstudiante().getCarrera());
        lblEstudiante.setFont(Tema.FUENTE_CUERPO);
        lblEstudiante.setForeground(Tema.TEXTO_SECUNDARIO);

        JLabel lblHorario = new JLabel(
                reserva.getNombreDia() + "  ·  " + reserva.getHora()
                        + "  ·  " + reserva.getFecha().toString());
        lblHorario.setFont(Tema.FUENTE_PEQUENA);
        lblHorario.setForeground(Tema.PRIMARIO);

        datos.add(lblTutor);
        datos.add(Box.createVerticalStrut(3));
        datos.add(lblEstudiante);
        datos.add(Box.createVerticalStrut(3));
        datos.add(lblHorario);

        JPanel derecho = new JPanel(new BorderLayout(0, 4));
        derecho.setBackground(Tema.FONDO_TARJETA);

        JLabel badge = crearBadge(reserva);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(Tema.FUENTE_PEQUENA);
        btnCancelar.setBackground(Tema.PELIGRO);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setPreferredSize(new Dimension(90, 30));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.setEnabled(reserva.isActiva());
        btnCancelar.addActionListener(e -> cancelarReserva(reserva));

        derecho.add(badge, BorderLayout.NORTH);
        derecho.add(btnCancelar, BorderLayout.SOUTH);

        fila.add(foto, BorderLayout.WEST);
        fila.add(datos, BorderLayout.CENTER);
        fila.add(derecho, BorderLayout.EAST);
        return fila;
    }

    /**
     * Construye un rótulo opaco con bordes redondeados simulados para denotar visualmente
     * si la reserva continúa activa o ha sido revocada.
     *
     * @param reserva Entidad de la cual se lee la vigencia.
     * @return Componente JLabel configurado con colores de contraste del tema.
     */
    private JLabel crearBadge(Reserva reserva) {
        String texto = reserva.isActiva() ? "ACTIVA" : "CANCELADA";
        Color fondo = reserva.isActiva() ? Tema.ACENTO : Tema.TEXTO_SECUNDARIO;
        JLabel b = new JLabel(texto, SwingConstants.CENTER);
        b.setFont(Tema.FUENTE_PEQUENA);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBackground(fondo);
        b.setBorder(new EmptyBorder(2, 8, 2, 8));
        return b;
    }

    /**
     * Recupera una imagen de disco desde las rutas de recursos del sistema y aplica una máscara
     * geométrica elíptica para recortarla en forma de círculo.
     *
     * Si la ruta resulta inaccesible o es inexistente, el método se recupera de manera pasiva
     * renderizando un avatar circular con relleno plano que porta las iniciales del tutor en el centro.
     *
     * @param tutor Entidad de la cual se extrae la ruta del asset fotográfico y las iniciales.
     * @return Una etiqueta conteniendo el ImageIcon procesado con suavizado de bordes (Antialiasing).
     */
    private JLabel crearFotoCircular(Tutor tutor) {
        int size = 60;
        BufferedImage imagen = null;

        if (tutor.getFotoPath() != null && !tutor.getFotoPath().isEmpty()) {
            try {
                imagen = ImageIO.read(new File(tutor.getFotoPath()));
            } catch (IOException ignored) {}
        }

        BufferedImage circulo = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circulo.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (imagen != null) {
            g2.setClip(new Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(imagen.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            g2.setColor(Tema.PRIMARIO);
            g2.fillOval(0, 0, size, size);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            String iniciales = obtenerIniciales(tutor.getNombre());
            g2.drawString(iniciales,
                    (size - fm.stringWidth(iniciales)) / 2,
                    (size - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();

        JLabel label = new JLabel(new ImageIcon(circulo));
        label.setPreferredSize(new Dimension(size + 10, size + 10));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * Parsea una cadena de texto extrayendo el primer carácter de los dos primeros bloques
     * de palabras para conformar el juego de iniciales del avatar.
     *
     * @param nombre Cadena con el nombre del profesional.
     * @return Una cadena en mayúsculas de máximo dos caracteres.
     */
    private String obtenerIniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, partes.length); i++)
            if (!partes[i].isEmpty())
                sb.append(Character.toUpperCase(partes[i].charAt(0)));
        return sb.toString();
    }

    /**
     * Dispara un cuadro de diálogo confirmatorio e interrumpe la vigencia de la reserva
     * conmutando su estado interno en el modelo.
     *
     * @param reserva Instancia concreta de la reserva que se desea dar de baja.
     */
    private void cancelarReserva(Reserva reserva) {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Cancelar la reserva con " + reserva.getTutor().getNombre() + "?",
                "Confirmar cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
            refrescarHistorial();
        }
    }

    /**
     * Modela la barra superior corporativa (Zona Norte) disponiendo los títulos del panel
     * y la etiqueta dinámica de conteo estadístico.
     *
     * @return El panel JPanel del encabezado con sus fuentes y paddings corporativos.
     */
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PRIMARIO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.PRIMARIO);

        JLabel titulo = new JLabel("Historial de Reservas");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        labelConteo.setFont(Tema.FUENTE_CUERPO);
        labelConteo.setForeground(new Color(180, 210, 255));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(labelConteo);

        p.add(textos, BorderLayout.WEST);
        return p;
    }

    /**
     * Construye la faja de utilidades del extremo inferior (Zona Sur) acoplando los botones
     * de escape direccional y el disparador de des-hacer.
     *
     * @return Un panel contenedor alineado a la derecha con los botones estilizados del pie.
     */
    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(
                Tema.PADDING_PEQUEÑO, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JButton btnVolver = new JButton("← Volver al inicio");
        btnVolver.setFont(Tema.FUENTE_BOTON);
        btnVolver.setBackground(Tema.TEXTO_SECUNDARIO);
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setPreferredSize(new Dimension(170, Tema.ALTO_BOTON));
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> navegador.mostrarBienvenida());

        btnDeshacer.setFont(Tema.FUENTE_BOTON);
        btnDeshacer.setBackground(new Color(200, 100, 0));
        btnDeshacer.setForeground(Color.WHITE);
        btnDeshacer.setFocusPainted(false);
        btnDeshacer.setBorderPainted(false);
        btnDeshacer.setPreferredSize(new Dimension(230, Tema.ALTO_BOTON));
        btnDeshacer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDeshacer.addActionListener(e -> {
            boolean deshecho = HistorialOperaciones.getInstancia().deshacerUltimo();
            if (deshecho) {
                refrescarHistorial();
                JOptionPane.showMessageDialog(this,
                        "Última reserva deshecha. La solicitud vuelve a estado pendiente.",
                        "Deshacer exitoso", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        p.add(btnVolver);
        p.add(btnDeshacer);
        return p;
    }
}