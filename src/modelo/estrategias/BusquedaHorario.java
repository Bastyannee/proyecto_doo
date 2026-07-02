package modelo.estrategias;

import modelo.entidades.ConstantesHorario;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Estrategia concreta del patrón <strong>Strategy</strong> que filtra tutores
 * por <em>coincidencia horaria</em>: un tutor es compatible con una
 * {@link Solicitud} si existe al menos un bloque de la semana en el que
 * tanto el tutor está disponible como el estudiante lo solicitó.
 *
 * <h2>Modelo matemático del algoritmo</h2>
 * <p>El sistema representa la disponibilidad semanal como una matriz
 * booleana de dimensiones fijas
 * {@code boolean[}{@value ConstantesHorario#DIAS}{@code ][}{@value ConstantesHorario#BLOQUES}{@code ]},
 * donde la fila {@code dia} ∈ [0, {@value ConstantesHorario#DIAS}) representa
 * el día de la semana y la columna {@code bloque} ∈
 * [0, {@value ConstantesHorario#BLOQUES}) representa el bloque horario.</p>
 *
 * <p>Sean:</p>
 * <ul>
 *   <li>{@code T(d, b)} = {@code tutor.isDisponible(d, b)}</li>
 *   <li>{@code S(d, b)} = {@code solicitud.isBloqueSolicitado(d, b)}</li>
 * </ul>
 *
 * <p>Un tutor {@code t} es <strong>compatible</strong> con la solicitud
 * {@code s} si y solo si se cumple la siguiente proposición existencial sobre
 * el dominio matricial completo:</p>
 *
 * <pre>
 *   ∃ d ∈ [0, DIAS), ∃ b ∈ [0, BLOQUES) :  T(d, b) ∧ S(d, b)
 * </pre>
 *
 * <p>Es decir, basta con que <strong>una sola celda</strong> de ambas matrices
 * coincida en {@code true} para que el tutor pase el filtro; no se exige
 * coincidencia total de horarios, ya que el sistema busca cualquier
 * intersección viable que el administrador pueda ofrecer al estudiante.</p>
 *
 * <h2>Complejidad algorítmica</h2>
 * <p>Sea {@code N} el tamaño de la lista de tutores. Para cada tutor se
 * realiza, en el peor caso, un recorrido matricial bidimensional completo de
 * {@code DIAS × BLOQUES} celdas mediante bucles {@code for} anidados.  Dado
 * que {@code DIAS} ({@value ConstantesHorario#DIAS}) y {@code BLOQUES}
 * ({@value ConstantesHorario#BLOQUES}) son <strong>constantes fijas</strong>
 * del dominio (no escalan con la entrada), el costo de inspeccionar un tutor
 * es {@code O(DIAS × BLOQUES) = O(30) = O(1)} respecto al tamaño de la
 * entrada real del problema.</p>
 *
 * <p>Por lo tanto, la complejidad temporal total del algoritmo es:</p>
 * <pre>
 *   O(N × DIAS × BLOQUES)  ≡  O(N)   (con DIAS y BLOQUES constantes)
 * </pre>
 *
 * <p>En el <strong>mejor caso</strong> (el primer bloque inspeccionado, en la
 * esquina {@code [0][0]}, ya es una coincidencia), el corte temprano mediante
 * la etiqueta {@code recorridoTutor} reduce la inspección de ese tutor a
 * {@code O(1)} celdas. En el <strong>peor caso</strong> (el tutor no es
 * compatible en ningún bloque, o la única coincidencia está en la última
 * celda {@code [DIAS-1][BLOQUES-1]}), se inspeccionan las
 * {@code DIAS × BLOQUES} celdas completas antes de descartarlo o aceptarlo.</p>
 *
 * <p>La complejidad espacial es {@code O(K)}, donde {@code K} ≤ {@code N} es
 * el número de tutores compatibles encontrados, correspondiente a la nueva
 * lista de resultados.</p>
 *
 * <h2>Acoplamiento a las invariantes del dominio</h2>
 * <p>Esta clase no asume ningún valor concreto para las dimensiones de la
 * matriz: los bucles se construyen estrictamente sobre
 * {@link ConstantesHorario#DIAS} y {@link ConstantesHorario#BLOQUES}. Esto
 * garantiza que, si en el futuro cambiara la granularidad horaria del
 * sistema (por ejemplo, de 6 a 8 bloques diarios), el algoritmo seguiría
 * siendo correcto sin requerir modificación, ya que delega completamente la
 * validación de rangos e índices en {@link Tutor#isDisponible(int, int)} y
 * {@link Solicitud#isBloqueSolicitado(int, int)} — ambos métodos ya
 * garantizan, por invariante de clase, que sus matrices internas miden
 * exactamente {@code DIAS × BLOQUES}.</p>
 *
 * @author  Bastián
 * @version 1.0
 * @see     EstrategiaBusqueda
 * @see     ConstantesHorario
 * @see     Tutor#isDisponible(int, int)
 * @see     Solicitud#isBloqueSolicitado(int, int)
 */
public class BusquedaHorario implements EstrategiaBusqueda {

    /**
     * Filtra la lista de tutores recibida, conservando únicamente aquellos
     * cuya disponibilidad semanal coincide con el horario deseado de la
     * solicitud en al menos un bloque.
     *
     * <p>Para cada tutor se recorre la matriz {@code DIAS × BLOQUES} con
     * bucles anidados. Tan pronto se encuentra la primera celda
     * {@code (dia, bloque)} donde ambos, tutor y solicitud, marcan
     * disponibilidad/preferencia simultánea, el tutor se agrega una única
     * vez a la lista de resultados y el algoritmo corta inmediatamente el
     * recorrido de ese tutor (mediante un {@code break} etiquetado) para
     * pasar al siguiente, evitando tanto trabajo redundante como duplicados
     * en la lista de salida.</p>
     *
     * @param tutores   lista de tutores candidatos; no puede ser {@code null}
     * @param solicitud solicitud con el horario deseado del estudiante; no
     *                  puede ser {@code null}
     * @return nueva lista con los tutores horarialmente compatibles,
     *         preservando el orden relativo de {@code tutores}; nunca
     *         {@code null} y puede ser vacía si ninguno coincide
     * @throws IllegalArgumentException si {@code tutores} o {@code solicitud}
     *                                  son {@code null}
     */
    @Override
    public List<Tutor> buscar(List<Tutor> tutores, Solicitud solicitud) {
        validarParametros(tutores, solicitud);

        List<Tutor> compatibles = new ArrayList<>();

        for (Tutor tutor : tutores) {
            if (tutor == null) {
                // Programación defensiva: se ignoran entradas nulas en la
                // lista de candidatos en lugar de propagar un NullPointerException.
                continue;
            }

            if (esHorarioCompatible(tutor, solicitud)) {
                compatibles.add(tutor);
            }
        }

        return compatibles;
    }

    /**
     * Determina si existe al menos un bloque horario en el que un tutor
     * específico esté disponible y dicho bloque coincida con uno solicitado
     * por el estudiante.
     *
     * <p>Implementa el recorrido matricial bidimensional descrito en la
     * documentación de clase, usando un {@code break} etiquetado
     * ({@code recorridoTutor}) para finalizar ambos bucles tan pronto se
     * detecta la primera coincidencia, optimizando el caso promedio sin
     * alterar la complejidad de peor caso.</p>
     *
     * @param tutor     tutor a evaluar; se asume no nulo (validado por el
     *                  llamador)
     * @param solicitud solicitud con el horario deseado; se asume no nula
     *                  (validada por el llamador)
     * @return {@code true} si existe al menos una celda {@code (dia, bloque)}
     *         donde {@code tutor.isDisponible(dia, bloque)} y
     *         {@code solicitud.isBloqueSolicitado(dia, bloque)} son ambos
     *         {@code true}
     */
    private boolean esHorarioCompatible(Tutor tutor, Solicitud solicitud) {
        boolean coincidenciaEncontrada = false;

        recorridoTutor:
        for (int dia = 0; dia < ConstantesHorario.DIAS; dia++) {
            for (int bloque = 0; bloque < ConstantesHorario.BLOQUES; bloque++) {

                boolean deseadoPorEstudiante = solicitud.isBloqueSolicitado(dia, bloque);
                boolean disponiblePorTutor   = tutor.isDisponible(dia, bloque);

                if (deseadoPorEstudiante && disponiblePorTutor) {
                    coincidenciaEncontrada = true;
                    break recorridoTutor;
                }
            }
        }

        return coincidenciaEncontrada;
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
                "La lista de tutores no puede ser nula para BusquedaHorario.");
        if (solicitud == null)
            throw new IllegalArgumentException(
                "La solicitud no puede ser nula para BusquedaHorario.");
    }
}
