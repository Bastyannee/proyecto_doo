package controlador.comandos;
import modelo.ConflictoHorarioException;
import modelo.GestorDatos;
import modelo.entidades.Reserva;
import modelo.entidades.Solicitud;
import modelo.entidades.Solicitud.EstadoSolicitud;
import modelo.entidades.Tutor;
import java.time.LocalDate;

/**
 * Comando concreto que encapsula la creación de una reserva de clase entre un estudiante y un
 * profesor en un día y bloque horario específicos. Es construido e invocado por el Administrador
 * (típicamente a través de HistorialOperaciones) para agendar la hora, validando antes de
 * confirmar que ambos participantes estén libres en el calendario compartido. En caso de que el
 * profesor ya tenga otra clase agendada en ese mismo bloque, la operación se interrumpe
 * lanzando HorarioOcupadoException y el calendario no sufre ninguna modificación.
 */
public class ComandoCrearReserva implements Comando {
    private final Solicitud solicitud;
    private final Tutor tutor;
    private final LocalDate fecha;
    private final int diaIndex;
    private final int bloqueIndex;

    /**
     * Reserva efectivamente creada y persistida por este comando.
     * Permanece Null si la ejecución terminó en un conflicto de horario.
     */
    private Reserva reservaCreada;

    /**
     * Mensaje del último ConflictoHorarioException capturado al ejecutar el comando.
     * Permanece Null si la reserva se creó sin problemas.
     */
    private String mensajeError;

    public ComandoCrearReserva(Solicitud solicitud, Tutor tutor, LocalDate fecha, int diaIndex, int bloqueIndex) {
        this.solicitud = solicitud;
        this.tutor = tutor;
        this.fecha = fecha;
        this.diaIndex = diaIndex;
        this.bloqueIndex = bloqueIndex;
    }

    /**
     * Verifica disponibilidad en el calendario y, si corresponde, registra la ocupación del profesor y del estudiante.
     * @throws ConflictoHorarioException si el profesor ya está ocupado con otro estudiante en ese bloque.
     */
    @Override
    public void execute() {
        Reserva nuevaReserva = new Reserva(tutor, solicitud.getEstudiante(), solicitud, fecha, diaIndex, bloqueIndex);
        try {
            GestorDatos.getInstancia().guardarReserva(nuevaReserva);
            solicitud.setEstado(EstadoSolicitud.CONVERTIDA);
            this.reservaCreada = nuevaReserva;
        }
        catch (ConflictoHorarioException e) {
            this.mensajeError = e.getMessage();
            }
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getDiaIndex() {
        return diaIndex;
    }

    public int getBloqueIndex() {
        return bloqueIndex;
    }

    /**
     * @return la reserva creada por este comando, o Null si la ejecución no llegó a completarse por un conflicto de horario.
     */
    public Reserva getReservaCreada() {
        return reservaCreada;
    }

    /**
     * @return True si execute() logró crear y persistir la reserva sin conflicto de horario.
     */
    public boolean fueExitosa() {
        return reservaCreada != null;
    }

    /**
     * @return el mensaje del conflicto de horario detectado.
     */
    public String getMensajeError() {
        return mensajeError;
    }
}