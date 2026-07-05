package vista.paneles;

import controlador.eventos.Observador;
import modelo.entidades.ConstantesHorario;
import vista.Navegador;
import vista.proxy.PerfilSeleccionable;
import vista.proxy.ProxyTutor;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Panel que muestra la disponibilidad del tutor seleccionado.

 * PATRÓN OBSERVER:
 * Implementa Observador y se registra en ProxyTutor al construirse.
 * Cuando el admin selecciona un tutor en PanelBusqueda, ProxyTutor
 * llama a actualizar() aquí y la grilla se redibuja automáticamente.

 * PATRÓN PROXY:
 * Trabaja con PerfilSeleccionable, nunca con Tutor directamente.
 * No sabe si habla con el objeto real o con el ProxyTutor.
 */
public class PanelCalendario extends JPanel implements Observador {

    private static final String[] NOMBRES_DIAS = {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"
    };
    private static final String[] NOMBRES_BLOQUES = {
            "08:00-09:30", "09:30-11:00", "11:00-12:30",
            "12:30-14:00", "14:00-15:30", "15:30-17:00"
    };

    private static final Color COLOR_DISPONIBLE    = new Color(144, 238, 144);
    private static final Color COLOR_NO_DISPONIBLE = new Color(220, 220, 220);
    private static final Color COLOR_ENCABEZADO    = new Color(70, 130, 180);

    private final Navegador navegador;
    private final JLabel    labelNombreTutor;
    private final JLabel    labelMateria;
    private final JLabel    labelTarifa;
    private final JPanel    grillaDias;
    private final JLabel[][] celdas;

    public PanelCalendario(Navegador navegador) {
        this.navegador        = navegador;
        this.labelNombreTutor = new JLabel("Sin tutor seleccionado", SwingConstants.CENTER);
        this.labelMateria     = new JLabel("", SwingConstants.CENTER);
        this.labelTarifa      = new JLabel("", SwingConstants.CENTER);
        this.celdas           = new JLabel[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.grillaDias       = construirGrilla();

        construirUI();

        // AUTO-REGISTRO: ProxyTutor llamará a actualizar() cada vez
        // que el tutor seleccionado cambie en PanelBusqueda.
        ProxyTutor.getInstancia().registrarObservador(this);
    }

    /**
     * Llamado automáticamente por ProxyTutor cuando el tutor cambia.
     * Actualiza encabezado y toda la grilla de disponibilidad.
     */
    @Override
    public void actualizar(PerfilSeleccionable perfil) {
        labelNombreTutor.setText(perfil.getNombre());
        labelMateria.setText("Materia: " + perfil.getMateria());
        labelTarifa.setText("Tarifa: $" + String.format("%.0f", perfil.getTarifa()) + "/hora");

        for (int dia = 0; dia < ConstantesHorario.DIAS; dia++) {
            for (int bloque = 0; bloque < ConstantesHorario.BLOQUES; bloque++) {
                boolean disponible = perfil.isDisponible(dia, bloque);
                celdas[dia][bloque].setBackground(
                        disponible ? COLOR_DISPONIBLE : COLOR_NO_DISPONIBLE);
                celdas[dia][bloque].setText(disponible ? "✓" : "");
            }
        }

        repaint();
        revalidate();
    }

    private void construirUI() {
        setLayout(new BorderLayout(5, 5));

        JPanel panelEncabezado = new JPanel(new GridLayout(3, 1));
        labelNombreTutor.setFont(new Font("Arial", Font.BOLD, 20));
        panelEncabezado.add(labelNombreTutor);
        panelEncabezado.add(labelMateria);
        panelEncabezado.add(labelTarifa);

        JButton btnVolver = new JButton("Volver a búsqueda");
        btnVolver.addActionListener(e -> navegador.mostrarBusqueda());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnVolver);

        add(panelEncabezado, BorderLayout.NORTH);
        add(grillaDias,      BorderLayout.CENTER);
        add(panelBotones,    BorderLayout.SOUTH);
    }

    private JPanel construirGrilla() {
        JPanel grilla = new JPanel(new GridLayout(
                ConstantesHorario.BLOQUES + 1,
                ConstantesHorario.DIAS   + 1,
                2, 2));

        grilla.add(crearEncabezado(""));
        for (String dia : NOMBRES_DIAS) {
            grilla.add(crearEncabezado(dia));
        }

        for (int bloque = 0; bloque < ConstantesHorario.BLOQUES; bloque++) {
            grilla.add(crearEncabezado(NOMBRES_BLOQUES[bloque]));
            for (int dia = 0; dia < ConstantesHorario.DIAS; dia++) {
                JLabel celda = new JLabel("", SwingConstants.CENTER);
                celda.setOpaque(true);
                celda.setBackground(COLOR_NO_DISPONIBLE);
                celda.setBorder(new LineBorder(Color.WHITE, 1));
                celdas[dia][bloque] = celda;
                grilla.add(celda);
            }
        }

        return grilla;
    }

    private JLabel crearEncabezado(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(COLOR_ENCABEZADO);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setBorder(new LineBorder(Color.WHITE, 1));
        return label;
    }
}