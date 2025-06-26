package net.oblique.shoving_shovels.server.event;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.oblique.shoving_shovels.ShovingShovels;
import net.oblique.shoving_shovels.server.registry.AttributeRegistry;

@Mod.EventBusSubscriber(modid = ShovingShovels.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        //Gotta make sure that the attribute is also registered to the player so that we don't crash when checking for damage
        event.add(EntityType.PLAYER, AttributeRegistry.SHOVE_DAMAGE.get());
    }
}
