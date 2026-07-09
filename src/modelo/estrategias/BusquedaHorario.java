package modelo.estrategias;

import modelo.entidades.ConstantesHorario;
import modelo.entidades.Solicitud;
import modelo.entidades.Tutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Estrategia concreta del patrón Strategy que filtra tutores por coincidencia horaria.
 * Un tutor es compatible con una Solicitud si existe al menos un bloque de la semana
 * en el que tanto el tutor está disponible como el estudiante lo solicitó.
 *
 * Modelo matemático del algoritmo:
 * El sistema representa la disponibilidad semanal como una matriz booleana de dimensiones
 * fijas (DIAS x BLOQUES), donde la fila representa el día de la semana y la columna
 * representa el bloque horario.
 *
 * Sean:
 * - T(d, b) = tutor.isDisponible(dia, bloque)
 * - S(d, b) = solicitud.isBloqueSolicitado(dia, bloque)
 *
 * Un tutor es compatible con la solicitud si y solo si se cumple la siguiente
 * proposición existencial sobre el dominio matricial completo:
 * Existe un dia y un bloque tal que T(dia, bloque) es verdadero Y S(dia, bloque) es verdadero.
 *
 * Es decir, basta con que una sola celda de ambas matrices coincida en verdadero para
 * que el tutor pase el filtro; no se exige coincidencia total de horarios.
 *
 * Complejidad algorítmica:
 * Para cada tutor se realiza, en el peor caso, un recorrido matricial bidimensional completo.
 * Dado que DIAS y BLOQUES son constantes fijas del dominio que no escalan con la entrada,
 * el costo de inspeccionar un tutor es constante, resultando en una complejidad temporal
 * total lineal respecto al tamaño de la lista de tutores.
 *
 * En el mejor caso (coincidencia en el primer bloque evaluado), el corte temprano reduce la
 * inspección de ese tutor al instante. En el peor caso (sin coincidencia o coincidencia en
 * la última celda), se evalúa la matriz completa. La complejidad espacial es proporcional
 * al número de tutores compatibles encontrados.
 *
 * Acoplamiento a las invariantes del dominio:
 * Esta clase no asume ningún valor numérico fijo en duro para las dimensiones: los bucles
 * se construyen sobre ConstantesHorario.DIAS y ConstantesHorario.BLOQUES. Si cambiara la
 * granularidad horaria del sistema, el algoritmo seguiría siendo correcto sin modificaciones.
 */
public class BusquedaHorario implements EstrategiaBusqueda {

    /**
     * Filtra la lista de tutores recibida, conservando únicamente aquellos cuya disponibilidad
     * semanal coincide con el horario deseado de la solicitud en al menos un bloque.
     *
     * Tan pronto se encuentra la primera celda donde ambos marcan disponibilidad simultánea,
     * el tutor se agrega a la lista de resultados y el algoritmo corta inmediatamente el
     * recorrido de ese tutor para pasar al siguiente, optimizando el rendimiento.
     *
     * @param tutores   Lista de tutores candidatos a evaluar.
     * @param solicitud Solicitud con el horario deseado del estudiante.
     * @return Una nueva lista con los tutores horarialmente compatibles, manteniendo el orden original.
     * @throws IllegalArgumentException Si la lista de tutores o la solicitud son nulas.
     */
    @Override
    public List<Tutor> buscar(List<Tutor> tutores, Solicitud solicitud) {
        validarParametros(tutores, solicitud);

        List<Tutor> compatibles = new ArrayList<>();

        for (Tutor tutor : tutores) {
            if (tutor == null) {
                continue;
            }

            if (esHorarioCompatible(tutor, solicitud)) {
                compatibles.add(tutor);
            }
        }

        return compatibles;
    }

    /**
     * Determina si existe al menos un bloque horario en el que un tutor específico
     * esté disponible y coincida con uno solicitado por el estudiante.
     *
     * Implementa el recorrido matricial bidimensional usando un break etiquetado para
     * finalizar ambos bucles tan pronto se detecta la primera coincidencia.
     *
     * @param tutor Tutor a evaluar (se asume no nulo).
     * @param solicitud Solicitud con el horario deseado (se asume no nula).
     * @return Verdadero si existe al menos una intersección horaria entre ambos, falso de lo contrario.
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
     * Valida de forma defensiva que ningún parámetro de entrada sea nulo.
     *
     * @param tutores Lista de tutores a validar.
     * @param solicitud Solicitud a validar.
     * @throws IllegalArgumentException Si la lista de tutores o la solicitud son nulas.
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
