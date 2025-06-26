package net.oblique.shoving_shovels.server.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.oblique.shoving_shovels.ShovingShovels;
import net.oblique.shoving_shovels.server.data.tags.ShovingShovelsItemTagProvider;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ShovingShovels.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Datagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        try {
            //These are required as constructor parameters for most data generators
            DataGenerator generator = event.getGenerator();
            PackOutput output = generator.getPackOutput();
            ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
            CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

            generator.addProvider(event.includeServer(), new ShovingShovelsEnLangProvider(output));
            generator.addProvider(event.includeClient(), new ShovingShovelsItemTagProvider(output, lookupProvider, existingFileHelper));
//            generator.addProvider(event.includeServer(), new ShovingShovelsDatapackProvider(output, lookupProvider));
        } catch (RuntimeException fail) {
            ShovingShovels.LOGGER.error("Failed to generate data, fail");
        }
    }
}
