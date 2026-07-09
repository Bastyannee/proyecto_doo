package modelo.entidades;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa a un tutor registrado en el Sistema de Reservas de Clases Particulares.
 *
 * Un Tutor posee un horario de disponibilidad semanal modelado como una matriz
 * bidimensional de booleanos, donde las filas representan los días de la semana
 * (Lunes a Viernes) y las columnas corresponden a los bloques horarios del día.
 * Un valor verdadero indica disponibilidad en ese slot, mientras que falso significa
 * que no está disponible o ya fue asignado.
 *
 * La clase aplica copia defensiva profunda en cada lectura y escritura de la matriz
 * para asegurar el encapsulamiento del estado interno frente a modificaciones externas.
 *
 * Invariantes de la clase:
 * - El nombre y la materia nunca pueden ser nulos ni estar vacíos.
 * - La matriz de disponibilidad siempre mantiene las dimensiones de ConstantesHorario.
 * - La capacidad máxima de estudiantes debe ser estrictamente mayor a cero.
 * - La tarifa por hora debe ser mayor o igual a cero.
 * - El ID único es inmutable durante todo el ciclo de vida del objeto.
 */
public class Tutor {

    /**
     * Capacidad máxima de estudiantes por defecto cuando el administrador no
     * especifica un límite al registrar un tutor.
     */
    public static final int MAX_ESTUDIANTES_DEFAULT = 5;

    /** Tarifa mínima permitida (sin cargo). */
    public static final double TARIFA_MINIMA = 0.0;

    /**
     * Identificador único del tutor, generado automáticamente como UUID
     * en la construcción. Inmutable.
     */
    private final String id;

    /** Nombre completo del tutor */
    private String nombre;

    /**
     * Descripción de la especialidad del tutor, visible en su perfil público.
     * (p. ej., "Docente certificada en Álgebra Lineal con 8 años de experiencia
     * en preparación universitaria").
     */
    private String descripcion;

    /**
     * Materia principal que imparte el tutor en el sistema
     * (p. ej., "Cálculo I", "Inglés B2", "Estadística Inferencial").
     */
    private String materia;

    /**
     * Afinidad o área de enfoque del tutor, utilizada por
     * {@code BusquedaPorAfinidad} para emparejar tutores con carreras o
     * perfiles de estudiantes compatibles
     * (p. ej., "Ciencias Exactas", "Humanidades", "Negocios").
     */
    private String afinidad;

    /**
     * Ruta del archivo correspondiente a la foto de perfil del tutor. Puede ser nula.
     */
    private String fotoPath;

    /**
     * Matriz de disponibilidad semanal donde true representa un bloque libre para agendar.
     */
    private boolean[][] disponibilidad;

    /**
     * Número máximo de estudiantes que el tutor puede atender simultáneamente
     * en su materia. Debe ser mayor a 0.
     */
    private int maxEstudiantes;

    /**
     * Tarifa por hora de tutoría expresada en la moneda local (CLP).
     * Debe ser mayor o igual a {@value #TARIFA_MINIMA}.
     */
    private double tarifa;

    /**
     * Crea una instancia de Tutor con todos sus atributos explícitamente definidos.
     *
     * @param nombre Nombre completo; no puede ser nulo ni estar en blanco.
     * @param descripcion Perfil o especialidad; no puede ser nula.
     * @param materia Asignatura principal; no puede ser nula ni estar en blanco.
     * @param afinidad Área de afinidad; si es nula se normaliza a texto vacío.
     * @param fotoPath Ruta de la imagen de perfil; puede ser nula.
     * @param disponibilidad Matriz de disponibilidad semanal; si es nula se genera libre por defecto.
     * @param maxEstudiantes Capacidad máxima de alumnos; debe ser mayor a 0.
     * @param tarifa Tarifa horaria; debe ser mayor o igual a la tarifa mínima.
     * @throws IllegalArgumentException Si alguno de los parámetros obligatorios no cumple con las restricciones de la clase.
     */
    public Tutor(String nombre, String descripcion, String materia, String afinidad, String fotoPath, boolean[][] disponibilidad, int maxEstudiantes, double tarifa) {

        validarCamposObligatorios(nombre, descripcion, materia, maxEstudiantes, tarifa);

        this.id = UUID.randomUUID().toString();
        this.nombre = nombre.strip();
        this.descripcion = descripcion.strip();
        this.materia = materia.strip();
        this.afinidad = (afinidad != null) ? afinidad.strip() : "";
        this.fotoPath = fotoPath;
        this.disponibilidad = (disponibilidad != null)
                              ? copiarMatriz(disponibilidad)
                              : matrizLibre();
        this.maxEstudiantes = maxEstudiantes;
        this.tarifa = tarifa;
    }

