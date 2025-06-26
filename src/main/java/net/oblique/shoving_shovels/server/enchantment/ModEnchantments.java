package net.oblique.shoving_shovels.server.enchantment;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.oblique.shoving_shovels.ShovingShovels;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ShovingShovels.MODID);
    public static final EnchantmentCategory SHOVEL_CATEGORY = EnchantmentCategory.create("SHOVEL", item -> item instanceof ShovelItem);

    public static final RegistryObject<Enchantment> UPHEAVAL = ENCHANTMENTS.register("upheaval", UpheavalEnchantment::new);
    public static final RegistryObject<Enchantment> WALLBASH = ENCHANTMENTS.register("wallbash", WallbashEnchantment::new);
    public static final RegistryObject<Enchantment> ROWDY = ENCHANTMENTS.register("rowdy", RowdyEnchantment::new);
}
