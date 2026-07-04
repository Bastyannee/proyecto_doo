/**
 * Comando concreto que encapsula la creación de una reserva de clase entre un estudiante y un
 * profesor en un día y bloque horario específicos. Es construido e invocado por el Administrador
 * (típicamente a través de HistorialOperaciones) para agendar la hora, validando antes de
 * confirmar que ambos participantes estén libres en el calendario compartido. En caso de que el
 * profesor ya tenga otra clase agendada en ese mismo bloque, la operación se interrumpe
 * lanzando HorarioOcupadoException y el calendario no sufre ninguna modificación.
 */
public class ComandoCrearReserva implements Comando {
    private final Estudiante estudiante;
    private final Profesor profesor;
    private final int dia;
    private final int bloque;
    private final Calendario calendario;

    /**
     * Indica si este comando efectivamente llegó a reservar el bloque
     */
    private boolean reservaConfirmada;

    public ComandoCrearReserva(Estudiante estudiante, Profesor profesor, int dia, int bloque, Calendario calendario) {
        this.estudiante = estudiante;
        this.profesor = profesor;
        this.dia = dia;
        this.bloque = bloque;
        this.calendario = calendario;
        this.reservaConfirmada = false;
    }

    /**
     * Verifica disponibilidad en el calendario y, si corresponde, registra la ocupación del profesor y del estudiante.
     * @throws HorarioOcupadoException si el profesor ya está ocupado con otro estudiante en ese bloque.
     */
    @Override
    public void execute() {
        if (!calendario.estaProfesorDisponible(profesor, dia, bloque)) {
            throw new HorarioOcupadoException("No se pudo agendar: el profesor " + profesor.getNombre() + " ya esta ocupado el " + Calendario.nombreDia(dia) + " en el bloque " + bloque + ".");
        }

        calendario.reservarBloque(estudiante, profesor, dia, bloque);
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public int getDia() {
        return dia;
    }

    public int getBloque() {
        return bloque;
    }
}
