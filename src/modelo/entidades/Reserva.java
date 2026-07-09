package modelo.entidades;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una reserva de tutoría confirmada en el Sistema de Reservas de Clases Particulares.
 *
 * Es el resultado concreto de procesar una Solicitud a través del administrador.
 * Asocia de forma definitiva a un Estudiante con un Tutor en un bloque horario
 * específico de una fecha determinada.
 *
 * El bloque se identifica por tres referencias coherentes con el sistema:
 * - diaIndex: Índice del día (0 = Lunes a 4 = Viernes), usado en las filas de la matriz.
 * - bloqueIndex: Índice del bloque horario (0 a 5), usado en las columnas de la matriz.
 * - fecha: Instancia de LocalDate en que se realizará físicamente la tutoría.
 *
 * Invariantes de la clase:
 * - El tutor, estudiante, solicitudOrigen y fecha nunca pueden ser nulos.
 * - Los índices de día y bloque deben estar dentro de los límites de ConstantesHorario.
 * - El ID único se autogenera en la construcción y es inmutable.
 */
public class Reserva {

    /**
     * Estados posibles durante el ciclo de vida de una Reserva.
     */
    public enum EstadoReserva {

        /**
         * La tutoría está programada y pendiente de realizarse.
         * Estado inicial de toda reserva recién creada.
         */
        CONFIRMADA,

        /**
         * La tutoría fue cancelada. Libera el bloque horario del tutor en el sistema.
         */
        CANCELADA,

        /**
         * La tutoría ya se realizó y fue movida al historial administrativo.
         * Una reserva archivada es de solo lectura.
         */
        ARCHIVADA
    }

    /**
     * Identificador único autogenerado de la reserva (UUID). Inmutable.
     */
    private final String id;

    /**
     * Tutor asignado a la tutoría. Referencia inmutable.
     * Nunca {@code null}.
     */
    private final Tutor tutor;

    /**
     * Estudiante asignado a la tutoría. Referencia inmutable.
     * Nunca {@code null}.
     */
    private final Estudiante estudiante;

    /**
     * Solicitud que originó esta reserva. Permite trazabilidad del proceso
     * administrativo y es usada por el patrón Command para revertir la
     * operación en {@code deshacer()}. Referencia inmutable.
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

    /**
     * Crea una nueva Reserva inicializada en estado CONFIRMADA.
     *
     * Este constructor se invoca tras validar que no existan topes de horario
     * con el tutor asignado.
     *
     * @param tutor Tutor asignado a la clase.
     * @param estudiante Estudiante que recibirá la clase.
     * @param solicitudOrigen Solicitud base que originó la reserva.
     * @param fecha Fecha del calendario de la tutoría.
     * @param diaIndex Índice de la fila (0 a 4).
     * @param bloqueIndex Índice de la columna (0 a 5).
     * @throws IllegalArgumentException Si algún objeto es nulo o los índices quedan fuera del rango válido.
     */
    public Reserva(Tutor tutor, Estudiante estudiante, Solicitud solicitudOrigen,
                   LocalDate fecha, int diaIndex, int bloqueIndex) {

        validarParametros(tutor, estudiante, solicitudOrigen, fecha, diaIndex, bloqueIndex);

        this.id = UUID.randomUUID().toString();
        this.tutor = tutor;
        this.estudiante = estudiante;
        this.solicitudOrigen = solicitudOrigen;
        this.fecha = fecha;
        this.diaIndex = diaIndex;
        this.bloqueIndex = bloqueIndex;
        this.estado = EstadoReserva.CONFIRMADA;
    }

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
     * Es utilizada por el patrón Command durante la operación
     * {@code deshacer()} para restaurar la solicitud a estado
     * {@link Solicitud.EstadoSolicitud#PENDIENTE}.
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
     * @return valor entre 0 (Lunes) y {@value ConstantesHorario#DIAS} − 1 (Viernes)
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
     * @return nombre del día
     */
    public String getNombreDia() {
        return ConstantesHorario.NOMBRES_DIAS[diaIndex];
    }

    /**
     * Retorna la descripción textual del bloque horario correspondiente a
     * {@link #bloqueIndex}, usando las etiquetas de {@link ConstantesHorario}.
     *
     * @return rango horario
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

    /**
     * Actualiza la fecha de la tutoría si se requiere una reagendación.
     *
     * @param fecha Nueva fecha asignada.
     * @throws IllegalArgumentException Si la fecha recibida es nula.
     */
    public void setFecha(LocalDate fecha) {
        if (fecha == null)
            throw new IllegalArgumentException("La fecha de la reserva no puede ser nula.");
        this.fecha = fecha;
    }

    /**
     * Actualiza el estado de la reserva.
     *
     * @param estado Nuevo estado del ciclo de vida.
     * @throws IllegalArgumentException Si el estado recibido es nulo.
     */
    public void setEstado(EstadoReserva estado) {
        if (estado == null)
            throw new IllegalArgumentException("El estado de la reserva no puede ser nulo.");
        this.estado = estado;
    }

    /**
     * Evalúa si la reserva se encuentra activa (sin cancelar).
     *
     * @return true si el estado actual es CONFIRMADA.
     */
    public boolean isActiva() {
        return estado == EstadoReserva.CONFIRMADA;
    }

    /**
     * Verifica si esta reserva se cruza en horario y tutor con otra reserva activa.
     *
     * Se considera conflicto si ambas están activas, comparten el mismo tutor
     * y coinciden en el mismo día y bloque.
     *
     * @param otra La otra reserva para verificar solapamiento.
     * @return true si existe un cruce de horarios activo.
     * @throws IllegalArgumentException Si la reserva a comparar es nula.
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
     * Genera un resumen textual breve que vincula los nombres e información de la cita.
     *
     * @return Texto formateado con tutor, estudiante, día y hora.
     */
    public String resumen() {
        return String.format("%s → %s | %s %s",
            tutor.getNombre(), estudiante.getNombre(), getNombreDia(), getHora());
    }

    /**
     * Valida todos los parámetros obligatorios del constructor.
     */
    private static void validarParametros(Tutor tutor, Estudiante estudiante, Solicitud solicitudOrigen, LocalDate fecha, int diaIndex, int bloqueIndex) {
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
