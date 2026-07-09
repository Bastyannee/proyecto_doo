package modelo.entidades;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa una solicitud de tutoría que un Estudiante genera y el administrador
 * registra en el Sistema de Reservas de Clases Particulares.
 *
 * Una Solicitud encapsula:
 * - El asunto o materia de apoyo requerida.
 * - Un comentario descriptivo del estudiante con detalles adicionales.
 * - Una matriz de booleanos que expresa los bloques horarios de preferencia del alumno,
 *   coherente con las dimensiones de disponibilidad del Tutor.
 *
 * El administrador utiliza este objeto como entrada para encontrar tutores compatibles
 * mediante estrategias de búsqueda y, posteriormente, para transformarla en una Reserva.
 *
 * Invariantes de la clase:
 * - El ID, asunto y estudiante nunca pueden ser nulos.
 * - La matriz horarioDeseado siempre mantiene las dimensiones de ConstantesHorario.
 * - El estado inicial siempre se inicializa como PENDIENTE.
 * - La fechaCreacion es inmutable y captura el instante exacto de registro en el sistema.
 */
public class Solicitud {

    /**
     * Estados posibles durante el ciclo de vida de una Solicitud.
     */
    public enum EstadoSolicitud {

        /**
         * La solicitud fue registrada y aún no ha sido procesada por el
         * administrador.  Es el estado inicial de toda solicitud nueva.
         */
        PENDIENTE,

        /**
         * La solicitud fue descartada por el administrador sin generar una
         * reserva (por ejemplo: por incompatibilidad de horarios o falta de tutores
         * disponibles).
         */
        ARCHIVADA,

        /**
         * La solicitud fue procesada exitosamente y se creó una {@link Reserva}
         * correspondiente a través del {@code ComandoAgendar}.
         */
        CONVERTIDA
    }

    /**
     * Identificador único de la solicitud.
     * Asignado por el administrador o generado por {@code GestorDatos} al
     * momento del registro. Inmutable.
     */
    private final String id;

    /**
     * Asunto de la solicitud; describe la materia o tema de apoyo requerido
     * (p. ej., "Ayuda con integrales dobles de Cálculo III").
     * Nunca {@code null} ni en blanco.
     */
    private String asunto;

    /**
     * Comentario libre del estudiante que detalla sus dudas, nivel de avance
     * u observaciones para el tutor.  Nunca {@code null}; puede ser vacío.
     */
    private String comentario;

    /**
     * Matriz de disponibilidad horaria donde true indica que el estudiante puede asistir
     * en ese bloque y dia de la semana. Estructura basada en boolean[DIAS][BLOQUES].
     */
    private boolean[][] horarioDeseado;

    /**
     * Estudiante que origina la solicitud. Referencia inmutable.
     * Nunca {@code null}.
     */
    private final Estudiante estudiante;

    /**
     * Estado actual de la solicitud en su ciclo de vida.
     * Nunca {@code null}.
     */
    private EstadoSolicitud estado;

    /**
     * Fecha y hora en que la solicitud fue registrada en el sistema.
     * Inmutable; capturado en la construcción del objeto.
     */
    private final LocalDateTime fechaCreacion;

