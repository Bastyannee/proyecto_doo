package modelo;

import modelo.entidades.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Punto de acceso central y único a los datos del Sistema de Reservas de Clases Particulares.
 *
 * GestorDatos implementa el patrón de diseño Singleton: dado que el sistema no utiliza una base
 * de datos externa, esta clase actúa como la "Base de Datos en Memoria" de la aplicación,
 * manteniendo las cuatro listas centrales del dominio:
 * - Tutor: perfiles de tutores registrados.
 * - Estudiante: perfiles de estudiantes registrados.
 * - Solicitud: solicitudes de tutoría pendientes, archivadas o convertidas.
 * - Reserva: clases agendadas, canceladas o archivadas.
 *
 * Todas las capas del sistema (controlador y vista) deben obtener y modificar estos datos
 * exclusivamente a través de getInstancia(); ninguna otra clase debe instanciar GestorDatos
 * directamente, ya que el constructor es privado.
 *
 * La inicialización de la instancia es perezosa (lazy) y thread-safe mediante doble verificación
 * de bloqueo (double-checked locking) sobre un campo volatile.
 *
 * Ejemplo de uso típico desde el controlador:
 *
 * GestorDatos gestor = GestorDatos.getInstancia();
 * gestor.inicializarDatosEstaticos();
 *
 * List<Tutor> disponibles = gestor.getTutores();
 * gestor.guardarReserva(nuevaReserva);
 */
public class GestorDatos {

    /**
     * Única instancia de GestorDatos en toda la aplicación.
     *
     * Declarada volatile para garantizar la visibilidad correcta entre hilos durante la
     * inicialización perezosa con doble verificación de bloqueo (necesario porque la vista Swing
     * corre en el Event Dispatch Thread y eventuales tareas en segundo plano podrían acceder al
     * gestor concurrentemente).
     */
    private static volatile GestorDatos instancia;

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

    /**
     * Constructor privado que impide la instanciación externa, garantizando que GestorDatos
     * tenga una única instancia en toda la aplicación.
     *
     * Inicializa las cuatro listas centrales vacías; el Mock Data se carga explícitamente
     * mediante inicializarDatosEstaticos().
     */
    private GestorDatos() {
        this.tutores = new ArrayList<>();
        this.estudiantes = new ArrayList<>();
        this.solicitudes = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.datosInicializados = false;
    }

