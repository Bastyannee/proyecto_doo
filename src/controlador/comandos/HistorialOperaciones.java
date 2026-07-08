package controlador.comandos;

import modelo.GestorDatos;
import modelo.entidades.Reserva;
import modelo.entidades.Solicitud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Gestor del historial de comandos ejecutados.

 * SINGLETON: una sola instancia porque el historial es global.

 * DESHACER:
 * Comando no tiene unexecute(), así que HistorialOperaciones implementa
 * el deshacer directamente: obtiene la reserva del ComandoCrearReserva
 * apilado, la cancela via setEstado(CANCELADA) y restaura la solicitud
 * a PENDIENTE. Esto respeta el modelo real sin modificar interfaces.
 */
public class HistorialOperaciones {

    private static volatile HistorialOperaciones instancia;

    private HistorialOperaciones() {
        this.historial = new Stack<>();
    }

    public static HistorialOperaciones getInstancia() {
        if (instancia == null) {
            synchronized (HistorialOperaciones.class) {
                if (instancia == null) {
                    instancia = new HistorialOperaciones();
                }
            }
        }
        return instancia;
    }

    private final Stack<Comando> historial;

    /**
     * Ejecuta un comando y lo apila solo si fue exitoso.
     * ComandoCrearReserva maneja ConflictoHorarioException internamente,
     * verificamos fueExitosa() para decidir si apilar.
     */
    public void ejecutar(Comando comando) {
        comando.execute();
        if (comando instanceof ComandoCrearReserva ccr) {
            if (ccr.fueExitosa()) historial.push(comando);
        } else {
            historial.push(comando);
        }
    }

    /**
     * Deshace el último ComandoCrearReserva del historial:
     * - Cancela la reserva via setEstado(CANCELADA)
     * - Restaura la solicitud a PENDIENTE
     * - La elimina del stack
     *
     * @return true si había algo que deshacer
     */
    public boolean deshacerUltimo() {
        if (historial.isEmpty()) return false;

        Comando ultimo = historial.pop();

        if (ultimo instanceof ComandoCrearReserva ccr) {
            Reserva reserva = ccr.getReservaCreada();
            if (reserva != null) {
                // Cancelar la reserva usando el metodo correcto de Reserva
                reserva.setEstado(Reserva.EstadoReserva.CANCELADA);

                // Restaurar la solicitud a pendiente para que vuelva a aparecer
                Solicitud solicitud = ccr.getSolicitud();
                if (solicitud != null) {
                    solicitud.setEstado(Solicitud.EstadoSolicitud.PENDIENTE);
                }
            }
        }

        return true;
    }

    public boolean puedeDeshacer()       { return !historial.isEmpty(); }
    public int     cantidadOperaciones() { return historial.size(); }

    public List<Comando> getHistorial() {
        List<Comando> copia = new ArrayList<>(historial);
        Collections.reverse(copia);
        return copia;
    }
}