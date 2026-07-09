package vista;

import java.awt.Color;
import java.awt.Font;

/**
 * Sistema de diseño y constantes visuales centralizadas de la aplicación.
 *
 * Propósito del Módulo:
 * Esta clase provee un único punto de verdad para los atributos estéticos del
 * entorno gráfico. Todos los subpaneles y componentes de la interfaz de usuario
 * importan estas definiciones para garantizar la coherencia visual, la uniformidad
 * de la identidad de marca y facilitar el mantenimiento global del estilo sin
 * duplicar valores en las clases de la vista.
 */
public final class Tema {

    /**
     * Constructor privado para prevenir la instanciación de la clase,
     * dado que actúa estrictamente como un contenedor estático de constantes.
     */
    private Tema() {}

    /** Color de identidad principal, utilizado en barras de título y elementos jerárquicos superiores. */
    public static final Color PRIMARIO = new Color(30,  80,  160);
    /** Variante oscura del color primario, empleada en estados de interacción como el hover de botones. */
    public static final Color PRIMARIO_OSCURO = new Color(20,  55,  110);
    /** Color complementario para llamadas a la acción afirmativas o confirmaciones de éxito. */
    public static final Color ACENTO = new Color(52,  168, 83);
    /** Color de advertencia o peligro, reservado para cancelaciones, errores o acciones destructivas. */
    public static final Color PELIGRO = new Color(220, 53,  69);
    /** Color base de fondo para las ventanas y contenedores principales del sistema. */
    public static final Color FONDO = new Color(245, 247, 250);
    /** Superficie de contraste limpia para tarjetas contenedoras, paneles de contenido y listas. */
    public static final Color FONDO_TARJETA = Color.WHITE;
    /** Tonalidad de alta legibilidad para el texto principal, títulos y etiquetas destacadas. */
    public static final Color TEXTO_PRIMARIO = new Color(30,  30,  30);
    /** Tonalidad suavizada para descripciones de soporte, subtítulos o metadatos de menor jerarquía. */
    public static final Color TEXTO_SECUNDARIO = new Color(100, 100, 100);
    /** Tonalidad neutra para líneas divisorias, grillas y contornos de componentes. */
    public static final Color BORDE = new Color(220, 220, 228);
    /** Estado positivo de agenda, indicando un bloque horario habilitado para reserva. */
    public static final Color DISPONIBLE = new Color(144, 238, 144);
    /** Estado deshabilitado de agenda, indicando un bloque inactivo u ocupado. */
    public static final Color NO_DISPONIBLE = new Color(235, 235, 235);
    /** Color de fondo para las celdas de cabecera en tablas de datos. */
    public static final Color ENCABEZADO_TABLA = new Color(30,  80,  160);

    /** Tipografía destacada para títulos principales de secciones o ventanas. */
    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    /** Tipografía secundaria para encabezados de tarjetas o agrupaciones de datos. */
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 15);
    /** Tipografía base estandarizada para textos informativos, formularios y tablas. */
    public static final Font FUENTE_CUERPO = new Font("Segoe UI", Font.PLAIN, 13);
    /** Tipografía compacta para anotaciones marginales, notas al pie o marcas de tiempo. */
    public static final Font FUENTE_PEQUENA = new Font("Segoe UI", Font.PLAIN, 11);
    /** Tipografía de alta visibilidad parametrizada para componentes interactivos de botón. */
    public static final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);

    /** Espaciado interno general estándar para márgenes de paneles principales. */
    public static final int PADDING = 20;
    /** Espaciado interno reducido para elementos contiguos, iconos o celdas densas. */
    public static final int PADDING_PEQUEÑO = 10;
    /** Altura vertical uniforme para los botones del sistema. */
    public static final int ALTO_BOTON = 38;
}