package net.oblique.shoving_shovels.server.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class RowdyEnchantment extends Enchantment {
    protected RowdyEnchantment() {
        super(Rarity.COMMON, ModEnchantments.SHOVEL_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 5 + 10 * (enchantmentLevel - 1);
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return super.getMinCost(enchantmentLevel) + 25;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
