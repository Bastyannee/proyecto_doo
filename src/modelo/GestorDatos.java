package modelo;

import modelo.entidades.Estudiante;
import modelo.entidades.Reserva;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Punto de acceso central y único a los datos del Sistema de Reservas de
 * Clases Particulares.
 *
 * <p>{@code GestorDatos} implementa el patrón de diseño <strong>Singleton</strong>:
 * dado que el sistema no utiliza una base de datos externa, esta clase actúa
 * como la "Base de Datos en Memoria" de la aplicación, manteniendo las cuatro
 * listas centrales del dominio:</p>
 * <ul>
 *   <li>{@link Tutor} — perfiles de tutores registrados.</li>
 *   <li>{@link Estudiante} — perfiles de estudiantes registrados.</li>
 *   <li>{@link Solicitud} — solicitudes de tutoría pendientes, archivadas o
 *       convertidas.</li>
 *   <li>{@link Reserva} — clases agendadas, canceladas o archivadas.</li>
 * </ul>
 *
 * <p>Todas las capas del sistema (controlador y vista) deben obtener y
 * modificar estos datos exclusivamente a través de
 * {@link #getInstancia()}; ninguna otra clase debe instanciar
 * {@code GestorDatos} directamente, ya que el constructor es privado.</p>
 *
 * <p>La inicialización de la instancia es <em>perezosa</em> (lazy) y
 * thread-safe mediante doble verificación de bloqueo
 * (<em>double-checked locking</em>) sobre un campo {@code volatile}.</p>
 *
 * <p>Ejemplo de uso típico desde el controlador:</p>
 * <pre>{@code
 * GestorDatos gestor = GestorDatos.getInstancia();
 * gestor.inicializarDatosEstaticos();
 *
 * List<Tutor> disponibles = gestor.getTutores();
 * gestor.guardarReserva(nuevaReserva);
 * }</pre>
 *
 * @author  Bastián
 * @version 1.0
 * @see     Tutor
 * @see     Estudiante
 * @see     Solicitud
 * @see     Reserva
 * @see     ConflictoHorarioException
 */
public class GestorDatos {

    // -------------------------------------------------------------------------
    // Instancia Singleton
    // -------------------------------------------------------------------------

    /**
     * Única instancia de {@code GestorDatos} en toda la aplicación.
     *
     * <p>Declarada {@code volatile} para garantizar la visibilidad correcta
     * entre hilos durante la inicialización perezosa con doble verificación
     * de bloqueo (necesario porque la vista Swing corre en el Event Dispatch
     * Thread y eventuales tareas en segundo plano podrían acceder al gestor
     * concurrentemente).</p>
     */
    private static volatile GestorDatos instancia;

    // -------------------------------------------------------------------------
    // Listas centrales (estado en memoria)
    // -------------------------------------------------------------------------

    /** Lista de todos los tutores registrados en el sistema. */
    private final List<Tutor> tutores;

    /** Lista de todos los estudiantes registrados en el sistema. */
    private final List<Estudiante> estudiantes;

    /** Lista de todas las solicitudes de tutoría registradas en el sistema. */
    private final List<Solicitud> solicitudes;

    /** Lista de todas las reservas (clases agendadas) del sistema. */
    private final List<Reserva> reservas;

    /**
     * Bandera que indica si {@link #inicializarDatosEstaticos()} ya fue
     * ejecutado, para evitar duplicar el Mock Data ante llamadas repetidas.
     */
    private boolean datosInicializados;

    // -------------------------------------------------------------------------
    // Constructor privado (Singleton)
    // -------------------------------------------------------------------------

    /**
     * Constructor privado que impide la instanciación externa, garantizando
     * que {@code GestorDatos} tenga una única instancia en toda la
     * aplicación.
     *
     * <p>Inicializa las cuatro listas centrales vacías; el Mock Data se
     * carga explícitamente mediante {@link #inicializarDatosEstaticos()}.</p>
     */
    private GestorDatos() {
        this.tutores            = new ArrayList<>();
        this.estudiantes        = new ArrayList<>();
        this.solicitudes        = new ArrayList<>();
        this.reservas           = new ArrayList<>();
        this.datosInicializados = false;
    }

    /**
     * Retorna la única instancia de {@code GestorDatos} en la aplicación,
     * creándola en el primer llamado (inicialización perezosa).
     *
     * <p>Implementa doble verificación de bloqueo para minimizar el costo de
     * sincronización en llamados posteriores al primero.</p>
     *
     * @return instancia única y compartida de {@code GestorDatos}
     */
    public static GestorDatos getInstancia() {
        GestorDatos resultado = instancia;
        if (resultado == null) {
            synchronized (GestorDatos.class) {
                resultado = instancia;
                if (resultado == null) {
                    instancia = resultado = new GestorDatos();
                }
            }
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // Inicialización de Mock Data
    // -------------------------------------------------------------------------

    /**
     * Puebla el gestor con los datos estáticos (Mock Data) usados durante el
     * desarrollo y las demostraciones del sistema: 4 estudiantes y 3 tutores,
     * tomados de los bocetos de interfaz del proyecto.
     *
     * <p>Estudiantes creados:</p>
     * <ul>
     *   <li>Pedro Pablo González — Ingeniería Comercial, 3er semestre.</li>
     *   <li>María José Fernández — Psicología, 5to semestre.</li>
     *   <li>Juan Ignacio Soto — Ingeniería Civil Industrial, 2do semestre.</li>
     *   <li>Laura Beatriz Muñoz — Derecho, 4to semestre.</li>
     * </ul>
     *
     * <p>Tutores creados:</p>
     * <ul>
     *   <li>Jong Si-yun — Estadística Inferencial (afinidad Ciencias Exactas).</li>
     *   <li>Carla Andrea Reyes — Inglés B2 (afinidad Humanidades).</li>
     *   <li>Roberto Esteban Díaz — Cálculo I (afinidad Ciencias Exactas).</li>
     * </ul>
     *
     * <p>Este método es <strong>idempotente</strong>: si ya fue ejecutado
     * anteriormente sobre esta instancia, las llamadas subsiguientes no
     * tienen efecto y no duplican los registros. Para forzar una recarga
     * completa, use {@link #reiniciar()} antes de volver a invocar este
     * método.</p>
     */
    public void inicializarDatosEstaticos() {
    if (datosInicializados) return;
    crearEstudiantesMock();
    crearTutoresMock();
    crearSolicitudesMock();
    datosInicializados = true;
    }

    /**
     * Crea y registra los 4 estudiantes de Mock Data.
     */
    private void crearEstudiantesMock() {
        registrarEstudiante(new Estudiante(
            "Pedro Pablo González",
            "Ingeniería Comercial",
            3,
            "pedro.gonzalez@mail.com",
            "Necesita apoyo en macroeconomía antes del próximo certamen.",
            "assets/fotos/pedro.png"
        ));

        registrarEstudiante(new Estudiante(
            "María José Fernández",
            "Psicología",
            5,
            "maria.fernandez@mail.com",
            "Busca reforzar estadística aplicada para su seminario de título.",
            "assets/fotos/maria.png"
        ));

        registrarEstudiante(new Estudiante(
            "Juan Ignacio Soto",
            "Ingeniería Civil Industrial",
            2,
            "juan.soto@mail.com",
            "Requiere ayuda con álgebra lineal y sistemas de ecuaciones.",
            "assets/fotos/juan.png"
        ));

        registrarEstudiante(new Estudiante(
            "Laura Beatriz Muñoz",
            "Derecho",
            4,
            "laura.munoz@mail.com",
            "Quiere practicar redacción de informes jurídicos en inglés.",
            "assets/fotos/laura.png"
        ));
    }

    private void crearSolicitudesMock() {
    // Obtenemos los estudiantes recién creados (asumiendo el orden de inserción)
    Estudiante pedro = estudiantes.get(0); // Pedro Jiménez
    Estudiante maria = estudiantes.get(1); // María
    
    // Matriz de horario deseado para Pedro (Desea Lunes, Miércoles y Viernes en las mañanas)
    boolean[][] horarioPedro = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
    horarioPedro[0][0] = true; horarioPedro[0][1] = true; horarioPedro[0][2] = true; // Lunes
    horarioPedro[2][0] = true; horarioPedro[2][1] = true; horarioPedro[2][2] = true; // Miércoles
    horarioPedro[4][0] = true; horarioPedro[4][1] = true; horarioPedro[4][2] = true; // Viernes
    
    registrarSolicitud(new Solicitud(
        "S-001", 
        "Necesito tutoría de cálculo 1", 
        "Busco clases sobre la materia de derivadas y regla de la cadena.", 
        horarioPedro, 
        pedro
    ));

    // Crear al menos la solicitud vacía de María para que se vea en el panel
    registrarSolicitud(new Solicitud(
        "S-002", 
        "Refuerzo de Química", 
        "Necesito ayuda con la tabla periódica...", 
        null, 
        maria
    ));
}
    /**
     * Crea y registra los 3 tutores de Mock Data, cada uno con una matriz
     * de disponibilidad {@code boolean[5][6]} distinta para permitir probar
     * los algoritmos de {@code BusquedaPorHorario} y {@code BusquedaPorAfinidad}.
     */
    private void crearTutoresMock() {

        // Jong Si-yun: disponible en bloques matutinos de Lunes, Miércoles y Viernes.
        boolean[][] horarioJong = {
            { true,  true,  false, false, false, false }, // Lunes
            { false, false, false, false, false, false }, // Martes
            { true,  true,  false, false, false, false }, // Miércoles
            { false, false, false, false, false, false }, // Jueves
            { true,  true,  false, false, false, false }  // Viernes
        };
        registrarTutor(new Tutor(
            "Jong Si-yun",
            "Profesor especializado en Matemáticas Aplicadas y Estadística, "
            + "con experiencia en preparación para certámenes universitarios.",
            "Estadística Inferencial",
            "Ciencias Exactas",
            "assets/fotos/jong_siyun.png",
            horarioJong,
            4,
            15000.0
        ));

        // Carla Andrea Reyes: disponible en bloques de tarde, Martes a Jueves.
        boolean[][] horarioCarla = {
            { false, false, false, false, false, false }, // Lunes
            { false, false, false, true,  true,  false }, // Martes
            { false, false, false, true,  true,  false }, // Miércoles
            { false, false, false, true,  true,  false }, // Jueves
            { false, false, false, false, false, false }  // Viernes
        };
        registrarTutor(new Tutor(
            "Carla Andrea Reyes",
            "Profesora de inglés certificada (TKT), enfocada en comprensión "
            + "lectora y redacción académica de nivel B2-C1.",
            "Inglés B2",
            "Humanidades",
            "assets/fotos/carla_reyes.png",
            horarioCarla,
            6,
            12000.0
        ));

        // Roberto Esteban Díaz: disponible en bloques variados, Lunes a Viernes.
        boolean[][] horarioRoberto = {
            { false, false, true,  true,  false, false }, // Lunes
            { false, false, true,  true,  false, false }, // Martes
            { false, false, false, false, false, false }, // Miércoles
            { false, false, true,  true,  false, false }, // Jueves
            { false, false, false, false, true,  true  }  // Viernes
        };
        registrarTutor(new Tutor(
            "Roberto Esteban Díaz",
            "Ingeniero Civil con 6 años de experiencia impartiendo tutorías "
            + "de matemáticas para primeros años de carreras de ingeniería.",
            "Cálculo I",
            "Ciencias Exactas",
            "assets/fotos/roberto_diaz.png",
            horarioRoberto,
            5,
            13500.0
        ));
    }

    // -------------------------------------------------------------------------
    // Operaciones sobre Tutores
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo tutor en el sistema.
     *
     * @param tutor tutor a registrar; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code tutor} es {@code null}
     */
    public void registrarTutor(Tutor tutor) {
        if (tutor == null)
            throw new IllegalArgumentException("El tutor a registrar no puede ser nulo.");
        tutores.add(tutor);
    }

    /**
     * Elimina un tutor del sistema.
     *
     * <p>Esta operación no valida si el tutor posee reservas activas; esa
     * regla de negocio corresponde a la capa de controlador antes de invocar
     * este método.</p>
     *
     * @param tutor tutor a eliminar
     * @return {@code true} si el tutor estaba registrado y fue eliminado
     */
    public boolean eliminarTutor(Tutor tutor) {
        return tutores.remove(tutor);
    }

    /**
     * Retorna una vista <strong>no modificable</strong> de la lista de
     * tutores registrados.
     *
     * <p>Para agregar o quitar tutores debe usarse
     * {@link #registrarTutor(Tutor)} o {@link #eliminarTutor(Tutor)}; intentar
     * modificar la lista retornada lanza {@link UnsupportedOperationException}.</p>
     *
     * @return lista no modificable de tutores; nunca {@code null}
     */
    public List<Tutor> getTutores() {
        return Collections.unmodifiableList(tutores);
    }

    /**
     * Busca un tutor por su identificador único.
     *
     * @param id identificador del tutor
     * @return {@link Optional} con el tutor encontrado, o vacío si no existe
     */
    public Optional<Tutor> buscarTutorPorId(String id) {
        return tutores.stream()
                       .filter(t -> t.getId().equals(id))
                       .findFirst();
    }

    // -------------------------------------------------------------------------
    // Operaciones sobre Estudiantes
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo estudiante en el sistema.
     *
     * @param estudiante estudiante a registrar; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code estudiante} es {@code null}
     */
    public void registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null)
            throw new IllegalArgumentException("El estudiante a registrar no puede ser nulo.");
        estudiantes.add(estudiante);
    }

    /**
     * Elimina un estudiante del sistema.
     *
     * @param estudiante estudiante a eliminar
     * @return {@code true} si el estudiante estaba registrado y fue eliminado
     */
    public boolean eliminarEstudiante(Estudiante estudiante) {
        return estudiantes.remove(estudiante);
    }

    /**
     * Retorna una vista <strong>no modificable</strong> de la lista de
     * estudiantes registrados.
     *
     * @return lista no modificable de estudiantes; nunca {@code null}
     */
    public List<Estudiante> getEstudiantes() {
        return Collections.unmodifiableList(estudiantes);
    }

    /**
     * Busca un estudiante por su identificador único.
     *
     * @param id identificador del estudiante
     * @return {@link Optional} con el estudiante encontrado, o vacío si no existe
     */
    public Optional<Estudiante> buscarEstudiantePorId(String id) {
        return estudiantes.stream()
                          .filter(e -> e.getId().equals(id))
                          .findFirst();
    }

    // -------------------------------------------------------------------------
    // Operaciones sobre Solicitudes
    // -------------------------------------------------------------------------

    /**
     * Registra una nueva solicitud de tutoría en el sistema.
     *
     * <p>Las solicitudes recién registradas inician en estado
     * {@link Solicitud.EstadoSolicitud#PENDIENTE} (definido por el propio
     * constructor de {@link Solicitud}).</p>
     *
     * @param solicitud solicitud a registrar; no puede ser {@code null}
     * @throws IllegalArgumentException si {@code solicitud} es {@code null}
     */
    public void registrarSolicitud(Solicitud solicitud) {
        if (solicitud == null)
            throw new IllegalArgumentException("La solicitud a registrar no puede ser nula.");
        solicitudes.add(solicitud);
    }

    /**
     * Elimina una solicitud del sistema de forma permanente.
     *
     * <p>Para descartar una solicitud sin perder su historial, prefiera
     * cambiar su estado a {@link Solicitud.EstadoSolicitud#ARCHIVADA} mediante
     * {@code ComandoArchivar} en lugar de eliminarla con este método.</p>
     *
     * @param solicitud solicitud a eliminar
     * @return {@code true} si la solicitud estaba registrada y fue eliminada
     */
    public boolean eliminarSolicitud(Solicitud solicitud) {
        return solicitudes.remove(solicitud);
    }

    /**
     * Retorna una vista <strong>no modificable</strong> de la lista completa
     * de solicitudes (en cualquier estado).
     *
     * @return lista no modificable de solicitudes; nunca {@code null}
     */
    public List<Solicitud> getSolicitudes() {
        return Collections.unmodifiableList(solicitudes);
    }

    /**
     * Retorna únicamente las solicitudes en estado
     * {@link Solicitud.EstadoSolicitud#PENDIENTE}, tal como se listan en el
     * panel de bienvenida del administrador.
     *
     * @return nueva lista (modificable) con las solicitudes pendientes;
     *         vacía si no hay ninguna
     */
    public List<Solicitud> getSolicitudesPendientes() {
        List<Solicitud> pendientes = new ArrayList<>();
        for (Solicitud s : solicitudes) {
            if (s.isPendiente()) {
                pendientes.add(s);
            }
        }
        return pendientes;
    }

    /**
     * Busca una solicitud por su identificador único.
     *
     * @param id identificador de la solicitud
     * @return {@link Optional} con la solicitud encontrada, o vacío si no existe
     */
    public Optional<Solicitud> buscarSolicitudPorId(String id) {
        return solicitudes.stream()
                          .filter(s -> s.getId().equals(id))
                          .findFirst();
    }

    // -------------------------------------------------------------------------
    // Operaciones sobre Reservas
    // -------------------------------------------------------------------------

    /**
     * Registra una nueva reserva en el sistema, previniendo conflictos
     * horarios.
     *
     * <p>Antes de agregar la reserva, este método recorre todas las reservas
     * activas y verifica, mediante {@link Reserva#conflictaCon(Reserva)}, que
     * ninguna comparta el mismo tutor, día y bloque horario con la reserva
     * entrante.</p>
     *
     * <p>Este es el método central que {@code ComandoAgendar} invoca al
     * mover una {@link Solicitud} a una {@code Reserva} confirmada.</p>
     *
     * @param reserva reserva a registrar; no puede ser {@code null}
     * @throws IllegalArgumentException   si {@code reserva} es {@code null}
     * @throws ConflictoHorarioException  si la reserva colisiona con otra
     *                                    reserva activa del mismo tutor en el
     *                                    mismo bloque horario
     */
    public void guardarReserva(Reserva reserva) {
        if (reserva == null)
            throw new IllegalArgumentException("La reserva a registrar no puede ser nula.");

        for (Reserva existente : reservas) {
            if (reserva.conflictaCon(existente)) {
                throw new ConflictoHorarioException(String.format(
                    "Conflicto de horario: el tutor '%s' ya tiene una clase activa "
                    + "el %s en el bloque %s.",
                    reserva.getTutor().getNombre(),
                    reserva.getNombreDia(),
                    reserva.getHora()));
            }
        }

        reservas.add(reserva);
    }

    /**
     * Elimina una reserva del sistema de forma permanente.
     *
     * <p>Para anular una clase sin perder su historial, prefiera cambiar su
     * estado a {@link Reserva.EstadoReserva#CANCELADA} mediante
     * {@code reserva.setEstado(EstadoReserva.CANCELADA)} en lugar de
     * eliminarla con este método. Una reserva cancelada libera
     * automáticamente el bloque horario, ya que
     * {@link Reserva#conflictaCon(Reserva)} ignora reservas inactivas.</p>
     *
     * @param reserva reserva a eliminar
     * @return {@code true} si la reserva estaba registrada y fue eliminada
     */
    public boolean eliminarReserva(Reserva reserva) {
        return reservas.remove(reserva);
    }

    /**
     * Retorna una vista <strong>no modificable</strong> de la lista completa
     * de reservas (en cualquier estado).
     *
     * @return lista no modificable de reservas; nunca {@code null}
     */
    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    /**
     * Retorna las reservas activas (no canceladas) registradas para una
     * fecha específica, base del "calendario" general del sistema.
     *
     * @param fecha fecha a consultar; no puede ser {@code null}
     * @return nueva lista (modificable) con las reservas activas de esa
     *         fecha; vacía si no hay ninguna
     * @throws IllegalArgumentException si {@code fecha} es {@code null}
     */
    public List<Reserva> getReservasPorFecha(LocalDate fecha) {
        if (fecha == null)
            throw new IllegalArgumentException("La fecha de consulta no puede ser nula.");

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.isActiva() && r.getFecha().equals(fecha)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * Retorna la vista filtrada del calendario para un tutor específico:
     * todas sus reservas activas, ordenadas implícitamente por orden de
     * inserción.
     *
     * @param tutor tutor cuyas reservas se desean consultar; no puede ser
     *              {@code null}
     * @return nueva lista (modificable) con las reservas activas del tutor;
     *         vacía si no tiene ninguna
     * @throws IllegalArgumentException si {@code tutor} es {@code null}
     */
    public List<Reserva> getReservasPorTutor(Tutor tutor) {
        if (tutor == null)
            throw new IllegalArgumentException("El tutor de consulta no puede ser nulo.");

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.isActiva() && r.getTutor().equals(tutor)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * Retorna la vista filtrada del calendario para un estudiante específico:
     * todas sus reservas activas.
     *
     * @param estudiante estudiante cuyas reservas se desean consultar; no
     *                   puede ser {@code null}
     * @return nueva lista (modificable) con las reservas activas del
     *         estudiante; vacía si no tiene ninguna
     * @throws IllegalArgumentException si {@code estudiante} es {@code null}
     */
    public List<Reserva> getReservasPorEstudiante(Estudiante estudiante) {
        if (estudiante == null)
            throw new IllegalArgumentException("El estudiante de consulta no puede ser nulo.");

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.isActiva() && r.getEstudiante().equals(estudiante)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * Busca una reserva por su identificador único.
     *
     * @param id identificador de la reserva
     * @return {@link Optional} con la reserva encontrada, o vacío si no existe
     */
    public Optional<Reserva> buscarReservaPorId(String id) {
        return reservas.stream()
                       .filter(r -> r.getId().equals(id))
                       .findFirst();
    }

    // -------------------------------------------------------------------------
    // Utilidades de ciclo de vida del gestor
    // -------------------------------------------------------------------------

    /**
     * Vacía por completo las cuatro listas centrales y reinicia la bandera de
     * inicialización, permitiendo recargar el Mock Data desde cero.
     *
     * <p>Pensado principalmente para pruebas unitarias y para el modo de
     * desarrollo, donde puede ser necesario reiniciar el estado del sistema
     * sin reiniciar la aplicación completa.</p>
     */
    public void reiniciar() {
        tutores.clear();
        estudiantes.clear();
        solicitudes.clear();
        reservas.clear();
        datosInicializados = false;
    }

    /**
     * Indica si {@link #inicializarDatosEstaticos()} ya fue ejecutado sobre
     * esta instancia.
     *
     * @return {@code true} si el Mock Data ya fue cargado
     */
    public boolean isDatosInicializados() {
        return datosInicializados;
    }
}
