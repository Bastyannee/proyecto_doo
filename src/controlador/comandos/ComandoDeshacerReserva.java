package controlador.comandos;
import modelo.entidades.Reserva;
import modelo.entidades.Reserva.EstadoReserva;
import modelo.entidades.Solicitud;
import modelo.entidades.Solicitud.EstadoSolicitud;

/**
 * Permite al Administrador cancelar de forma directa una reserva ya existente en el calendario,
 * a diferencia de revertir el comando de creación desde el historial. Al ejecutarse (execute()),
 * aplica la lógica inversa liberando el horario de ambos participantes, mientras que al deshacerse
 * (unexecute()) vuelve a reservar el bloque, lo que permite que sea apilado en el historial de
 * operaciones para aprovechar el mecanismo de deshacer y rehacer.
 */
public class ComandoDeshacerReserva implements Comando {
    private final Reserva reserva;
    private boolean deshecha;

    public ComandoDeshacerReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    @Override
    public void execute() {
        deshacer();
    }

    public void deshacer() {
        if (reserva == null || deshecha) {
            return;
        }

        reserva.setEstado(EstadoReserva.CANCELADA);

        Solicitud solicitudOrigen = reserva.getSolicitudOrigen();
        if (solicitudOrigen != null) {
            solicitudOrigen.setEstado(EstadoSolicitud.PENDIENTE);
        }

        deshecha = true;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public boolean isDeshecha() {
        return deshecha;
    }
}

