package modelo.entidades;

/**
 * Constantes inmutables del modelo de horario semanal para el Sistema de Reservas.
 *
 * Define las dimensiones y etiquetas para manejar la matriz de disponibilidad
 * del sistema, estructurada como: boolean[DIAS][BLOQUES].
 *
 * Distribución de la matriz:
 * - Filas (dias): Representan los días laborales de Lunes a Viernes (índices 0 a 4).
 * - Columnas (bloques): Representan los tramos horarios desde las 08:00 hasta las 17:30 (índices 0 a 5).
 *
 * Esta clase es estrictamente utilitaria y no puede ser instanciada.
 */
public final class ConstantesHorario {

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

    /**
     * Nombres descriptivos de los días laborales en orden cronológico.
     * Útil para renderizar los encabezados de las filas en la interfaz visual.
     */
    public static final String[] NOMBRES_DIAS = {
        "Lunes",
        "Martes",
        "Miércoles",
        "Jueves",
        "Viernes"
    };

    /**
     * Rangos horarios correspondientes a cada bloque del sistema.
     * Útil para mostrar las horas exactas de cada clase en la interfaz visual.
     */
    public static final String[] NOMBRES_BLOQUES = {
        "08:00 - 09:30",
        "09:30 - 11:00",
        "11:00 - 12:30",
        "13:00 - 14:30",
        "14:30 - 16:00",
        "16:00 - 17:30"
    };

    /**
     * Constructor privado que previene la creación de instancias.
     * Lanza una excepción si se intenta acceder mediante reflexión.
     *
     * @throws UnsupportedOperationException siempre que se intente invocar
     */
    private ConstantesHorario() {
        throw new UnsupportedOperationException(
            "ConstantesHorario es una clase de constantes y no debe ser instanciada.");
    }
}
