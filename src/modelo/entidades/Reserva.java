package modelo.entidades;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una reserva de tutoría confirmada en el Sistema de Reservas de
 * Clases Particulares.
 *
 * <p>Una {@code Reserva} es el resultado concreto de que el administrador
 * procese una {@link Solicitud} a través del {@code ComandoAgendar}.  Asocia
 * de forma definitiva a un {@link Estudiante} con un {@link Tutor} en un
 * bloque horario específico de la semana.</p>
 *
 * <p>El bloque queda identificado por tres referencias complementarias:</p>
 * <ul>
 *   <li>{@link #diaIndex} – índice del día (0 = Lunes … 4 = Viernes), coherente
 *       con la primera dimensión de las matrices de disponibilidad.</li>
 *   <li>{@link #bloqueIndex} – índice del bloque horario (0–5), coherente con
 *       la segunda dimensión de las matrices.</li>
 *   <li>{@link #fecha} – fecha concreta ({@link LocalDate}) en que se realizará
 *       la clase, que permite al sistema mantener el calendario con vistas
 *       filtradas por tutor o estudiante.</li>
 * </ul>
 *
 * <p>La reserva expone los métodos de utilidad {@link #isActiva()} y
 * {@link #conflictaCon(Reserva)} que el {@code GestorDatos} usa para prevenir
 * solapamientos horarios antes de confirmar una nueva reserva.</p>
 *
 * <p>El estado lo gestiona el administrador mediante
 * {@link #setEstado(EstadoReserva)} o a través de los comandos del
 * controlador:</p>
 * <pre>
 *   CONFIRMADA ──► CANCELADA  (via ComandoCancelar / deshacer)
 *              └─► ARCHIVADA  (via ComandoArchivar, clase ya realizada)
 * </pre>
 *
 * <p>Invariantes de clase:</p>
 * <ul>
 *   <li>{@code tutor}, {@code estudiante}, {@code solicitudOrigen} y
 *       {@code fecha} nunca son {@code null}.</li>
 *   <li>{@code diaIndex} ∈ [0, {@value ConstantesHorario#DIAS} − 1].</li>
 *   <li>{@code bloqueIndex} ∈ [0, {@value ConstantesHorario#BLOQUES} − 1].</li>
 *   <li>{@code estado} nunca es {@code null}.</li>
 *   <li>{@code id} es inmutable durante toda la vida del objeto.</li>
 * </ul>
 *
 * @author  Bastián
 * @version 1.0
 * @see     Tutor
 * @see     Estudiante
 * @see     Solicitud
 * @see     ConstantesHorario
 */
public class Reserva {

    // -------------------------------------------------------------------------
    // Enumeración de estado
    // -------------------------------------------------------------------------

    /**
     * Estados posibles durante el ciclo de vida de una {@code Reserva}.
     */
    public enum EstadoReserva {

        /**
         * La tutoría está programada y pendiente de realizarse.
         * Estado inicial de toda reserva recién creada.
         */
        CONFIRMADA,

        /**
         * La tutoría fue cancelada antes de realizarse, ya sea por el
         * administrador, el tutor o el estudiante.  Una reserva cancelada
         * libera el bloque horario del tutor en el sistema.
         */
        CANCELADA,

        /**
         * La tutoría ya se realizó y fue movida al historial administrativo.
         * Una reserva archivada es de solo lectura.
         */
        ARCHIVADA
    }

    // -------------------------------------------------------------------------
    // Atributos
    // -------------------------------------------------------------------------

    /**
     * Identificador único de la reserva, generado automáticamente como UUID
     * en la construcción.  Inmutable.
     */
    private final String id;

    /**
     * Tutor asignado a la tutoría.  Referencia inmutable.
     * Nunca {@code null}.
     */
    private final Tutor tutor;

    /**
     * Estudiante asignado a la tutoría.  Referencia inmutable.
     * Nunca {@code null}.
     */
    private final Estudiante estudiante;

    /**
     * Solicitud que originó esta reserva.  Permite trazabilidad del proceso
     * administrativo y es usada por el patrón Command para revertir la
     * operación en {@code deshacer()}.  Referencia inmutable.
     * Nunca {@code null}.
     */
    private final Solicitud solicitudOrigen;

    /**
     * Fecha concreta en que se realizará la tutoría.
     * Permite al sistema construir vistas de calendario filtradas por
     * semana, tutor o estudiante.
     */
    private LocalDate fecha;

    /**
     * Índice del día de la semana en la matriz de horario.
     * Rango válido: 0 (Lunes) – {@value ConstantesHorario#DIAS} − 1 (Viernes).
     * Inmutable; definido al crear la reserva.
     */
    private final int diaIndex;

    /**
     * Índice del bloque horario en la matriz de disponibilidad.
     * Rango válido: 0 – {@value ConstantesHorario#BLOQUES} − 1.
     * Inmutable; definido al crear la reserva.
     */
    private final int bloqueIndex;

    /**
     * Estado actual de la reserva en su ciclo de vida.
     * Nunca {@code null}.
     */
    private EstadoReserva estado;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Crea una nueva {@code Reserva} en estado {@link EstadoReserva#CONFIRMADA}.
     *
     * <p>Este constructor es llamado exclusivamente por {@code ComandoAgendar}
     * después de verificar que no existe conflicto horario en
     * {@code GestorDatos}.</p>
     *
     * @param tutor           tutor asignado; no puede ser {@code null}
     * @param estudiante      estudiante asignado; no puede ser {@code null}
     * @param solicitudOrigen solicitud que originó la reserva; no puede ser
     *                        {@code null}
     * @param fecha           fecha concreta de la tutoría; no puede ser
     *                        {@code null}
     * @param diaIndex        índice del día (0–{@value ConstantesHorario#DIAS}−1)
     * @param bloqueIndex     índice del bloque (0–{@value ConstantesHorario#BLOQUES}−1)
     * @throws IllegalArgumentException si algún parámetro obligatorio es
     *                                  {@code null} o si los índices están
     *                                  fuera de rango
     */
    public Reserva(Tutor tutor, Estudiante estudiante, Solicitud solicitudOrigen,
                   LocalDate fecha, int diaIndex, int bloqueIndex) {

        validarParametros(tutor, estudiante, solicitudOrigen, fecha, diaIndex, bloqueIndex);

        this.id              = UUID.randomUUID().toString();
        this.tutor           = tutor;
        this.estudiante      = estudiante;
        this.solicitudOrigen = solicitudOrigen;
        this.fecha           = fecha;
        this.diaIndex        = diaIndex;
        this.bloqueIndex     = bloqueIndex;
        this.estado          = EstadoReserva.CONFIRMADA;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Retorna el identificador único e inmutable de la reserva.
     *
     * @return UUID en formato {@code String}
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna el tutor asignado a esta tutoría.
     *
     * @return referencia inmutable al {@link Tutor}; nunca {@code null}
     */
    public Tutor getTutor() {
        return tutor;
    }

    /**
     * Retorna el estudiante asignado a esta tutoría.
     *
     * @return referencia inmutable al {@link Estudiante}; nunca {@code null}
     */
    public Estudiante getEstudiante() {
        return estudiante;
    }

    /**
     * Retorna la solicitud que originó esta reserva.
     *
     * <p>Es utilizada por el patrón Command durante la operación
     * {@code deshacer()} para restaurar la solicitud a estado
     * {@link Solicitud.EstadoSolicitud#PENDIENTE}.</p>
     *
     * @return referencia inmutable a la {@link Solicitud}; nunca {@code null}
     */
    public Solicitud getSolicitudOrigen() {
        return solicitudOrigen;
    }

    /**
     * Retorna la fecha concreta en que se realizará la tutoría.
     *
     * @return fecha de la clase; nunca {@code null}
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Retorna el índice del día de la semana de esta reserva, compatible con
     * la primera dimensión de las matrices de disponibilidad.
     *
     * @return valor entre 0 (Lunes) y {@value ConstantesHorario#DIAS} − 1
     *         (Viernes)
     */
    public int getDiaIndex() {
        return diaIndex;
    }

    /**
     * Retorna el índice del bloque horario de esta reserva, compatible con la
     * segunda dimensión de las matrices de disponibilidad.
     *
     * @return valor entre 0 y {@value ConstantesHorario#BLOQUES} − 1
     */
    public int getBloqueIndex() {
        return bloqueIndex;
    }

    /**
     * Retorna el nombre del día de la semana correspondiente a
     * {@link #diaIndex}, usando las etiquetas de {@link ConstantesHorario}.
     *
     * @return nombre del día (p. ej., "Lunes", "Miércoles")
     */
    public String getNombreDia() {
        return ConstantesHorario.NOMBRES_DIAS[diaIndex];
    }

    /**
     * Retorna la descripción textual del bloque horario correspondiente a
     * {@link #bloqueIndex}, usando las etiquetas de {@link ConstantesHorario}.
     *
     * <p>Este método satisface el atributo {@code hora} especificado en el
     * enunciado del proyecto, derivándolo del índice de bloque para evitar
     * redundancia de datos.</p>
     *
     * @return rango horario (p. ej., {@code "08:00 - 09:30"})
     */
    public String getHora() {
        return ConstantesHorario.NOMBRES_BLOQUES[bloqueIndex];
    }

    /**
     * Retorna el estado actual de la reserva en su ciclo de vida.
     *
     * @return estado; nunca {@code null}
     */
    public EstadoReserva getEstado() {
        return estado;
    }

    // -------------------------------------------------------------------------
    // Setters con validación
    // -------------------------------------------------------------------------

    /**
     * Actualiza la fecha concreta de la tutoría.
     *
     * <p>Sólo el administrador debería invocar este método para reagendar una
     * clase; el bloque (día e índice) permanece inmutable.</p>
     *
     * @param fecha nueva fecha; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code fecha} es {@code null}
     */
    public void setFecha(LocalDate fecha) {
        if (fecha == null)
            throw new IllegalArgumentException("La fecha de la reserva no puede ser nula.");
        this.fecha = fecha;
    }

    /**
     * Actualiza el estado de la reserva.
     *
     * <p>Normalmente esta operación la realizan los comandos del controlador
     * ({@code ComandoAgendar}, {@code ComandoArchivar}) para mantener el
     * historial de operaciones deshacer/rehacer.</p>
     *
     * @param estado nuevo estado; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code estado} es {@code null}
     */
    public void setEstado(EstadoReserva estado) {
        if (estado == null)
            throw new IllegalArgumentException("El estado de la reserva no puede ser nulo.");
        this.estado = estado;
    }

    // -------------------------------------------------------------------------
    // Métodos de utilidad
    // -------------------------------------------------------------------------

    /**
     * Indica si la reserva está activa (programada y sin cancelar).
     *
     * <p>El {@code GestorDatos} usa este método durante la verificación
     * de conflictos horarios antes de registrar una nueva reserva.</p>
     *
     * @return {@code true} si el estado es {@link EstadoReserva#CONFIRMADA}
     */
    public boolean isActiva() {
        return estado == EstadoReserva.CONFIRMADA;
    }

    /**
     * Verifica si esta reserva genera un conflicto horario con otra reserva
     * existente.
     *
     * <p>Existe conflicto cuando ambas reservas están {@link #isActiva() activas},
     * comparten el mismo {@link Tutor} y ocupan el mismo bloque de día e
     * índice.  El {@code GestorDatos} debe invocar este método sobre todas
     * las reservas activas antes de confirmar una nueva.</p>
     *
     * <pre>{@code
     * // Ejemplo de uso en GestorDatos
     * for (Reserva r : reservas) {
     *     if (nueva.conflictaCon(r)) {
     *         throw new ConflictoHorarioException("El tutor ya tiene clase asignada.");
     *     }
     * }
     * }</pre>
     *
     * @param otra reserva contra la que se compara; no puede ser {@code null}
     * @return {@code true} si hay solapamiento de tutor, día y bloque entre
     *         dos reservas activas
     * @throws IllegalArgumentException si {@code otra} es {@code null}
     */
    public boolean conflictaCon(Reserva otra) {
        if (otra == null)
            throw new IllegalArgumentException(
                "La reserva comparada no puede ser nula al verificar conflictos.");

        return this.isActiva()
            && otra.isActiva()
            && this.tutor.getId().equals(otra.tutor.getId())
            && this.diaIndex   == otra.diaIndex
            && this.bloqueIndex == otra.bloqueIndex;
    }

    /**
     * Genera una descripción compacta de la clase programada, útil para la
     * capa de vista en el calendario.
     *
     * @return cadena con tutor, estudiante, día y hora
     *         (p. ej., {@code "Ana López → Pedro Soto | Lunes 08:00-09:30"})
     */
    public String resumen() {
        return String.format("%s → %s | %s %s",
            tutor.getNombre(), estudiante.getNombre(), getNombreDia(), getHora());
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Valida todos los parámetros obligatorios del constructor.
     */
    private static void validarParametros(Tutor tutor, Estudiante estudiante,
                                          Solicitud solicitudOrigen, LocalDate fecha,
                                          int diaIndex, int bloqueIndex) {
        if (tutor == null)
            throw new IllegalArgumentException("El tutor de la reserva no puede ser nulo.");
        if (estudiante == null)
            throw new IllegalArgumentException("El estudiante de la reserva no puede ser nulo.");
        if (solicitudOrigen == null)
            throw new IllegalArgumentException("La solicitud de origen no puede ser nula.");
        if (fecha == null)
            throw new IllegalArgumentException("La fecha de la reserva no puede ser nula.");
        if (diaIndex < 0 || diaIndex >= ConstantesHorario.DIAS)
            throw new IllegalArgumentException(String.format(
                "Índice de día fuera de rango: %d (rango válido: 0–%d).",
                diaIndex, ConstantesHorario.DIAS - 1));
        if (bloqueIndex < 0 || bloqueIndex >= ConstantesHorario.BLOQUES)
            throw new IllegalArgumentException(String.format(
                "Índice de bloque fuera de rango: %d (rango válido: 0–%d).",
                bloqueIndex, ConstantesHorario.BLOQUES - 1));
    }

    // -------------------------------------------------------------------------
    // Sobreescritura de Object
    // -------------------------------------------------------------------------

    /**
     * Dos instancias de {@code Reserva} son iguales si comparten el mismo
     * {@code id}.
     *
     * @param obj objeto a comparar
     * @return {@code true} si ambos representan la misma reserva
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Reserva)) return false;
        Reserva otra = (Reserva) obj;
        return id.equals(otra.id);
    }

    /**
     * El código hash se basa exclusivamente en el {@code id} de la reserva.
     *
     * @return código hash consistente con {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representación legible de la reserva, útil para depuración, logs y el
     * calendario del administrador.
     *
     * @return cadena con los campos más relevantes de la reserva
     */
    @Override
    public String toString() {
        return String.format(
            "Reserva{id='%s', tutor='%s', estudiante='%s', fecha=%s, "
            + "dia='%s', hora='%s', estado=%s}",
            id, tutor.getNombre(), estudiante.getNombre(),
            fecha, getNombreDia(), getHora(), estado);
    }
}
