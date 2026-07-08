package controlador.comandos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Gestor del historial de comandos ejecutados.
 * Implementa el patrón Command con soporte de deshacer (undo).
 *
 * SINGLETON: una sola instancia porque el historial es global.
 *
 * FLUJO:
 *   PanelCalendario crea ComandoCrearReserva
 *     → HistorialOperaciones.ejecutar(comando)
 *     → comando.execute() internamente
 *     → si fue exitosa, apila el comando
 *     → si no, el mensaje de error está en comando.getMensajeError()
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
     * por lo que verificamos fueExitosa() después de execute().
     *
     * @param comando el comando a ejecutar
     */
    public void ejecutar(Comando comando) {
        comando.execute();
        // Solo apilamos si la operación tuvo éxito
        if (comando instanceof ComandoCrearReserva ccr) {
            if (ccr.fueExitosa()) {
                historial.push(comando);
            }
        } else {
            historial.push(comando);
        }
    }

    /**
     * Deshace el último comando ejecutado.
     *
     * @return true si había algo que deshacer
     */
    public boolean deshacerUltimo() {
        if (historial.isEmpty()) return false;
        historial.pop().unexecute();
        return true;
    }

    public boolean puedeDeshacer()         { return !historial.isEmpty(); }
    public int     cantidadOperaciones()   { return historial.size(); }

    public List<Comando> getHistorial() {
        List<Comando> copia = new ArrayList<>(historial);
        Collections.reverse(copia);
        return copia;
    }
}