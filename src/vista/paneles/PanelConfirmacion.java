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

public class PanelConfirmacion extends JPanel {

    private final Navegador navegador;
    private final JLabel    labelResumen;

    public PanelConfirmacion(Navegador navegador) {
        this.navegador    = navegador;
        this.labelResumen = new JLabel();
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(),     BorderLayout.CENTER);
        add(crearBotones(),    BorderLayout.SOUTH);
    }

    public void cargarConfirmacion(Tutor tutor, Estudiante estudiante,
                                   Solicitud solicitud, int dia, int bloque) {
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