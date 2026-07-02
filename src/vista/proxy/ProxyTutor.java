package vista.proxy;

import controlador.eventos.SujetoObservable;
import controlador.eventos.Observador;
import modelo.entidades.Tutor;

/**
 * Proxy virtual del tutor actualmente seleccionado en la aplicación.
 *
 * PATRÓN PROXY (Virtual):
 * ProxyTutor implementa PerfilSeleccionable igual que Tutor, por lo
 * que cualquier panel que trabaje con PerfilSeleccionable puede recibir
 * un ProxyTutor sin saberlo. Cuando el admin selecciona otro tutor en
 * la lista, no se recargan todos los paneles manualmente: simplemente
 * se llama a seleccionar(tutor) y el proxy redirige todas las llamadas
 * al nuevo tutor, notificando automáticamente a los paneles registrados.
 *
 * PATRÓN SINGLETON:
 * Existe exactamente una instancia en toda la aplicación porque solo
 * hay un "tutor seleccionado" en un momento dado. Múltiples instancias
 * generarían inconsistencias entre paneles.
 *
 * INTEGRACIÓN CON OBSERVER:
 * ProxyTutor extiende SujetoObservable, heredando la gestión de
 * observadores y la notificación. Cuando cambia el tutor activo,
 * llama a notificarObservadores() y cada panel registrado recibe
 * actualizar() y ejecuta repaint() automáticamente.
 *
 * FLUJO COMPLETO:
 *   Admin hace clic en tutor de la lista
 *     → PanelDirectorio llama: ProxyTutor.getInstancia().seleccionar(tutor)
 *     → ProxyTutor actualiza tutorActual
 *     → SujetoObservable.notificarObservadores() avisa a cada panel
 *     → Cada panel refresca su contenido y llama repaint()
 */
public class ProxyTutor extends SujetoObservable implements PerfilSeleccionable {

    // Singleton
    // =========================================================

    private static ProxyTutor instancia;

    /**
     * Constructor privado: impide instanciación externa.
     */
    private ProxyTutor() {
        super();
        this.tutorActual = null;
    }

    /**
     * Devuelve la única instancia de ProxyTutor.
     * Usa double-checked locking para thread-safety con Swing,
     * que trabaja con el Event Dispatch Thread por separado.
     *
     * @return la instancia única del proxy
     */
    public static ProxyTutor getInstancia() {
        if (instancia == null) {
            synchronized (ProxyTutor.class) {
                if (instancia == null) {
                    instancia = new ProxyTutor();
                }
            }
        }
        return instancia;
    }

    // Estado del Proxy
    // =========================================================

    /** El tutor real actualmente seleccionado. */
    private Tutor tutorActual;

    // Operación central del Proxy
    // =========================================================

    /**
     * Selecciona un tutor como el perfil activo de la aplicación
     * y notifica automáticamente a todos los paneles registrados.
     *
     * Este es el unico metodo que PanelDirectorio (o cualquier lista
     * de tutores) necesita llamar cuando el admin hace clic en uno.
     *
     * @param tutor el tutor a seleccionar (null limpia la selección)
     */
    public void seleccionar(Tutor tutor) {
        this.tutorActual = tutor;
        notificarObservadores(this);
    }

    /**
     * Devuelve el tutor real actualmente seleccionado.
     * Útil cuando un Command necesita el objeto Tutor concreto.
     *
     * @return el tutor activo, o null si no hay selección
     */
    public Tutor getTutorActual() {
        return tutorActual;
    }

    /**
     * Indica si hay algún tutor seleccionado actualmente.
     *
     * @return true si hay un tutor activo
     */
    public boolean hayTutorSeleccionado() {
        return tutorActual != null;
    }

    // Implementación de PerfilSeleccionable — delegación al real
    // =========================================================
    // Cada metodo verifica que haya tutor antes de delegar.
    // Si no hay tutor seleccionado, devuelve valores neutros para
    // que los paneles puedan inicializarse vacíos sin NullPointerException.

    @Override
    public String getId() {
        return tutorActual != null ? tutorActual.getId() : "";
    }

    @Override
    public String getNombre() {
        return tutorActual != null ? tutorActual.getNombre() : "Sin tutor seleccionado";
    }

    @Override
    public String getDescripcion() {
        return tutorActual != null ? tutorActual.getDescripcion() : "";
    }

    @Override
    public String getMateria() {
        return tutorActual != null ? tutorActual.getMateria() : "";
    }

    @Override
    public String getAfinidad() {
        return tutorActual != null ? tutorActual.getAfinidad() : "";
    }

    @Override
    public String getFotoPath() {
        return tutorActual != null ? tutorActual.getFotoPath() : "";
    }

    @Override
    public double getTarifa() {
        return tutorActual != null ? tutorActual.getTarifa() : 0.0;
    }

    @Override
    public int getMaxEstudiantes() {
        return tutorActual != null ? tutorActual.getMaxEstudiantes() : 0;
    }

    @Override
    public boolean isDisponible(int dia, int bloque) {
        return tutorActual != null && tutorActual.isDisponible(dia, bloque);
    }

    @Override
    public String toString() {
        return tutorActual != null
                ? "ProxyTutor → [" + tutorActual.getNombre() + "]"
                : "ProxyTutor → [sin selección]";
    }
}