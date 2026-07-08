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
 * Muestra la disponibilidad del tutor activo y permite confirmar una reserva.
 *
 * PATRÓN OBSERVER: se registra en ProxyTutor al construirse.
 * PATRÓN PROXY: trabaja con PerfilSeleccionable, nunca con Tutor directamente.
 *
 * FLUJO DE CONFIRMACIÓN:
 *   1. Crea ComandoCrearReserva(solicitud, tutor, fecha, dia, bloque)
 *   2. HistorialOperaciones.ejecutar(comando) → llama execute() internamente
 *   3. Si comando.fueExitosa() → navegar a confirmación
 *   4. Si no → mostrar comando.getMensajeError() al admin
 *
 * Este flujo respeta que ComandoCrearReserva maneja ConflictoHorarioException
 * internamente y lo guarda en mensajeError en vez de relanzarlo.
 */
public class PanelCalendario extends JPanel implements Observador {

    private static final String[] DIAS = {
            "Lun", "Mar", "Mié", "Jue", "Vie"
    };
    private static final String[] BLOQUES = {
            "08:00", "09:30", "11:00", "12:30", "14:00", "15:30"
    };

    private final Navegador  navegador;
    private final JLabel     labelNombre;
    private final JLabel     labelInfo;
    private final JLabel[][] celdas;
    private final JPanel     grillaPanel;
    private final JButton    btnConfirmar;

    private int diaSeleccionado    = -1;
    private int bloqueSeleccionado = -1;

    public PanelCalendario(Navegador navegador) {
        this.navegador    = navegador;
        this.labelNombre  = new JLabel("Sin tutor seleccionado");
        this.labelInfo    = new JLabel(" ");
        this.celdas       = new JLabel[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.grillaPanel  = new JPanel();
        this.btnConfirmar = new JButton("Confirmar reserva");

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(),     BorderLayout.CENTER);
        add(crearBotones(),    BorderLayout.SOUTH);

        ProxyTutor.getInstancia().registrarObservador(this);
    }

    // ── Observador ───────────────────────────────────────────

    @Override
    public void actualizar(PerfilSeleccionable perfil) {
        labelNombre.setText(perfil.getNombre() + "  ·  " + perfil.getMateria());
        labelInfo.setText(String.format(
                "$%.0f/hora  ·  Haz clic en un bloque disponible para seleccionarlo",
                perfil.getTarifa()));

        diaSeleccionado    = -1;
        bloqueSeleccionado = -1;
        btnConfirmar.setEnabled(false);

        for (int d = 0; d < ConstantesHorario.DIAS; d++) {
            for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
                boolean disponible = perfil.isDisponible(d, b);
                JLabel celda = celdas[d][b];
                celda.setBackground(disponible ? Tema.DISPONIBLE : Tema.NO_DISPONIBLE);
                celda.setForeground(Tema.TEXTO_PRIMARIO);
                celda.setText(disponible ? "✓" : "");

                for (var l : celda.getMouseListeners())
                    celda.removeMouseListener(l);

                if (disponible) {
                    celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    final int fd = d, fb = b;
                    celda.addMouseListener(new MouseAdapter() {
                        @Override public void mouseClicked(MouseEvent e) {
                            seleccionarBloque(fd, fb);
                        }
                        @Override public void mouseEntered(MouseEvent e) {
                            if (fd != diaSeleccionado || fb != bloqueSeleccionado)
                                celda.setBackground(new Color(100, 200, 100));
                        }
                        @Override public void mouseExited(MouseEvent e) {
                            if (fd != diaSeleccionado || fb != bloqueSeleccionado)
                                celda.setBackground(Tema.DISPONIBLE);
                        }
                    });
                } else {
                    celda.setCursor(Cursor.getDefaultCursor());
                }
            }
        }
        repaint();
        revalidate();
    }

    // ── Selección y confirmación ──────────────────────────────

    private void seleccionarBloque(int dia, int bloque) {
        if (diaSeleccionado >= 0) {
            celdas[diaSeleccionado][bloqueSeleccionado].setBackground(Tema.DISPONIBLE);
            celdas[diaSeleccionado][bloqueSeleccionado].setForeground(Tema.TEXTO_PRIMARIO);
            celdas[diaSeleccionado][bloqueSeleccionado].setText("✓");
        }
        diaSeleccionado    = dia;
        bloqueSeleccionado = bloque;
        celdas[dia][bloque].setBackground(Tema.PRIMARIO);
        celdas[dia][bloque].setForeground(Color.WHITE);
        celdas[dia][bloque].setText(DIAS[dia] + " " + BLOQUES[bloque]);
        btnConfirmar.setEnabled(true);
    }

    /**
     * Crea y ejecuta ComandoCrearReserva via HistorialOperaciones.
     *
     * Constructor correcto: (Solicitud, Tutor, LocalDate, diaIndex, bloqueIndex)
     * Verificamos fueExitosa() porque ComandoCrearReserva captura
     * ConflictoHorarioException internamente en vez de relanzarla.
     */
    private void confirmarReserva() {
        Tutor     tutor     = ProxyTutor.getInstancia().getTutorActual();
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

        // Firma correcta del constructor real de ComandoCrearReserva
        ComandoCrearReserva comando = new ComandoCrearReserva(
                solicitud,
                tutor,
                LocalDate.now(),
                diaSeleccionado,
                bloqueSeleccionado
        );

        // execute() maneja ConflictoHorarioException internamente
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
            // El comando capturó el conflicto — mostramos el mensaje guardado
            JOptionPane.showMessageDialog(this,
                    comando.getMensajeError(),
                    "Conflicto de horario",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── UI ───────────────────────────────────────────────────

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

    private JPanel crearCuerpo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, 0, Tema.PADDING));
        grillaPanel.setLayout(new GridLayout(
                ConstantesHorario.BLOQUES + 1,
                ConstantesHorario.DIAS   + 1, 3, 3));
        grillaPanel.setBackground(Tema.FONDO);
        construirGrilla();
        p.add(grillaPanel, BorderLayout.CENTER);
        return p;
    }

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

    private JLabel celdaEncabezado(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(Tema.ENCABEZADO_TABLA);
        l.setForeground(Color.WHITE);
        l.setFont(Tema.FUENTE_SUBTITULO);
        l.setBorder(new LineBorder(Color.WHITE, 1));
        return l;
    }

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
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> navegador.mostrarBusqueda());

        btnConfirmar.setFont(Tema.FUENTE_BOTON);
        btnConfirmar.setBackground(Tema.ACENTO);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBorderPainted(false);
        btnConfirmar.setPreferredSize(new Dimension(190, Tema.ALTO_BOTON));
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirmar.setEnabled(false);
        btnConfirmar.addActionListener(e -> confirmarReserva());

        p.add(btnVolver);
        p.add(btnConfirmar);
        return p;
    }
}