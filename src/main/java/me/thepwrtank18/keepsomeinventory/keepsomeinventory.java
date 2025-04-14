package me.thepwrtank18.keepsomeinventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(keepsomeinventory.MODID)
public class keepsomeinventory {

    public static final GameRules.Key<GameRules.IntegerValue> KEEP_DURABILITY_PERCENT =
            GameRules.register("keepDurabilityPercent", GameRules.Category.PLAYER, GameRules.IntegerValue.create(50));

    public static final GameRules.Key<GameRules.IntegerValue> KEEP_HUNGER_PERCENT =
            GameRules.register("keepHungerPercent", GameRules.Category.PLAYER, GameRules.IntegerValue.create(100));

    public static final GameRules.Key<GameRules.IntegerValue> KEEP_EXPERIENCE_PERCENT =
            GameRules.register("keepExperiencePercent", GameRules.Category.PLAYER, GameRules.IntegerValue.create(50));

    public static final GameRules.Key<GameRules.IntegerValue> KEEP_HEALTH_PERCENT =
            GameRules.register("keepHealthPercent", GameRules.Category.PLAYER, GameRules.IntegerValue.create(100));

    // Define mod id in a common place for everything to reference
    public static final String MODID = "keepsomeinventory";
    // Directly reference a slf4j logger

    public keepsomeinventory() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();
        if (!level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        int durabilityPercent = level.getGameRules().getInt(KEEP_DURABILITY_PERCENT);
        durabilityPercent = Math.max(0, Math.min(100, durabilityPercent)); // Clamp to 0–100

        for (ItemStack stack : player.getInventory().items) {
            if (stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage()) {
                int remaining = stack.getMaxDamage() - stack.getDamageValue();
                int newRemaining = (int) Math.ceil(remaining * (durabilityPercent / 100.0));
                int newDamage = stack.getMaxDamage() - newRemaining;
                stack.setDamageValue(newDamage);
            }
        }

        // get the armor too
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage()) {
                int remaining = stack.getMaxDamage() - stack.getDamageValue();
                int newRemaining = (int) Math.ceil(remaining * (durabilityPercent / 100.0));
                int newDamage = stack.getMaxDamage() - newRemaining;
                stack.setDamageValue(newDamage);
            }
        }

        // get the offhand too
        ItemStack offhand = player.getInventory().offhand.get(0);
        if (offhand.isDamageableItem() && offhand.getDamageValue() < offhand.getMaxDamage()) {
            int remaining = offhand.getMaxDamage() - offhand.getDamageValue();
            int newRemaining = (int) Math.ceil(remaining * (durabilityPercent / 100.0));
            int newDamage = offhand.getMaxDamage() - newRemaining;
            offhand.setDamageValue(newDamage);
        }

        // TODO: Traveler's Backpack compatibility
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();
        if (!level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        // HUNGER
        int hungerPercent = level.getGameRules().getInt(KEEP_HUNGER_PERCENT);
        hungerPercent = Math.max(0, Math.min(100, hungerPercent));
        int newHunger = (int) Math.ceil(player.getFoodData().getFoodLevel() * (hungerPercent / 100.0));
        player.getFoodData().setFoodLevel(newHunger);

        // EXPERIENCE
        int currentExperience = player.experienceLevel;
        int experiencePercent = level.getGameRules().getInt(KEEP_EXPERIENCE_PERCENT);
        experiencePercent = Math.max(0, Math.min(100, experiencePercent));
        int newExperience = (int) Math.ceil(currentExperience * (experiencePercent / 100.0));
        player.setExperienceLevels(newExperience);

        // HEALTH
        int healthPercent = level.getGameRules().getInt(KEEP_HEALTH_PERCENT);
        healthPercent = Math.max(0, Math.min(100, healthPercent));
        float newHealth = player.getMaxHealth() * (healthPercent / 100.0f);
        player.setHealth(newHealth);
    }
}
