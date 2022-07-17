package net.electrohoney.foodmastermod.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.integration.categories.*;
import net.electrohoney.foodmastermod.recipe.cooking.AgerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.ButterChurnBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.ChopperBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.baker.BakerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.baker.BroilerBlockRecipe;
import net.electrohoney.foodmastermod.screen.screens.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIFoodMasterPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FoodMaster.MOD_ID, "jei_plugin");
    }


    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new
                PotBoilingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                AgerAgeingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                BakerBakingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                BroilerBakingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                ChurnerChurningRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                ChopperChoppingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POT_BLOCK.get()),new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class));
        //this is the order of the block registration in Mod Blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.AGER_BLOCK.get()),new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BAKER_BLOCK.get()),new RecipeType<>(BakerBakingRecipeCategory.UID, BakerBlockRecipe.class));
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BAKER_BLOCK.get()),new RecipeType<>(BroilerBakingRecipeCategory.UID, BroilerBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BUTTER_CHURN.get()),new RecipeType<>(ChurnerChurningRecipeCategory.UID, ButterChurnBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPER_BLOCK.get()),new RecipeType<>(ChopperChoppingRecipeCategory.UID, ChopperBlockRecipe.class));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PotBlockScreen.class, 110-23/2, 35, 23, 15, new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class));

        registration.addRecipeClickArea(AgerBlockScreen.class, 79, 2, 16, 16, new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class));

        registration.addRecipeClickArea(BakerBlockScreen.class, 107, 48-15, 23, 15, new RecipeType<>(BakerBakingRecipeCategory.UID, BakerBlockRecipe.class));
        registration.addRecipeClickArea(BakerBlockScreen.class, 107, 48-15, 23, 15, new RecipeType<>(BroilerBakingRecipeCategory.UID, BroilerBlockRecipe.class));

        registration.addRecipeClickArea(ButterChurnBlockScreen.class, 98, 37, 12, 27, new RecipeType<>(ChurnerChurningRecipeCategory.UID, ButterChurnBlockRecipe.class));

        registration.addRecipeClickArea(ChopperBlockScreen.class, 91, 40, 18, 18, new RecipeType<>(ChopperChoppingRecipeCategory.UID, ChopperBlockRecipe.class));
    }


    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<PotBlockRecipe> potRecipes = rm.getAllRecipesFor(PotBlockRecipe.Type.INSTANCE);

        List<AgerBlockRecipe> agerRecipes = rm.getAllRecipesFor(AgerBlockRecipe.Type.INSTANCE);

        List<BakerBlockRecipe> bakerRecipes = rm.getAllRecipesFor(BakerBlockRecipe.Type.INSTANCE);
        List<BroilerBlockRecipe> broilerRecipes = rm.getAllRecipesFor(BroilerBlockRecipe.Type.INSTANCE);

        List<ButterChurnBlockRecipe> churningRecipes = rm.getAllRecipesFor(ButterChurnBlockRecipe.Type.INSTANCE);

        List<ChopperBlockRecipe> choppingRecipes = rm.getAllRecipesFor(ChopperBlockRecipe.Type.INSTANCE);

        registration.addRecipes(new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class), potRecipes);

        registration.addRecipes(new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class), agerRecipes);

        registration.addRecipes(new RecipeType<>(BakerBakingRecipeCategory.UID, BakerBlockRecipe.class), bakerRecipes);
        registration.addRecipes(new RecipeType<>(BroilerBakingRecipeCategory.UID, BroilerBlockRecipe.class), broilerRecipes);

        registration.addRecipes(new RecipeType<>(ChurnerChurningRecipeCategory.UID, ButterChurnBlockRecipe.class), churningRecipes);

        registration.addRecipes(new RecipeType<>(ChopperChoppingRecipeCategory.UID, ChopperBlockRecipe.class), choppingRecipes);
    }
}
