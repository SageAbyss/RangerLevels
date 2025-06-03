package rl.sage.rangerlevels.database;

import java.util.UUID;

/**
 * Interfaz genérica para gestionar Data.json en flatfile (JSON).
 * Permite:
 *   - loadAll(): cargar todos los datos al mapa en memoria.
 *   - saveAll(): guardar todo el mapa en disco (atomico).
 *   - get(): obtener PlayerData para un UUID (o null).
 *   - getOrCreate(): obtener o crear un PlayerData.
 *   - savePlayerData(): guardar sólo un registro de jugador.
 *   - reload(): recarga desde disco y aplica a jugadores online.
 */
public interface IPlayerDataManager {

    /** Carga TODO lo que haya en Data.json al mapa en memoria. */
    void loadAll();

    /** Guarda TODO el mapa en memoria en Data.json. */
    void saveAll();

    /** Devuelve PlayerData para un UUID dado; si no existe, devuelve null. */
    PlayerData get(UUID uuid);

    /**
     * Devuelve PlayerData para uuid. Si no existe, lo crea con valores por defecto
     * y lo devuelve. Actualiza el nickname.
     */
    PlayerData getOrCreate(UUID uuid, String nickname);

    /**
     * Guarda solo los datos de este jugador:
     *  1) Actualiza el mapa en memoria.
     *  2) Llama a saveAll() para asegurar coherencia.
     */
    void savePlayerData(PlayerData data);

    /**
     * Recarga TODO Data.json desde disco y actualiza el mapa en memoria.
     * Adicionalmente, aplica esos valores a la capacidad ILevel de los jugadores online.
     */
    void reload();
}
