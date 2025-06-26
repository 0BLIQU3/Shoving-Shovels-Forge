package net.oblique.shoving_shovels.server.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.oblique.shoving_shovels.ShovingShovels;

import java.util.UUID;

public class AttributeRegistry {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, ShovingShovels.MODID);

    public static final RegistryObject<Attribute> SHOVE_DAMAGE = ATTRIBUTES.register("shove_damage", () -> (new RangedAttribute("attribute.shoving_shovels.shove_damage", 0.0D, 0.0D, 2048.0D)).setSyncable(true));
    public static final UUID SHOVE_DAMAGE_UUID = UUID.randomUUID();
}
