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
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        this.tabla       = crearTabla();
        this.labelConteo = new JLabel();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(),      BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearPieDePagina(),     BorderLayout.SOUTH);

        refrescarSolicitudes();
    }

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
        labelConteo.setForeground(n == 0 ? Tema.TEXTO_SECUNDARIO : new Color(200, 220, 255));
    }

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

        JLabel sub = new JLabel("Selecciona una solicitud para asignarle un tutor");
        sub.setFont(Tema.FUENTE_CUERPO);
        sub.setForeground(new Color(180, 205, 255));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(sub);

        labelConteo.setFont(Tema.FUENTE_CUERPO);
        p.add(textos,      BorderLayout.WEST);
        p.add(labelConteo, BorderLayout.EAST);
        return p;
    }

    private JPanel crearPieDePagina() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(
                Tema.PADDING_PEQUEÑO, Tema.PADDING, Tema.PADDING_PEQUEÑO, Tema.PADDING));

        JLabel instruccion = new JLabel(
                "Haz clic en \"Ver detalle\" para revisar una solicitud y asignarle un tutor.");
        instruccion.setFont(Tema.FUENTE_PEQUENA);
        instruccion.setForeground(Tema.TEXTO_SECUNDARIO);

        JButton btnHistorial = new JButton("Ver historial de reservas →");
        btnHistorial.setFont(Tema.FUENTE_BOTON);
        btnHistorial.setBackground(Tema.PRIMARIO);
        btnHistorial.setForeground(Color.WHITE);
        btnHistorial.setFocusPainted(false);
        btnHistorial.setBorderPainted(false);
        btnHistorial.setPreferredSize(new Dimension(230, 34));
        btnHistorial.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHistorial.addActionListener(e -> navegador.mostrarHistorial());

        p.add(instruccion,   BorderLayout.WEST);
        p.add(btnHistorial,  BorderLayout.EAST);
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
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(1).setPreferredWidth(200);
        t.getColumnModel().getColumn(2).setPreferredWidth(160);
        t.getColumnModel().getColumn(3).setPreferredWidth(160);
        t.getColumnModel().getColumn(4).setPreferredWidth(110);
        t.getColumnModel().getColumn(5).setPreferredWidth(110);
        t.getColumnModel().getColumn(5).setCellRenderer(new BotonRenderer());
        t.getColumnModel().getColumn(5).setCellEditor(
                new BotonEditor(new JCheckBox(), navegador));
        return t;
    }

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
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean s, boolean f, int r, int c) { return boton; }
    }

    static class BotonEditor extends DefaultCellEditor {
        private final JButton   boton;
        private String          solicitudId;
        private final Navegador navegador;

        BotonEditor(JCheckBox cb, Navegador navegador) {
            super(cb);
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
                navegador.mostrarDetalleSolicitud(solicitudId);
            });
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean s, int r, int c) {
            solicitudId = (String) t.getValueAt(r, 0);
            return boton;
        }
        @Override public Object getCellEditorValue() { return "Ver detalle"; }
    }
}