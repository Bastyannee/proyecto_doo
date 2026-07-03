/**
 * Permite al Administrador cancelar de forma directa una reserva ya existente en el calendario,
 * a diferencia de revertir el comando de creación desde el historial. Al ejecutarse (execute()),
 * aplica la lógica inversa liberando el horario de ambos participantes, mientras que al deshacerse
 * (unexecute()) vuelve a reservar el bloque, lo que permite que sea apilado en el historial de
 * operaciones para aprovechar el mecanismo de deshacer y rehacer.
 */
public class ComandoDeshacerReserva implements Comando {
    private final Estudiante estudiante;
    private final Profesor profesor;
    private final int dia;
    private final int bloque;
    private final Calendario calendario;

    public ComandoDeshacerReserva(Estudiante estudiante, Profesor profesor, int dia, int bloque, Calendario calendario) {
        this.estudiante = estudiante;
        this.profesor = profesor;
        this.dia = dia;
        this.bloque = bloque;
        this.calendario = calendario;
    }

    /**
     * Libera el bloque horario del profesor y del estudiante en el calendario, cancelando la reserva existente.
     */
    @Override
    public void execute() {
        calendario.liberarBloque(estudiante, profesor, dia, bloque);
        bloqueLiberado = true;
    }
}
