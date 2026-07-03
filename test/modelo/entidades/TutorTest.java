package test.modelo.entidades;

import modelo.entidades.ConstantesHorario;
import modelo.entidades.Tutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite de pruebas unitarias para la entidad Tutor")
class TutorTest {

    private static final String NOMBRE_VALIDO      = "Ana María López";
    private static final String DESCRIPCION_VALIDA = "Docente certificada en Álgebra Lineal.";
    private static final String MATERIA_VALIDA      = "Cálculo I";
    private static final String AFINIDAD_VALIDA     = "Ciencias Exactas";
    private static final String FOTO_PATH_VALIDA    = "assets/fotos/ana_lopez.png";

    private boolean[][] disponibilidadValida;
    private Tutor tutor;

    @BeforeEach
    @DisplayName("Arrange común: crea una matriz de disponibilidad válida y un tutor base antes de cada prueba")
    void setUp() {
        disponibilidadValida = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        disponibilidadValida[0][0] = true; // Lunes, bloque 08:00-09:30
        disponibilidadValida[2][3] = true; // Miércoles, bloque 13:00-14:30

        tutor = new Tutor(NOMBRE_VALIDO, DESCRIPCION_VALIDA, MATERIA_VALIDA,
                AFINIDAD_VALIDA, FOTO_PATH_VALIDA, disponibilidadValida, 5, 15000.0);
    }

    // -------------------------------------------------------------------------
    // Inmutabilidad y copias defensivas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getDisponibilidad() retorna una instancia de matriz distinta en memoria a la interna")
    void getDisponibilidad_retornaInstanciaDistintaEnMemoria() {
        // Arrange (tutor ya construido en setUp con disponibilidadValida)

        // Act
        boolean[][] copiaUno = tutor.getDisponibilidad();
        boolean[][] copiaDos = tutor.getDisponibilidad();

        // Assert
        assertNotSame(copiaUno, copiaDos,
                "Cada llamada a getDisponibilidad() debe retornar un arreglo nuevo, no la misma referencia.");
        assertNotSame(disponibilidadValida, copiaUno,
                "La copia retornada no debe ser la misma referencia que la matriz original pasada al constructor.");
    }

    @Test
    @DisplayName("Modificar el arreglo retornado por getDisponibilidad() no altera el estado interno del tutor")
    void getDisponibilidad_modificarCopiaExternaNoAlteraEstadoInterno() {
        // Arrange
        boolean[][] copiaObtenida = tutor.getDisponibilidad();
        assertTrue(copiaObtenida[0][0], "Precondición: el bloque [0][0] debe estar disponible antes de mutar la copia.");

        // Act: se muta agresivamente la copia externa en todas las celdas
        for (boolean[] fila : copiaObtenida) {
            java.util.Arrays.fill(fila, false);
        }

        // Assert: el estado interno del tutor permanece intacto
        assertTrue(tutor.isDisponible(0, 0),
                "El bloque [0][0] debe seguir disponible en el tutor pese a la mutación externa.");
        assertTrue(tutor.isDisponible(2, 3),
                "El bloque [2][3] debe seguir disponible en el tutor pese a la mutación externa.");
    }

    @Test
    @DisplayName("Modificar la matriz original pasada al constructor no afecta el estado interno del tutor")
    void constructor_copiaDefensivaDeMatrizDeEntrada() {
        // Arrange
        boolean[][] matrizOriginal = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        matrizOriginal[1][1] = true;
        Tutor tutorLocal = new Tutor(NOMBRE_VALIDO, DESCRIPCION_VALIDA, MATERIA_VALIDA,
                AFINIDAD_VALIDA, FOTO_PATH_VALIDA, matrizOriginal, 5, 15000.0);

        // Act: se muta la matriz original después de construir el tutor
        matrizOriginal[1][1] = false;
        matrizOriginal[4][5] = true;

        // Assert
        assertTrue(tutorLocal.isDisponible(1, 1),
                "El tutor debe conservar el valor original pese a la mutación posterior del arreglo fuente.");
        assertFalse(tutorLocal.isDisponible(4, 5),
                "El tutor no debe reflejar cambios realizados en la matriz original tras la construcción.");
    }

    @Test
    @DisplayName("setDisponibilidad() aplica copia defensiva sobre la nueva matriz recibida")
    void setDisponibilidad_aplicaCopiaDefensiva() {
        // Arrange
        boolean[][] nuevaMatriz = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES];
        nuevaMatriz[3][2] = true;

        // Act
        tutor.setDisponibilidad(nuevaMatriz);
        nuevaMatriz[3][2] = false;

