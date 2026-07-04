package vista.paneles;

import controlador.eventos.Observador;
import vista.proxy.PerfilSeleccionable;
import vista.proxy.ProxyTutor;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.Font;

/**
 * Panel visual que muestra el calendario de disponibilidad
 * del tutor actualmente seleccionado.
 *
 * PATRÓN OBSERVER:
 * Implementa Observador y se registra en ProxyTutor al crearse.
 * Cuando el admin selecciona un tutor distinto en PanelDirectorio,
 * ProxyTutor llama automáticamente a actualizar() en este panel,
 * que refresca su contenido y ejecuta repaint() — sin que nadie
 * tenga que llamarlo manualmente.
 *
 * NOTA PARA MARI:
 * La estructura del Observer ya esta completa. Para mejorar
 * este panel con mas componentes Swing (tabla de horarios,
 * botones de reserva, etc.), agregar los componentes en
 * inicializarComponentes() y actualizar sus valores en actualizar().
 */
public class PanelCalendario extends JPanel implements Observador {

    // Nombres de dias y bloques para mostrar en la UI
    private static final String[] DIAS = {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"
    };
    private static final String[] BLOQUES = {
            "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"
    };

    // Componentes Swing
    private JLabel labelNombre;
    private JLabel labelMateria;
    private JLabel labelTarifa;
    private JLabel labelDisponibilidad;

    /**
     * Crea el panel e inmediatamente se registra como observador
     * en ProxyTutor para recibir actualizaciones automaticas.
     */
    public PanelCalendario() {
        inicializarComponentes();
        // Auto-registro: a partir de aqui, cada vez que el admin
        // seleccione un tutor, ProxyTutor llamara a actualizar()
        ProxyTutor.getInstancia().registrarObservador(this);
    }

    /**
     * Inicializa y organiza los componentes visuales del panel.
     */
    private void inicializarComponentes() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        labelNombre = new JLabel("Sin tutor seleccionado");
        labelNombre.setFont(new Font("Arial", Font.BOLD, 16));

        labelMateria          = new JLabel("Materia: -");
        labelTarifa           = new JLabel("Tarifa: -");
        labelDisponibilidad   = new JLabel("Disponibilidad: -");

        add(labelNombre);
        add(labelMateria);
        add(labelTarifa);
        add(labelDisponibilidad);
    }

    /**
     * Llamado automaticamente por ProxyTutor cuando el tutor
     * seleccionado cambia. Actualiza todos los componentes del panel.
     *
     * @param perfil el tutor activo via PerfilSeleccionable
     */
    @Override
    public void actualizar(PerfilSeleccionable perfil) {
        labelNombre.setText(perfil.getNombre());
        labelMateria.setText("Materia: " + perfil.getMateria());
        labelTarifa.setText("Tarifa: $" + perfil.getTarifa() + "/hora");
        labelDisponibilidad.setText(construirResumenDisponibilidad(perfil));
        repaint();
        revalidate();
    }

    /**
     * Construye un texto resumen de la disponibilidad del tutor.
     * Mari puedes reemplazar esto por una tabla visual más elaborada.
     *
     * @param perfil el tutor a consultar
     * @return texto con los bloques disponibles
     */
    private String construirResumenDisponibilidad(PerfilSeleccionable perfil) {
        StringBuilder sb = new StringBuilder("<html>Disponibilidad:<br>");
        for (int dia = 0; dia < DIAS.length; dia++) {
            StringBuilder bloquesDia = new StringBuilder();
            for (int bloque = 0; bloque < BLOQUES.length; bloque++) {
                if (perfil.isDisponible(dia, bloque)) {
                    if (bloquesDia.length() > 0) bloquesDia.append(", ");
                    bloquesDia.append(BLOQUES[bloque]);
                }
            }
            if (bloquesDia.length() > 0) {
                sb.append("&nbsp;&nbsp;")
                        .append(DIAS[dia])
                        .append(": ")
                        .append(bloquesDia)
                        .append("<br>");
            }
        }
        sb.append("</html>");
        return sb.toString();
    }
}