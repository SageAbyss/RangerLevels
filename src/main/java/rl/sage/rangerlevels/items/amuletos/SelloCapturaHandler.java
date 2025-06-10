// File: rl/sage/rangerlevels/items/SelloCapturaHandler.java
package rl.sage.rangerlevels.items.amuletos;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent.StartCapture;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent.SuccessfulCapture;
import com.pixelmonmod.pixelmon.api.pokemon.catching.CaptureValues;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import rl.sage.rangerlevels.items.RangerItemDefinition;

public class SelloCapturaHandler {

    /**
     * Registra el handler en el bus de Pixelmon
     */
    public static void register() {
        Pixelmon.EVENT_BUS.register(SelloCapturaHandler.class);
    }

    /**
     * Antes de que Pixelmon calcule la captura, modificamos los valores en CaptureValues:
     * - catchRate a 255
     * - ballBonus a 100.0
     * - marcamos la captura como exitosa
     * <p>
     * Según la API, CaptureValues expone:
     * void setCatchRate(int)       :contentReference[oaicite:0]{index=0}
     * void setBallBonus(double)    :contentReference[oaicite:1]{index=1}
     * void setCaught()             :contentReference[oaicite:2]{index=2}
     */
    @SubscribeEvent
    public static void onStartCapture(StartCapture event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        // Buscamos el sello en el inventario
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (!stack.isEmpty()
                    && SelloCapturaEpico.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {

                CaptureValues vals = event.getCaptureValues();
                vals.setCatchRate(255);      // forzamos al máximo (1–255) :contentReference[oaicite:3]{index=3}
                vals.setBallBonus(100.0);    // bonus muy alto :contentReference[oaicite:4]{index=4}
                vals.setCaught();            // marcamos captura exitosa :contentReference[oaicite:5]{index=5}

                return;
            }
        }
    }

    /**
     * Después de que Pixelmon confirma la captura, consumimos el sello
     * y enviamos un mensaje bonito al jugador.
     */
    @SubscribeEvent
    public static void onCaptureSuccess(SuccessfulCapture event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        PixelmonEntity pkmnEntity = event.getPokemon();
        // Obtenemos el nombre localizable del Pokémon
        String pokeName = pkmnEntity.getPokemon().getSpecies().getLocalizedName();

        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (!stack.isEmpty()
                    && SelloCapturaEpico.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {

                // 1) Consumir el sello
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.inventory.setItem(i, ItemStack.EMPTY);
                }

                // 2) Enviar título y subtítulo con el nombre del Pokémon
                StringTextComponent title = new StringTextComponent("§6✦ Sello Activado ✦");
                StringTextComponent subtitle = new StringTextComponent("§a¡Capturaste un " + pokeName + "!");
                // Configuramos tiempos: fadeIn=10, stay=70, fadeOut=20 ticks
                player.connection.send(new STitlePacket(STitlePacket.Type.TIMES, title, 10, 70, 20));
                player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, title));
                player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, subtitle));

                // 3) (Opcional) Mensaje en chat
                player.displayClientMessage(
                        new StringTextComponent(
                                "§b✦§d§ki§b✦ Sello de Captura consumido ✦§d§ki§b✦ §r\n"
                        ),
                        false
                );
                return;
            }
        }
    }
}