    /**
     * Constructor simplificado que inicializa la disponibilidad completamente libre,
     * la capacidad por defecto y la tarifa mínima permitida.
     *
     * @param nombre Nombre completo; no puede ser nulo ni estar en blanco.
     * @param descripcion Perfil o especialidad; no puede ser nula.
     * @param materia Asignatura principal; no puede ser nula ni estar en blanco.
     * @param afinidad Área de afinidad académica; puede ser nula.
     * @param fotoPath Ruta de la imagen de perfil; puede ser nula.
     */
    public Tutor(String nombre, String descripcion, String materia, String afinidad, String fotoPath) {
        this(nombre, descripcion, materia, afinidad, fotoPath, null, MAX_ESTUDIANTES_DEFAULT, TARIFA_MINIMA);
    }

    /**
     * Retorna el identificador único e inmutable del tutor.
     *
     * @return UUID en formato {@code String}
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna el nombre completo del tutor.
     *
     * @return nombre; nunca {@code null} ni en blanco
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Retorna la descripción/especialidad del tutor.
     *
     * @return descripción de la especialidad
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Retorna la materia principal que imparte el tutor.
     *
     * @return nombre de la materia; nunca {@code null} ni en blanco
     */
    public String getMateria() {
        return materia;
    }

    /**
     * Retorna el área de afinidad del tutor, utilizada por
     * {@code BusquedaPorAfinidad} para encontrar coincidencias con el perfil
     * del estudiante.
     *
     * @return área de afinidad; cadena vacía si no se definió
     */
    public String getAfinidad() {
        return afinidad;
    }

    /**
     * Retorna la ruta al archivo de imagen de perfil del tutor.
     *
     * @return ruta de la foto, o {@code null} si no se asignó ninguna
     */
    public String getFotoPath() {
        return fotoPath;
    }

    /**
     * Retorna una copia defensiva profunda de la matriz de disponibilidad.
     * Modificar el arreglo devuelto no altera la agenda interna del tutor.
     *
     * @return Copia independiente de la estructura boolean[DIAS][BLOQUES].
     */
    public boolean[][] getDisponibilidad() {
        return copiarMatriz(disponibilidad);
    }

    /**
     * Consulta el estado de disponibilidad en un bloque y día determinados.
     *
     * @param dia Índice del día de la semana (0 a 4).
     * @param bloque Índice del bloque horario del día (0 a 5).
     * @return true si el tutor se encuentra disponible en ese espacio.
     * @throws IllegalArgumentException Si los índices se encuentran fuera de los rangos de la grilla.
     */
    public boolean isDisponible(int dia, int bloque) {
        validarIndices(dia, bloque);
        return disponibilidad[dia][bloque];
    }

    /**
     * Retorna la capacidad máxima de estudiantes simultáneos para este tutor.
     *
     * @return número entero positivo
     */
    public int getMaxEstudiantes() {
        return maxEstudiantes;
    }

    /**
     * Retorna la tarifa por hora de tutoría.
     *
     * @return tarifa en moneda local; siempre ≥ {@value #TARIFA_MINIMA}
     */
    public double getTarifa() {
        return tarifa;
    }

    /**
     * Actualiza el nombre completo del tutor.
     *
     * @param nombre Nuevo nombre; no puede ser nulo ni estar en blanco.
     * @throws IllegalArgumentException Si el nombre provisto es inválido.
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del tutor no puede ser nulo ni en blanco.");
        this.nombre = nombre.strip();
    }

    /**
     * Actualiza la descripción del perfil del tutor.
     *
     * @param descripcion Nueva descripción; no puede ser nula.
     * @throws IllegalArgumentException Si la descripción recibida es nula.
     */
    public void setDescripcion(String descripcion) {
        if (descripcion == null)
            throw new IllegalArgumentException("La descripción no puede ser nula.");
        this.descripcion = descripcion.strip();
    }

    /**
     * Actualiza la materia que imparte el tutor.
     *
     * @param materia nueva materia; no puede ser {@code null} ni en blanco
     * @throws IllegalArgumentException si {@code materia} es inválida
     */
    public void setMateria(String materia) {
        if (materia == null || materia.isBlank())
            throw new IllegalArgumentException("La materia no puede ser nula ni en blanco.");
        this.materia = materia.strip();
    }

    /**
     * Actualiza el área de afinidad del tutor.
     *
     * @param afinidad nueva afinidad; si es nula se normaliza a cadena vacía
     */
    public void setAfinidad(String afinidad) {
        this.afinidad = (afinidad != null) ? afinidad.strip() : "";
    }

    /**
     * Actualiza la ruta al archivo de imagen de perfil.
     *
     * @param fotoPath nueva ruta; puede ser {@code null}
     */
    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    /**
     * Reemplaza la matriz de disponibilidad completa por medio de una copia defensiva profunda.
     *
     * @param disponibilidad Nueva matriz de disponibilidad semanal.
     * @throws IllegalArgumentException Si la matriz provista es nula o cuenta con dimensiones erróneas.
     */
    public void setDisponibilidad(boolean[][] disponibilidad) {
        validarDimensionesMatriz(disponibilidad);
        this.disponibilidad = copiarMatriz(disponibilidad);
    }

