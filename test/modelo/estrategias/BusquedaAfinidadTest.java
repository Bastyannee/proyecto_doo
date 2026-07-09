package modelo.estrategias;

import modelo.entidades.Estudiante;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;
import modelo.estrategias.BusquedaAfinidad;
import modelo.estrategias.EstrategiaBusqueda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para el componente de estrategia BusquedaAfinidad.
 *
 * Esta clase valida el comportamiento del algoritmo de emparejamiento basado en la
 * afinidad disciplinaria entre la carrera del estudiante y el área declarada por el tutor.
 * Se verifican de manera exhaustiva las reglas de negocio de normalización de cadenas
 * (insensibilidad a mayúsculas y minúsculas), el correcto funcionamiento del mapa
 * heurístico de palabras clave para clasificar facultades, el mecanismo de respaldo (fallback)
 * cuando una carrera no se encuentra indexada y el control de excepciones ante parámetros nulos.
 */
@DisplayName("Suite de pruebas unitarias para la estrategia BusquedaAfinidad")
class BusquedaAfinidadTest {

    private EstrategiaBusqueda estrategia;

    /**
     * Configura el escenario de ejecución base, instanciando la estrategia bajo
     * análisis antes de cada caso de prueba aislado.
     */
    @BeforeEach
    @DisplayName("Arrange común: instancia la estrategia de búsqueda por afinidad antes de cada prueba")
    void setUp() {
        estrategia = new BusquedaAfinidad();
    }

    @Test
    @DisplayName("buscar() empareja a un estudiante de Ingeniería con un tutor de afinidad 'Ciencias Exactas', ignorando mayúsculas/minúsculas")
    void buscar_empareja_estudianteIngenieria_conTutorCienciasExactas_ignorandoMayusculasMinusculas() {

        Estudiante estudianteIngenieria = new Estudiante(
                "Juan Ignacio Soto", "Ingeniería Civil Industrial", 2,
                "juan.soto@mail.com", "Comentario", "assets/fotos/juan.png");

        Tutor tutorCienciasExactas = new Tutor(
                "Jong Si-yun", "Especialista en Estadística", "Estadística Inferencial",
                "ciencias EXACTAS", "assets/fotos/jong.png"); // afinidad con mezcla deliberada de mayúsculas/minúsculas

        Solicitud solicitud = new Solicitud("SOL-101", "Ayuda con álgebra lineal", "Comentario",
                null, estudianteIngenieria);

        List<Tutor> tutores = List.of(tutorCienciasExactas);

        List<Tutor> resultado = estrategia.buscar(tutores, solicitud);

        assertEquals(1, resultado.size(),
                "Un estudiante de 'Ingeniería Civil Industrial' debe emparejar con un tutor de afinidad 'Ciencias Exactas', "
                + "ya que la palabra clave 'ingenier' clasifica la carrera hacia esa categoría.");
        assertTrue(resultado.contains(tutorCienciasExactas),
                "El tutor con afinidad 'ciencias EXACTAS' debe formar parte del resultado pese a la diferencia de mayúsculas/minúsculas frente a 'Ciencias Exactas'.");
    }

    @Test
    @DisplayName("buscar() empareja mediante fallback una carrera no mapeada con un tutor cuya afinidad declarada coincide textualmente")
    void buscar_empareja_carreraNoMapeada_conTutorDeAfinidadIdenticaViaFallback() {

        Estudiante estudianteArquitectura = new Estudiante(
                "Laura Beatriz Muñoz", "Arquitectura", 4,
                "laura.munoz@mail.com", "Comentario", "assets/fotos/laura.png");

        Tutor tutorArquitectura = new Tutor(
                "Ismael Peña", "Especialista en diseño y urbanismo", "Taller de Diseño",
                "Arquitectura", "assets/fotos/ismael.png");

        Solicitud solicitud = new Solicitud("SOL-102", "Ayuda con taller de diseño", "Comentario",
                null, estudianteArquitectura);

        List<Tutor> tutores = List.of(tutorArquitectura);

        List<Tutor> resultado = estrategia.buscar(tutores, solicitud);

        assertEquals(1, resultado.size(),
                "Ante una carrera no mapeada heurísticamente ('Arquitectura'), la afinidad implícita debe caer al fallback "
                + "(usar la carrera misma) y emparejar con un tutor cuya afinidad declarada sea idéntica.");
        assertTrue(resultado.contains(tutorArquitectura),
                "El tutor con afinidad exacta 'Arquitectura' debe formar parte del resultado gracias al mecanismo de respaldo del algoritmo.");
    }

    @Test
    @DisplayName("buscar() retorna lista vacía cuando ningún tutor tiene una afinidad compatible con la del estudiante")
    void buscar_retornaListaVacia_cuandoNoHayCoincidenciaDeAfinidad() {

        Estudiante estudiantePsicologia = new Estudiante(
                "María José Fernández", "Psicología", 5,
                "maria.fernandez@mail.com", "Comentario", "assets/fotos/maria.png");

        Tutor tutorCienciasExactas = new Tutor(
                "Jong Si-yun", "Especialista en Estadística", "Estadística Inferencial",
                "Ciencias Exactas", "assets/fotos/jong.png");

        Solicitud solicitud = new Solicitud("SOL-103", "Ayuda con estadística aplicada", "Comentario",
                null, estudiantePsicologia);

        List<Tutor> tutores = List.of(tutorCienciasExactas);

        List<Tutor> resultado = estrategia.buscar(tutores, solicitud);

        assertTrue(resultado.isEmpty(),
                "No debe existir coincidencia entre la afinidad implícita 'Humanidades' (derivada de 'Psicología') "
                + "y la afinidad declarada 'Ciencias Exactas' del tutor.");
    }

    @Test
    @DisplayName("buscar() lanza IllegalArgumentException si la lista de tutores es nula")
    void buscar_lanzaExcepcion_siListaDeTutoresEsNula() {

        Estudiante estudiante = new Estudiante(
                "Pedro Pablo González", "Ingeniería Comercial", 3,
                "pedro.gonzalez@mail.com", "Comentario", "assets/fotos/pedro.png");
        Solicitud solicitud = new Solicitud("SOL-104", "Ayuda", "Comentario", null, estudiante);

        assertThrows(IllegalArgumentException.class, () -> estrategia.buscar(null, solicitud),
                "Debe lanzarse IllegalArgumentException cuando la lista de tutores recibida es nula.");
    }

    @Test
    @DisplayName("buscar() lanza IllegalArgumentException si la solicitud es nula")
    void buscar_lanzaExcepcion_siSolicitudEsNula() {

        Tutor tutor = new Tutor(
                "Jong Si-yun", "Especialista en Estadística", "Estadística Inferencial",
                "Ciencias Exactas", "assets/fotos/jong.png");
        List<Tutor> tutores = List.of(tutor);

        assertThrows(IllegalArgumentException.class, () -> estrategia.buscar(tutores, null),
                "Debe lanzarse IllegalArgumentException cuando la solicitud recibida es nula.");
    }
}