    /**
     * Crea una nueva Solicitud en estado PENDIENTE con su marca de tiempo actual.
     *
     * @param id Identificador único; no puede estar vacío ni ser nulo.
     * @param asunto Tema o materia de consulta; no puede estar vacío ni ser nulo.
     * @param comentario Detalle de la consulta; si es nulo se normaliza a texto vacío.
     * @param horarioDeseado Matriz de bloques seleccionados; si es nula se genera vacía.
     * @param estudiante Estudiante que genera la solicitud.
     * @throws IllegalArgumentException Si algún campo obligatorio es inválido o la matriz no cumple las dimensiones necesarias.
     */
    public Solicitud(String id, String asunto, String comentario, boolean[][] horarioDeseado, Estudiante estudiante) {

        validarParametrosObligatorios(id, asunto, estudiante);
        if (horarioDeseado != null) validarDimensionesMatriz(horarioDeseado);

        this.id = id.strip();
        this.asunto = asunto.strip();
        this.comentario = (comentario != null) ? comentario.strip() : "";
        this.horarioDeseado = (horarioDeseado != null)
                              ? copiarMatriz(horarioDeseado)
                              : new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.estudiante = estudiante;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Retorna el identificador único e inmutable de la solicitud.
     *
     * @return identificador de la solicitud; nunca {@code null}
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna el asunto de la solicitud.
     *
     * @return asunto; nunca {@code null} ni en blanco
     */
    public String getAsunto() {
        return asunto;
    }

    /**
     * Retorna el comentario del estudiante.
     *
     * @return comentario; cadena vacía si no se proporcionó ninguno
     */
    public String getComentario() {
        return comentario;
    }

    /**
     * Retorna una copia defensiva profunda de la matriz de horario deseado.
     * Modificar el arreglo devuelto no altera la estructura interna de la solicitud.
     *
     * @return Una copia de la matriz boolean[DIAS][BLOQUES].
     */
    public boolean[][] getHorarioDeseado() {
        return copiarMatriz(horarioDeseado);
    }

    /**
     * Evalúa si un bloque de tiempo y día específico fue seleccionado en la solicitud.
     *
     * @param dia Índice del día de la semana (0 a 4).
     * @param bloque Índice del bloque de horas (0 a 5).
     * @return true si el bloque se encuentra marcado como disponible.
     * @throws IllegalArgumentException Si los índices se encuentran fuera de la grilla permitida.
     */
    public boolean isBloqueSolicitado(int dia, int bloque) {
        validarIndices(dia, bloque);
        return horarioDeseado[dia][bloque];
    }

    /**
     * Retorna el estudiante dueño de esta solicitud.
     *
     * @return referencia inmutable al {@link Estudiante}; nunca {@code null}
     */
    public Estudiante getEstudiante() {
        return estudiante;
    }

    /**
     * Retorna el estado actual de la solicitud.
     *
     * @return estado del ciclo de vida; nunca {@code null}
     */
    public EstadoSolicitud getEstado() {
        return estado;
    }

    /**
     * Retorna la fecha y hora en que la solicitud fue registrada en el sistema.
     *
     * @return timestamp inmutable de creación
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Actualiza el asunto de la solicitud.
     *
     * @param asunto nuevo asunto; no puede ser {@code null} ni en blanco
     * @throws IllegalArgumentException si {@code asunto} es inválido
     */
    public void setAsunto(String asunto) {
        if (asunto == null || asunto.isBlank())
            throw new IllegalArgumentException("El asunto de la solicitud no puede ser vacío.");
        this.asunto = asunto.strip();
    }

    /**
     * Actualiza el comentario del estudiante.
     *
     * @param comentario nuevo comentario; {@code null} se normaliza a cadena vacía
     */
    public void setComentario(String comentario) {
        this.comentario = (comentario != null) ? comentario.strip() : "";
    }

    /**
     * Reemplaza por completo la matriz de horario utilizando una copia defensiva profunda.
     *
     * @param horarioDeseado Nueva matriz de horarios semanales.
     * @throws IllegalArgumentException Si la matriz es nula o posee dimensiones incorrectas.
     */
    public void setHorarioDeseado(boolean[][] horarioDeseado) {
        validarDimensionesMatriz(horarioDeseado);
        this.horarioDeseado = copiarMatriz(horarioDeseado);
    }

    /**
     * Modifica la preferencia de un bloque horario específico.
     *
     * @param dia índice del día (0–4)
     * @param bloque índice del bloque (0–5)
     * @param solicitado {@code true} para marcar el bloque como deseado
     * @throws IllegalArgumentException si los índices están fuera de rango
     */
    public void setBloqueSolicitado(int dia, int bloque, boolean solicitado) {
        validarIndices(dia, bloque);
        horarioDeseado[dia][bloque] = solicitado;
    }

    /**
     * Modifica el estado del ciclo de vida de la solicitud.
     *
     * @param estado Nuevo estado a registrar.
     * @throws IllegalArgumentException Si el estado es nulo.
     */
    public void setEstado(EstadoSolicitud estado) {
        if (estado == null)
            throw new IllegalArgumentException("El estado de la solicitud no puede ser nulo.");
        this.estado = estado;
    }

    /**
     * Evalúa si la solicitud se encuentra pendiente de procesamiento.
     *
     * @return true si el estado actual es PENDIENTE.
     */
    public boolean isPendiente() {
        return estado == EstadoSolicitud.PENDIENTE;
    }

    /**
     * Obtiene el conteo total de todos los bloques marcados como disponibles por el estudiante.
     *
     * @return Cantidad total de celdas en verdadero en la matriz de horario.
     */
    public int contarBloquesDeseados() {
        int count = 0;
        for (boolean[] dia : horarioDeseado)
            for (boolean bloque : dia)
                if (bloque) count++;
        return count;
    }

    /**
     * Valida los parámetros obligatorios del constructor.
     */
    private static void validarParametrosObligatorios(String id, String asunto,
                                                       Estudiante estudiante) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("El id de la solicitud es obligatorio.");
        if (asunto == null || asunto.isBlank())
            throw new IllegalArgumentException("El asunto de la solicitud es obligatorio.");
        if (estudiante == null)
            throw new IllegalArgumentException("El estudiante de la solicitud no puede ser nulo.");
    }

