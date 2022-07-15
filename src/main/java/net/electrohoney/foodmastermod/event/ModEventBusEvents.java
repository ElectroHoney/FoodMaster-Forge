package net.electrohoney.foodmastermod.event;

import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = FoodMaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
//    @SubscribeEvent
//    public static void registerModifierSerializers(@Nonnull final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event){
//
//    }

    @SubscribeEvent
    public static void registerRecipeTypes(@Nonnull final RegistryEvent.Register<RecipeSerializer<?>> event){
        Registry.register(Registry.RECIPE_TYPE, PotBlockRecipe.Type.ID, PotBlockRecipe.Type.INSTANCE);
    }
}
