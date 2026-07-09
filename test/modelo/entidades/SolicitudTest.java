package modelo.entidades;

import modelo.entidades.ConstantesHorario;
import modelo.entidades.Estudiante;
import modelo.entidades.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para la clase de dominio Solicitud.
 *
 * Esta clase valida de manera exhaustiva las invariantes del modelo, precondiciones
 * del constructor, encapsulamiento de estado mediante copias defensivas, consistencia
 * de la máquina de estados y las restricciones dimensionales de las matrices de horarios.
 */
@DisplayName("Suite de pruebas unitarias para la entidad Solicitud")
class SolicitudTest {

    private Estudiante estudianteValido;
    private boolean[][] horarioValido;
    private Solicitud solicitud;

    /**
     * Inicializa y configura el entorno común antes de la ejecución de cada caso de prueba.
     */
    @BeforeEach
    @DisplayName("Arrange común: crea un estudiante, una matriz de horario válida y una solicitud base antes de cada prueba")
    void setUp() {
        estudianteValido = new Estudiante(
                "Pedro Pablo González",
                "Ingeniería Comercial",
                3,
                "pedro.gonzalez@mail.com",
                "Necesita apoyo en macroeconomía.",
                "assets/fotos/pedro.png");

        horarioValido = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        horarioValido[0][0] = true; // Lunes, bloque 08:00-09:30
        horarioValido[1][4] = true; // Martes, bloque 14:30-16:00

        solicitud = new Solicitud("SOL-001", "Ayuda con integrales", "Comentario libre",
                horarioValido, estudianteValido);
    }

    // -------------------------------------------------------------------------
    // Inmutabilidad y copias defensivas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getHorarioDeseado() retorna una instancia de matriz distinta en memoria a la interna")
    void getHorarioDeseado_retornaInstanciaDistintaEnMemoria() {
        // Arrange (solicitud ya construida en setUp con horarioValido)

        // Act
        boolean[][] copiaUno = solicitud.getHorarioDeseado();
        boolean[][] copiaDos = solicitud.getHorarioDeseado();

        // Assert
        assertNotSame(copiaUno, copiaDos,
                "Cada llamada a getHorarioDeseado() debe retornar un arreglo nuevo, no la misma referencia.");
        assertNotSame(horarioValido, copiaUno,
                "La copia retornada no debe ser la misma referencia que la matriz original pasada al constructor.");
    }

    @Test
    @DisplayName("Modificar el arreglo retornado por getHorarioDeseado() no altera el estado interno de la solicitud")
    void getHorarioDeseado_modificarCopiaExternaNoAlteraEstadoInterno() {
        // Arrange
        boolean[][] copiaObtenida = solicitud.getHorarioDeseado();
        assertTrue(copiaObtenida[0][0], "Precondición: el bloque [0][0] debe estar solicitado antes de mutar la copia.");

        // Act: se muta agresivamente la copia externa en todas las celdas
        for (boolean[] fila : copiaObtenida) {
            java.util.Arrays.fill(fila, false);
        }

        // Assert: el estado interno de la solicitud permanece intacto
        assertTrue(solicitud.isBloqueSolicitado(0, 0),
                "El bloque [0][0] debe seguir solicitado pese a la mutación externa.");
        assertTrue(solicitud.isBloqueSolicitado(1, 4),
                "El bloque [1][4] debe seguir solicitado pese a la mutación externa.");
    }

    @Test
    @DisplayName("Modificar la matriz original pasada al constructor no afecta el estado interno de la solicitud")
    void constructor_copiaDefensivaDeMatrizDeEntrada() {
        // Arrange
        boolean[][] matrizOriginal = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        matrizOriginal[2][2] = true;
        Solicitud solicitudLocal = new Solicitud("SOL-002", "Asunto local", "Comentario",
                matrizOriginal, estudianteValido);

        // Act: se muta la matriz original después de construir la solicitud
        matrizOriginal[2][2] = false;
        matrizOriginal[4][5] = true;

        // Assert
        assertTrue(solicitudLocal.isBloqueSolicitado(2, 2),
                "La solicitud debe conservar el valor original pese a la mutación posterior del arreglo fuente.");
        assertFalse(solicitudLocal.isBloqueSolicitado(4, 5),
                "La solicitud no debe reflejar cambios realizados en la matriz original tras la construcción.");
    }

