package net.electrohoney.foodmastermod.recipe;


import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.custom.PotBlock;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.rmi.registry.Registry;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FoodMaster.MOD_ID);

    public static final RegistryObject<RecipeSerializer<PotBlockRecipe>> POT_BOILING_SERIALIZER =
            SERIALIZERS.register("boiling", ()-> PotBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<AgerBlockRecipe>> BARREL_AGEING_SERIALIZER =
            SERIALIZERS.register("ageing", ()-> AgerBlockRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus){
        SERIALIZERS.register(eventBus);
    }
}
