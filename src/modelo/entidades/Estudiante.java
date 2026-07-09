package modelo.entidades;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa a un estudiante registrado en el Sistema de Reservas de Clases Particulares.
 *
 * Es una entidad central del flujo del sistema: genera solicitudes de tutoría
 * que luego se procesan para agendar las reservas con los tutores.
 *
 * Invariantes de la clase:
 * - El nombre, la carrera y el correo son obligatorios (no nulos ni vacíos).
 * - El semestre debe estar estrictamente dentro del rango permitido (SEMESTRE_MIN a SEMESTRE_MAX).
 * - El ID se autogenera en la construcción y es único e inmutable.
 */
public class Estudiante {

    /** Semestre mínimo válido para cualquier estudiante del sistema. */
    public static final int SEMESTRE_MIN = 1;

    /** Semestre máximo válido para cualquier estudiante del sistema. */
    public static final int SEMESTRE_MAX = 12;

    /**
     * Identificador único del estudiante, generado automáticamente como UUID
     * al momento de la construcción. Inmutable durante toda la vida del objeto.
     */
    private final String id;

    /** Nombre completo del estudiante */
    private String nombre;

    /**
     * Carrera universitaria del estudiante
     */
    private String carrera;

    /**
     * Semestre académico actual del estudiante (debe estar entre 1 y 12).
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
     * Puede ser {@code null} si no se ha asignado foto.
     */
    private String fotoPath;

    /**
     * Crea un nuevo Estudiante y autogenera su identificador único.
     *
     * @param nombre Nombre completo; no puede ser nulo ni estar en blanco.
     * @param carrera Carrera universitaria; no puede ser nula ni estar en blanco.
     * @param semestre Semestre académico actual; debe estar entre 1 y 12.
     * @param correo Correo electrónico; no puede ser nulo ni estar en blanco.
     * @param comentario Nota del estudiante; si llega nulo se normaliza a texto vacío.
     * @param fotoPath Ruta del archivo de la foto; puede ser nula.
     * @throws IllegalArgumentException Si algún campo obligatorio es inválido o el semestre está fuera de rango.
     */
    public Estudiante(String nombre, String carrera, int semestre, String correo, String comentario, String fotoPath) {
        validarNombreCarreraCorreo(nombre, carrera, correo);
        validarSemestre(semestre);

        this.id = UUID.randomUUID().toString();
        this.nombre = nombre.strip();
        this.carrera = carrera.strip();
        this.semestre = semestre;
        this.correo = correo.strip();
        this.comentario = (comentario != null) ? comentario.strip() : "";
        this.fotoPath = fotoPath;
    }

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
     * @param semestre nuevo semestre; debe estar en el rango de 1 a 12.
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

    /**
     * Valida que los tres campos de texto obligatorios sean no nulos y no en
     * blanco.
     *
     * @param nombre  nombre a validar
     * @param carrera carrera a validar
     * @param correo  correo a validar
     * @throws IllegalArgumentException si alguno de los parámetros es inválido
     */
    private static void validarNombreCarreraCorreo(String nombre, String carrera, String correo) {
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

    /**
     * Compara la igualdad de dos estudiantes basándose únicamente en su ID único.
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
