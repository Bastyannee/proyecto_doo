package vista.paneles;

import modelo.GestorDatos;
import modelo.entidades.Tutor;
import vista.Navegador;
import vista.Tema;
import vista.proxy.ProxyTutor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de búsqueda de tutores con tarjetas visuales y filtro por materia.
 * Al seleccionar un tutor llama a ProxyTutor.seleccionar() que notifica
 * automáticamente a PanelCalendario.
 */
public class PanelBusqueda extends JPanel {

    private final Navegador navegador;
    private final JPanel    panelTarjetas;
    private final JTextField campoBusqueda;
    private List<Tutor>     tutoresActuales;

    public PanelBusqueda(Navegador navegador) {
        this.navegador     = navegador;
        this.panelTarjetas = new JPanel();
        this.campoBusqueda = new JTextField();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(new JScrollPane(panelTarjetas), BorderLayout.CENTER);
        add(crearBotones(),    BorderLayout.SOUTH);

        panelTarjetas.setBackground(Tema.FONDO);
        refrescarTutores();
    }

    // ── API pública ──────────────────────────────────────────

    public void refrescarTutores() {
        tutoresActuales = GestorDatos.getInstancia().getTutores();
        filtrarYMostrar("");
    }

    // ── Construcción UI ──────────────────────────────────────

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout(Tema.PADDING, 0));
        p.setBackground(Tema.PRIMARIO);
        p.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

        JLabel titulo = new JLabel("Seleccionar Tutor");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        campoBusqueda.setFont(Tema.FUENTE_CUERPO);
        campoBusqueda.setPreferredSize(new Dimension(250, 34));
        campoBusqueda.putClientProperty("JTextField.placeholderText", "Filtrar por materia...");
        campoBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            private void filtrar() { filtrarYMostrar(campoBusqueda.getText().trim()); }
        });

        p.add(titulo,        BorderLayout.WEST);
        p.add(campoBusqueda, BorderLayout.EAST);
        return p;
    }

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(Tema.PADDING_PEQUEÑO, Tema.PADDING,
                Tema.PADDING_PEQUEÑO, Tema.PADDING));
        JButton btnVolver = new JButton("← Volver");
        btnVolver.setFont(Tema.FUENTE_BOTON);
        btnVolver.setBackground(Tema.TEXTO_SECUNDARIO);
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setPreferredSize(new Dimension(130, Tema.ALTO_BOTON));
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> navegador.mostrarBienvenida());
        p.add(btnVolver);
        return p;
    }

    private void filtrarYMostrar(String filtro) {
        List<Tutor> filtrados = new ArrayList<>();
        for (Tutor t : tutoresActuales) {
            if (filtro.isEmpty() ||
                    t.getMateria().toLowerCase().contains(filtro.toLowerCase()) ||
                    t.getNombre().toLowerCase().contains(filtro.toLowerCase())) {
                filtrados.add(t);
            }
        }

        panelTarjetas.removeAll();
        panelTarjetas.setLayout(new GridLayout(0, 2, Tema.PADDING, Tema.PADDING));
        panelTarjetas.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING,
                Tema.PADDING, Tema.PADDING));

        if (filtrados.isEmpty()) {
            JLabel vacio = new JLabel("No se encontraron tutores.", SwingConstants.CENTER);
            vacio.setFont(Tema.FUENTE_CUERPO);
            vacio.setForeground(Tema.TEXTO_SECUNDARIO);
            panelTarjetas.setLayout(new BorderLayout());
            panelTarjetas.add(vacio, BorderLayout.CENTER);
        } else {
            for (Tutor t : filtrados) {
                panelTarjetas.add(crearTarjetaTutor(t));
            }
        }

        panelTarjetas.revalidate();
        panelTarjetas.repaint();
    }

    private JPanel crearTarjetaTutor(Tutor tutor) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(Tema.FONDO_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE, 1, true),
                new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING)));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Encabezado de la tarjeta
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Tema.FONDO_TARJETA);

        JLabel nombre = new JLabel(tutor.getNombre());
        nombre.setFont(Tema.FUENTE_SUBTITULO);
        nombre.setForeground(Tema.TEXTO_PRIMARIO);

        JLabel materia = new JLabel(tutor.getMateria());
        materia.setFont(Tema.FUENTE_CUERPO);
        materia.setForeground(Tema.PRIMARIO);

        JLabel afinidad = new JLabel(tutor.getAfinidad());
        afinidad.setFont(Tema.FUENTE_PEQUENA);
        afinidad.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.FONDO_TARJETA);
        textos.add(nombre);
        textos.add(materia);
        textos.add(afinidad);

        header.add(textos, BorderLayout.CENTER);

        // Info inferior
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Tema.FONDO_TARJETA);

        int bloques = tutor.contarBloquesDisponibles();
        JLabel infoTarifa = new JLabel(
                String.format("$%.0f/hr  ·  %d bloques libres", tutor.getTarifa(), bloques));
        infoTarifa.setFont(Tema.FUENTE_PEQUENA);
        infoTarifa.setForeground(Tema.TEXTO_SECUNDARIO);

        JButton btnSeleccionar = new JButton("Ver disponibilidad →");
        btnSeleccionar.setFont(Tema.FUENTE_BOTON);
        btnSeleccionar.setBackground(Tema.PRIMARIO);
        btnSeleccionar.setForeground(Color.WHITE);
        btnSeleccionar.setFocusPainted(false);
        btnSeleccionar.setBorderPainted(false);
        btnSeleccionar.setPreferredSize(new Dimension(180, 32));
        btnSeleccionar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSeleccionar.addActionListener(e -> {
            ProxyTutor.getInstancia().seleccionar(tutor);
            navegador.mostrarCalendario();
        });

        footer.add(infoTarifa,    BorderLayout.WEST);
        footer.add(btnSeleccionar, BorderLayout.EAST);

        tarjeta.add(header, BorderLayout.CENTER);
        tarjeta.add(footer, BorderLayout.SOUTH);

        return tarjeta;
    }
}