    @Test
    @DisplayName("setHorarioDeseado() aplica copia defensiva sobre la nueva matriz recibida")
    void setHorarioDeseado_aplicaCopiaDefensiva() {
        // Arrange
        boolean[][] nuevaMatriz = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        nuevaMatriz[3][1] = true;

        // Act
        solicitud.setHorarioDeseado(nuevaMatriz);
        nuevaMatriz[3][1] = false;

        // Assert
        assertTrue(solicitud.isBloqueSolicitado(3, 1),
                "La solicitud debe conservar el estado interno aunque la matriz fuente se mute tras el setter.");
    }

    // -------------------------------------------------------------------------
    // Validación de excepciones en el constructor
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el id es nulo")
    void constructor_lanzaExcepcion_siIdEsNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Solicitud(null, "Asunto", "Comentario", horarioValido, estudianteValido));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el id está en blanco")
    void constructor_lanzaExcepcion_siIdEstaEnBlanco() {
        assertThrows(IllegalArgumentException.class, () ->
                new Solicitud("   ", "Asunto", "Comentario", horarioValido, estudianteValido));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el asunto es nulo")
    void constructor_lanzaExcepcion_siAsuntoEsNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Solicitud("SOL-003", null, "Comentario", horarioValido, estudianteValido));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el asunto está en blanco")
    void constructor_lanzaExcepcion_siAsuntoEstaEnBlanco() {
        assertThrows(IllegalArgumentException.class, () ->
                new Solicitud("SOL-003", "   ", "Comentario", horarioValido, estudianteValido));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el estudiante es nulo")
    void constructor_lanzaExcepcion_siEstudianteEsNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Solicitud("SOL-003", "Asunto", "Comentario", horarioValido, null));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si la matriz tiene menos filas que DIAS")
    void constructor_lanzaExcepcion_siFilasIncorrectas() {
        // Arrange
        boolean[][] matrizInvalida = new boolean[ConstantesHorario.DIAS - 1][ConstantesHorario.BLOQUES];

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Solicitud("SOL-004", "Asunto", "Comentario", matrizInvalida, estudianteValido));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si alguna fila tiene menos columnas que BLOQUES")
    void constructor_lanzaExcepcion_siColumnasIncorrectas() {
        // Arrange
        boolean[][] matrizInvalida = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES - 1];

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Solicitud("SOL-004", "Asunto", "Comentario", matrizInvalida, estudianteValido));
    }

    @Test
    @DisplayName("El constructor acepta un horarioDeseado nulo y genera una matriz de ceros por defecto")
    void constructor_aceptaHorarioNuloYGeneraMatrizVacia() {
        // Arrange / Act
        Solicitud solicitudSinHorario = new Solicitud("SOL-005", "Asunto", "Comentario",
                null, estudianteValido);

        // Assert
        assertEquals(0, solicitudSinHorario.contarBloquesDeseados(),
                "Sin horario deseado explícito, la matriz interna debe inicializarse completamente en false.");
    }

    @Test
    @DisplayName("isBloqueSolicitado() lanza IllegalArgumentException si el índice de día está fuera de rango")
    void isBloqueSolicitado_lanzaExcepcion_siDiaFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () ->
                solicitud.isBloqueSolicitado(ConstantesHorario.DIAS, 0));
    }

    @Test
    @DisplayName("isBloqueSolicitado() lanza IllegalArgumentException si el índice de bloque está fuera de rango")
    void isBloqueSolicitado_lanzaExcepcion_siBloqueFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () ->
                solicitud.isBloqueSolicitado(0, ConstantesHorario.BLOQUES));
    }

    // -------------------------------------------------------------------------
    // Lógica de dominio adicional
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("La solicitud recién creada inicia en estado PENDIENTE")
    void constructor_estadoInicialEsPendiente() {
        assertEquals(Solicitud.EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertTrue(solicitud.isPendiente());
    }

    @Test
    @DisplayName("contarBloquesDeseados() retorna el número exacto de celdas marcadas como true")
    void contarBloquesDeseados_retornaConteoCorrecto() {
        assertEquals(2, solicitud.contarBloquesDeseados(),
                "El conteo de bloques deseados debe coincidir con las celdas marcadas true en setUp.");
    }
}