    /**
     * Retorna la única instancia de GestorDatos en la aplicación, creándola en el primer
     * llamado (inicialización perezosa).
     *
     * Implementa doble verificación de bloqueo para minimizar el costo de sincronización en
     * llamados posteriores al primero.
     *
     * @return Instancia única y compartida de GestorDatos.
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

    /**
     * Puebla el gestor con los datos estáticos (Mock Data) usados durante el desarrollo y las
     * demostraciones del sistema: 4 estudiantes y 3 tutores, tomados de los bocetos de interfaz.
     *
     * Este método es idempotente: si ya fue ejecutado anteriormente sobre esta instancia, las
     * llamadas subsiguientes no tienen efecto y no duplican los registros. Para forzar una recarga
     * completa, use reiniciar() antes de volver a invocar este método.
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

    /**
     * Crea y registra las solicitudes iniciales para el Mock Data.
     */
    private void crearSolicitudesMock() {
    Estudiante pedro = estudiantes.get(0);
    Estudiante maria = estudiantes.get(1);
    Estudiante juan = estudiantes.get(2);
    Estudiante laura = estudiantes.get(3);

    boolean[][] horarioPedro = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
    horarioPedro[0][0] = true; horarioPedro[0][1] = true; horarioPedro[0][2] = true;
    horarioPedro[2][0] = true; horarioPedro[2][1] = true; horarioPedro[2][2] = true;
    horarioPedro[4][0] = true; horarioPedro[4][1] = true; horarioPedro[4][2] = true;
    
    registrarSolicitud(new Solicitud(
        "S-001", 
        "Necesito tutoría de cálculo 1", 
        "Busco clases sobre la materia de derivadas y regla de la cadena.", 
        horarioPedro, 
        pedro
    ));

    boolean[][] horarioMaria = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
    horarioMaria[1][3] = true; horarioMaria[1][4] = true;
    horarioMaria[3][3] = true; horarioMaria[3][4] = true;

    registrarSolicitud(new Solicitud(
        "S-002", 
        "Refuerzo de Estadística",
        "Necesito ayuda con regresión lineal para mi seminario de título.",
        horarioMaria,
        maria
    ));

        boolean[][] horarioJuan = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioJuan[0][2] = true; horarioJuan[0][3] = true;
        horarioJuan[1][2] = true; horarioJuan[1][3] = true;

        registrarSolicitud(new Solicitud(
                "S-003",
                "Álgebra Lineal y Sistemas de Ecuaciones",
                "Requiero apoyo con matrices y transformaciones lineales.",
                horarioJuan,
                juan
        ));

        boolean[][] horarioLaura = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioLaura[1][3] = true; horarioLaura[1][4] = true;
        horarioLaura[2][3] = true; horarioLaura[2][4] = true;
        horarioLaura[3][3] = true; horarioLaura[3][4] = true;

        registrarSolicitud(new Solicitud(
                "S-004",
                "Inglés B2 - Redacción Jurídica",
                "Quiero practicar redacción de informes jurídicos en inglés.",
                horarioLaura,
                laura
        ));
}
    /**
     * Crea y registra los 3 tutores de Mock Data, cada uno con una matriz de disponibilidad distinta.
     * La matriz tiene un orden desde el Lunes al Viernes.
     */
    private void crearTutoresMock() {

        boolean[][] horarioJong = {
            { true,  true,  false, false, false, false },
            { false, false, false, false, false, false },
            { true,  true,  false, false, false, false },
            { false, false, false, false, false, false },
            { true,  true,  false, false, false, false }
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

        boolean[][] horarioCarla = {
            { false, false, false, false, false, false },
            { false, false, false, true,  true,  false },
            { false, false, false, true,  true,  false },
            { false, false, false, true,  true,  false },
            { false, false, false, false, false, false }
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

        boolean[][] horarioRoberto = {
            { false, false, true,  true,  false, false },
            { false, false, true,  true,  false, false },
            { false, false, false, false, false, false },
            { false, false, true,  true,  false, false },
            { false, false, false, false, true,  true  }
        };
        registrarTutor(new Tutor(
            "Roberto Esteban Díaz",
            "Ingeniero Civil con 6 años de experiencia impartiendo tutorías "
            + "de matemáticas para primeros años de carreras de ingeniería.",
            "Cálculo I y II",
            "Ciencias Exactas",
            "assets/fotos/roberto_diaz.png",
            horarioRoberto,
            5,
            13500.0
        ));
    }

    /**
     * Registra un nuevo tutor en el sistema.
     *
     * @param tutor Tutor a registrar.
     * @throws IllegalArgumentException Si el tutor es nulo.
     */
    public void registrarTutor(Tutor tutor) {
        if (tutor == null)
            throw new IllegalArgumentException("El tutor a registrar no puede ser nulo.");
        tutores.add(tutor);
    }

    /**
     * Elimina un tutor del sistema.
     *
     * @param tutor Tutor a eliminar.
     * @return Verdadero si el tutor estaba registrado y fue eliminado correctamente.
     */
    public boolean eliminarTutor(Tutor tutor) {
        return tutores.remove(tutor);
    }

    /**
     * Retorna una vista no modificable de la lista de tutores registrados.
     *
     * @return Lista no modificable de tutores; nunca nula.
     */
    public List<Tutor> getTutores() {
        return Collections.unmodifiableList(tutores);
    }

    /**
     * Busca un tutor por su identificador único.
     *
     * @param id Identificador único del tutor.
     * @return Un Optional con el tutor encontrado, o vacío si no existe.
     */
    public Optional<Tutor> buscarTutorPorId(String id) {
        return tutores.stream()
                       .filter(t -> t.getId().equals(id))
                       .findFirst();
    }

    /**
     * Registra un nuevo estudiante en el sistema.
     *
     * @param estudiante Estudiante a registrar.
     * @throws IllegalArgumentException Si el estudiante es nulo.
     */
    public void registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null)
            throw new IllegalArgumentException("El estudiante a registrar no puede ser nulo.");
        estudiantes.add(estudiante);
    }

    /**
     * Elimina un estudiante del sistema.
     *
     * @param estudiante Estudiante a eliminar.
     * @return Verdadero si el estudiante estaba registrado y fue eliminado.
     */
    public boolean eliminarEstudiante(Estudiante estudiante) {
        return estudiantes.remove(estudiante);
    }

    /**
     * Retorna una vista no modificable de la lista de estudiantes registrados.
     *
     * @return Lista no modificable de estudiantes; nunca nula.
     */
    public List<Estudiante> getEstudiantes() {
        return Collections.unmodifiableList(estudiantes);
    }

    /**
     * Busca un estudiante por su identificador único.
     *
     * @param id Identificador único del estudiante.
     * @return Un Optional con el estudiante encontrado, o vacío si no existe.
     */
    public Optional<Estudiante> buscarEstudiantePorId(String id) {
        return estudiantes.stream()
                          .filter(e -> e.getId().equals(id))
                          .findFirst();
    }

