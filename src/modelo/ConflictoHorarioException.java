package modelo;

/**
 * Excepción de dominio lanzada por {@link GestorDatos} cuando se intenta
 * registrar una {@link modelo.entidades.Reserva} que solapa con otra reserva
 * activa del mismo tutor en el mismo bloque horario.
 *
 * <p>Es una excepción no verificada ({@code RuntimeException}) porque
 * representa una violación de una regla de negocio que el administrador debe
 * resolver eligiendo otro bloque, no un error recuperable de forma genérica
 * por el código que invoca al gestor.</p>
 *
 * @author  Bastián
 * @version 1.0
 * @see     GestorDatos#guardarReserva(modelo.entidades.Reserva)
 */
public class ConflictoHorarioException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo del conflicto detectado.
     *
     * @param mensaje detalle del conflicto (p. ej., tutor, día y bloque
     *                involucrados)
     */
    public ConflictoHorarioException(String mensaje) {
        super(mensaje);
    }
}
