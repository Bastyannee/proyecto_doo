package vista.proxy;

/**
 * Contrato de diseño común y unificado entre las entidades reales de tipo Tutor
 * y sus representaciones estructurales diferidas mediante ProxyTutor.
 *
 * El propósito medular de esta interfaz consiste en proveer un desacoplamiento completo
 * a la capa visual del sistema, permitiendo que los diferentes subpaneles interactúen
 * con cualquiera de las dos implementaciones de manera polimórfica y completamente transparente.
 *
 * Esta abstracción fue diseñada específicamente para optimizar el consumo de la interfaz gráfica:
 * en lugar de comprometer u obligar a la vista a conocer las complejidades y relaciones internas del
 * modelo de dominio Tutor, define estrictamente las consultas operativas indispensables para poblar las
 * tarjetas, listados y agendas de la interfaz de usuario.
 */
public interface PerfilSeleccionable {

    /** @return identificador único del tutor */
    String getId();

    /** @return nombre completo del tutor */
    String getNombre();

    /** @return descripción o perfil del tutor */
    String getDescripcion();

    /** @return materia principal que dicta */
    String getMateria();

    /** @return afinidad o especialización adicional */
    String getAfinidad();

    /** @return ruta de la foto de perfil */
    String getFotoPath();

    /** @return tarifa por hora */
    double getTarifa();

    /** @return número máximo de estudiantes que puede atender */
    int getMaxEstudiantes();

    /**
     * Consulta el estado de disponibilidad del perfil evaluando las coordenadas de su matriz
     * de tiempos en un día y bloque cronológico específicos.
     *
     * @param dia Índice posicional representativo del día evaluado (0 para Lunes hasta 4 para Viernes).
     * @param bloque Índice posicional del rango horario evaluado (0 para las 08:00 hasta 7 para las 17:00).
     * @return Verdadero (true) si el tutor posee vacantes libres u horarios hábiles para la reserva en ese cuadrante.
     */
    boolean isDisponible(int dia, int bloque);
}