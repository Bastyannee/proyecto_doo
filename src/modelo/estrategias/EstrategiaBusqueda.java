package modelo.estrategias;

import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;

import java.util.List;

/**
 * Resumen de la estrategia de búsqueda por afinidad temática.
 *
 * Esta estrategia resuelve la incompatibilidad directa entre la carrera del
 * estudiante y la categoría de afinidad del tutor mediante un proceso de
 * traducción heurística basado en palabras clave.
 *
 * Características principales del flujo:
 * - Resolución implícita: Analiza el nombre de la carrera del estudiante
 *   utilizando un mapa ordenado de palabras clave para determinar su
 *   categoría equivalente (por ejemplo, "ingenier" se traduce a "Ciencias Exactas").
 * - Mecanismo de respaldo: Si ninguna palabra clave coincide, utiliza el
 *   nombre de la carrera original sin modificar, permitiendo emparejamientos
 *   directos en el futuro.
 * - Comparación final: Evalúa la categoría obtenida contra la afinidad
 *   declarada del tutor de forma insensible a mayúsculas y minúsculas.
 *
 * Rendimiento y seguridad:
 * - El cálculo de la afinidad se realiza una única vez por búsqueda,
 *   garantizando una complejidad temporal lineal respecto al número de tutores.
 * - Aplica programación defensiva ignorando elementos nulos dentro de la
 *   lista de candidatos y validando los parámetros de entrada.
 */
public interface EstrategiaBusqueda {

    /**
     * Filtra la lista de tutores recibida, retornando únicamente aquellos que resultan
     * compatibles con la Solicitud según el criterio particular de la estrategia concreta.
     *
     * @param tutores Lista de tutores candidatos sobre los que se aplicará el filtro.
     *                No puede ser nula (si está vacía, se retorna una lista vacía).
     * @param solicitud Solicitud del estudiante contra la cual se evalúa la compatibilidad.
     *                  No puede ser nula.
     * @return Una nueva lista, posiblemente vacía, con los tutores que cumplen el criterio
     *         de compatibilidad; nunca nula.
     * @throws IllegalArgumentException Si la lista de tutores o la solicitud son nulas.
     */
    List<Tutor> buscar(List<Tutor> tutores, Solicitud solicitud);
}
