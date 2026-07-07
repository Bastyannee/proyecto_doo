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
 * Panel inicial. Muestra la tabla de solicitudes pendientes.
 * El admin puede ver el detalle de cada una con el botón de la fila.
 */
public class PanelBienvenida extends JPanel {

    private final Navegador          navegador;
    private final DefaultTableModel  modeloTabla;
    private final JTable             tabla;
    private final JLabel             labelConteo;
    private List<Solicitud>          solicitudesActuales;

    public PanelBienvenida(Navegador navegador) {
        this.navegador   = navegador;
        this.modeloTabla = new DefaultTableModel(
                new String[]{"ID", "Asunto", "Estudiante", "Carrera", "Bloques pedidos", "Acción"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        this.tabla       = crearTabla();
        this.labelConteo = new JLabel();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

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
                : n + " solicitud(es) pendiente(s)");
    }

    // Construcción UI

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.PRIMARIO);
        panel.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JLabel titulo = new JLabel("Panel de Administración");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        labelConteo.setFont(Tema.FUENTE_CUERPO);
        labelConteo.setForeground(new Color(200, 220, 255));

        panel.add(titulo,      BorderLayout.WEST);
        panel.add(labelConteo, BorderLayout.EAST);
        return panel;
    }

    private JTable crearTabla() {
        JTable t = new JTable(modeloTabla);
        t.setFont(Tema.FUENTE_CUERPO);
        t.setRowHeight(42);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(230, 238, 255));
        t.setBackground(Tema.FONDO_TARJETA);

        // Estilo del encabezado
        t.getTableHeader().setFont(Tema.FUENTE_SUBTITULO);
        t.getTableHeader().setBackground(Tema.ENCABEZADO_TABLA);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Ocultar columna ID (se usa internamente)
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);

        // Columna botón "Ver detalle"
        t.getColumnModel().getColumn(5).setCellRenderer(new BotonRenderer());
        t.getColumnModel().getColumn(5).setCellEditor(
                new BotonEditor(new JCheckBox(), navegador));

        return t;
    }

    // Renderer y Editor del botón de la tabla

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
        private String solicitudId;
        private final Navegador navegador;

        BotonEditor(JCheckBox checkBox, Navegador navegador) {
            super(checkBox);
            this.navegador = navegador;
            boton = new JButton("Ver detalle");
            boton.setFont(Tema.FUENTE_BOTON);
            boton.setBackground(Tema.PRIMARIO_OSCURO);
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.addActionListener(e -> {
                fireEditingStopped();
                navegador.mostrarDetalleSolicitud(solicitudId);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            solicitudId = (String) table.getValueAt(row, 0);
            return boton;
        }

        @Override public Object getCellEditorValue() { return "Ver detalle"; }
    }
}