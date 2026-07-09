package vista;

import modelo.GestorDatos;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * Ventana principal y contenedor raíz de la interfaz gráfica de la aplicación.
 *
 * Responsabilidad Estructural:
 * Esta clase asume exclusivamente el rol de marco de nivel superior (Top-Level Container)
 * en el ciclo de vida de la aplicación. Sus responsabilidades se limitan estrictamente a:
 * 1. Orquestar la carga de persistencia en memoria local a través de GestorDatos.
 * 2. Alojar de manera compuesta y persistente al mediador de pantallas (Navegador).
 * 3. Configurar los parámetros operacionales y las dimensiones de la ventana nativa del sistema operativo.
 * De este modo, delega por completo la lógica de negocio del intercambio de vistas al Navegador.
 *
 * Relación de Composición y Ciclo de Vida:
 * Mantiene un acoplamiento fuerte por composición con la clase Navegador. La jerarquía visual
 * del gestor de pantallas carece de contexto operacional fuera de este contenedor, por lo que
 * sus ciclos de vida se encuentran vinculados de forma unívoca.
 */
public class VentanaPrincipal extends JFrame {

    private static final int ANCHO = 1100;
    private static final int ALTO = 700;
    private static final String TITULO = "Sistema de Reservas de Clases Particulares";

    /** Componente mediador encargado de la renderización dinámica de las pantallas del sistema. */
    private final Navegador navegador;

    /**
     * Construye la ventana principal y coordina la secuencia crítica de arranque de la aplicación.
     *
     * El orden de ejecución es secuencial y determinista:
     * 1. Se precargan los diccionarios y colecciones de datos del dominio, puesto que los paneles
     *    dependen de ellos durante su construcción.
     * 2. Se instancia el gestor de navegación central, el cual propaga las referencias hacia sus subpaneles.
     * 3. Se asignan las propiedades del marco y se transiciona el componente hacia un estado visible.
     */
    public VentanaPrincipal() {
        inicializarDatos();
        this.navegador = new Navegador(this);
        configurarVentana();
    }

    /**
     * Invoca el punto de acceso global del repositorio de datos para estructurar la
     * información base de entidades y configuraciones en memoria.
     */
    private void inicializarDatos() {
        GestorDatos.getInstancia().inicializarDatosEstaticos();
    }

    /**
     * Establece los parámetros operacionales del componente JFrame, enlazando el panel de
     * contenido principal y centrando la ventana en las coordenadas de la pantalla del usuario.
     */
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

    /**
     * Recupera el gestor de navegación asociado a la ventana principal.
     *
     * @return Instancia del Navegador activo en el panel de contenido.
     */
    public Navegador getNavegador() {
        return navegador;
    }
}