package modelo.entidades;

/**
 * Constantes inmutables del modelo de horario semanal para el Sistema de
 * Reservas de Clases Particulares.
 *
 * <p>Define las dimensiones de la matriz de disponibilidad
 * {@code boolean[DIAS][BLOQUES]} y los nombres descriptivos de cada día y
 * bloque horario. Toda clase que opere sobre matrices de horario debe referirse
 * exclusivamente a estas constantes para garantizar coherencia en el sistema.</p>
 *
 * <p>Estructura de la matriz:</p>
 * <pre>
 *             Bloque 0       Bloque 1       Bloque 2       Bloque 3       Bloque 4       Bloque 5
 *           08:00-09:30    09:30-11:00    11:00-12:30    13:00-14:30    14:30-16:00    16:00-17:30
 * Lunes   [  true/false  |  true/false  |  true/false  |  true/false  |  true/false  |  true/false ]
 * Martes  [  true/false  |  ...                                                                    ]
 * Miérc.  [  ...                                                                                   ]
 * Jueves  [  ...                                                                                   ]
 * Viernes [  ...                                                                                   ]
 * </pre>
 *
 * <p>Esta clase no puede ser instanciada; todas sus constantes son estáticas
 * y finales.</p>
 *
 * @author  Bastián
 * @version 1.0
 */
public final class ConstantesHorario {

    // -------------------------------------------------------------------------
    // Dimensiones de la matriz
    // -------------------------------------------------------------------------

    /**
     * Número de días laborales que componen las filas de la matriz.
     * Corresponde a Lunes (índice 0) hasta Viernes (índice 4).
     */
    public static final int DIAS = 5;

    /**
     * Número de bloques horarios por día que componen las columnas de la matriz.
     * Los bloques van del índice 0 (08:00) al índice 5 (17:30).
     */
    public static final int BLOQUES = 6;

    // -------------------------------------------------------------------------
    // Etiquetas descriptivas
    // -------------------------------------------------------------------------

    /**
     * Nombres de los días laborales indexados de {@code 0} a {@code DIAS - 1}.
     *
     * <pre>
     *   NOMBRES_DIAS[0] → "Lunes"
     *   NOMBRES_DIAS[1] → "Martes"
     *   NOMBRES_DIAS[2] → "Miércoles"
     *   NOMBRES_DIAS[3] → "Jueves"
     *   NOMBRES_DIAS[4] → "Viernes"
     * </pre>
     *
     * <p><b>Nota:</b> El arreglo es público para facilitar su uso en la capa
     * de vista (renderización de la grilla de horarios), pero sus elementos
     * son cadenas inmutables en Java, por lo que no existe riesgo de mutación.</p>
     */
    public static final String[] NOMBRES_DIAS = {
        "Lunes",
        "Martes",
        "Miércoles",
        "Jueves",
        "Viernes"
    };

    /**
     * Etiquetas de los bloques horarios indexados de {@code 0} a
     * {@code BLOQUES - 1}.
     *
     * <pre>
     *   NOMBRES_BLOQUES[0] → "08:00 - 09:30"
     *   NOMBRES_BLOQUES[1] → "09:30 - 11:00"
     *   NOMBRES_BLOQUES[2] → "11:00 - 12:30"
     *   NOMBRES_BLOQUES[3] → "13:00 - 14:30"
     *   NOMBRES_BLOQUES[4] → "14:30 - 16:00"
     *   NOMBRES_BLOQUES[5] → "16:00 - 17:30"
     * </pre>
     */
    public static final String[] NOMBRES_BLOQUES = {
        "08:00 - 09:30",
        "09:30 - 11:00",
        "11:00 - 12:30",
        "13:00 - 14:30",
        "14:30 - 16:00",
        "16:00 - 17:30"
    };

    // -------------------------------------------------------------------------
    // Constructor privado (clase utilitaria no instanciable)
    // -------------------------------------------------------------------------

    /**
     * Constructor privado que impide la instanciación directa de esta clase
     * utilitaria.
     *
     * @throws UnsupportedOperationException siempre, al intentar instanciarla
     */
    private ConstantesHorario() {
        throw new UnsupportedOperationException(
            "ConstantesHorario es una clase de constantes y no debe ser instanciada.");
    }
}
