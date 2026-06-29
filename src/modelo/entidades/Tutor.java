package modelo.entidades;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa a un tutor registrado en el Sistema de Reservas de Clases
 * Particulares.
 *
 * <p>Un {@code Tutor} posee un horario de disponibilidad semanal modelado como
 * una matriz {@code boolean[}{@value ConstantesHorario#DIAS}{@code ]
 * [}{@value ConstantesHorario#BLOQUES}{@code ]}, donde:</p>
 * <ul>
 *   <li><b>Filas (0–4):</b> días de la semana (Lunes a Viernes).</li>
 *   <li><b>Columnas (0–5):</b> bloques horarios del día
 *       (08:00–09:30 hasta 16:00–17:30).</li>
 *   <li>{@code true} = tutor disponible en ese bloque.</li>
 *   <li>{@code false} = tutor no disponible o ya asignado.</li>
 * </ul>
 *
 * <p>La clase aplica <em>copia defensiva</em> en cada lectura y escritura de
 * la matriz para preservar el principio de encapsulamiento; la vista y el
 * controlador nunca obtienen una referencia directa al arreglo interno.</p>
 *
 * <p>Invariantes de clase:</p>
 * <ul>
 *   <li>{@code nombre} y {@code materia} nunca son {@code null} ni en blanco.</li>
 *   <li>{@code disponibilidad} nunca es {@code null} y siempre mide
 *       {@value ConstantesHorario#DIAS} × {@value ConstantesHorario#BLOQUES}.</li>
 *   <li>{@code maxEstudiantes} siempre es mayor a 0.</li>
 *   <li>{@code tarifa} siempre es mayor o igual a 0.</li>
 *   <li>{@code id} es inmutable durante toda la vida del objeto.</li>
 * </ul>
 *
 * @author  Bastián
 * @version 1.0
 * @see     ConstantesHorario
 * @see     Reserva
 * @see     Solicitud
 */
public class Tutor {

    // -------------------------------------------------------------------------
    // Constantes de dominio
    // -------------------------------------------------------------------------

    /**
     * Capacidad máxima de estudiantes por defecto cuando el administrador no
     * especifica un límite al registrar un tutor.
     */
    public static final int MAX_ESTUDIANTES_DEFAULT = 5;

    /** Tarifa mínima permitida (sin cargo). */
    public static final double TARIFA_MINIMA = 0.0;

    // -------------------------------------------------------------------------
    // Atributos
    // -------------------------------------------------------------------------

    /**
     * Identificador único del tutor, generado automáticamente como UUID
     * en la construcción. Inmutable.
     */
    private final String id;

    /** Nombre completo del tutor (p. ej., "Ana María López"). */
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
     * Ruta relativa o absoluta al archivo de imagen de perfil del tutor
     * (p. ej., {@code "assets/fotos/ana_lopez.png"}).
     * Puede ser {@code null} si no se ha asignado foto.
     */
    private String fotoPath;

    /**
     * Matriz de disponibilidad semanal del tutor.
     *
     * <pre>
     *   disponibilidad[dia][bloque] == true  →  disponible
     *   disponibilidad[dia][bloque] == false →  no disponible
     * </pre>
     *
     * <p>El acceso externo se realiza siempre mediante copias defensivas.</p>
     */
    private boolean[][] disponibilidad;

    /**
     * Número máximo de estudiantes que el tutor puede atender simultáneamente
     * en su materia.  Debe ser mayor a 0.
     */
    private int maxEstudiantes;

    /**
     * Tarifa por hora de tutoría expresada en la moneda local (CLP por defecto).
     * Debe ser mayor o igual a {@value #TARIFA_MINIMA}.
     */
    private double tarifa;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------

    /**
     * Crea un {@code Tutor} con todos sus atributos explícitamente definidos.
     *
     * @param nombre         nombre completo; no puede ser {@code null} ni en blanco
     * @param descripcion    descripción/especialidad; no puede ser {@code null}
     * @param materia        materia que imparte; no puede ser {@code null} ni en blanco
     * @param afinidad       área de afinidad para búsquedas; puede ser {@code null}
     * @param fotoPath       ruta al archivo de imagen; puede ser {@code null}
     * @param disponibilidad matriz {@code boolean[5][6]} de disponibilidad;
     *                       si es {@code null} se genera una matriz completamente
     *                       libre (todos {@code true})
     * @param maxEstudiantes capacidad máxima; debe ser mayor a 0
     * @param tarifa         tarifa por hora; debe ser ≥ {@value #TARIFA_MINIMA}
     * @throws IllegalArgumentException si algún parámetro obligatorio es inválido
     */
    public Tutor(String nombre, String descripcion, String materia,
                 String afinidad, String fotoPath,
                 boolean[][] disponibilidad, int maxEstudiantes, double tarifa) {

        validarCamposObligatorios(nombre, descripcion, materia, maxEstudiantes, tarifa);

        this.id             = UUID.randomUUID().toString();
        this.nombre         = nombre.strip();
        this.descripcion    = descripcion.strip();
        this.materia        = materia.strip();
        this.afinidad       = (afinidad != null) ? afinidad.strip() : "";
        this.fotoPath       = fotoPath;
        this.disponibilidad = (disponibilidad != null)
                              ? copiarMatriz(disponibilidad)
                              : matrizLibre();
        this.maxEstudiantes = maxEstudiantes;
        this.tarifa         = tarifa;
    }

    /**
     * Constructor de conveniencia con disponibilidad totalmente libre y valores
     * por defecto para capacidad ({@value #MAX_ESTUDIANTES_DEFAULT}) y tarifa (0).
     *
     * <p>Ideal para poblar rápidamente el {@code GestorDatos} con Mock Data.</p>
     *
     * @param nombre      nombre completo; no puede ser {@code null} ni en blanco
     * @param descripcion descripción/especialidad; no puede ser {@code null}
     * @param materia     materia que imparte; no puede ser {@code null} ni en blanco
     * @param afinidad    área de afinidad; puede ser {@code null}
     * @param fotoPath    ruta al archivo de imagen; puede ser {@code null}
     */
    public Tutor(String nombre, String descripcion, String materia,
                 String afinidad, String fotoPath) {
        this(nombre, descripcion, materia, afinidad, fotoPath,
             null, MAX_ESTUDIANTES_DEFAULT, TARIFA_MINIMA);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

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
     * Retorna una <strong>copia defensiva profunda</strong> de la matriz de
     * disponibilidad semanal.
     *
     * <p>Modificar el arreglo retornado <em>no</em> altera el estado interno
     * del tutor.  Para modificar bloques individuales use
     * {@link #setDisponibilidadBloque(int, int, boolean)}.</p>
     *
     * @return copia profunda de {@code boolean[5][6]}
     */
    public boolean[][] getDisponibilidad() {
        return copiarMatriz(disponibilidad);
    }

    /**
     * Consulta la disponibilidad en un bloque específico de la semana.
     *
     * @param dia    índice del día (0 = Lunes … 4 = Viernes)
     * @param bloque índice del bloque horario (0–5)
     * @return {@code true} si el tutor está disponible en ese slot
     * @throws IllegalArgumentException si los índices están fuera de rango
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

    // -------------------------------------------------------------------------
    // Setters con validación
    // -------------------------------------------------------------------------

    /**
     * Actualiza el nombre completo del tutor.
     *
     * @param nombre nuevo nombre; no puede ser {@code null} ni en blanco
     * @throws IllegalArgumentException si {@code nombre} es inválido
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del tutor no puede ser nulo ni en blanco.");
        this.nombre = nombre.strip();
    }

    /**
     * Actualiza la descripción/especialidad del tutor.
     *
     * @param descripcion nueva descripción; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code descripcion} es {@code null}
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
     * @param afinidad nueva afinidad; {@code null} se normaliza a cadena vacía
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
     * Reemplaza toda la matriz de disponibilidad por una <em>copia defensiva</em>
     * de la proporcionada.
     *
     * @param disponibilidad nueva matriz {@code boolean[5][6]}; no puede ser
     *                       {@code null} ni tener dimensiones incorrectas
     * @throws IllegalArgumentException si la matriz es {@code null} o tiene
     *                                  dimensiones distintas de
     *                                  {@value ConstantesHorario#DIAS} ×
     *                                  {@value ConstantesHorario#BLOQUES}
     */
    public void setDisponibilidad(boolean[][] disponibilidad) {
        validarDimensionesMatriz(disponibilidad);
        this.disponibilidad = copiarMatriz(disponibilidad);
    }

    /**
     * Modifica la disponibilidad de un bloque horario específico.
     *
     * <p>Este método es el mecanismo preferido para que el administrador
     * actualice la agenda del tutor sin exponer la matriz completa.</p>
     *
     * @param dia        índice del día (0 = Lunes … 4 = Viernes)
     * @param bloque     índice del bloque horario (0–5)
     * @param disponible {@code true} para marcar el bloque como disponible;
     *                   {@code false} para bloquearlo
     * @throws IllegalArgumentException si los índices están fuera de rango
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

    // -------------------------------------------------------------------------
    // Métodos de utilidad
    // -------------------------------------------------------------------------

    /**
     * Cuenta el total de bloques horarios disponibles en la semana actual.
     *
     * @return número de slots marcados como {@code true} (entre 0 y
     *         {@value ConstantesHorario#DIAS} × {@value ConstantesHorario#BLOQUES})
     */
    public int contarBloquesDisponibles() {
        int count = 0;
        for (boolean[] dia : disponibilidad)
            for (boolean bloque : dia)
                if (bloque) count++;
        return count;
    }

    /**
     * Indica si el tutor tiene al menos un bloque libre en la semana.
     *
     * @return {@code true} si {@link #contarBloquesDisponibles()} > 0
     */
    public boolean tieneDisponibilidad() {
        return contarBloquesDisponibles() > 0;
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Valida los campos obligatorios del tutor en la construcción.
     */
    private static void validarCamposObligatorios(String nombre, String descripcion,
                                                   String materia, int maxEstudiantes,
                                                   double tarifa) {
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
     * <p>Se usa {@link System#arraycopy} por su eficiencia con arreglos
     * primitivos, evitando la copia superficial que realizaría {@code clone()}
     * sobre la dimensión exterior.</p>
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

    // -------------------------------------------------------------------------
    // Sobreescritura de Object
    // -------------------------------------------------------------------------

    /**
     * Dos instancias de {@code Tutor} son iguales si comparten el mismo
     * {@code id}.
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
