
package controlador.eventos;

import vista.proxy.PerfilSeleccionable;

/**
 * Interfaz del patrón Observer.
 * Define el contrato que deben cumplir todos los paneles visuales
 * que quieran reaccionar cuando el tutor seleccionado cambie.

 * INSTRUCCIONES PARA MARI:
 * Cada panel que muestre datos del tutor activo debe:
 *   1. Implementar esta interfaz.
 *   2. Registrarse en ProxyTutor:
 *      ProxyTutor.getInstancia().registrarObservador(this)
 *   3. En actualizar(), refrescar componentes y llamar repaint().

 * Ejemplo:
 *   public class PanelDetalle extends JPanel implements Observador {
 *       public void actualizar(PerfilSeleccionable perfil) {
 *           labelNombre.setText(perfil.getNombre());
 *           repaint();
 *       }
 *   }
 */
public interface Observador {

    /**
     * Llamado automaticamente por ProxyTutor cuando el tutor
     * seleccionado cambia.
     *
     * @param perfil el tutor activo, accesible via PerfilSeleccionable
     */
    void actualizar(PerfilSeleccionable perfil);
}