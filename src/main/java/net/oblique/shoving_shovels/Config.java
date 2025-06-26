package net.oblique.shoving_shovels;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ShovelItem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ShovingShovels.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config{
    public static final ForgeConfigSpec SPEC;
    public static final Config INSTANCE;
    public static ForgeConfigSpec.DoubleValue yMomentumFactor;
    public static ForgeConfigSpec.DoubleValue horizontalKnockbackFactor;
    public static ForgeConfigSpec.DoubleValue totalKnockbackFactor;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> shovelItemList;

    static {
        Pair<Config, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Config::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public Config(ForgeConfigSpec.Builder builder) {
        builder.translation("shoving_shovels.configuration.Physics").comment("Physics Multipliers").push("Physics");
        yMomentumFactor = builder
                .translation("shoving_shovels.configuration.yMomentumFactor")
                .comment("Vertical shove multiplier")
                .defineInRange("yMomentumFactor", 1.0, 0.0, 100.0);
        horizontalKnockbackFactor = builder
                .translation("shoving_shovels.configuration.horizontalKnockbackFactor")
                .comment("Horizontal shove multiplier")
                .defineInRange("horizontalKnockbackFactor", 1.0, 0.0, 100.0);
        totalKnockbackFactor = builder
                .translation("shoving_shovels.configuration.totalKnockbackFactor")
                .comment("Total shove multiplier")
                .defineInRange("totalKnockbackFactor", 1.0, 0.0, 100.0);
        builder.pop();

        builder.translation("shoving_shovels.configuration.Items").comment("Shove Items").push("Items");
        shovelItemList = builder
                .translation("shoving_shovels.configuration.shovelItems")
                .comment("List of shovel item IDs with damage values (format: 'modid:itemid=damage')")
                .comment("By default, this generates a list of all shovel items within your modpack, so if you're adding new mods with shovels, be sure to regenerate this config.")
                .defineListAllowEmpty("shovelItems", List.of(),
                        obj -> obj instanceof String && ((String) obj).contains("="));
        builder.pop();
    }
}