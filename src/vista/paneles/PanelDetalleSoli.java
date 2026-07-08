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
 * Muestra el detalle completo de una solicitud seleccionada.

 * POSICION EN EL FLUJO:
 * Bienvenida → [Ver detalle] → DetalleSolicitud → [Buscar tutor] → Busqueda

 * Este panel es el paso intermedio obligatorio. Al cargarse, registra
 * la solicitud activa en Navegador para que PanelCalendario pueda
 * usarla al crear la Reserva con el estudiante correcto.
 */
public class PanelDetalleSoli extends JPanel {

    private final Navegador navegador;
    private final JLabel    labelNombre;
    private final JLabel    labelCarrera;
    private final JLabel    labelCorreo;
    private final JLabel    labelAsunto;
    private final JLabel    labelComentario;
    private final JLabel    labelEstadoFlujo;
    private final JLabel[][] celdas;
    private final JPanel    grillaPanel;
    private Solicitud       solicitudActual;

    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie"};
    private static final String[] BLOQUES = {
            "08:00", "09:30", "11:00", "12:30", "14:00", "15:30"
    };

    public PanelDetalleSoli(Navegador navegador) {
        this.navegador       = navegador;
        this.labelNombre     = new JLabel();
        this.labelCarrera    = new JLabel();
        this.labelCorreo     = new JLabel();
        this.labelAsunto     = new JLabel();
        this.labelComentario = new JLabel();
        this.labelEstadoFlujo = new JLabel();
        this.celdas          = new JLabel[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.grillaPanel     = new JPanel();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(),     BorderLayout.CENTER);
        add(crearBotones(),    BorderLayout.SOUTH);
    }

    // ── API pública ──────────────────────────────────────────

    /**
     * Carga los datos de la solicitud seleccionada.
     * Llamado por Navegador.mostrarDetalleSolicitud() antes de navegar
     * a este panel — garantiza que solicitudActual nunca sea null
     * cuando el panel es visible.
     */
    public void cargarSolicitud(String solicitudId) {
        Optional<Solicitud> resultado =
                GestorDatos.getInstancia().buscarSolicitudPorId(solicitudId);
        if (resultado.isEmpty()) return;

        this.solicitudActual = resultado.get();
        var e = solicitudActual.getEstudiante();

        // Datos del estudiante
        labelNombre.setText(e.getNombre());
        labelCarrera.setText(e.getCarrera() + " — Semestre " + e.getSemestre());
        labelCorreo.setText(e.getCorreo());
        labelAsunto.setText(solicitudActual.getAsunto());
        labelComentario.setText("<html><i>" +
                (solicitudActual.getComentario().isEmpty()
                        ? "Sin comentarios adicionales."
                        : solicitudActual.getComentario()) + "</i></html>");

        // Indicador de flujo
        labelEstadoFlujo.setText("Solicitud activa: \"" + solicitudActual.getAsunto() + "\"");

        // Grilla de horarios deseados
        boolean[][] horario = solicitudActual.getHorarioDeseado();
        for (int d = 0; d < ConstantesHorario.DIAS; d++) {
            for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
                celdas[d][b].setBackground(
                        horario[d][b] ? Tema.DISPONIBLE : Tema.NO_DISPONIBLE);
                celdas[d][b].setText(horario[d][b] ? "✓" : "");
            }
        }

        revalidate();
        repaint();
    }

    public Solicitud getSolicitudActual() { return solicitudActual; }

    // ── Construcción UI ──────────────────────────────────────

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

    private JPanel crearCuerpo() {
        JPanel p = new JPanel(new GridLayout(1, 2, Tema.PADDING, 0));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, 0, Tema.PADDING));
        p.add(crearTarjetaEstudiante());
        p.add(crearTarjetaHorario());
        return p;
    }

    private JPanel crearTarjetaEstudiante() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Tema.FONDO_TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE, 1, true),
                new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING)));

        agregarCampo(p, "Estudiante",  labelNombre);
        agregarCampo(p, "Carrera",     labelCarrera);
        agregarCampo(p, "Correo",      labelCorreo);
        agregarCampo(p, "Asunto",      labelAsunto);
        p.add(Box.createVerticalStrut(8));
        labelComentario.setFont(Tema.FUENTE_CUERPO);
        labelComentario.setForeground(Tema.TEXTO_SECUNDARIO);
        p.add(labelComentario);

        return p;
    }

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
                ConstantesHorario.DIAS   + 1, 2, 2));
        grillaPanel.setBackground(Tema.FONDO);
        construirGrilla();

        p.add(titulo,      BorderLayout.NORTH);
        p.add(grillaPanel, BorderLayout.CENTER);
        return p;
    }

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

    private JLabel celdaEncabezado(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(Tema.ENCABEZADO_TABLA);
        l.setForeground(Color.WHITE);
        l.setFont(Tema.FUENTE_PEQUENA);
        l.setBorder(new LineBorder(Color.WHITE, 1));
        return l;
    }

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