        // Assert
        assertTrue(tutor.isDisponible(3, 2),
                "El tutor debe conservar el estado interno aunque la matriz fuente se mute tras el setter.");
    }

    // -------------------------------------------------------------------------
    // Validación de excepciones en el constructor
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el nombre es nulo")
    void constructor_lanzaExcepcion_siNombreEsNulo() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Tutor(null, DESCRIPCION_VALIDA, MATERIA_VALIDA, AFINIDAD_VALIDA,
                        FOTO_PATH_VALIDA, disponibilidadValida, 5, 15000.0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si el nombre está en blanco")
    void constructor_lanzaExcepcion_siNombreEstaEnBlanco() {
        assertThrows(IllegalArgumentException.class, () ->
                new Tutor("   ", DESCRIPCION_VALIDA, MATERIA_VALIDA, AFINIDAD_VALIDA,
                        FOTO_PATH_VALIDA, disponibilidadValida, 5, 15000.0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si la materia es nula")
    void constructor_lanzaExcepcion_siMateriaEsNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Tutor(NOMBRE_VALIDO, DESCRIPCION_VALIDA, null, AFINIDAD_VALIDA,
                        FOTO_PATH_VALIDA, disponibilidadValida, 5, 15000.0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si maxEstudiantes es menor o igual a 0")
    void constructor_lanzaExcepcion_siMaxEstudiantesNoEsPositivo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Tutor(NOMBRE_VALIDO, DESCRIPCION_VALIDA, MATERIA_VALIDA, AFINIDAD_VALIDA,
                        FOTO_PATH_VALIDA, disponibilidadValida, 0, 15000.0));
    }

    @Test
    @DisplayName("El constructor lanza IllegalArgumentException si la tarifa es negativa")
    void constructor_lanzaExcepcion_siTarifaEsNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                new Tutor(NOMBRE_VALIDO, DESCRIPCION_VALIDA, MATERIA_VALIDA, AFINIDAD_VALIDA,
                        FOTO_PATH_VALIDA, disponibilidadValida, 5, -1.0));
    }

    @Test
    @DisplayName("setDisponibilidad() lanza IllegalArgumentException si la matriz tiene menos filas que DIAS")
    void setDisponibilidad_lanzaExcepcion_siFilasIncorrectas() {
        // Arrange
        boolean[][] matrizInvalida = new boolean[ConstantesHorario.DIAS - 1][ConstantesHorario.BLOQUES];

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> tutor.setDisponibilidad(matrizInvalida));
    }

    @Test
    @DisplayName("setDisponibilidad() lanza IllegalArgumentException si alguna fila tiene menos columnas que BLOQUES")
    void setDisponibilidad_lanzaExcepcion_siColumnasIncorrectas() {
        // Arrange
        boolean[][] matrizInvalida = new boolean[ConstantesHorario.DIAS][ConstantesHorario.BLOQUES - 1];

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> tutor.setDisponibilidad(matrizInvalida));
    }

    @Test
    @DisplayName("setDisponibilidad() lanza IllegalArgumentException si la matriz es nula")
    void setDisponibilidad_lanzaExcepcion_siMatrizEsNula() {
        assertThrows(IllegalArgumentException.class, () -> tutor.setDisponibilidad(null));
    }

    @Test
    @DisplayName("isDisponible() lanza IllegalArgumentException si el índice de día está fuera de rango")
    void isDisponible_lanzaExcepcion_siDiaFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> tutor.isDisponible(ConstantesHorario.DIAS, 0));
    }

    @Test
    @DisplayName("isDisponible() lanza IllegalArgumentException si el índice de bloque está fuera de rango")
    void isDisponible_lanzaExcepcion_siBloqueFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> tutor.isDisponible(0, ConstantesHorario.BLOQUES));
    }

    // -------------------------------------------------------------------------
    // Lógica de dominio adicional
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("contarBloquesDisponibles() retorna el número exacto de celdas marcadas como true")
    void contarBloquesDisponibles_retornaConteoCorrecto() {
        // Arrange (tutor de setUp tiene exactamente 2 bloques en true)

        // Act
        int total = tutor.contarBloquesDisponibles();

        // Assert
        assertEquals(2, total, "El conteo de bloques disponibles debe coincidir con las celdas marcadas true.");
    }

    @Test
    @DisplayName("Constructor de conveniencia sin matriz genera disponibilidad totalmente libre")
    void constructorConveniencia_generaDisponibilidadLibre() {
        // Arrange / Act
        Tutor tutorLibre = new Tutor(NOMBRE_VALIDO, DESCRIPCION_VALIDA, MATERIA_VALIDA,
                AFINIDAD_VALIDA, FOTO_PATH_VALIDA);

        // Assert
        assertEquals(ConstantesHorario.DIAS * ConstantesHorario.BLOQUES,
                tutorLibre.contarBloquesDisponibles(),
                "El constructor de conveniencia debe dejar todos los bloques disponibles.");
    }
}