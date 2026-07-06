package controlador.comandos;

/**
 * Es la interfaz base para el patrón Command. Modela cualquier acción del Administrador (como crear o cancelar reservas) como un objeto.
 * Esto permite al historial de operaciones ejecutar o revertir las acciones de forma uniforme, sin necesidad de saber cómo funciona cada una por dentro.
 */
public interface Comando {
    /**
     * Ejecuta la acción concreta representada por el comando
     */
    void execute();
}
