package modelo.entidades;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa una solicitud de tutoría que un {@link Estudiante} genera y el
 * administrador registra en el Sistema de Reservas de Clases Particulares.
 *
 * <p>Una {@code Solicitud} encapsula:</p>
 * <ul>
 *   <li>El asunto o materia de apoyo requerida.</li>
 *   <li>Un comentario libre del estudiante con detalles adicionales.</li>
 *   <li>Una matriz {@code boolean[5][6]} que expresa los bloques horarios
 *       de preferencia del estudiante, compatible con la disponibilidad de
 *       {@link Tutor}.</li>
 * </ul>
 *
 * <p>El administrador utiliza este objeto como entrada del patrón Strategy
 * ({@code BusquedaPorHorario}, {@code BusquedaPorAfinidad}) para encontrar
 * tutores compatibles y, posteriormente, como insumo del patrón Command
 * ({@code ComandoAgendar}) para transformar la solicitud en una
 * {@link Reserva}.</p>
 *
 * <p>Ciclo de vida del estado:</p>
 * <pre>
 *   PENDIENTE ──► CONVERTIDA  (via ComandoAgendar)
 *             └─► ARCHIVADA   (via ComandoArchivar)
 * </pre>
 *
 * <p>Invariantes de clase:</p>
 * <ul>
 *   <li>{@code id}, {@code asunto} y {@code estudiante} nunca son
 *       {@code null}.</li>
 *   <li>{@code horarioDeseado} siempre tiene dimensiones
 *       {@value ConstantesHorario#DIAS} × {@value ConstantesHorario#BLOQUES}.</li>
 *   <li>{@code estado} nunca es {@code null}.</li>
 *   <li>{@code fechaCreacion} es inmutable y refleja el instante de registro.</li>
 * </ul>
 *
 * @author  Bastián
 * @version 1.0
 * @see     Estudiante
 * @see     Reserva
 * @see     ConstantesHorario
 */
public class Solicitud {

    // -------------------------------------------------------------------------
    // Enumeración de estado
    // -------------------------------------------------------------------------

    /**
     * Estados posibles durante el ciclo de vida de una {@code Solicitud}.
     *
     * <p>La transición de estado la gestiona exclusivamente el administrador
     * a través de los comandos del controlador.</p>
     */
    public enum EstadoSolicitud {

        /**
         * La solicitud fue registrada y aún no ha sido procesada por el
         * administrador.  Es el estado inicial de toda solicitud nueva.
         */
        PENDIENTE,

        /**
         * La solicitud fue descartada por el administrador sin generar una
         * reserva (p. ej., por incompatibilidad de horarios o falta de tutores
         * disponibles).
         */
        ARCHIVADA,

        /**
         * La solicitud fue procesada exitosamente y se creó una {@link Reserva}
         * correspondiente a través del {@code ComandoAgendar}.
         */
        CONVERTIDA
    }

    // -------------------------------------------------------------------------
    // Atributos
    // -------------------------------------------------------------------------

    /**
     * Identificador único de la solicitud.
     * Asignado por el administrador o generado por {@code GestorDatos} al
     * momento del registro.  Inmutable.
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
     * Matriz {@code boolean[5][6]} de bloques horarios deseados por el
     * estudiante.
     *
     * <pre>
     *   horarioDeseado[dia][bloque] == true  → el estudiante puede en ese slot
     *   horarioDeseado[dia][bloque] == false → el estudiante NO está disponible
     * </pre>
     *
     * <p>Se compara con {@code Tutor.disponibilidad} en
     * {@code BusquedaPorHorario} para encontrar coincidencias.</p>
     */
    private boolean[][] horarioDeseado;

    /**
     * Estudiante que origina la solicitud.  Referencia inmutable.
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

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Crea una nueva {@code Solicitud} en estado {@link EstadoSolicitud#PENDIENTE}.
     *
     * @param id             identificador único; no puede ser {@code null} ni en blanco
     * @param asunto         asunto de la solicitud; no puede ser {@code null} ni en blanco
     * @param comentario     comentario del estudiante; {@code null} se normaliza a
     *                       cadena vacía
     * @param horarioDeseado matriz {@code boolean[5][6]} con los bloques preferidos;
     *                       si es {@code null} se crea una matriz de ceros
     *                       (sin preferencia declarada)
     * @param estudiante     estudiante que genera la solicitud; no puede ser
     *                       {@code null}
     * @throws IllegalArgumentException si {@code id}, {@code asunto} o
     *                                  {@code estudiante} son inválidos, o si
     *                                  {@code horarioDeseado} tiene dimensiones
     *                                  incorrectas
     */
    public Solicitud(String id, String asunto, String comentario,
                     boolean[][] horarioDeseado, Estudiante estudiante) {

        validarParametrosObligatorios(id, asunto, estudiante);
        if (horarioDeseado != null) validarDimensionesMatriz(horarioDeseado);

        this.id             = id.strip();
        this.asunto         = asunto.strip();
        this.comentario     = (comentario != null) ? comentario.strip() : "";
        this.horarioDeseado = (horarioDeseado != null)
                              ? copiarMatriz(horarioDeseado)
                              : new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        this.estudiante     = estudiante;
        this.estado         = EstadoSolicitud.PENDIENTE;
        this.fechaCreacion  = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

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
     * Retorna una <strong>copia defensiva profunda</strong> de la matriz de
     * horario deseado.
     *
     * <p>Modificar el arreglo retornado <em>no</em> afecta al estado interno
     * de la solicitud.  Para actualizar bloques individuales use
     * {@link #setBloqueSolicitado(int, int, boolean)}.</p>
     *
     * @return copia profunda de {@code boolean[5][6]}
     */
    public boolean[][] getHorarioDeseado() {
        return copiarMatriz(horarioDeseado);
    }

    /**
     * Indica si el estudiante marcó como deseable un bloque horario específico.
     *
     * @param dia    índice del día (0 = Lunes … 4 = Viernes)
     * @param bloque índice del bloque horario (0–5)
     * @return {@code true} si el estudiante desea clase en ese slot
     * @throws IllegalArgumentException si los índices están fuera de rango
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

    // -------------------------------------------------------------------------
    // Setters con validación
    // -------------------------------------------------------------------------

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
     * Reemplaza toda la matriz de horario deseado por una copia defensiva de
     * la proporcionada.
     *
     * @param horarioDeseado nueva matriz {@code boolean[5][6]}; no puede ser
     *                       {@code null} ni tener dimensiones incorrectas
     * @throws IllegalArgumentException si la matriz es inválida
     */
    public void setHorarioDeseado(boolean[][] horarioDeseado) {
        validarDimensionesMatriz(horarioDeseado);
        this.horarioDeseado = copiarMatriz(horarioDeseado);
    }

    /**
     * Modifica la preferencia de un bloque horario específico.
     *
     * @param dia        índice del día (0–4)
     * @param bloque     índice del bloque (0–5)
     * @param solicitado {@code true} para marcar el bloque como deseado
     * @throws IllegalArgumentException si los índices están fuera de rango
     */
    public void setBloqueSolicitado(int dia, int bloque, boolean solicitado) {
        validarIndices(dia, bloque);
        horarioDeseado[dia][bloque] = solicitado;
    }

    /**
     * Actualiza el estado de la solicitud en su ciclo de vida.
     *
     * <p>Normalmente esta operación la realiza el controlador a través de los
     * comandos {@code ComandoAgendar} y {@code ComandoArchivar}.</p>
     *
     * @param estado nuevo estado; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code estado} es {@code null}
     */
    public void setEstado(EstadoSolicitud estado) {
        if (estado == null)
            throw new IllegalArgumentException("El estado de la solicitud no puede ser nulo.");
        this.estado = estado;
    }

    // -------------------------------------------------------------------------
    // Métodos de utilidad
    // -------------------------------------------------------------------------

    /**
     * Indica si la solicitud aún puede ser procesada por el administrador.
     *
     * @return {@code true} si el estado es {@link EstadoSolicitud#PENDIENTE}
     */
    public boolean isPendiente() {
        return estado == EstadoSolicitud.PENDIENTE;
    }

    /**
     * Cuenta el número de bloques horarios que el estudiante marcó como
     * deseados.
     *
     * @return número de slots solicitados (entre 0 y
     *         {@value ConstantesHorario#DIAS} × {@value ConstantesHorario#BLOQUES})
     */
    public int contarBloquesDeseados() {
        int count = 0;
        for (boolean[] dia : horarioDeseado)
            for (boolean bloque : dia)
                if (bloque) count++;
        return count;
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares privados
    // -------------------------------------------------------------------------

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
     * Valida las dimensiones de la matriz recibida.
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

    // -------------------------------------------------------------------------
    // Sobreescritura de Object
    // -------------------------------------------------------------------------

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
