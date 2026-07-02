package modelo.estrategias;

import modelo.entidades.Estudiante;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Estrategia concreta del patrón <strong>Strategy</strong> que filtra tutores
 * por <em>afinidad temática</em>: un tutor es compatible con una
 * {@link Solicitud} si el área de {@link Tutor#getAfinidad()} declarada por
 * el tutor coincide, en texto exacto e insensible a mayúsculas/minúsculas,
 * con la <em>afinidad implícita</em> del estudiante que emite la solicitud.
 *
 * <h2>El problema de la "afinidad implícita"</h2>
 * <p>La entidad {@link Estudiante} no almacena un campo {@code afinidad}
 * explícito — solo {@code carrera} — mientras que {@link Tutor} sí declara
 * una {@code afinidad} categórica (p. ej., {@code "Ciencias Exactas"},
 * {@code "Humanidades"}, {@code "Negocios"}). Comparar literalmente
 * {@code tutor.getAfinidad()} contra {@code estudiante.getCarrera()} con
 * {@code equalsIgnoreCase} casi nunca produciría coincidencias reales, ya
 * que <em>"Ingeniería Comercial"</em> y <em>"Ciencias Exactas"</em> son
 * cadenas distintas aunque conceptualmente relacionadas.</p>
 *
 * <p>Por ello, esta estrategia resuelve primero una
 * <strong>afinidad implícita</strong> a partir de la carrera del estudiante,
 * usando un mapa de clasificación por palabras clave
 * ({@link #MAPA_CARRERA_AFINIDAD}), y <em>luego</em> aplica la comparación
 * exacta {@code equalsIgnoreCase} exigida por el negocio entre esa afinidad
 * implícita y la afinidad declarada del tutor. Esto preserva la semántica
 * de "coincidencia exacta de texto" en la comparación final, sin renunciar a
 * la utilidad práctica del filtro.</p>
 *
 * <h2>Algoritmo de clasificación</h2>
 * <ol>
 *   <li>Se normaliza {@code estudiante.getCarrera()} a minúsculas.</li>
 *   <li>Se recorre {@link #MAPA_CARRERA_AFINIDAD} en orden de inserción,
 *       buscando la primera palabra clave que esté <em>contenida</em> en la
 *       carrera normalizada.</li>
 *   <li>Si se encuentra una palabra clave, la afinidad implícita es la
 *       categoría asociada (p. ej., {@code "ingenier"} → {@code "Ciencias Exactas"}).</li>
 *   <li>Si ninguna palabra clave coincide, la afinidad implícita es la
 *       carrera misma, sin transformar — esto permite que, si en el futuro
 *       el administrador registra estudiantes con {@code carrera} igual a
 *       una categoría de afinidad ya existente, el emparejamiento directo
 *       siga funcionando sin requerir cambios en el algoritmo.</li>
 *   <li>Finalmente, se compara {@code tutor.getAfinidad()} contra la
 *       afinidad implícita resuelta mediante {@code equalsIgnoreCase}; si no
 *       hay coincidencia exacta, el tutor se descarta.</li>
 * </ol>
 *
 * <p><b>Nota de mantenimiento:</b> {@link #MAPA_CARRERA_AFINIDAD} es un mapa
 * de clasificación heurístico, pensado para evolucionar junto con el catálogo
 * de carreras y afinidades reales del sistema. A medida que el equipo
 * defina categorías oficiales, este mapa puede externalizarse a un archivo
 * de configuración o a una tabla de la futura base de datos sin alterar la
 * firma pública de esta clase.</p>
 *
 * <h2>Complejidad algorítmica</h2>
 * <p>Sea {@code N} el tamaño de la lista de tutores y {@code K} el número
 * de entradas de {@link #MAPA_CARRERA_AFINIDAD} (constante, no escala con la
 * entrada). Resolver la afinidad implícita de la solicitud cuesta
 * {@code O(K)} y se realiza <strong>una sola vez</strong> por llamado a
 * {@link #buscar(List, Solicitud)} (no por tutor), ya que la carrera del
 * estudiante no cambia durante el recorrido. Evaluar cada tutor cuesta
 * {@code O(1)} (una comparación de cadenas). Por lo tanto, la complejidad
 * temporal total es:</p>
 * <pre>
 *   O(K + N) ≡ O(N)   (con K constante)
 * </pre>
 * <p>La complejidad espacial es {@code O(M)}, donde {@code M} ≤ {@code N} es
 * el número de tutores compatibles encontrados.</p>
 *
 * @author  Bastián
 * @version 1.0
 * @see     EstrategiaBusqueda
 * @see     Tutor#getAfinidad()
 * @see     Estudiante#getCarrera()
 */
public class BusquedaAfinidad implements EstrategiaBusqueda {

    // -------------------------------------------------------------------------
    // Tabla de clasificación carrera → afinidad implícita
    // -------------------------------------------------------------------------

    /**
     * Mapa de clasificación heurística que traduce palabras clave presentes
     * en el nombre de una carrera (en minúsculas, sin acentos eliminados) a
     * una categoría de afinidad compatible con
     * {@link Tutor#getAfinidad()}.
     *
     * <p>Se recorre en <strong>orden de inserción</strong>
     * ({@link LinkedHashMap}) y se aplica la primera palabra clave que esté
     * contenida en la carrera del estudiante, por lo que el orden de las
     * entradas importa cuando una carrera podría calzar con más de una
     * categoría.</p>
     */
    private static final Map<String, String> MAPA_CARRERA_AFINIDAD = new LinkedHashMap<>();

    static {
        // Ciencias Exactas: ingenierías, matemáticas y ciencias duras.
        MAPA_CARRERA_AFINIDAD.put("ingenier",    "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("matemátic",   "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("matematic",   "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("física",      "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("fisica",      "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("química",     "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("quimica",     "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("estadística", "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("estadistica", "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("informátic",  "Ciencias Exactas");
        MAPA_CARRERA_AFINIDAD.put("informatic",  "Ciencias Exactas");

        // Negocios: economía, administración y carreras comerciales.
        MAPA_CARRERA_AFINIDAD.put("comercial",      "Negocios");
        MAPA_CARRERA_AFINIDAD.put("administraci",   "Negocios");
        MAPA_CARRERA_AFINIDAD.put("economía",       "Negocios");
        MAPA_CARRERA_AFINIDAD.put("economia",       "Negocios");
        MAPA_CARRERA_AFINIDAD.put("contador",       "Negocios");
        MAPA_CARRERA_AFINIDAD.put("negocios",       "Negocios");

        // Humanidades: ciencias sociales, derecho, comunicación y letras.
        MAPA_CARRERA_AFINIDAD.put("psicolog",     "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("derecho",      "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("comunicaci",   "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("periodismo",   "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("historia",     "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("literatura",   "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("filosofía",    "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("filosofia",    "Humanidades");
        MAPA_CARRERA_AFINIDAD.put("pedagog",      "Humanidades");

        // Ciencias de la Salud: medicina y carreras afines.
        MAPA_CARRERA_AFINIDAD.put("medicina",     "Ciencias de la Salud");
        MAPA_CARRERA_AFINIDAD.put("enfermería",   "Ciencias de la Salud");
        MAPA_CARRERA_AFINIDAD.put("enfermeria",   "Ciencias de la Salud");
        MAPA_CARRERA_AFINIDAD.put("kinesiolog",   "Ciencias de la Salud");
        MAPA_CARRERA_AFINIDAD.put("nutrición",    "Ciencias de la Salud");
        MAPA_CARRERA_AFINIDAD.put("nutricion",    "Ciencias de la Salud");
    }

    // -------------------------------------------------------------------------
    // Implementación del contrato Strategy
    // -------------------------------------------------------------------------

    /**
     * Filtra la lista de tutores recibida, conservando únicamente aquellos
     * cuya {@link Tutor#getAfinidad() afinidad declarada} coincide en texto
     * exacto (ignorando mayúsculas/minúsculas) con la afinidad implícita
     * derivada de la carrera del estudiante que emite la solicitud.
     *
     * @param tutores   lista de tutores candidatos; no puede ser {@code null}
     * @param solicitud solicitud cuyo estudiante define la afinidad
     *                  implícita objetivo; no puede ser {@code null}
     * @return nueva lista con los tutores cuya afinidad coincide
     *         exactamente con la afinidad implícita del estudiante,
     *         preservando el orden relativo de {@code tutores}; nunca
     *         {@code null} y puede ser vacía si ninguno coincide
     * @throws IllegalArgumentException si {@code tutores} o {@code solicitud}
     *                                  son {@code null}
     */
    @Override
    public List<Tutor> buscar(List<Tutor> tutores, Solicitud solicitud) {
        validarParametros(tutores, solicitud);

        String afinidadImplicita = inferirAfinidadImplicita(
            solicitud.getEstudiante().getCarrera());

        List<Tutor> compatibles = new ArrayList<>();

        for (Tutor tutor : tutores) {
            if (tutor == null) {
                // Programación defensiva: se ignoran entradas nulas en la
                // lista de candidatos en lugar de propagar un NullPointerException.
                continue;
            }

            if (tutor.getAfinidad().equalsIgnoreCase(afinidadImplicita)) {
                compatibles.add(tutor);
            }
        }

        return compatibles;
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Resuelve la afinidad implícita de un estudiante a partir del nombre de
     * su carrera, usando {@link #MAPA_CARRERA_AFINIDAD} como tabla de
     * clasificación por palabras clave.
     *
     * @param carrera carrera del estudiante (garantizada no nula ni en
     *                blanco por la invariante de {@link Estudiante})
     * @return la categoría de afinidad asociada a la primera palabra clave
     *         encontrada en {@code carrera}; si ninguna coincide, retorna la
     *         carrera original sin modificar, como mecanismo de respaldo
     *         para emparejamientos directos futuros
     */
    private static String inferirAfinidadImplicita(String carrera) {
        String carreraNormalizada = carrera.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, String> entrada : MAPA_CARRERA_AFINIDAD.entrySet()) {
            String palabraClave = entrada.getKey();
            if (carreraNormalizada.contains(palabraClave)) {
                return entrada.getValue();
            }
        }

        // Sin coincidencia heurística: se usa la carrera tal cual como
        // afinidad implícita de respaldo.
        return carrera;
    }

    /**
     * Valida de forma defensiva que ningún parámetro de entrada del método
     * {@link #buscar(List, Solicitud)} sea {@code null}.
     *
     * @param tutores   lista de tutores a validar
     * @param solicitud solicitud a validar
     * @throws IllegalArgumentException si {@code tutores} o {@code solicitud}
     *                                  son {@code null}
     */
    private static void validarParametros(List<Tutor> tutores, Solicitud solicitud) {
        if (tutores == null)
            throw new IllegalArgumentException(
                "La lista de tutores no puede ser nula para BusquedaAfinidad.");
        if (solicitud == null)
            throw new IllegalArgumentException(
                "La solicitud no puede ser nula para BusquedaAfinidad.");
    }
}