    /**
     * Registra una nueva solicitud de tutoría en el sistema.
     *
     * @param solicitud Solicitud a registrar.
     * @throws IllegalArgumentException Si la solicitud es nula.
     */
    public void registrarSolicitud(Solicitud solicitud) {
        if (solicitud == null)
            throw new IllegalArgumentException("La solicitud a registrar no puede ser nula.");
        solicitudes.add(solicitud);
    }

    /**
     * Elimina una solicitud del sistema de forma permanente.
     *
     * @param solicitud Solicitud a eliminar.
     * @return Verdadero si la solicitud existía y fue eliminada.
     */
    public boolean eliminarSolicitud(Solicitud solicitud) {
        return solicitudes.remove(solicitud);
    }

    /**
     * Retorna una vista no modificable de la lista completa de solicitudes.
     *
     * @return Lista no modificable de solicitudes; nunca nula.
     */
    public List<Solicitud> getSolicitudes() {
        return Collections.unmodifiableList(solicitudes);
    }

    /**
     * Retorna únicamente las solicitudes que se encuentran en estado pendiente.
     *
     * @return Nueva lista modificable con las solicitudes pendientes.
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
     * @param id Identificador único de la solicitud.
     * @return Un Optional con la solicitud encontrada, o vacío si no existe.
     */
    public Optional<Solicitud> buscarSolicitudPorId(String id) {
        return solicitudes.stream()
                          .filter(s -> s.getId().equals(id))
                          .findFirst();
    }

    /**
     * Registra una nueva reserva en el sistema previniendo conflictos horarios de tutores
     * y asegurando que el bloque agendado coincida con la disponibilidad declarada por el
     * estudiante en su solicitud de origen.
     *
     * @param reserva Reserva a registrar.
     * @throws IllegalArgumentException  Si la reserva es nula.
     * @throws ConflictoHorarioException Si la reserva colisiona con otra reserva activa del mismo
     * tutor, o si el día/bloque elegido no fue marcado por el estudiante como disponible en su solicitud de origen.
     */
    public void guardarReserva(Reserva reserva) {
        if (reserva == null)
            throw new IllegalArgumentException("La reserva a registrar no puede ser nula.");

        // BLOQUE NUEVO — antes no existía esta validación
        if (!reserva.getSolicitudOrigen().isBloqueSolicitado(reserva.getDiaIndex(), reserva.getBloqueIndex())) {
            throw new ConflictoHorarioException(String.format(
                    "Conflicto de horario: el estudiante '%s' no indicó disponibilidad "
                            + "el %s en el bloque %s.",
                    reserva.getEstudiante().getNombre(),
                    reserva.getNombreDia(),
                    reserva.getHora()));
        }

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
     * @param reserva Reserva a eliminar.
     * @return Verdadero si la reserva existía y fue removida con éxito.
     */
    public boolean eliminarReserva(Reserva reserva) {
        return reservas.remove(reserva);
    }

    /**
     * Retorna una vista no modificable de la lista completa de reservas.
     *
     * @return Lista no modificable de reservas; nunca nula.
     */
    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    /**
     * Retorna las reservas activas asociadas a una fecha específica de calendario.
     *
     * @param fecha Fecha de consulta.
     * @return Nueva lista modificable con las reservas activas de esa fecha.
     * @throws IllegalArgumentException Si la fecha proporcionada es nula.
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
     * Retorna las reservas activas asignadas a un tutor específico.
     *
     * @param tutor Tutor cuyas reservas se desean consultar.
     * @return Nueva lista modificable con las reservas activas del tutor.
     * @throws IllegalArgumentException Si el tutor es nulo.
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
     * Retorna las reservas activas asignadas a un estudiante específico.
     *
     * @param estudiante Estudiante cuyas reservas se desean consultar.
     * @return Nueva lista modificable con las reservas activas del estudiante.
     * @throws IllegalArgumentException Si el estudiante es nulo.
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
     * @param id Identificador único de la reserva.
     * @return Un Optional con la reserva encontrada, o vacío si no existe.
     */
    public Optional<Reserva> buscarReservaPorId(String id) {
        return reservas.stream()
                       .filter(r -> r.getId().equals(id))
                       .findFirst();
    }

    /**
     * Vacía por completo las listas centrales y reinicia la bandera de inicialización.
     */
    public void reiniciar() {
        tutores.clear();
        estudiantes.clear();
        solicitudes.clear();
        reservas.clear();
        datosInicializados = false;
    }

    /**
     * Indica si los datos estáticos del Mock Data ya se cargaron en el sistema.
     *
     * @return Verdadero si los datos ya fueron inicializados.
     */
    public boolean isDatosInicializados() {
        return datosInicializados;
    }
}
