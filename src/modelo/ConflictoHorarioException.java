package modelo;

/**
 * Excepción de dominio lanzada por GestorDatos cuando se intenta registrar una
 * Reserva que se solapa con otra reserva activa del mismo tutor en el mismo bloque horario.
 *
 * Es una excepción no verificada (RuntimeException) porque representa una violación
 * de una regla de negocio que el administrador debe resolver eligiendo otro bloque,
 * no un error recuperable de forma genérica por el código que invoca al gestor.
 */
public class ConflictoHorarioException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo del conflicto detectado.
     *
     * @param mensaje Detalle del conflicto (por ejemplo: tutor, día y bloque involucrados).
     */
    public ConflictoHorarioException(String mensaje) {
        super(mensaje);
    }
}
