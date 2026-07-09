package controlador.eventos;
import vista.proxy.PerfilSeleccionable;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base abstracta para el sujeto del patrón Observer.
 * Gestiona y notifica a la lista de paneles observadores registrados.
 *
 * Separar esta clase de ProxyTutor respeta el principio de responsabilidad única
 * y permite reutilizar esta lógica en otros objetos del sistema en el futuro.
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
     * Notifica el cambio de tutor activo a todos los observadores registrados.
     * Usa una copia de la lista para evitar errores de modificación concurrente.
     *
     * @param perfil Los datos del tutor activo actual.
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