    /**
     * Valida que los índices de día y bloque estén dentro del rango definido
     * por {@link ConstantesHorario}.
     */
    private static void validarIndices(int dia, int bloque) {
        if (dia < 0 || dia >= ConstantesHorario.DIAS)
            throw new IllegalArgumentException(String.format(
                "Índice de día fuera de rango: %d (rango válido: 0–%d).",
                dia, ConstantesHorario.DIAS - 1));
        if (bloque < 0 || bloque >= ConstantesHorario.BLOQUES)
            throw new IllegalArgumentException(String.format(
                "Índice de bloque fuera de rango: %d (rango válido: 0–%d).",
                bloque, ConstantesHorario.BLOQUES - 1));
    }

    /**
     * Valida que la estructura bidimensional cumpla con la cantidad de filas y columnas requeridas.
     */
    private static void validarDimensionesMatriz(boolean[][] m) {
        if (m == null)
            throw new IllegalArgumentException(
                "La matriz de horario deseado no puede ser nula.");
        if (m.length != ConstantesHorario.DIAS)
            throw new IllegalArgumentException(String.format(
                "La matriz debe tener %d filas (días). Recibida: %d.",
                ConstantesHorario.DIAS, m.length));
        for (int i = 0; i < m.length; i++) {
            if (m[i] == null || m[i].length != ConstantesHorario.BLOQUES)
                throw new IllegalArgumentException(String.format(
                    "La fila %d debe tener %d columnas (bloques).",
                    i, ConstantesHorario.BLOQUES));
        }
    }

    /**
     * Realiza una copia profunda de una matriz {@code boolean[5][6]}.
     */
    private static boolean[][] copiarMatriz(boolean[][] original) {
        boolean[][] copia = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        for (int i = 0; i < ConstantesHorario.DIAS; i++)
            System.arraycopy(original[i], 0, copia[i], 0, ConstantesHorario.BLOQUES);
        return copia;
    }

    /**
     * Dos instancias de {@code Solicitud} son iguales si comparten el mismo
     * {@code id}.
     *
     * @param obj objeto a comparar
     * @return {@code true} si ambos objetos representan la misma solicitud
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Solicitud)) return false;
        Solicitud otra = (Solicitud) obj;
        return id.equals(otra.id);
    }

    /**
     * El código hash se basa exclusivamente en el {@code id} de la solicitud.
     *
     * @return código hash consistente con {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representación legible de la solicitud, útil para depuración y logs.
     *
     * @return cadena con los campos más relevantes de la solicitud
     */
    @Override
    public String toString() {
        return String.format(
            "Solicitud{id='%s', asunto='%s', estudiante='%s', estado=%s, "
            + "bloquesDeseados=%d, creada=%s}",
            id, asunto, estudiante.getNombre(), estado,
            contarBloquesDeseados(), fechaCreacion);
    }
}
