package modelo.entidades;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa a un estudiante registrado en el Sistema de Reservas de Clases
 * Particulares.
 *
 * <p>Un {@code Estudiante} es la entidad central del flujo del sistema: genera
 * {@link Solicitud}es de tutoría que el administrador procesa para crear
 * {@link Reserva}s con un {@link Tutor} disponible.</p>
 *
 * <p>Invariantes de clase:</p>
 * <ul>
 *   <li>{@code nombre}, {@code carrera} y {@code correo} nunca son {@code null}
 *       ni cadenas en blanco.</li>
 *   <li>{@code semestre} siempre está en el rango
 *       [{@value #SEMESTRE_MIN}, {@value #SEMESTRE_MAX}].</li>
 *   <li>{@code id} es inmutable y único dentro de la sesión.</li>
 * </ul>
 *
 * <p>Ejemplo de construcción:</p>
 * <pre>{@code
 * Estudiante e = new Estudiante(
 *     "Pedro Soto",
 *     "Ingeniería Comercial",
 *     3,
 *     "pedro.soto@mail.com",
 *     "Necesito ayuda con derivadas",
 *     "assets/fotos/pedro.png"
 * );
 * }</pre>
 *
 * @author  Bastián
 * @version 1.0
 * @see     Solicitud
 * @see     Reserva
 */
public class Estudiante {

    // -------------------------------------------------------------------------
    // Constantes de dominio
    // -------------------------------------------------------------------------

    /** Semestre mínimo válido para cualquier estudiante del sistema. */
    public static final int SEMESTRE_MIN = 1;

    /** Semestre máximo válido para cualquier estudiante del sistema. */
    public static final int SEMESTRE_MAX = 12;

    // -------------------------------------------------------------------------
    // Atributos
    // -------------------------------------------------------------------------

    /**
     * Identificador único del estudiante, generado automáticamente como UUID
     * al momento de la construcción. Inmutable durante toda la vida del objeto.
     */
    private final String id;

    /** Nombre completo del estudiante (p. ej., "Pedro Soto García"). */
    private String nombre;

    /**
     * Carrera universitaria del estudiante (p. ej., "Ingeniería Comercial",
     * "Psicología", "Derecho").
     */
    private String carrera;

    /**
     * Semestre académico actual del estudiante.
     * Debe estar en el rango [{@value #SEMESTRE_MIN}, {@value #SEMESTRE_MAX}].
     */
    private int semestre;

    /**
     * Dirección de correo electrónico de contacto del estudiante.
     * Se utiliza para comunicaciones administrativas.
     */
    private String correo;

    /**
     * Comentario libre que el estudiante adjunta a su perfil, describiendo
     * sus necesidades de apoyo, nivel de avance u observaciones relevantes
     * para el tutor asignado.
     * Nunca es {@code null}; puede ser cadena vacía.
     */
    private String comentario;

    /**
     * Ruta relativa o absoluta al archivo de imagen de perfil del estudiante
     * dentro del sistema de archivos de la aplicación
     * (p. ej., {@code "assets/fotos/pedro.png"}).
     * Puede ser {@code null} si no se ha asignado foto.
     */
    private String fotoPath;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Crea un nuevo {@code Estudiante} con todos sus atributos principales.
     *
     * <p>El {@code id} es generado automáticamente; no se proporciona como
     * parámetro.</p>
     *
     * @param nombre     nombre completo del estudiante; no puede ser
     *                   {@code null} ni en blanco
     * @param carrera    carrera universitaria; no puede ser {@code null} ni en
     *                   blanco
     * @param semestre   semestre actual; debe estar en
     *                   [{@value #SEMESTRE_MIN}, {@value #SEMESTRE_MAX}]
     * @param correo     dirección de correo electrónico; no puede ser
     *                   {@code null} ni en blanco
     * @param comentario comentario libre del estudiante; {@code null} se
     *                   normaliza a cadena vacía
     * @param fotoPath   ruta al archivo de imagen; puede ser {@code null}
     * @throws IllegalArgumentException si {@code nombre}, {@code carrera} o
     *                                  {@code correo} son nulos/en blanco, o si
     *                                  {@code semestre} está fuera de rango
     */
    public Estudiante(String nombre, String carrera, int semestre,
                      String correo, String comentario, String fotoPath) {

        validarNombreCarreraCorreo(nombre, carrera, correo);
        validarSemestre(semestre);

        this.id         = UUID.randomUUID().toString();
        this.nombre     = nombre.strip();
        this.carrera    = carrera.strip();
        this.semestre   = semestre;
        this.correo     = correo.strip();
        this.comentario = (comentario != null) ? comentario.strip() : "";
        this.fotoPath   = fotoPath;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Retorna el identificador único e inmutable del estudiante.
     *
     * @return UUID en formato {@code String}
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna el nombre completo del estudiante.
     *
     * @return nombre del estudiante; nunca {@code null} ni en blanco
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Retorna la carrera universitaria del estudiante.
     *
     * @return nombre de la carrera; nunca {@code null} ni en blanco
     */
    public String getCarrera() {
        return carrera;
    }

    /**
     * Retorna el semestre académico actual del estudiante.
     *
     * @return entero entre {@value #SEMESTRE_MIN} y {@value #SEMESTRE_MAX}
     */
    public int getSemestre() {
        return semestre;
    }

    /**
     * Retorna la dirección de correo electrónico del estudiante.
     *
     * @return correo electrónico; nunca {@code null} ni en blanco
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Retorna el comentario libre del estudiante sobre sus necesidades de apoyo.
     *
     * @return comentario; cadena vacía si no se proporcionó ninguno
     */
    public String getComentario() {
        return comentario;
    }

    /**
     * Retorna la ruta al archivo de imagen de perfil del estudiante.
     *
     * @return ruta de la foto de perfil, o {@code null} si no se asignó ninguna
     */
    public String getFotoPath() {
        return fotoPath;
    }

    // -------------------------------------------------------------------------
    // Setters con validación
    // -------------------------------------------------------------------------

    /**
     * Actualiza el nombre completo del estudiante.
     *
     * @param nombre nuevo nombre; no puede ser {@code null} ni en blanco
     * @throws IllegalArgumentException si {@code nombre} es inválido
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del estudiante no puede ser nulo ni en blanco.");
        this.nombre = nombre.strip();
    }

    /**
     * Actualiza la carrera universitaria del estudiante.
     *
     * @param carrera nueva carrera; no puede ser {@code null} ni en blanco
     * @throws IllegalArgumentException si {@code carrera} es inválida
     */
    public void setCarrera(String carrera) {
        if (carrera == null || carrera.isBlank())
            throw new IllegalArgumentException("La carrera no puede ser nula ni en blanco.");
        this.carrera = carrera.strip();
    }

    /**
     * Actualiza el semestre académico actual del estudiante.
     *
     * @param semestre nuevo semestre; debe estar en
     *                 [{@value #SEMESTRE_MIN}, {@value #SEMESTRE_MAX}]
     * @throws IllegalArgumentException si el semestre está fuera de rango
     */
    public void setSemestre(int semestre) {
        validarSemestre(semestre);
        this.semestre = semestre;
    }

    /**
     * Actualiza la dirección de correo electrónico del estudiante.
     *
     * @param correo nuevo correo; no puede ser {@code null} ni en blanco
     * @throws IllegalArgumentException si {@code correo} es inválido
     */
    public void setCorreo(String correo) {
        if (correo == null || correo.isBlank())
            throw new IllegalArgumentException("El correo no puede ser nulo ni en blanco.");
        this.correo = correo.strip();
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
     * Actualiza la ruta al archivo de imagen de perfil.
     *
     * @param fotoPath nueva ruta; puede ser {@code null} para quitar la foto
     */
    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares privados de validación
    // -------------------------------------------------------------------------

    /**
     * Valida que los tres campos de texto obligatorios sean no nulos y no en
     * blanco.
     *
     * @param nombre  nombre a validar
     * @param carrera carrera a validar
     * @param correo  correo a validar
     * @throws IllegalArgumentException si alguno de los parámetros es inválido
     */
    private static void validarNombreCarreraCorreo(String nombre,
                                                   String carrera,
                                                   String correo) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del estudiante es obligatorio.");
        if (carrera == null || carrera.isBlank())
            throw new IllegalArgumentException("La carrera del estudiante es obligatoria.");
        if (correo == null || correo.isBlank())
            throw new IllegalArgumentException("El correo del estudiante es obligatorio.");
    }

    /**
     * Valida que el semestre esté dentro del rango permitido.
     *
     * @param semestre semestre a validar
     * @throws IllegalArgumentException si el semestre está fuera de rango
     */
    private static void validarSemestre(int semestre) {
        if (semestre < SEMESTRE_MIN || semestre > SEMESTRE_MAX)
            throw new IllegalArgumentException(String.format(
                "El semestre debe estar entre %d y %d. Valor recibido: %d.",
                SEMESTRE_MIN, SEMESTRE_MAX, semestre));
    }

    // -------------------------------------------------------------------------
    // Sobreescritura de Object
    // -------------------------------------------------------------------------

    /**
     * Dos instancias de {@code Estudiante} son iguales si y sólo si comparten
     * el mismo {@code id}.
     *
     * @param obj objeto a comparar
     * @return {@code true} si ambos objetos representan al mismo estudiante
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Estudiante)) return false;
        Estudiante otro = (Estudiante) obj;
        return id.equals(otro.id);
    }

    /**
     * El código hash se basa exclusivamente en el {@code id} del estudiante.
     *
     * @return código hash consistente con {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representación legible del estudiante, útil para depuración y logs.
     *
     * @return cadena con los campos más relevantes del estudiante
     */
    @Override
    public String toString() {
        return String.format(
            "Estudiante{id='%s', nombre='%s', carrera='%s', semestre=%d, correo='%s'}",
            id, nombre, carrera, semestre, correo);
    }
}
