package net.electrohoney.foodmastermod.recipe;


import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.recipe.cooking.AgerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.BakerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FoodMaster.MOD_ID);

    public static final RegistryObject<RecipeSerializer<PotBlockRecipe>> POT_BOILING_SERIALIZER =
            SERIALIZERS.register("boiling", ()-> PotBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<AgerBlockRecipe>> BARREL_AGEING_SERIALIZER =
            SERIALIZERS.register("ageing", ()-> AgerBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<BakerBlockRecipe>> BAKER_BAKING_SERIALIZER =
            SERIALIZERS.register("baking", ()->BakerBlockRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus){
        SERIALIZERS.register(eventBus);
    }
}
