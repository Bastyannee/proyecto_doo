package vista;

import java.awt.Color;
import java.awt.Font;

/**
 * Constantes visuales centralizadas de la aplicación.
 * Todos los paneles importan este archivo para mantener
 * coherencia visual sin repetir valores en cada clase.
 */
public final class Tema {

    private Tema() {}

    // =========================================================
    // Colores
    // =========================================================
    public static final Color PRIMARIO        = new Color(30,  80,  160);
    public static final Color PRIMARIO_OSCURO = new Color(20,  55,  110);
    public static final Color ACENTO          = new Color(52,  168, 83);
    public static final Color PELIGRO         = new Color(220, 53,  69);
    public static final Color FONDO           = new Color(245, 247, 250);
    public static final Color FONDO_TARJETA   = Color.WHITE;
    public static final Color TEXTO_PRIMARIO  = new Color(30,  30,  30);
    public static final Color TEXTO_SECUNDARIO= new Color(100, 100, 100);
    public static final Color BORDE           = new Color(220, 220, 228);
    public static final Color DISPONIBLE      = new Color(144, 238, 144);
    public static final Color NO_DISPONIBLE   = new Color(235, 235, 235);
    public static final Color ENCABEZADO_TABLA= new Color(30,  80,  160);

    // =========================================================
    // Fuentes
    // =========================================================
    public static final Font FUENTE_TITULO    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FUENTE_CUERPO    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_PEQUENA   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FUENTE_BOTON     = new Font("Segoe UI", Font.BOLD,  13);

    // =========================================================
    // Dimensiones
    // =========================================================
    public static final int PADDING           = 20;
    public static final int PADDING_PEQUEÑO   = 10;
    public static final int ALTO_BOTON        = 38;
    public static final int ANCHO_SIDEBAR     = 200;
}