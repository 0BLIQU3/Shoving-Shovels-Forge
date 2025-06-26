package net.oblique.shoving_shovels.server.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.oblique.shoving_shovels.Config;
import net.oblique.shoving_shovels.ShovingShovels;
import net.oblique.shoving_shovels.server.enchantment.ModEnchantments;
import net.oblique.shoving_shovels.server.registry.AttributeRegistry;
import net.oblique.shoving_shovels.server.util.ShoveDamageHandler;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ShovingShovels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GameEventBusEvents {
    private static final String TAG_SHOVED = "Shoved";
    private static final String TAG_SHOVE_AGE = "ShoveAge";
    private static final String TAG_SHOVE_ID = "ShoveID";
    private static final String LAST_SHOVE_HIT_ID = "LastShoveHitID";
    private static final String TAG_SHOVE_DAMAGE = "ShoveDamage";
    private static final String TAG_SHOVE_SOURCE = "ShoveSource";
    private static final String TAG_WALLBASH_LEVEL = "WallbashLevel";
    public static double Y_MOMENTUM_FACTOR = 1;
    public static double HORIZONTAL_KNOCKBACK_FACTOR = 1;
    public static double TOTAL_KNOCKBACK_FACTOR = 1;


    @SubscribeEvent
    //Using CriticalHitEvent as it always fires for melee attacks, even if not critical
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        Player player = event.getEntity();
        //Check if we're using a shovel in the first place
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof ShovelItem)) {
            return;
        }
        int upheavalLevel = heldItem.getEnchantmentLevel(ModEnchantments.UPHEAVAL.get());
        int wallbashLevel = heldItem.getEnchantmentLevel(ModEnchantments.WALLBASH.get());
        int rowdyLevel = heldItem.getEnchantmentLevel(ModEnchantments.ROWDY.get());
        //Rowdy is 50% damage per level
        int shovingDamage = (int) (player.getAttributeValue(AttributeRegistry.SHOVE_DAMAGE.get()) * ((rowdyLevel * 0.5) + 1));

        Vec3 lookingAngle = player.getLookAngle(); //Already a unit vector
        //Actually apply the knockback
        TOTAL_KNOCKBACK_FACTOR = Config.totalKnockbackFactor.get();
        double knockbackStrength = 2 * TOTAL_KNOCKBACK_FACTOR;
        //Make sure it scales properly with attack cooldown
        float strengthScale = player.getAttackStrengthScale(0.5F);
        knockbackStrength *= strengthScale;
        knockbackStrength *= 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

        //This Upheaval enchantment is the entire reason we're in the CriticalHitEvent.....
        if (upheavalLevel > 0 && event.isVanillaCritical()) {
            target.setOnGround(false);
            target.setDeltaMovement(
                    0,
                    0.5 * knockbackStrength,
                    0
            );
        }
        else {
            Vec3 shoveMotion = lookingAngle.scale(knockbackStrength);
            Vec3 currentMotion = target.getDeltaMovement();

            double yShove = getYShove(shoveMotion, lookingAngle);
            //Must set target to be off ground, or y momentum gets zero-ed out, thank you Minecraft
            target.setOnGround(false);
            HORIZONTAL_KNOCKBACK_FACTOR = Config.horizontalKnockbackFactor.get();
            target.setDeltaMovement(
                    currentMotion.x * 0.5 + (shoveMotion.x * 1.1 * HORIZONTAL_KNOCKBACK_FACTOR),
                    currentMotion.y * 0.5 + yShove,
                    currentMotion.z * 0.5 + (shoveMotion.z * 1.1 * HORIZONTAL_KNOCKBACK_FACTOR)
            );
        }
        target.hasImpulse = true;

        //Mark target for shoving logic
        UUID shoveID = UUID.randomUUID();
        target.getPersistentData().putBoolean(TAG_SHOVED, true);
        target.getPersistentData().putInt(TAG_SHOVE_AGE, 0);
        target.getPersistentData().putUUID(TAG_SHOVE_ID, shoveID);
        target.getPersistentData().putInt(TAG_SHOVE_DAMAGE, shovingDamage);
        target.getPersistentData().putUUID(TAG_SHOVE_SOURCE, player.getUUID());
        if (wallbashLevel > 0) {
            target.getPersistentData().putInt(TAG_WALLBASH_LEVEL, wallbashLevel);
        }
    }

    @SubscribeEvent
    public static void onEntityUpdate(LivingEvent.LivingTickEvent entityTickEvent) {
//        if (!(entityTickEvent.getEntity() instanceof LivingEntity shovedTarget)) {
//            return;
//        }
        LivingEntity shovedTarget = entityTickEvent.getEntity();
        if (!shovedTarget.getPersistentData().getBoolean(TAG_SHOVED)) {
            return;
        }
        //If it's just dead anyway, don't even bother!
        if (!shovedTarget.isAlive()) {
            clearShoveData(shovedTarget);
            return;
        }
        shovedTarget.getPersistentData().putInt(TAG_SHOVE_AGE, shovedTarget.getPersistentData().getInt(TAG_SHOVE_AGE) + 1);
        //1 second cooldown on Shoving mobs. May change this?
        if (shovedTarget.getPersistentData().getInt(TAG_SHOVE_AGE) > 20) {
            clearShoveData(shovedTarget);
            return;
        }
        //We need to create particles for the shoved mob for some good visual feedback
        if (shovedTarget.level() instanceof ServerLevel serverLevel) {
            if (shovedTarget.getPersistentData().getInt(TAG_SHOVE_AGE) < 20) {
                //This should slightly shift the particles back to form a proper "trail"
                Vec3 back = shovedTarget.position().subtract(shovedTarget.getDeltaMovement().scale(1.0));
                serverLevel.sendParticles(
                        ParticleTypes.POOF,
                        back.x,
                        back.y + shovedTarget.getBbHeight() / 2,
                        back.z,
                        1,
                        0.2, 0.2, 0.2,
                        0.02
                );
            }
        }
        //Get shoveID of target: we'll want to check it against hit targets and then copy it to them if there's a mismatch (don't damage if it's the same)
        //This results in each shove being able to damage once, since shoving a target creates a new ID each time (so most of the time there will be a mismatch anyway)
        UUID shoveID = shovedTarget.getPersistentData().getUUID(TAG_SHOVE_ID);
        int shoveDamage = shovedTarget.getPersistentData().getInt(TAG_SHOVE_DAMAGE);
        UUID sourceUUID = shovedTarget.getPersistentData().getUUID(TAG_SHOVE_SOURCE);

        //We now have an entity that has been Shoved recently, so let's do our damage
        AABB hitbox = shovedTarget.getBoundingBox().inflate(0.3);
        //Create a list of every living entity within the hitbox of the shove
        //Also make sure that you can't shove yourself if you do this while very close to the target
        List<LivingEntity> targets = shovedTarget.level().getEntitiesOfClass(LivingEntity.class, hitbox,
                e -> e != shovedTarget && e.isAlive() && e != e.level().getPlayerByUUID(sourceUUID));

        for (LivingEntity target : targets) {
            //If the target has already been hit by this specific shove before, skip it
            if (target.getPersistentData().hasUUID(LAST_SHOVE_HIT_ID) && target.getPersistentData().getUUID(LAST_SHOVE_HIT_ID).equals(shoveID)) {
                continue;
            }
            //Make sure we're getting the correct source of damage (that being, the player)
            if (shovedTarget.level() instanceof ServerLevel serverLevel) {
                Player sourcePlayer = serverLevel.getPlayerByUUID(sourceUUID);
                if (sourcePlayer != null) {
                    target.hurt(serverLevel.damageSources().playerAttack(sourcePlayer), shoveDamage);
                    target.level().playSound(
                            null,
                            target.getX(),
                            target.getY(),
                            target.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }
                else {
                    target.hurt(serverLevel.damageSources().generic(), shoveDamage);
                }
            }
            target.getPersistentData().putUUID(LAST_SHOVE_HIT_ID, shoveID);
        }
        //Do our logic for the Wallbash enchantment
        if (!(shovedTarget.getPersistentData().getInt(TAG_WALLBASH_LEVEL) > 0)) {
            return;
        }
        if (shovedTarget.horizontalCollision && !shovedTarget.level().isClientSide()) {
            //Proper damageless explosion only on 1.21.1+
//            shovedTarget.level().explode(
//                    shovedTarget,
//                    null,
//                    new SimpleExplosionDamageCalculator(false, false, Optional.of(1f), Optional.empty()),
//                    shovedTarget.getX(),
//                    shovedTarget.getY(),
//                    shovedTarget.getZ(),
//                    2F,
//                    false,
//                    Level.ExplosionInteraction.TRIGGER,
//                    ParticleTypes.EXPLOSION,
//                    ParticleTypes.EXPLOSION,
//                    SoundEvents.GENERIC_EXPLODE
//            );
            shovedTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
            shovedTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            shovedTarget.getPersistentData().remove(TAG_WALLBASH_LEVEL);
        }
    }


    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent itemAttributeModifierEvent) {
        ItemStack itemStack = itemAttributeModifierEvent.getItemStack();
        if (!(itemStack.getItem() instanceof ShovelItem)) {
            return;
        }
        if (itemAttributeModifierEvent.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        //This will check the config for the shove damage  of a shovel. If it's not in the config, then default to 2 damage
        double baseDamage = ShoveDamageHandler.getShoveDamage(itemStack);
        //This adds our Shove Damage attribute to anything that is a ShovelItem
        itemAttributeModifierEvent.addModifier(AttributeRegistry.SHOVE_DAMAGE.get(), new AttributeModifier(
                AttributeRegistry.SHOVE_DAMAGE_UUID,
                "shoving_shovels_shovel_damage",
                baseDamage,
                AttributeModifier.Operation.ADDITION
                )
        );
    }

    private static double getYShove(Vec3 shoveMotion, Vec3 lookingAngle) {
        //Moderate reduction in y momentum as this can otherwise send people FLYING if you are directly under them
        Y_MOMENTUM_FACTOR = Config.yMomentumFactor.get();
        double yShove = shoveMotion.y * 0.58 * Y_MOMENTUM_FACTOR;
        //For very small looking negative angles, apply a little upwards momentum, so we're not bashing them into the ground and losing knockback
        //May make this configurable and also change this logic a bit later, but for now we just roll with it haha
        if (yShove < 0.05 && lookingAngle.y > -0.8) {
            yShove += 0.2;
        }
        return yShove;
    }

    private static void clearShoveData(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_SHOVED);
        entity.getPersistentData().remove(TAG_SHOVE_AGE);
        entity.getPersistentData().remove(TAG_SHOVE_ID);
        entity.getPersistentData().remove(LAST_SHOVE_HIT_ID);
        entity.getPersistentData().remove(TAG_SHOVE_DAMAGE);
        entity.getPersistentData().remove(TAG_SHOVE_SOURCE);
        entity.getPersistentData().remove(TAG_WALLBASH_LEVEL);
    }
}
