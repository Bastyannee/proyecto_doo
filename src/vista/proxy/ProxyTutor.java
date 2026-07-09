package vista.proxy;

import controlador.eventos.SujetoObservable;
import controlador.eventos.Observador;
import modelo.entidades.Tutor;

/**
 * Proxy virtual que representa de manera diferida al tutor actualmente seleccionado
 * en el entorno de la aplicación.
 *
 * Arquitectura basada en Patrón Proxy (Virtual):
 * ProxyTutor implementa la interfaz PerfilSeleccionable de forma homóloga a la clase Tutor.
 * Gracias a esto, cualquier componente o subpanel visual parametrizado con PerfilSeleccionable
 * puede consumir esta clase de manera transparente. Al conmutar el perfil activo, se evita la
 * re-instanciación manual o el refresco acoplado de las vistas: basta con invocar el método
 * seleccionar(tutor) para redirigir las delegaciones de datos internas y disparar las alertas
 * a la UI de forma automática.
 *
 * Restricción de Instancia (Patrón Singleton):
 * Garantiza la existencia de una única referencia global a lo largo del ciclo de vida de la
 * aplicación, asegurando la consistencia del estado de selección unificado entre múltiples
 * pantallas concurrentes.
 *
 * Integración con el Patrón Observer:
 * Al extender de SujetoObservable, esta clase hereda los mecanismos de suscripción y despacho.
 * Cada modificación en el tutor de referencia gatilla la rutina notificarObservadores(),
 * provocando la actualización reactiva y el re-pintado automático de los paneles acoplados.
 */
public class ProxyTutor extends SujetoObservable implements PerfilSeleccionable {

    /** Instancia única y compartida del proxy virtual. */
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

    /** El tutor real actualmente seleccionado. */
    private Tutor tutorActual;

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