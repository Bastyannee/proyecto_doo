package controlador.eventos;

import vista.proxy.PerfilSeleccionable;

/**
 * Interfaz del patrón Observer para reaccionar al cambio de tutor seleccionado.
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