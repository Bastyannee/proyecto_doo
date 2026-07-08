package vista.paneles;

import modelo.GestorDatos;
import modelo.entidades.Solicitud;
import vista.Navegador;
import vista.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel inicial del sistema.

 * FLUJO OBLIGATORIO:
 * El único camino para llegar a PanelBusqueda es hacer clic en
 * "Ver detalle" de una solicitud específica. Esto garantiza que
 * solicitudActiva nunca sea null cuando el admin llegue al calendario.

 * Se eliminó el botón "Buscar Tutores" directo que antes permitía
 * saltarse la selección de solicitud, dejando solicitudActiva en null
 * y rompiendo la creación de la Reserva.
 */
public class PanelBienvenida extends JPanel {

    private final Navegador         navegador;
    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;
    private final JLabel            labelConteo;
    private List<Solicitud>         solicitudesActuales;

    public PanelBienvenida(Navegador navegador) {
        this.navegador   = navegador;
        this.modeloTabla = new DefaultTableModel(
                new String[]{"ID", "Asunto", "Estudiante", "Carrera", "Bloques pedidos", "Acción"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        this.tabla       = crearTabla();
        this.labelConteo = new JLabel();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(),          BorderLayout.NORTH);
        add(new JScrollPane(tabla),     BorderLayout.CENTER);
        add(crearPieDePagina(),         BorderLayout.SOUTH);

        refrescarSolicitudes();
    }

    // ── API pública ──────────────────────────────────────────

    public void refrescarSolicitudes() {
        solicitudesActuales = GestorDatos.getInstancia().getSolicitudesPendientes();
        modeloTabla.setRowCount(0);

        for (Solicitud s : solicitudesActuales) {
            modeloTabla.addRow(new Object[]{
                    s.getId(),
                    s.getAsunto(),
                    s.getEstudiante().getNombre(),
                    s.getEstudiante().getCarrera(),
                    s.contarBloquesDeseados() + " bloques",
                    "Ver detalle"
            });
        }

        int n = solicitudesActuales.size();
        labelConteo.setText(n == 0
                ? "No hay solicitudes pendientes"
                : n + " solicitud(es) pendiente(s) — selecciona una para comenzar");
        labelConteo.setForeground(n == 0 ? Tema.TEXTO_SECUNDARIO : new Color(200, 220, 255));
    }

    // ── Construcción UI ──────────────────────────────────────

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PRIMARIO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.PRIMARIO);

        JLabel titulo = new JLabel("Panel de Administración");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Selecciona una solicitud para asignarle un tutor");
        subtitulo.setFont(Tema.FUENTE_CUERPO);
        subtitulo.setForeground(new Color(180, 205, 255));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);

        labelConteo.setFont(Tema.FUENTE_CUERPO);
        p.add(textos,      BorderLayout.WEST);
        p.add(labelConteo, BorderLayout.EAST);
        return p;
    }

    /**
     * Pie de página informativo — sin botón "Buscar Tutores" suelto.
     * El único punto de entrada al flujo es la tabla de solicitudes.
     */
    private JPanel crearPieDePagina() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(
                Tema.PADDING_PEQUEÑO, Tema.PADDING, Tema.PADDING_PEQUEÑO, Tema.PADDING));

        JLabel instruccion = new JLabel(
                "Haz clic en \"Ver detalle\" para revisar una solicitud y asignarle un tutor.");
        instruccion.setFont(Tema.FUENTE_PEQUENA);
        instruccion.setForeground(Tema.TEXTO_SECUNDARIO);
        p.add(instruccion);
        return p;
    }

    private JTable crearTabla() {
        JTable t = new JTable(modeloTabla);
        t.setFont(Tema.FUENTE_CUERPO);
        t.setRowHeight(44);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(230, 238, 255));
        t.setBackground(Tema.FONDO_TARJETA);

        t.getTableHeader().setFont(Tema.FUENTE_SUBTITULO);
        t.getTableHeader().setBackground(Tema.ENCABEZADO_TABLA);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setPreferredSize(new Dimension(0, 42));

        // Ocultar columna ID (uso interno)
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);

        // Anchos de columnas visibles
        t.getColumnModel().getColumn(1).setPreferredWidth(200); // Asunto
        t.getColumnModel().getColumn(2).setPreferredWidth(160); // Estudiante
        t.getColumnModel().getColumn(3).setPreferredWidth(160); // Carrera
        t.getColumnModel().getColumn(4).setPreferredWidth(110); // Bloques
        t.getColumnModel().getColumn(5).setPreferredWidth(110); // Acción

        // Botón por fila
        t.getColumnModel().getColumn(5).setCellRenderer(new BotonRenderer());
        t.getColumnModel().getColumn(5).setCellEditor(
                new BotonEditor(new JCheckBox(), navegador));

        return t;
    }

    // ── Renderer y Editor del botón ──────────────────────────

    static class BotonRenderer implements TableCellRenderer {
        private final JButton boton = new JButton("Ver detalle");

        BotonRenderer() {
            boton.setFont(Tema.FUENTE_BOTON);
            boton.setBackground(Tema.PRIMARIO);
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return boton;
        }
    }

    static class BotonEditor extends DefaultCellEditor {
        private final JButton boton;
        private String        solicitudId;
        private final Navegador navegador;

        BotonEditor(JCheckBox checkBox, Navegador navegador) {
            super(checkBox);
            this.navegador = navegador;
            this.boton     = new JButton("Ver detalle");
            boton.setFont(Tema.FUENTE_BOTON);
            boton.setBackground(Tema.PRIMARIO_OSCURO);
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.addActionListener(e -> {
                fireEditingStopped();
                // ÚNICO punto de entrada al flujo de reserva:
                // asigna solicitudActiva en Navegador antes de navegar
                navegador.mostrarDetalleSolicitud(solicitudId);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            // Leer el id de la columna oculta (índice 0)
            solicitudId = (String) table.getValueAt(row, 0);
            return boton;
        }

        @Override
        public Object getCellEditorValue() { return "Ver detalle"; }
    }
}