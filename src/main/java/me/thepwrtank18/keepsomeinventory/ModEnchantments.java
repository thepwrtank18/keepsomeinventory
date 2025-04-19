package me.thepwrtank18.keepsomeinventory;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, keepsomeinventory.MODID);

    public static final RegistryObject<Enchantment> PRESERVE =
            ENCHANTMENTS.register("preserve", PreserveEnchantment::new);
}