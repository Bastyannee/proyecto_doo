package vista.proxy;

/**
 * Esta interfaz es el contrato común entre el Tutor real y nuestro ProxyTutor.
 * Su propósito es que los paneles de la interfaz gráfica puedan interactuar con
 * cualquiera de los dos sin notar la diferencia
 *
 * La creé específicamente para la capa visual. En lugar de exponer toda la
 * información interna del Tutor, aquí solo definimos los datos exactos que la UI
 * necesita mostrar. Así mantenemos nuestro modelo de dominio limpio y completamente
 * separado de la vista.
 *
 * para bastian:
 * Para conectar tu parte con la mía, necesito que tu clase Tutor implemente
 * esta interfaz. Como los métodos requeridos ya los tienes programados en tu clase,
 * el único cambio que debes hacer es agregar el "implements" en la declaración:
 * * public class Tutor implements PerfilSeleccionable { ... }
 */
public interface PerfilSeleccionable {

    /** @return identificador único del tutor */
    String getId();

    /** @return nombre completo del tutor */
    String getNombre();

    /** @return descripción o perfil del tutor */
    String getDescripcion();

    /** @return materia principal que dicta */
    String getMateria();

    /** @return afinidad o especialización adicional */
    String getAfinidad();

    /** @return ruta de la foto de perfil */
    String getFotoPath();

    /** @return tarifa por hora */
    double getTarifa();

    /** @return número máximo de estudiantes que puede atender */
    int getMaxEstudiantes();

    /**
     * Consulta si el tutor está disponible en un día y bloque.
     *
     * @param dia    índice del día (0=Lunes ... 4=Viernes)
     * @param bloque índice del bloque horario (0=08:00 ... 7=17:00)
     * @return true si el tutor tiene ese bloque disponible
     */
    boolean isDisponible(int dia, int bloque);
}