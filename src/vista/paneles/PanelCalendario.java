package vista.paneles;

import controlador.eventos.Observador;
import modelo.ConflictoHorarioException;
import modelo.GestorDatos;
import modelo.entidades.ConstantesHorario;
import modelo.entidades.Reserva;
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

 * PATRÓN OBSERVER:
 * Implementa Observador y se registra en ProxyTutor al construirse.
 * Cuando el admin selecciona un tutor en PanelBusqueda, actualizar()
 * se ejecuta automáticamente y la grilla se redibuja sola.

 * PATRÓN PROXY:
 * Trabaja con PerfilSeleccionable, nunca con Tutor directamente.
 * No sabe si habla con el objeto real o con el ProxyTutor.

 * FLUJO DE RESERVA:
 * 1. Admin selecciona tutor en PanelBusqueda → ProxyTutor notifica → actualizar()
 * 2. Admin hace clic en un bloque verde → seleccionarBloque()
 * 3. Admin hace clic en "Confirmar reserva" → confirmarReserva()
 * 4. Se crea Reserva con (Tutor, Estudiante, Solicitud, fecha, dia, bloque)
 * 5. GestorDatos.guardarReserva() valida conflictos y guarda
 * 6. Navegador navega a PanelConfirmacion con los datos del resumen
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

    // Bloque seleccionado por el admin para reservar
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

        // AUTO-REGISTRO: ProxyTutor llamará a actualizar() cada vez
        // que el tutor seleccionado cambie en PanelBusqueda.
        ProxyTutor.getInstancia().registrarObservador(this);
    }

    // =========================================================
    // Implementación de Observador
    // =========================================================

    /**
     * Llamado automáticamente por ProxyTutor cuando cambia el tutor.
     * Actualiza encabezado, resetea la selección y redibuja la grilla.
     */
    @Override
    public void actualizar(PerfilSeleccionable perfil) {
        labelNombre.setText(perfil.getNombre() + "  ·  " + perfil.getMateria());
        labelInfo.setText(String.format(
                "$%.0f/hora  ·  Haz clic en un bloque disponible para seleccionarlo",
                perfil.getTarifa()));

        // Resetear selección anterior
        diaSeleccionado    = -1;
        bloqueSeleccionado = -1;
        btnConfirmar.setEnabled(false);

        // Actualizar cada celda de la grilla
        for (int d = 0; d < ConstantesHorario.DIAS; d++) {
            for (int b = 0; b < ConstantesHorario.BLOQUES; b++) {
                boolean disponible = perfil.isDisponible(d, b);
                JLabel celda = celdas[d][b];

                celda.setBackground(disponible ? Tema.DISPONIBLE : Tema.NO_DISPONIBLE);
                celda.setForeground(Tema.TEXTO_PRIMARIO);
                celda.setText(disponible ? "✓" : "");

                // Limpiar listeners anteriores para evitar acumulación
                for (var l : celda.getMouseListeners())
                    celda.removeMouseListener(l);

                if (disponible) {
                    celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    final int fd = d, fb = b;
                    celda.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            seleccionarBloque(fd, fb);
                        }
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (fd != diaSeleccionado || fb != bloqueSeleccionado)
                                celda.setBackground(new Color(100, 200, 100));
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
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

    // =========================================================
    // Lógica de selección y confirmación de reserva
    // =========================================================

    private void seleccionarBloque(int dia, int bloque) {
        // Restaurar celda previamente seleccionada
        if (diaSeleccionado >= 0) {
            celdas[diaSeleccionado][bloqueSeleccionado].setBackground(Tema.DISPONIBLE);
            celdas[diaSeleccionado][bloqueSeleccionado].setForeground(Tema.TEXTO_PRIMARIO);
            celdas[diaSeleccionado][bloqueSeleccionado].setText("✓");
        }

        // Marcar nueva selección
        diaSeleccionado    = dia;
        bloqueSeleccionado = bloque;
        celdas[dia][bloque].setBackground(Tema.PRIMARIO);
        celdas[dia][bloque].setForeground(Color.WHITE);
        celdas[dia][bloque].setText(DIAS[dia] + " " + BLOQUES[bloque]);
        btnConfirmar.setEnabled(true);
    }

    /**
     * Crea y guarda la reserva con el constructor correcto de Reserva:
     * (Tutor, Estudiante, Solicitud, LocalDate, diaIndex, bloqueIndex)

     * La Solicitud viene de PanelDetalleSoli a través del Navegador.
     * Si no hay solicitud activa, se usa la primera pendiente como fallback.
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

        try {
            // Constructor correcto: (Tutor, Estudiante, Solicitud, LocalDate, dia, bloque)
            Reserva reserva = new Reserva(
                    tutor,
                    solicitud.getEstudiante(),
                    solicitud,
                    LocalDate.now(),
                    diaSeleccionado,
                    bloqueSeleccionado
            );

            GestorDatos.getInstancia().guardarReserva(reserva);

            // Navegar a confirmación pasando todos los datos para el resumen
            navegador.mostrarConfirmacion(
                    tutor,
                    solicitud.getEstudiante(),
                    solicitud,
                    diaSeleccionado,
                    bloqueSeleccionado
            );

        } catch (ConflictoHorarioException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Conflicto de horario",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // =========================================================
    // Construcción de la UI
    // =========================================================

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
        // Esquina vacía
        grillaPanel.add(celdaEncabezado(""));

        // Encabezados de días
        for (String d : DIAS)
            grillaPanel.add(celdaEncabezado(d));

        // Filas de bloques
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