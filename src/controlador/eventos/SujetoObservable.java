package controlador.eventos;
import vista.proxy.PerfilSeleccionable;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base del patrón Observer — el "sujeto" que notifica.
 *
 * Centraliza la lógica de mantener una lista de observadores
 * y notificarlos. ProxyTutor extiende esta clase para heredar
 * ese comportamiento sin repetir código.
 *
 * DECISIÓN DE DISEÑO:
 * Separar SujetoObservable de ProxyTutor respeta el principio
 * de responsabilidad única (SRP):
 *   - SujetoObservable sabe cómo gestionar y notificar observadores.
 *   - ProxyTutor sabe cómo delegar al Tutor real.
 * Ninguno hace el trabajo del otro.
 *
 * REUTILIZACIÓN:
 * Si en el futuro otro objeto del sistema también necesita notificar
 * observadores (por ejemplo, GestorDatos al agregar una reserva),
 * puede extender esta misma clase sin duplicar código.
 */
public abstract class SujetoObservable {

    private final List<Observador> observadores;

    protected SujetoObservable() {
        this.observadores = new ArrayList<>();
    }

    /**
     * Registra un panel como observador.
     * Si el panel ya está registrado, no se agrega de nuevo.
     *
     * @param observador el panel a registrar, no puede ser null
     */
    public void registrarObservador(Observador observador) {
        if (observador == null) return;
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    /**
     * Elimina un panel de la lista de observadores.
     * Llamar cuando el panel se cierre o deje de necesitar
     * actualizaciones.
     *
     * @param observador el panel a eliminar
     */
    public void eliminarObservador(Observador observador) {
        observadores.remove(observador);
    }

    /**
     * Notifica a todos los observadores registrados.
     * Se itera sobre una copia de la lista para evitar
     * ConcurrentModificationException si un observador
     * se desregistra durante la notificación.
     *
     * @param perfil el perfil del tutor activo a comunicar
     */
    protected void notificarObservadores(PerfilSeleccionable perfil) {
        List<Observador> copia = new ArrayList<>(observadores);
        for (Observador o : copia) {
            o.actualizar(perfil);
        }
    }

    /**
     * Devuelve cuántos observadores están registrados.
     * Útil para depuración y tests.
     */
    public int cantidadObservadores() {
        return observadores.size();
    }
}