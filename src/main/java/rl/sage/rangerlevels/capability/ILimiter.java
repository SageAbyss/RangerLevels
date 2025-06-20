// src/main/java/rl/sage/rangerlevels/capability/ILimiter.java
package rl.sage.rangerlevels.capability;

/**
 * Interfaz de capacidad para controlar el límite de EXP que un jugador puede obtener
 * dentro de una ventana de tiempo configurada, con un flag para notificar solo una vez.
 */
public interface ILimiter {

    /** @return timestamp (en segundos) en que comenzó la ventana actual */
    long getWindowStart();
    /** Ajusta el timestamp (en segundos) de inicio de la ventana */
    void setWindowStart(long timestamp);

    /** @return EXP acumulada en la ventana actual */
    int getAccumulatedExp();
    /** Ajusta la EXP acumulada en la ventana */
    void setAccumulatedExp(int exp);

    /**
     * Reinicia la ventana de límite:
     * - marca de tiempo nueva
     * - contador a 0
     * - y resetea el flag de notificación
     */
    void resetWindow(long newStartTimestamp);
    void resetWindowSilent(long newStartTimestamp);

    /**
     * Intenta sumar EXP, sin superar maxAllowed.
     * @param amount     EXP solicitada
     * @param maxAllowed tope de EXP en la ventana
     * @return la EXP efectivamente agregada (0..amount)
     */
    int addExp(int amount, int maxAllowed);

    /** @return true si ya se mostró el mensaje de “límite alcanzado” esta ventana */
    boolean wasNotified();
    /** Marca si ya se ha notificado al jugador que agotó su límite esta ventana */
    void setNotified(boolean notified);
}
