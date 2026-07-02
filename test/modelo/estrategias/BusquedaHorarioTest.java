package test.modelo.estrategias;

import modelo.entidades.ConstantesHorario;
import modelo.entidades.Estudiante;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import modelo.estrategias.BusquedaHorario;
import modelo.estrategias.EstrategiaBusqueda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite de pruebas unitarias para la estrategia BusquedaHorario")
class BusquedaHorarioTest {

    private EstrategiaBusqueda estrategia;
    private Estudiante estudianteBase;
    private Tutor tutorBase;

    @BeforeEach
    @DisplayName("Arrange común: instancia la estrategia, un estudiante base y un tutor base con un único bloque disponible")
    void setUp() {
        estrategia = new BusquedaHorario();

        estudianteBase = new Estudiante(
                "Pedro Pablo González", "Ingeniería Comercial", 3,
                "pedro.gonzalez@mail.com", "Comentario", "assets/fotos/pedro.png");

        boolean[][] disponibilidadTutorBase = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        disponibilidadTutorBase[0][0] = true; // Lunes, bloque 08:00-09:30

        tutorBase = new Tutor(
                "Jong Si-yun", "Especialista en Estadística", "Estadística Inferencial",
                "Ciencias Exactas", "assets/fotos/jong.png",
                disponibilidadTutorBase, 4, 15000.0);
    }

    @Test
    @DisplayName("buscar() retorna el tutor cuando su disponibilidad coincide exactamente con el bloque [0][0] solicitado")
    void buscar_retornaTutor_cuandoHayInterseccionExactaEnBloqueSolicitado() {
        // Arrange
        boolean[][] horarioDeseado = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioDeseado[0][0] = true; // mismo bloque que tutorBase: Lunes 08:00-09:30

        Solicitud solicitud = new Solicitud("SOL-001", "Ayuda con estadística", "Comentario",
                horarioDeseado, estudianteBase);

        List<Tutor> tutores = List.of(tutorBase);

        // Act
        List<Tutor> resultado = estrategia.buscar(tutores, solicitud);

        // Assert
        assertEquals(1, resultado.size(),
                "Debe retornarse exactamente un tutor cuando existe intersección en el bloque [0][0].");
        assertTrue(resultado.contains(tutorBase),
                "El tutor retornado debe ser tutorBase, ya que coincide en disponibilidad con la solicitud en [0][0].");
    }

    @Test
    @DisplayName("buscar() retorna lista vacía cuando las matrices de tutor y solicitud son ortogonales (sin ninguna coincidencia)")
    void buscar_retornaListaVacia_cuandoMatricesSonOrtogonales() {
        // Arrange: tutorBase solo disponible en [0][0]; la solicitud desea únicamente el bloque opuesto [4][5]
        boolean[][] horarioDeseado = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioDeseado[4][5] = true; // Viernes, último bloque: no se solapa con [0][0]

        Solicitud solicitud = new Solicitud("SOL-002", "Ayuda con estadística", "Comentario",
                horarioDeseado, estudianteBase);

        List<Tutor> tutores = List.of(tutorBase);

        // Act
        List<Tutor> resultado = estrategia.buscar(tutores, solicitud);

        // Assert
        assertTrue(resultado.isEmpty(),
                "La lista de resultados debe estar vacía cuando ninguna celda de ambas matrices coincide en true simultáneamente (matrices ortogonales), "
                + "validando que el algoritmo recorre las " + ConstantesHorario.DIAS + "x" + ConstantesHorario.BLOQUES
                + " celdas sin encontrar coincidencia antes de descartar al tutor.");
    }

    @Test
    @DisplayName("buscar() no duplica un tutor en el resultado aunque coincida en múltiples bloques horarios")
    void buscar_noDuplicaTutorConMultiplesCoincidencias() {
        // Arrange: tutor y solicitud coinciden en dos bloques distintos: [0][0] y [2][3]
        boolean[][] disponibilidadMultiple = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        disponibilidadMultiple[0][0] = true;
        disponibilidadMultiple[2][3] = true;

        Tutor tutorConMultiplesBloques = new Tutor(
                "Roberto Esteban Díaz", "Ingeniero Civil", "Cálculo I",
                "Ciencias Exactas", "assets/fotos/roberto.png",
                disponibilidadMultiple, 5, 13500.0);

        boolean[][] horarioDeseado = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioDeseado[0][0] = true;
        horarioDeseado[2][3] = true;

        Solicitud solicitud = new Solicitud("SOL-003", "Ayuda con cálculo", "Comentario",
                horarioDeseado, estudianteBase);

        List<Tutor> tutores = List.of(tutorConMultiplesBloques);

        // Act
        List<Tutor> resultado = estrategia.buscar(tutores, solicitud);

        // Assert
        assertEquals(1, resultado.size(),
                "El tutor debe aparecer una única vez en el resultado, sin importar cuántos bloques de la matriz coincidan simultáneamente.");
    }

    @Test
    @DisplayName("buscar() preserva el orden relativo de los tutores compatibles respecto a la lista de entrada")
    void buscar_preservaOrdenRelativoDeTutoresCompatibles() {
        // Arrange: dos tutores compatibles con la solicitud, en un orden específico de entrada
        boolean[][] disponibilidadCompartida = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        disponibilidadCompartida[1][2] = true;

        Tutor tutorUno = new Tutor("Tutor Uno", "Descripción", "Materia", "Afinidad",
                "foto1.png", disponibilidadCompartida, 5, 10000.0);
        Tutor tutorDos = new Tutor("Tutor Dos", "Descripción", "Materia", "Afinidad",
                "foto2.png", disponibilidadCompartida, 5, 10000.0);

        boolean[][] horarioDeseado = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioDeseado[1][2] = true;

        Solicitud solicitud = new Solicitud("SOL-004", "Ayuda general", "Comentario",
                horarioDeseado, estudianteBase);

        List<Tutor> tutores = Arrays.asList(tutorUno, tutorDos);

        // Act
        List<Tutor> resultado = estrategia.buscar(tutores, solicitud);

        // Assert
        assertEquals(List.of(tutorUno, tutorDos), resultado,
                "El orden relativo de los tutores compatibles en el resultado debe coincidir con el orden de la lista de entrada.");
    }

    @Test
    @DisplayName("buscar() lanza IllegalArgumentException si la lista de tutores es nula")
    void buscar_lanzaExcepcion_siListaDeTutoresEsNula() {
        // Arrange
        boolean[][] horarioDeseado = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        Solicitud solicitud = new Solicitud("SOL-005", "Ayuda", "Comentario",
                horarioDeseado, estudianteBase);

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> estrategia.buscar(null, solicitud),
                "Debe lanzarse IllegalArgumentException cuando la lista de tutores recibida es nula.");
    }

    @Test
    @DisplayName("buscar() lanza IllegalArgumentException si la solicitud es nula")
    void buscar_lanzaExcepcion_siSolicitudEsNula() {
        // Arrange
        List<Tutor> tutores = List.of(tutorBase);

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> estrategia.buscar(tutores, null),
                "Debe lanzarse IllegalArgumentException cuando la solicitud recibida es nula.");
    }
}