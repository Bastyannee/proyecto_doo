package vista.paneles;

import controlador.comandos.ComandoCrearReserva;
import controlador.comandos.Comando;
import controlador.comandos.HistorialOperaciones;
import modelo.GestorDatos;
import modelo.entidades.ConstantesHorario;
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
 * Panel de historial de reservas confirmadas.

 * Muestra todas las reservas registradas en GestorDatos con:
 * - Foto circular del tutor (desde assets/fotos/)
 * - Datos completos de la reserva
 * - Botón "Cancelar" por fila

 * También permite deshacer la última operación via
 * HistorialOperaciones.deshacerUltimo(), que llama a
 * unexecute() del ComandoCrearReserva en el tope del stack.
 */
public class PanelHistorial extends JPanel {

    private final Navegador navegador;
    private final JPanel    panelReservas;
    private final JLabel    labelConteo;
    private final JButton   btnDeshacer;

    public PanelHistorial(Navegador navegador) {
        this.navegador     = navegador;
        this.panelReservas = new JPanel();
        this.labelConteo   = new JLabel();
        this.btnDeshacer   = new JButton("↩ Deshacer última reserva");

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(),              BorderLayout.NORTH);
        add(new JScrollPane(panelReservas), BorderLayout.CENTER);
        add(crearBotones(),                 BorderLayout.SOUTH);

        panelReservas.setBackground(Tema.FONDO);
        refrescarHistorial();
    }

    // ── API pública ──────────────────────────────────────────

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
        labelConteo.setText(n == 0
            ? "Sin reservas"
            : n + " reserva(s) en el historial");

        btnDeshacer.setEnabled(HistorialOperaciones.getInstancia().puedeDeshacer());
        panelReservas.revalidate();
        panelReservas.repaint();
    }

    // ── Construcción de filas ─────────────────────────────────

    private JPanel crearFilaReserva(Reserva reserva) {
        JPanel fila = new JPanel(new BorderLayout(Tema.PADDING, 0));
        fila.setBackground(Tema.FONDO_TARJETA);
        fila.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Tema.BORDE, 1, true),
            new EmptyBorder(Tema.PADDING_PEQUEÑO, Tema.PADDING,
                            Tema.PADDING_PEQUEÑO, Tema.PADDING)));

        // Foto circular del tutor
        JLabel foto = crearFotoCircular(reserva.getTutor());

        // Datos de la reserva
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

        // Badge de estado
        JLabel lblEstado = crearBadgeEstado(reserva);

        datos.add(lblTutor);
        datos.add(Box.createVerticalStrut(3));
        datos.add(lblEstudiante);
        datos.add(Box.createVerticalStrut(3));
        datos.add(lblHorario);

        // Panel derecho: estado + botón cancelar
        JPanel derecho = new JPanel(new BorderLayout(0, 4));
        derecho.setBackground(Tema.FONDO_TARJETA);

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

        derecho.add(lblEstado,  BorderLayout.NORTH);
        derecho.add(btnCancelar, BorderLayout.SOUTH);

        fila.add(foto,   BorderLayout.WEST);
        fila.add(datos,  BorderLayout.CENTER);
        fila.add(derecho, BorderLayout.EAST);

        return fila;
    }

    private JLabel crearBadgeEstado(Reserva reserva) {
        String texto = reserva.isActiva() ? "ACTIVA" : "CANCELADA";
        Color  fondo = reserva.isActiva() ? Tema.ACENTO : Tema.TEXTO_SECUNDARIO;

        JLabel badge = new JLabel(texto, SwingConstants.CENTER);
        badge.setFont(Tema.FUENTE_PEQUENA);
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(fondo);
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        return badge;
    }

    /**
     * Carga la foto del tutor desde assets/fotos/ y la recorta en círculo.
     * Si el archivo no existe, genera un avatar con las iniciales del tutor.
     */
    private JLabel crearFotoCircular(Tutor tutor) {
        int size = 60;
        BufferedImage imagen = null;

        // Intentar cargar desde assets/fotos/
        if (tutor.getFotoPath() != null && !tutor.getFotoPath().isEmpty()) {
            try {
                imagen = ImageIO.read(new File(tutor.getFotoPath()));
            } catch (IOException ignored) {
                // Si no existe el archivo, usamos el avatar de iniciales
            }
        }

        BufferedImage circulo = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circulo.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (imagen != null) {
            // Escalar y recortar en círculo
            g2.setClip(new Ellipse2D.Float(0, 0, size, size));
            Image escalada = imagen.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            g2.drawImage(escalada, 0, 0, null);
        } else {
            // Avatar de iniciales
            g2.setColor(Tema.PRIMARIO);
            g2.fillOval(0, 0, size, size);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            String iniciales = obtenerIniciales(tutor.getNombre());
            int x = (size - fm.stringWidth(iniciales)) / 2;
            int y = (size - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(iniciales, x, y);
        }

        g2.dispose();

        JLabel label = new JLabel(new ImageIcon(circulo));
        label.setPreferredSize(new Dimension(size + 10, size + 10));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private String obtenerIniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, partes.length); i++) {
            if (!partes[i].isEmpty())
                sb.append(Character.toUpperCase(partes[i].charAt(0)));
        }
        return sb.toString();
    }

    // ── Lógica de cancelar y deshacer ────────────────────────

    private void cancelarReserva(Reserva reserva) {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Cancelar la reserva con " + reserva.getTutor().getNombre() + "?",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            reserva.cancelar();
            refrescarHistorial();
        }
    }

    // ── Construcción UI ──────────────────────────────────────

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
                    "Última reserva deshecha exitosamente.",
                    "Deshacer", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        p.add(btnVolver);
        p.add(btnDeshacer);
        return p;
    }
}
