/**
 * excepción lanzada cuando el administrador intenta reservar un bloque horario que ya se encuentra ocupado.
 */
public class HorarioOcupadoException extends RuntimeException {

    public HorarioOcupadoException(String mensaje) {
        super(mensaje);
    }
}
