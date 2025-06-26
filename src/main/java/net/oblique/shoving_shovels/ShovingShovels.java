package net.oblique.shoving_shovels;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ShovelItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.oblique.shoving_shovels.server.enchantment.ModEnchantments;
import net.oblique.shoving_shovels.server.registry.AttributeRegistry;
import net.oblique.shoving_shovels.server.util.ShoveDamageHandler;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ShovingShovels.MODID)
public class ShovingShovels
{
    public static final String MODID = "shoving_shovels";
    public static final Logger LOGGER = LogUtils.getLogger();
    public ShovingShovels(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        //Registers
        AttributeRegistry.ATTRIBUTES.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(this::onLoadComplete);
    }
    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            if (Config.shovelItemList.get().isEmpty()) {
                List<String> shovels = BuiltInRegistries.ITEM.stream()
                        .filter(item -> item instanceof ShovelItem)
                        .map(item -> {
                            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                            double attackDamage = ((ShovelItem) item).getTier().getAttackDamageBonus();
                            return id + "=" + (attackDamage + 1);
                        }).toList();

                Config.shovelItemList.set(shovels);
                ShovingShovels.LOGGER.info("Detected and loaded default shovel items from all mods: " + shovels);
            }
        });
        ShoveDamageHandler.loadFromConfig();
    }
}
