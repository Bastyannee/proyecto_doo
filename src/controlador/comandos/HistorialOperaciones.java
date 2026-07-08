package controlador.comandos;

import java.util.Stack;

/**
 * Gestor del historial de comandos ejecutados — implementación del
 * patrón Command con soporte de deshacer (undo).

 * RESPONSABILIDAD:
 * Mantiene un Stack de comandos ejecutados. Cada reserva confirmada
 * se apila aquí como un ComandoCrearReserva ya ejecutado.
 * "Deshacer" saca el último comando y llama a unexecute().

 * SINGLETON:
 * Una sola instancia en toda la app porque el historial es global
 * — no tiene sentido tener historiales separados por panel.

 * INTEGRACIÓN CON PANELCALENDARIO:
 * PanelCalendario ya no llama a GestorDatos.guardarReserva() directamente.
 * En cambio crea un ComandoCrearReserva, lo pasa a
 * HistorialOperaciones.ejecutar(), que internamente llama a execute()
 * y apila el comando.
 */
public class HistorialOperaciones {

    // ── Singleton ────────────────────────────────────────────

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

    // ── Estado ───────────────────────────────────────────────

    private final Stack<Comando> historial;

    // ── Operaciones ──────────────────────────────────────────

    /**
     * Ejecuta un comando y lo apila en el historial.
     * Si execute() lanza excepción, el comando NO se apila
     * (la operación falló, no hay nada que deshacer).
     *
     * @param comando el comando a ejecutar
     * @throws HorarioOcupadoException si el comando detecta conflicto
     */
    public void ejecutar(Comando comando) {
        comando.execute();      // puede lanzar HorarioOcupadoException
        historial.push(comando); // solo apila si execute() tuvo éxito
    }

    /**
     * Deshace el último comando ejecutado.
     * Llama a unexecute() del comando en el tope del stack
     * y lo elimina del historial.
     *
     * @return true si había algo que deshacer, false si el historial está vacío
     */
    public boolean deshacerUltimo() {
        if (historial.isEmpty()) return false;
        historial.pop().unexecute();
        return true;
    }

    /**
     * Indica si hay operaciones que se puedan deshacer.
     */
    public boolean puedeDeshacer() {
        return !historial.isEmpty();
    }

    /**
     * Devuelve cuántas operaciones hay en el historial.
     */
    public int cantidadOperaciones() {
        return historial.size();
    }

    /**
     * Devuelve una copia del historial como lista para mostrar en la UI.
     * La copia es en orden inverso (más reciente primero).
     */
    public java.util.List<Comando> getHistorial() {
        java.util.List<Comando> copia = new java.util.ArrayList<>(historial);
        java.util.Collections.reverse(copia);
        return copia;
    }
}
