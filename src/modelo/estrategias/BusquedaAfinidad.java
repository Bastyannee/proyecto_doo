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
 * Estrategia concreta del patrón Strategy que filtra tutores por afinidad temática.
 * Un tutor se considera compatible con una Solicitud si el área de afinidad declarada
 * por él coincide, ignorando mayúsculas y minúsculas, con la afinidad implícita del
 * estudiante que emite la solicitud.
 *
 * El problema de la afinidad implícita:
 * La entidad Estudiante no almacena un campo de afinidad explícito (solo su carrera),
 * mientras que Tutor sí cuenta con una afinidad categórica definida (por ejemplo,
 * "Ciencias Exactas", "Humanidades", "Negocios"). Comparar de manera literal la afinidad
 * del tutor con la carrera del estudiante raramente produciría coincidencias reales.
 * Por ello, esta estrategia resuelve primero una afinidad implícita a partir de la carrera
 * del alumno mediante un mapa de clasificación por palabras clave y, posteriormente,
 * aplica la comparación exacta requerida por el negocio.
 *
 * Algoritmo de clasificación:
 * 1. Se normaliza el texto de la carrera del estudiante a minúsculas.
 * 2. Se recorre el mapa de afinidades en su orden de inserción, buscando la primera
 *    palabra clave que se encuentre contenida en la carrera normalizada.
 * 3. Si se encuentra una coincidencia, la afinidad implícita será la categoría asociada.
 * 4. Si ninguna palabra clave coincide, se retorna la carrera misma sin transformar como
 *    mecanismo de respaldo para emparejamientos directos futuros.
 * 5. Finalmente, se compara la afinidad del tutor contra la afinidad implícita resuelta;
 *    si no hay coincidencia exacta ignorando mayúsculas, el tutor es descartada.
 *
 * Complejidad algorítmica:
 * Resolver la afinidad implícita de la solicitud se realiza una sola vez por llamado,
 * de modo que evaluar a cada tutor individual toma un tiempo constante. La complejidad
 * temporal total es lineal respecto al número de tutores en la lista, mientras que la
 * complejidad espacial es proporcional a la cantidad de tutores compatibles encontrados.
 */
public class BusquedaAfinidad implements EstrategiaBusqueda {

    /**
     * Mapa de clasificación heurística que traduce palabras clave presentes en el nombre
     * de una carrera a una categoría de afinidad compatible con el perfil del Tutor.
     * Se utiliza un LinkedHashMap para respetar de manera estricta el orden de inserción
     * al evaluar las palabras clave.
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

    /**
     * Filtra la lista de tutores candidatos, conservando únicamente a aquellos cuya
     * afinidad declarada coincide con la afinidad implícita derivada de la carrera del estudiante.
     *
     * @param tutores   Lista de tutores candidatos a evaluar.
     * @param solicitud Solicitud que contiene al estudiante y su carrera de origen.
     * @return Una nueva lista con los tutores compatibles detectados, manteniendo el orden original.
     * @throws IllegalArgumentException Si la lista de tutores o la solicitud recibida son nulas.
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

    /**
     * Infiere la categoría de afinidad implícita evaluando las palabras clave del mapa
     * sobre la carrera del estudiante.
     *
     * @param carrera Nombre de la carrera universitaria del estudiante.
     * @return La categoría de afinidad emparejada, o la carrera original en caso de no hallar coincidencia.
     */
    private static String inferirAfinidadImplicita(String carrera) {
        String carreraNormalizada = carrera.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, String> entrada : MAPA_CARRERA_AFINIDAD.entrySet()) {
            String palabraClave = entrada.getKey();
            if (carreraNormalizada.contains(palabraClave)) {
                return entrada.getValue();
            }
        }

        return carrera;
    }

    /**
     * Valida de manera defensiva que las referencias de los parámetros de búsqueda no sean nulas.
     *
     * @param tutores Lista de tutores a comprobar.
     * @param solicitud Solicitud a comprobar.
     * @throws IllegalArgumentException Si alguno de los dos objetos es nulo.
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
