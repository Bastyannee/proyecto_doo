package test.modelo.entidades;

import modelo.entidades.ConstantesHorario;
import modelo.entidades.Estudiante;
import modelo.entidades.Reserva;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite de pruebas unitarias para la entidad Reserva")
class ReservaTest {

    private Tutor tutorA;
    private Tutor tutorB;
    private Estudiante estudiante;
    private Solicitud solicitudOrigen;
    private LocalDate fechaValida;

    @BeforeEach
    @DisplayName("Arrange común: crea dos tutores distintos, un estudiante, una solicitud y una fecha antes de cada prueba")
    void setUp() {
        boolean[][] disponibilidadLibre = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        for (boolean[] fila : disponibilidadLibre) {
            java.util.Arrays.fill(fila, true);
        }

        tutorA = new Tutor("Jong Si-yun", "Especialista en Estadística", "Estadística Inferencial",
                "Ciencias Exactas", "assets/fotos/jong.png", disponibilidadLibre, 4, 15000.0);

        tutorB = new Tutor("Carla Andrea Reyes", "Profesora de Inglés B2", "Inglés B2",
                "Humanidades", "assets/fotos/carla.png", disponibilidadLibre, 6, 12000.0);

        estudiante = new Estudiante("Pedro Pablo González", "Ingeniería Comercial", 3,
                "pedro.gonzalez@mail.com", "Comentario", "assets/fotos/pedro.png");

        boolean[][] horarioDeseado = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioDeseado[0][0] = true;

        solicitudOrigen = new Solicitud("SOL-100", "Ayuda con estadística", "Comentario",
                horarioDeseado, estudiante);

        fechaValida = LocalDate.of(2026, 8, 10); // Lunes
    }

    // -------------------------------------------------------------------------
    // Validación de excepciones en el constructor
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el tutor es nulo")
    void constructor_lanzaExcepcion_siTutorEsNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reserva(null, estudiante, solicitudOrigen, fechaValida, 0, 0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el estudiante es nulo")
    void constructor_lanzaExcepcion_siEstudianteEsNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reserva(tutorA, null, solicitudOrigen, fechaValida, 0, 0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si la solicitud de origen es nula")
    void constructor_lanzaExcepcion_siSolicitudOrigenEsNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reserva(tutorA, estudiante, null, fechaValida, 0, 0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si la fecha es nula")
    void constructor_lanzaExcepcion_siFechaEsNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reserva(tutorA, estudiante, solicitudOrigen, null, 0, 0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el índice de día está fuera de rango")
    void constructor_lanzaExcepcion_siDiaIndexFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, ConstantesHorario.DIAS, 0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el índice de bloque está fuera de rango")
    void constructor_lanzaExcepcion_siBloqueIndexFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, ConstantesHorario.BLOQUES));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el índice de día es negativo")
    void constructor_lanzaExcepcion_siDiaIndexEsNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, -1, 0));
    }

    // -------------------------------------------------------------------------
    // Estado inicial y setters
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Una reserva recién creada inicia en estado CONFIRMADA y activa")
    void constructor_estadoInicialEsConfirmadaYActiva() {
        // Arrange / Act
        Reserva reserva = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Assert
        assertEquals(Reserva.EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertTrue(reserva.isActiva());
    }

    @Test
    @DisplayName("setEstado(CANCELADA) hace que isActiva() retorne false")
    void setEstado_cancelada_desactivaLaReserva() {
        // Arrange
        Reserva reserva = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Act
        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);

        // Assert
        assertFalse(reserva.isActiva(),
                "Una reserva cancelada no debe considerarse activa.");
    }

    @Test
    @DisplayName("setEstado() lanza IllegalArgumentException si el nuevo estado es nulo")
    void setEstado_lanzaExcepcion_siEstadoEsNulo() {
        // Arrange
        Reserva reserva = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> reserva.setEstado(null));
    }

    // -------------------------------------------------------------------------
    // Lógica de dominio: conflictaCon(Reserva otra)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("conflictaCon() retorna true cuando ambas reservas activas comparten mismo tutor, día y bloque")
    void conflictaCon_retornaTrue_cuandoHaySolapamientoDeTutorDiaYBloque() {
        // Arrange: dos reservas distintas, mismo tutor, mismo bloque horario (Lunes, bloque 0)
        Reserva reservaExistente = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);
        Reserva reservaNueva     = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Act
        boolean hayConflicto = reservaNueva.conflictaCon(reservaExistente);

        // Assert
        assertTrue(hayConflicto,
                "Debe existir conflicto: mismo tutor, mismo día y mismo bloque, ambas reservas activas.");
    }

    @Test
    @DisplayName("conflictaCon() retorna false cuando las reservas tienen tutores distintos, aunque compartan día y bloque")
    void conflictaCon_retornaFalse_cuandoLosTutoresSonDistintos() {
        // Arrange: mismo día y bloque, pero tutores diferentes
        Reserva reservaTutorA = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);
        Reserva reservaTutorB = new Reserva(tutorB, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Act
        boolean hayConflicto = reservaTutorA.conflictaCon(reservaTutorB);

        // Assert
        assertFalse(hayConflicto,
                "No debe existir conflicto entre reservas de tutores distintos, aunque compartan bloque horario.");
    }

    @Test
    @DisplayName("conflictaCon() retorna false cuando el mismo tutor tiene reservas en bloques horarios distintos")
    void conflictaCon_retornaFalse_cuandoElMismoTutorTieneBloquesDistintos() {
        // Arrange: mismo tutor, mismo día, pero bloque horario distinto
        Reserva reservaBloqueUno = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);
        Reserva reservaBloqueDos = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 1);

        // Act
        boolean hayConflicto = reservaBloqueUno.conflictaCon(reservaBloqueDos);

        // Assert
        assertFalse(hayConflicto,
                "No debe existir conflicto si el mismo tutor tiene reservas en bloques horarios diferentes.");
    }

    @Test
    @DisplayName("conflictaCon() retorna false cuando la reserva existente está CANCELADA, incluso con mismo tutor, día y bloque")
    void conflictaCon_retornaFalse_cuandoLaOtraReservaEstaCancelada() {
        // Arrange: mismo tutor, día y bloque, pero la reserva existente fue cancelada
        Reserva reservaCancelada = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);
        reservaCancelada.setEstado(Reserva.EstadoReserva.CANCELADA);
        Reserva reservaNueva = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Act
        boolean hayConflicto = reservaNueva.conflictaCon(reservaCancelada);

        // Assert
        assertFalse(hayConflicto,
                "Una reserva cancelada no debe generar conflicto, ya que libera el bloque horario del tutor.");
    }

    @Test
    @DisplayName("conflictaCon() lanza IllegalArgumentException si la reserva comparada es nula")
    void conflictaCon_lanzaExcepcion_siOtraReservaEsNula() {
        // Arrange
        Reserva reserva = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> reserva.conflictaCon(null));
    }

    // -------------------------------------------------------------------------
    // Getters derivados
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getNombreDia() y getHora() derivan correctamente las etiquetas desde ConstantesHorario")
    void getNombreDiaYGetHora_derivanEtiquetasCorrectas() {
        // Arrange
        Reserva reserva = new Reserva(tutorA, estudiante, solicitudOrigen, fechaValida, 0, 0);

        // Act / Assert
        assertEquals(ConstantesHorario.NOMBRES_DIAS[0], reserva.getNombreDia());
        assertEquals(ConstantesHorario.NOMBRES_BLOQUES[0], reserva.getHora());
    }
}