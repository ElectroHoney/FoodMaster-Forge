package net.electrohoney.foodmastermod.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.recipe.AgerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.screens.PotBlockScreen;
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
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.itemsList.get(0).get()),new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class));
        //this is the order of the block registration in Mod Blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.itemsList.get(1).get()),new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PotBlockScreen.class, 110-23/2, 35, 23, 15, new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class));

        registration.addRecipeClickArea(PotBlockScreen.class, 79, 17, 16, 16, new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class));
    }

//    @Override
//    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
//        registration.addRecipeTransferHandler(PotBlockMenu.class, new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class), 0, 12, 8, 36);
//    }


    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<PotBlockRecipe> potRecipes = rm.getAllRecipesFor(PotBlockRecipe.Type.INSTANCE);

        List<AgerBlockRecipe> agerRecipes = rm.getAllRecipesFor(AgerBlockRecipe.Type.INSTANCE);

        registration.addRecipes(new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class), potRecipes);

        registration.addRecipes(new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class), agerRecipes);
    }
}
