package vista.paneles;

import modelo.GestorDatos;
import modelo.entidades.Tutor;
import vista.Navegador;
import vista.Tema;
import vista.proxy.ProxyTutor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de interfaz gráfica destinado a la búsqueda, filtrado y selección de tutores académicos.
 *
 * Esta clase proporciona una vista en cuadrícula (Grid) compuesta por tarjetas visuales que
 * resumen el perfil de cada tutor. Cuenta con una barra de búsqueda superior que actúa en
 * tiempo real, procesando cadenas de texto para aislar registros específicos basándose en
 * coincidencias con la materia impartida o el nombre del tutor.
 *
 * Al interactuar con el botón de disponibilidad de una tarjeta, la vista delega la gestión
 * del estado de selección al patrón Proxy (ProxyTutor). Este componente actúa como un intermediario
 * centralizado que almacena de forma segura al tutor elegido, notificando automáticamente a
 * las vistas dependientes (como el panel del calendario) para sincronizar la grilla de bloques
 * horarios antes de efectuar la transición de pantalla.
 */
public class PanelBusqueda extends JPanel {
    /** Coordinador de rutas de la interfaz que gestiona el cambio seguro de pantallas. */
    private final Navegador navegador;
    /** Contenedor interno dinámico que aloja y distribuye las tarjetas gráficas de los tutores. */
    private final JPanel panelTarjetas;
    /** Campo de entrada de texto que captura las consultas de filtrado en tiempo real. */
    private final JTextField campoBusqueda;
    /** Caché local que resguarda la lista total de tutores activos recuperados desde la memoria central. */
    private List<Tutor> tutoresActuales;

