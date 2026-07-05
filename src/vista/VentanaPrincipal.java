package vista;

import modelo.GestorDatos;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * Ventana principal de la aplicación — el JFrame raiz.

 * RESPONSABILIDAD UNICA:
 * Solo hace tres cosas:
 *   1. Inicializar los datos (GestorDatos).
 *   2. Crear y alojar al Navegador.
 *   3. Configurar y mostrar la ventana.
 * No sabe qué paneles existen ni cómo navegar entre ellos.

 * RELACION CON NAVEGADOR — Composicion:
 * El Navegador no tiene sentido fuera de esta ventana.
 * Su ciclo de vida está atado al de VentanaPrincipal.
 */
public class VentanaPrincipal extends JFrame {

    private static final int    ANCHO  = 1100;
    private static final int    ALTO   = 700;
    private static final String TITULO = "Sistema de Reservas de Clases Particulares";

    private final Navegador navegador;

    /**
     * Construye la ventana e inicializa toda la aplicación.
     * Orden importante:
     *   1. Datos (GestorDatos) — los paneles los necesitan al construirse.
     *   2. Navegador — crea los paneles.
     *   3. Configurar y mostrar la ventana.
     */
    public VentanaPrincipal() {
        inicializarDatos();
        this.navegador = new Navegador(this);
        configurarVentana();
    }

    private void inicializarDatos() {
        GestorDatos.getInstancia().inicializarDatosEstaticos();
    }

    private void configurarVentana() {
        setTitle(TITULO);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setMinimumSize(new Dimension(800, 500));
        setContentPane(navegador);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Navegador getNavegador() {
        return navegador;
    }
}