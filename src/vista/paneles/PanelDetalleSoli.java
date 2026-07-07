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
 * Muestra el detalle completo de una solicitud: datos del estudiante,
 * comentario y grilla de bloques horarios deseados.
 */
public class PanelDetalleSoli extends JPanel {

    private final Navegador navegador;
    private final JLabel    labelNombre;
    private final JLabel    labelCarrera;
    private final JLabel    labelCorreo;
    private final JLabel    labelAsunto;
    private final JLabel    labelComentario;
    private final JLabel[][] celdas;
    private final JPanel    grillaPanel;
    private Solicitud       solicitudActual;

    private static final String[] DIAS    = {"Lun","Mar","Mié","Jue","Vie"};
    private static final String[] BLOQUES = {
            "08:00","09:30","11:00","12:30","14:00","15:30"
    };

    public PanelDetalleSoli(Navegador navegador) {
        this.navegador      = navegador;
        this.labelNombre    = new JLabel();
        this.labelCarrera   = new JLabel();
        this.labelCorreo    = new JLabel();
        this.labelAsunto    = new JLabel();
        this.labelComentario= new JLabel();
        this.celdas         = new JLabel[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.grillaPanel    = new JPanel();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(),  BorderLayout.NORTH);
        add(crearCuerpo(),      BorderLayout.CENTER);
        add(crearBotones(),     BorderLayout.SOUTH);
    }

    // ── API pública ──────────────────────────────────────────

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

        boolean[][] horario = solicitudActual.getHorarioDeseado();
        for (int d = 0; d < ConstantesHorario.DIAS; d++) {
            for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
                celdas[d][b].setBackground(
                        horario[d][b] ? Tema.DISPONIBLE : Tema.NO_DISPONIBLE);
                celdas[d][b].setText(horario[d][b] ? "✓" : "");
            }
        }
    }

    public Solicitud getSolicitudActual() { return solicitudActual; }

    //  Construcción UI

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PRIMARIO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));
        JLabel t = new JLabel("Detalle de Solicitud");
        t.setFont(Tema.FUENTE_TITULO);
        t.setForeground(Color.WHITE);
        p.add(t, BorderLayout.WEST);
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

        JLabel titulo = new JLabel("Horarios Deseados");
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
        grillaPanel.add(celda("", Tema.ENCABEZADO_TABLA, Color.WHITE, Tema.FUENTE_PEQUENA));
        for (String d : DIAS)
            grillaPanel.add(celda(d, Tema.ENCABEZADO_TABLA, Color.WHITE, Tema.FUENTE_PEQUENA));

        for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
            grillaPanel.add(celda(BLOQUES[b], Tema.ENCABEZADO_TABLA, Color.WHITE, Tema.FUENTE_PEQUENA));
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

    private JLabel celda(String texto, Color fondo, Color texto2, Font fuente) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(fondo);
        l.setForeground(texto2);
        l.setFont(fuente);
        l.setBorder(new LineBorder(Color.WHITE, 1));
        return l;
    }

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(Tema.PADDING_PEQUEÑO, Tema.PADDING,
                Tema.PADDING, Tema.PADDING));

        JButton btnVolver  = boton("← Volver",        Tema.TEXTO_SECUNDARIO, Color.WHITE);
        JButton btnBuscar  = boton("Buscar tutor →",  Tema.PRIMARIO,         Color.WHITE);

        btnVolver.addActionListener(e -> navegador.mostrarBienvenida());
        btnBuscar.addActionListener(e -> navegador.mostrarBusqueda());

        p.add(btnVolver);
        p.add(btnBuscar);
        return p;
    }

    private JButton boton(String texto, Color fondo, Color texto2) {
        JButton b = new JButton(texto);
        b.setFont(Tema.FUENTE_BOTON);
        b.setBackground(fondo);
        b.setForeground(texto2);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(160, Tema.ALTO_BOTON));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}