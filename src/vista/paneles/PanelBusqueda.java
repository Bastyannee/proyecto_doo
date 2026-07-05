package vista.paneles;

import modelo.GestorDatos;
import modelo.entidades.Tutor;
import vista.Navegador;
import vista.proxy.ProxyTutor;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

/**
 * Panel de búsqueda y selección de tutores.

 * PUNTO DE INTEGRACIÓN CON EL PROXY:
 * Al hacer clic en "Ver disponibilidad", llama a:
 *   ProxyTutor.getInstancia().seleccionar(tutor)
 * Esa unica línea actualiza ProxyTutor y dispara actualizar()
 * en PanelCalendario automaticamente, sin que este panel
 * sepa nada de el.
 */
public class PanelBusqueda extends JPanel {

    private final Navegador               navegador;
    private final DefaultListModel<String> modeloLista;
    private final JList<String>           listaTutores;
    private List<Tutor>                   tutoresActuales;

    public PanelBusqueda(Navegador navegador) {
        this.navegador    = navegador;
        this.modeloLista  = new DefaultListModel<>();
        this.listaTutores = new JList<>(modeloLista);
        listaTutores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Buscar Tutores", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));

        JButton btnVerCalendario = new JButton("Ver disponibilidad del tutor seleccionado");
        btnVerCalendario.addActionListener(e -> seleccionarTutorYNavegar());

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> navegador.mostrarBienvenida());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnVolver);
        panelBotones.add(btnVerCalendario);

        add(titulo,                        BorderLayout.NORTH);
        add(new JScrollPane(listaTutores), BorderLayout.CENTER);
        add(panelBotones,                  BorderLayout.SOUTH);

        refrescarTutores();
    }

    /**
     * Recarga la lista de tutores desde GestorDatos.
     * Llamado por Navegador al navegar a este panel.
     */
    public void refrescarTutores() {
        tutoresActuales = GestorDatos.getInstancia().getTutores();
        modeloLista.clear();
        for (Tutor t : tutoresActuales) {
            modeloLista.addElement(t.getNombre() + " — " + t.getMateria());
        }
    }

    /**
     * PARTICIPACION DEL PROXY:
     * Obtiene el tutor seleccionado, lo pasa al ProxyTutor
     * (que notifica a PanelCalendario y demás observadores)
     * y navega al calendario en tres lines
     */
    private void seleccionarTutorYNavegar() {
        int indice = listaTutores.getSelectedIndex();
        if (indice < 0 || tutoresActuales == null) return;

        Tutor seleccionado = tutoresActuales.get(indice);
        ProxyTutor.getInstancia().seleccionar(seleccionado);
        navegador.mostrarCalendario();
    }
}