    /**
     * Modifica de manera individual el estado de disponibilidad de un bloque específico.
     *
     * @param dia Índice del día de la semana (0 a 4).
     * @param bloque Índice del bloque horario (0 a 5).
     * @param disponible true para habilitar el bloque, false para deshabilitarlo o reservarlo.
     * @throws IllegalArgumentException Si los índices proporcionados exceden los límites de la grilla.
     */
    public void setDisponibilidadBloque(int dia, int bloque, boolean disponible) {
        validarIndices(dia, bloque);
        disponibilidad[dia][bloque] = disponible;
    }

    /**
     * Actualiza la capacidad máxima de estudiantes del tutor.
     *
     * @param maxEstudiantes nueva capacidad; debe ser mayor a 0
     * @throws IllegalArgumentException si {@code maxEstudiantes} es ≤ 0
     */
    public void setMaxEstudiantes(int maxEstudiantes) {
        if (maxEstudiantes <= 0)
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a 0.");
        this.maxEstudiantes = maxEstudiantes;
    }

    /**
     * Actualiza la tarifa por hora del tutor.
     *
     * @param tarifa nueva tarifa; debe ser ≥ {@value #TARIFA_MINIMA}
     * @throws IllegalArgumentException si {@code tarifa} es negativa
     */
    public void setTarifa(double tarifa) {
        if (tarifa < TARIFA_MINIMA)
            throw new IllegalArgumentException(
                "La tarifa no puede ser negativa. Valor recibido: " + tarifa);
        this.tarifa = tarifa;
    }

    /**
     * Cuantifica el número total de bloques marcados como disponibles en la semana.
     *
     * @return Total de celdas en verdadero dentro de la matriz de horarios.
     */
    public int contarBloquesDisponibles() {
        int count = 0;
        for (boolean[] dia : disponibilidad)
            for (boolean bloque : dia)
                if (bloque) count++;
        return count;
    }

    /**
     * Evalúa si el tutor cuenta con al menos un bloque de tiempo disponible en su agenda.
     *
     * @return true si el conteo de bloques disponibles es mayor a cero.
     */
    public boolean tieneDisponibilidad() {
        return contarBloquesDisponibles() > 0;
    }

    /**
     * Valida las restricciones fundamentales de los campos requeridos en la construcción.
     */
    private static void validarCamposObligatorios(String nombre, String descripcion, String materia, int maxEstudiantes, double tarifa) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del tutor es obligatorio.");
        if (descripcion == null)
            throw new IllegalArgumentException("La descripción del tutor no puede ser nula.");
        if (materia == null || materia.isBlank())
            throw new IllegalArgumentException("La materia del tutor es obligatoria.");
        if (maxEstudiantes <= 0)
            throw new IllegalArgumentException("maxEstudiantes debe ser mayor a 0.");
        if (tarifa < TARIFA_MINIMA)
            throw new IllegalArgumentException("La tarifa no puede ser negativa.");
    }

    /**
     * Comprueba que los índices suministrados correspondan a los límites de días y bloques del sistema.
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
     * Valida que la matriz no sea {@code null} y tenga las dimensiones correctas.
     */
    private static void validarDimensionesMatriz(boolean[][] m) {
        if (m == null)
            throw new IllegalArgumentException(
                "La matriz de disponibilidad no puede ser nula.");
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
     *
     * Se usa {@link System#arraycopy} por su eficiencia con arreglos
     * primitivos, evitando la copia superficial que realizaría {@code clone()}
     * sobre la dimensión exterior.
     *
     * @param original matriz fuente; debe tener las dimensiones correctas
     * @return nueva instancia de matriz con los mismos valores
     */
    private static boolean[][] copiarMatriz(boolean[][] original) {
        boolean[][] copia = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        for (int i = 0; i < ConstantesHorario.DIAS; i++)
            System.arraycopy(original[i], 0, copia[i], 0, ConstantesHorario.BLOQUES);
        return copia;
    }

    /**
     * Crea una matriz con todos los bloques marcados como disponibles
     * ({@code true}).
     *
     * @return nueva matriz {@code boolean[5][6]} completamente libre
     */
    private static boolean[][] matrizLibre() {
        boolean[][] m = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        for (boolean[] fila : m)
            java.util.Arrays.fill(fila, true);
        return m;
    }

    /**
     * Compara la igualdad de dos tutores basándose de manera única en su ID inmutable.
     *
     * @param obj objeto a comparar
     * @return {@code true} si ambos representan al mismo tutor
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tutor)) return false;
        Tutor otro = (Tutor) obj;
        return id.equals(otro.id);
    }

    /**
     * El código hash se basa exclusivamente en el {@code id} del tutor.
     *
     * @return código hash consistente con {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representación legible del tutor, útil para depuración y logs.
     *
     * @return cadena con los campos más relevantes del tutor
     */
    @Override
    public String toString() {
        return String.format(
            "Tutor{id='%s', nombre='%s', materia='%s', maxEstudiantes=%d, "
            + "tarifa=%.2f, bloquesLibres=%d}",
            id, nombre, materia, maxEstudiantes, tarifa, contarBloquesDisponibles());
    }
}
