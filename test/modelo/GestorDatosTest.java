package test.modelo;

import modelo.ConflictoHorarioException;
import modelo.GestorDatos;
import modelo.entidades.Estudiante;
import modelo.entidades.Reserva;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para {@link GestorDatos}, el punto de acceso
 * central (patrón Singleton) a la "Base de Datos en Memoria" del Sistema de
 * Reservas de Clases Particulares.
 *
 * <h2>Consideración crítica de aislamiento entre pruebas</h2>
 * <p>Como {@link GestorDatos} es un <strong>Singleton</strong>, su única
 * instancia persiste en memoria durante toda la ejecución de la suite (e
 * incluso entre distintas clases de prueba dentro del mismo proceso de la
 * JVM). Si un test deja tutores, estudiantes, solicitudes o reservas
 * registrados y el siguiente test no parte de un estado limpio, los
 * resultados quedarían contaminados por datos de pruebas anteriores,
 * produciendo fallos intermitentes difíciles de depurar (falsos positivos o
 * negativos según el orden de ejecución).</p>
 *
 * <p>Por ello, {@link #setUp()} invoca obligatoriamente
 * {@code GestorDatos.getInstancia().reiniciar()} antes de <strong>cada</strong>
 * método de prueba, vaciando las cuatro listas centrales y reiniciando la
 * bandera {@code datosInicializados}. Esto garantiza que cada test se
 * ejecute sobre un gestor en un estado predecible, independientemente del
 * orden en que JUnit decida ejecutar los métodos.</p>
 *
 * @author  Bastián
 * @version 1.0
 * @see     GestorDatos
 * @see     ConflictoHorarioException
 */
@DisplayName("Suite de pruebas unitarias para el Singleton GestorDatos")
class GestorDatosTest {

    private GestorDatos gestor;

    /**
     * Obtiene la instancia única de {@link GestorDatos} y la reinicia por
     * completo antes de cada prueba.
     *
     * <p>Este método es la pieza central del aislamiento de la suite: sin la
     * llamada a {@code reiniciar()}, el estado en memoria dejado por un test
     * (tutores, estudiantes, solicitudes o reservas registrados) se filtraría
     * hacia el siguiente test, ya que ambos comparten literalmente el mismo
     * objeto en memoria por tratarse de un Singleton.</p>
     */
    @BeforeEach
    @DisplayName("Arrange común: obtiene la instancia Singleton y la reinicia para garantizar aislamiento entre pruebas")
    void setUp() {
        gestor = GestorDatos.getInstancia();
        gestor.reiniciar();
    }

    // -------------------------------------------------------------------------
    // Instancia única (Singleton)
    // -------------------------------------------------------------------------

    /**
     * Verifica la propiedad fundamental del patrón Singleton: dos variables
     * que invocan {@link GestorDatos#getInstancia()} en momentos distintos
     * deben apuntar exactamente a la misma dirección de memoria, no a dos
     * objetos distintos con el mismo contenido.
     *
     * <p>Se usa {@code assertSame} (comparación por referencia, {@code ==})
     * en lugar de {@code assertEquals} (comparación por igualdad lógica),
     * porque lo que se quiere demostrar es identidad de objeto, no
     * equivalencia de estado.</p>
     */
    @Test
    @DisplayName("getInstancia() retorna siempre la misma referencia en memoria")
    void getInstancia_retornaSiempreLaMismaReferenciaEnMemoria() {
        // Arrange / Act
        GestorDatos instanciaUno = GestorDatos.getInstancia();
        GestorDatos instanciaDos = GestorDatos.getInstancia();

        // Assert
        assertSame(instanciaUno, instanciaDos,
            "Ambas variables deben apuntar exactamente a la misma dirección de memoria: "
            + "GestorDatos es un Singleton con constructor privado, por lo que jamás debe "
            + "existir más de una instancia viva en la aplicación.");
    }

    // -------------------------------------------------------------------------
    // Mock Data / inicialización
    // -------------------------------------------------------------------------

    /**
     * Verifica que {@link GestorDatos#inicializarDatosEstaticos()} puebla
     * correctamente las listas centrales con el Mock Data definido para el
     * proyecto: 4 estudiantes, 3 tutores y al menos una solicitud de ejemplo.
     *
     * <p>Se comprueban precondiciones (listas vacías tras {@code reiniciar()})
     * antes del Act para dejar explícito que el crecimiento de las listas es
     * consecuencia directa de la llamada bajo prueba y no de un estado previo
     * accidental.</p>
     */
    @Test
    @DisplayName("inicializarDatosEstaticos() puebla las listas de tutores, estudiantes y solicitudes con el Mock Data esperado")
    void inicializarDatosEstaticos_pueblaListasConMockData() {
        // Arrange
        assertTrue(gestor.getTutores().isEmpty(),
            "Precondición: la lista de tutores debe iniciar vacía justo después de reiniciar().");
        assertTrue(gestor.getEstudiantes().isEmpty(),
            "Precondición: la lista de estudiantes debe iniciar vacía justo después de reiniciar().");
        assertTrue(gestor.getSolicitudes().isEmpty(),
            "Precondición: la lista de solicitudes debe iniciar vacía justo después de reiniciar().");

        // Act
        gestor.inicializarDatosEstaticos();

        // Assert
        assertEquals(4, gestor.getEstudiantes().size(),
            "El Mock Data debe registrar exactamente 4 estudiantes (Pedro, María, Juan y Laura); "
            + "tamaño 0 indicaría que inicializarDatosEstaticos() no está poblando la lista.");
        assertEquals(3, gestor.getTutores().size(),
            "El Mock Data debe registrar exactamente 3 tutores (Jong Si-yun, Carla Andrea Reyes "
            + "y Roberto Esteban Díaz); tamaño 0 indicaría que inicializarDatosEstaticos() no está "
            + "poblando la lista.");
        assertTrue(gestor.getSolicitudes().size() > 0,
            "El Mock Data debe registrar al menos una solicitud de ejemplo para poblar el panel "
            + "de bienvenida del administrador.");
        assertTrue(gestor.getReservas().isEmpty(),
            "La lista de reservas debe permanecer vacía tras la inicialización, ya que el Mock Data "
            + "no crea reservas confirmadas por diseño (esa responsabilidad corresponde a ComandoAgendar).");
        assertTrue(gestor.isDatosInicializados(),
            "La bandera datosInicializados debe quedar en true luego de ejecutar inicializarDatosEstaticos().");
    }

    /**
     * Verifica la idempotencia de {@link GestorDatos#inicializarDatosEstaticos()}:
     * invocarlo una segunda vez sobre una instancia ya inicializada no debe
     * duplicar ningún registro en las listas centrales.
     *
     * <p>Esta prueba es crítica porque, sin la bandera interna
     * {@code datosInicializados}, cada llamada adicional agregaría una nueva
     * copia de los 4 estudiantes, 3 tutores y solicitudes de Mock Data,
     * corrompiendo silenciosamente el estado del sistema.</p>
     */
    @Test
    @DisplayName("inicializarDatosEstaticos() es idempotente: llamarlo una segunda vez no duplica los registros")
    void inicializarDatosEstaticos_esIdempotente_noDuplicaAlLlamarloDosVeces() {
        // Arrange
        gestor.inicializarDatosEstaticos();
        int tutoresTrasPrimeraLlamada     = gestor.getTutores().size();
        int estudiantesTrasPrimeraLlamada = gestor.getEstudiantes().size();
        int solicitudesTrasPrimeraLlamada = gestor.getSolicitudes().size();

        // Act: se invoca una segunda vez sobre la misma instancia ya inicializada
        gestor.inicializarDatosEstaticos();

        // Assert
        assertEquals(tutoresTrasPrimeraLlamada, gestor.getTutores().size(),
            "La cantidad de tutores no debe cambiar tras una segunda llamada a "
            + "inicializarDatosEstaticos(); un aumento indicaría que el Mock Data se duplicó.");
        assertEquals(estudiantesTrasPrimeraLlamada, gestor.getEstudiantes().size(),
            "La cantidad de estudiantes no debe cambiar tras una segunda llamada a "
            + "inicializarDatosEstaticos(); un aumento indicaría que el Mock Data se duplicó.");
        assertEquals(solicitudesTrasPrimeraLlamada, gestor.getSolicitudes().size(),
            "La cantidad de solicitudes no debe cambiar tras una segunda llamada a "
            + "inicializarDatosEstaticos(), confirmando la idempotencia completa del método.");
    }

    // -------------------------------------------------------------------------
    // Control de colisiones: guardarReserva() y ConflictoHorarioException
    // -------------------------------------------------------------------------

    /**
     * Verifica la regla de negocio central de {@link GestorDatos#guardarReserva(Reserva)}:
     * dos reservas activas para el <strong>mismo tutor</strong> en el
     * <strong>mismo día y bloque horario</strong> no pueden coexistir en el
     * sistema.
     *
     * <p>Se registra primero una reserva activa (Lunes, bloque 0) y luego se
     * intenta guardar una segunda reserva para el mismo tutor exactamente en
     * ese bloque; el gestor debe rechazarla lanzando
     * {@link ConflictoHorarioException} <em>antes</em> de agregarla a la
     * lista interna de reservas.</p>
     */
    @Test
    @DisplayName("guardarReserva() lanza ConflictoHorarioException al intentar reservar el mismo tutor en el mismo bloque ya ocupado")
    void guardarReserva_lanzaConflictoHorarioException_cuandoElMismoTutorYaTieneReservaActivaEnElMismoBloque() {
        // Arrange
        Tutor tutor = new Tutor("Jong Si-yun", "Especialista en Estadística",
            "Estadística Inferencial", "Ciencias Exactas", "assets/fotos/jong.png");
        gestor.registrarTutor(tutor);

        Estudiante estudianteUno = new Estudiante("Pedro Pablo González", "Ingeniería Comercial", 3,
            "pedro.gonzalez@mail.com", "Comentario", "assets/fotos/pedro.png");
        Estudiante estudianteDos = new Estudiante("María José Fernández", "Psicología", 5,
            "maria.fernandez@mail.com", "Comentario", "assets/fotos/maria.png");
        gestor.registrarEstudiante(estudianteUno);
        gestor.registrarEstudiante(estudianteDos);

        Solicitud solicitudUno = new Solicitud("SOL-201", "Ayuda con estadística", "Comentario",
            null, estudianteUno);
        Solicitud solicitudDos = new Solicitud("SOL-202", "Ayuda con estadística", "Comentario",
            null, estudianteDos);

        LocalDate fecha = LocalDate.of(2026, 8, 10); // Lunes

        Reserva reservaExistente = new Reserva(tutor, estudianteUno, solicitudUno, fecha, 0, 0);
        gestor.guardarReserva(reservaExistente);

        Reserva reservaConflictiva = new Reserva(tutor, estudianteDos, solicitudDos, fecha, 0, 0);

        // Act
        ConflictoHorarioException excepcion = assertThrows(ConflictoHorarioException.class,
            () -> gestor.guardarReserva(reservaConflictiva),
            "Debe lanzarse ConflictoHorarioException porque el tutor ya tiene una reserva activa "
            + "en el mismo día (índice 0) y bloque horario (índice 0).");

        // Assert
        assertTrue(excepcion.getMessage().contains(tutor.getNombre()),
            "El mensaje de la excepción debe identificar al tutor involucrado en el conflicto de horario.");
        assertEquals(1, gestor.getReservas().size(),
            "La lista de reservas debe seguir teniendo un único elemento, ya que la reserva "
            + "conflictiva no debe agregarse a la lista tras el lanzamiento de la excepción.");
    }

    /**
     * Caso de control negativo complementario: dos reservas para el mismo
     * tutor en bloques horarios <strong>distintos</strong> del mismo día no
     * representan un conflicto y ambas deben poder coexistir.
     *
     * <p>Esta prueba delimita el alcance exacto de la regla de negocio: el
     * conflicto depende de la coincidencia simultánea de tutor, día
     * <strong>y</strong> bloque; basta con que uno solo de esos tres
     * componentes difiera para que la operación sea válida.</p>
     */
    @Test
    @DisplayName("guardarReserva() permite registrar una nueva reserva para el mismo tutor en un bloque horario distinto")
    void guardarReserva_permiteRegistrar_cuandoElMismoTutorTieneUnBloqueHorarioDistinto() {
        // Arrange
        Tutor tutor = new Tutor("Carla Andrea Reyes", "Profesora de Inglés",
            "Inglés B2", "Humanidades", "assets/fotos/carla.png");
        gestor.registrarTutor(tutor);

        Estudiante estudiante = new Estudiante("Juan Ignacio Soto", "Ingeniería Civil Industrial", 2,
            "juan.soto@mail.com", "Comentario", "assets/fotos/juan.png");
        gestor.registrarEstudiante(estudiante);

        Solicitud solicitud = new Solicitud("SOL-203", "Ayuda con inglés", "Comentario",
            null, estudiante);

        LocalDate fecha = LocalDate.of(2026, 8, 11); // Martes

        Reserva reservaBloqueUno = new Reserva(tutor, estudiante, solicitud, fecha, 1, 3);
        gestor.guardarReserva(reservaBloqueUno);

        Reserva reservaBloqueDos = new Reserva(tutor, estudiante, solicitud, fecha, 1, 4);

        // Act
        assertDoesNotThrow(() -> gestor.guardarReserva(reservaBloqueDos),
            "No debe lanzarse ninguna excepción: el bloque horario 4 es distinto al bloque 3 "
            + "ya ocupado, por lo que no existe conflicto real para el mismo tutor.");

        // Assert
        assertEquals(2, gestor.getReservas().size(),
            "Ambas reservas deben quedar registradas en la lista, ya que no existe solapamiento "
            + "de bloque horario entre ellas.");
    }

    /**
     * Verifica la validación defensiva de {@link GestorDatos#guardarReserva(Reserva)}
     * ante una entrada nula, independientemente de la lógica de conflictos
     * de horario.
     */
    @Test
    @DisplayName("guardarReserva() lanza IllegalArgumentException si la reserva a registrar es nula")
    void guardarReserva_lanzaIllegalArgumentException_siLaReservaEsNula() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> gestor.guardarReserva(null),
            "Debe lanzarse IllegalArgumentException cuando se intenta guardar una reserva nula, "
            + "antes de siquiera evaluar conflictos de horario.");
    }
}