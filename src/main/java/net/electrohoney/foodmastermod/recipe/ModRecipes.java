package net.electrohoney.foodmastermod.recipe;


import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.recipe.cooking.*;
import net.electrohoney.foodmastermod.recipe.cooking.baker.BakerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.baker.BroilerBlockRecipe;
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
    public static final RegistryObject<RecipeSerializer<BroilerBlockRecipe>> BAKER_BROILING_SERIALIZER =
            SERIALIZERS.register("broiling", ()-> BroilerBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ButterChurnBlockRecipe>> CHURNER_CHURNING_SERIALIZER =
            SERIALIZERS.register("churning", ()-> ButterChurnBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ChopperBlockRecipe>> CHOPPER_CHOPPING_SERIALIZER =
            SERIALIZERS.register("chopping", ()-> ChopperBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<DistillerBlockRecipe>> DISTILLER_DISTILLING_SERIALIZER =
            SERIALIZERS.register("distilling", ()-> DistillerBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<FermenterBlockRecipe>> FERMENTER_FERMENTING_SERIALIZER =
            SERIALIZERS.register("fermenting", ()-> FermenterBlockRecipe.Serializer.INSTANCE);
    public static void register(IEventBus eventBus){
        SERIALIZERS.register(eventBus);
    }
}
