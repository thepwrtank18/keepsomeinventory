package me.thepwrtank18.keepsomeinventory;

import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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

    public keepsomeinventory(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
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
            ApplyDamage(durabilityPercent, stack);
        }

        // get the armor too
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            ApplyDamage(durabilityPercent, stack);
        }

        // get the offhand too
        ItemStack offhand = player.getInventory().offhand.get(0);
        ApplyDamage(durabilityPercent, offhand);
    }

    private void ApplyDamage(int durabilityPercent, ItemStack stack) {
        if (stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage()) {
            if (EnchantmentHelper.getEnchantments(stack).containsKey(ModEnchantments.PRESERVE.get())) {
                return;
            }

            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            int newRemaining = (int) Math.ceil(remaining * (durabilityPercent / 100.0));
            int newDamage = stack.getMaxDamage() - newRemaining;
            stack.setDamageValue(newDamage);
        }
    }


    private void processBackpackSlots(IItemHandler slots, int durabilityPercent) {
        for (int i = 0; i < slots.getSlots(); i++) {
            ItemStack stack = slots.getStackInSlot(i);
            ApplyDamage(durabilityPercent, stack);
        }
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

        int durabilityPercent = level.getGameRules().getInt(KEEP_DURABILITY_PERCENT);

        if (ModList.get().isLoaded("travelersbackpack")) {
            BackpackWrapper wrapper = CapabilityUtils.getBackpackWrapper(player);
            if (wrapper != null) {
                processBackpackSlots(wrapper.getStorage(), durabilityPercent);

                processBackpackSlots(wrapper.getTools(), durabilityPercent);
            }
        }
    }
}