    /**
     * Construye el panel de búsqueda configurando los contenedores de controles, la barra de
     * herramientas superior y los listeners encargados del monitoreo de texto.
     *
     * @param navegador Instancia global del despachador de vistas del sistema.
     */
    public PanelBusqueda(Navegador navegador) {
        this.navegador = navegador;
        this.panelTarjetas = new JPanel();
        this.campoBusqueda = new JTextField();

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(new JScrollPane(panelTarjetas), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        panelTarjetas.setBackground(Tema.FONDO);
        refrescarTutores();
    }

    /**
     * Sincroniza la lista interna del panel consultando el estado vigente de los tutores en el
     * GestorDatos y restablece el campo de filtrado visualizando la totalidad de los registros.
     */
    public void refrescarTutores() {
        tutoresActuales = GestorDatos.getInstancia().getTutores();
        filtrarYMostrar("");
    }

    /**
     * Diseña la barra de herramientas del extremo superior (Zona Norte) fijando el título del
     * módulo y el control de búsqueda textual.
     *
     * El campo de texto incorpora un listener reactivo vinculado a su documento interno. Cualquier
     * inserción, remoción o modificación de caracteres gatilla de manera inmediata el recalculo
     * de la lista visible sin requerir confirmaciones por teclado o clics adicionales.
     *
     * @return El panel contenedor del encabezado con sus estilos de margen aplicados.
     */
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            private void filtrar() { filtrarYMostrar(campoBusqueda.getText().trim()); }
        });

        p.add(titulo, BorderLayout.WEST);
        p.add(campoBusqueda, BorderLayout.EAST);
        return p;
    }

    /**
     * Modela la botonera inferior de salida (Zona Sur) encargada de proveer rutas alternativas de navegación.
     *
     * @return Panel inferior dotado de un botón de retorno rápido al menú principal.
     */
    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(Tema.FONDO);
        p.setBorder(new EmptyBorder(Tema.PADDING_PEQUEÑO, Tema.PADDING, Tema.PADDING_PEQUEÑO, Tema.PADDING));
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

    /**
     * Discrimina los registros de la caché basándose en la cadena provista, vacía el lienzo
     * actual y recompone las tarjetas que superaron los criterios de búsqueda.
     *
     * Si tras procesar el filtro no se hallan coincidencias con los parámetros del usuario,
     * el layout conmuta de forma segura a una disposición unidireccional (BorderLayout) para
     * centrar un mensaje de advertencia pasivo que notifica la ausencia de resultados.
     *
     * @param filtro Texto plano que representa el criterio de búsqueda (materia o nombre).
     */
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
        panelTarjetas.setBorder(new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING));

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

    /**
     * Construye un componente modular en forma de tarjeta contenedor para desplegar de forma
     * compacta la información de un tutor.
     *
     * La tarjeta agrupa el nombre, la especialidad técnica y el área de afinidad en un bloque
     * descriptivo superior, mientras que en la base calcula dinámicamente la tarifa por hora
     * estructurada junto con el saldo total de bloques de tiempo desocupados.
     *
     * El botón de acción enlaza al tutor con la instancia del Proxy global antes de instruir
     * al navegador el despliegue del calendario de reservas.
     *
     * @param tutor Objeto entidad de donde se extraen los atributos del perfil.
     * @return El subpanel estructurado con la información y acciones del tutor.
     */
    private JPanel crearTarjetaTutor(Tutor tutor) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(Tema.FONDO_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE, 1, true),
                new EmptyBorder(Tema.PADDING, Tema.PADDING, Tema.PADDING, Tema.PADDING)));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel header = new JPanel(new BorderLayout(Tema.PADDING, 0));
        header.setBackground(Tema.FONDO_TARJETA);

        JLabel foto = crearFoto(tutor);

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

        header.add(foto, BorderLayout.WEST);
        header.add(textos, BorderLayout.CENTER);

        JLabel descripcion = new JLabel(
                "<html><body style='width:300px'>" + tutor.getDescripcion() + "</body></html>");
        descripcion.setFont(Tema.FUENTE_PEQUENA);
        descripcion.setForeground(Tema.TEXTO_SECUNDARIO);
        descripcion.setBorder(new EmptyBorder(Tema.PADDING_PEQUEÑO, 0, 0, 0));

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(Tema.FONDO_TARJETA);
        cuerpo.add(header, BorderLayout.NORTH);
        cuerpo.add(descripcion, BorderLayout.CENTER);

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

        footer.add(infoTarifa, BorderLayout.WEST);
        footer.add(btnSeleccionar, BorderLayout.EAST);

        tarjeta.add(cuerpo, BorderLayout.CENTER);
        tarjeta.add(footer, BorderLayout.SOUTH);

        return tarjeta;
    }

    /**
     * Carga la foto del tutor desde los recursos y la escala al tamaño del contenedor,
     * recortando el centro para mantener la proporción original sin distorsionarla.
     *
     * Si la ruta no existe o falla al cargar, devuelve un recuadro con las
     * iniciales del tutor como alternativa.
     *
     * @param tutor El tutor del cual se obtiene la foto y sus iniciales.
     * @return Un JLabel con la imagen lista para ser añadida a la interfaz.
     */
    private JLabel crearFoto(Tutor tutor) {
        int size = 120;
        BufferedImage imagen = null;

        if (tutor.getFotoPath() != null && !tutor.getFotoPath().isEmpty()) {
            try {
                imagen = ImageIO.read(new File(tutor.getFotoPath()));
            } catch (IOException ignored) {}
        }

        BufferedImage cuadro = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = cuadro.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (imagen != null) {
            int origAncho = imagen.getWidth();
            int origAlto = imagen.getHeight();
            int lado = Math.min(origAncho, origAlto);
            int x = (origAncho - lado) / 2;
            int y = (origAlto - lado) / 2;
            BufferedImage recorte = imagen.getSubimage(x, y, lado, lado);
            g2.drawImage(recorte.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            g2.setColor(Tema.PRIMARIO);
            g2.fillRect(0, 0, size, size);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            String iniciales = obtenerIniciales(tutor.getNombre());
            g2.drawString(iniciales,
                    (size - fm.stringWidth(iniciales)) / 2,
                    (size - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();

        JLabel label = new JLabel(new ImageIcon(cuadro));
        label.setPreferredSize(new Dimension(size + 8, size + 8));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * Genera las iniciales del tutor tomando la primera letra de sus dos primeras palabras.
     * Si el nombre tiene una sola palabra, devuelve solo esa inicial en mayúscula.
     *
     * @param nombre El nombre completo del tutor.
     * @return Las iniciales en mayúsculas (máximo dos caracteres).
     */
    private String obtenerIniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, partes.length); i++)
            if (!partes[i].isEmpty())
                sb.append(Character.toUpperCase(partes[i].charAt(0)));
        return sb.toString();
    }
}