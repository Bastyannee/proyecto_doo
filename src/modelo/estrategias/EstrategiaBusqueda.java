package modelo.estrategias;

import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;

import java.util.List;

/**
 * Contrato único del patrón de diseño <strong>Strategy</strong> para los
 * algoritmos de búsqueda y filtrado de {@link Tutor}es compatibles con una
 * {@link Solicitud} en el Sistema de Reservas de Clases Particulares.
 *
 * <p>Cada implementación concreta encapsula un criterio de compatibilidad
 * distinto (coincidencia horaria, afinidad temática, etc.) bajo la misma
 * firma, permitiendo que el componente invocador —típicamente
 * {@code BuscadorDeTutores} en el controlador— intercambie el algoritmo en
 * tiempo de ejecución sin conocer los detalles internos de cada estrategia
 * ni modificar su propio código (principio Abierto/Cerrado).</p>
 *
 * <p>Diagrama conceptual de colaboración:</p>
 * <pre>
 *   Administrador → BuscadorDeTutores.setEstrategia(unaEstrategia)
 *   Administrador → BuscadorDeTutores.ejecutarBusqueda(tutores, solicitud)
 *                        │
 *                        └──► EstrategiaBusqueda.buscar(tutores, solicitud)
 *                                 ├── BusquedaHorario   (coincidencia de matriz)
 *                                 └── BusquedaAfinidad  (coincidencia de texto)
 * </pre>
 *
 * <p><strong>Contrato general que toda implementación debe respetar:</strong></p>
 * <ul>
 *   <li>No debe mutar la lista {@code tutores} recibida ni los objetos
 *       {@link Tutor} que contiene; el filtrado es de solo lectura.</li>
 *   <li>No debe mutar la {@link Solicitud} recibida.</li>
 *   <li>Debe retornar una <strong>nueva</strong> instancia de {@link List},
 *       nunca la misma referencia que el parámetro {@code tutores}.</li>
 *   <li>Debe validar de forma defensiva que ningún parámetro sea
 *       {@code null}, lanzando {@link IllegalArgumentException} en caso
 *       contrario.</li>
 *   <li>Debe ser independiente de la capa de presentación: ninguna
 *       implementación puede importar {@code javax.swing.*},
 *       {@code java.awt.*} ni desplegar elementos visuales.</li>
 * </ul>
 *
 * @author  Bastián
 * @version 1.0
 * @see     Tutor
 * @see     Solicitud
 */
public interface EstrategiaBusqueda {

    /**
     * Filtra la lista de tutores recibida, retornando únicamente aquellos que
     * resultan compatibles con la {@link Solicitud} según el criterio
     * particular de la estrategia concreta.
     *
     * @param tutores   lista de tutores candidatos sobre los que se aplicará
     *                  el filtro; no puede ser {@code null} (puede estar
     *                  vacía, en cuyo caso se retorna una lista vacía)
     * @param solicitud solicitud del estudiante contra la cual se evalúa la
     *                  compatibilidad; no puede ser {@code null}
     * @return nueva lista, posiblemente vacía, con los tutores que cumplen el
     *         criterio de compatibilidad de la estrategia; nunca {@code null}
     * @throws IllegalArgumentException si {@code tutores} o {@code solicitud}
     *                                  son {@code null}
     */
    List<Tutor> buscar(List<Tutor> tutores, Solicitud solicitud);
}
