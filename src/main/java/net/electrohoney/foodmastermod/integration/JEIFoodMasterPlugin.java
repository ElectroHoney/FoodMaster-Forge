package net.electrohoney.foodmastermod.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.block.entity.custom.FermenterBlockEntity;
import net.electrohoney.foodmastermod.integration.categories.*;
import net.electrohoney.foodmastermod.recipe.cooking.*;
import net.electrohoney.foodmastermod.recipe.cooking.baker.BakerBlockRecipe;
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
                BakerBroilingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                ChurnerChurningRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                ChopperChoppingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                DistillerDistillingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                FermenterFermentingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                FreezerFreezingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                GrillerGrillingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new
                InfuserInfusingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POT_BLOCK.get()),new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class));
        //this is the order of the block registration in Mod Blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.AGER_BLOCK.get()),new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BAKER_BLOCK.get()),new RecipeType<>(BakerBakingRecipeCategory.UID, BakerBlockRecipe.class));
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BAKER_BLOCK.get()),new RecipeType<>(BakerBroilingRecipeCategory.UID, BroilerBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BUTTER_CHURN.get()),new RecipeType<>(ChurnerChurningRecipeCategory.UID, ButterChurnBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHOPPER_BLOCK.get()),new RecipeType<>(ChopperChoppingRecipeCategory.UID, ChopperBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DISTILLER_BLOCK.get()),new RecipeType<>(DistillerDistillingRecipeCategory.UID, DistillerBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FERMENTER_BLOCK.get()),new RecipeType<>(FermenterFermentingRecipeCategory.UID, FermenterBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FREEZER_BLOCK.get()),new RecipeType<>(FreezerFreezingRecipeCategory.UID, FreezerBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GRILLER_BLOCK.get()),new RecipeType<>(GrillerGrillingRecipeCategory.UID, GrillerBlockRecipe.class));

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INFUSER_BLOCK.get()),new RecipeType<>(InfuserInfusingRecipeCategory.UID, InfuserBlockRecipe.class));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PotBlockScreen.class, 110-23/2, 35, 23, 15, new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class));

        registration.addRecipeClickArea(AgerBlockScreen.class, 79, 2, 16, 16, new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class));

        registration.addRecipeClickArea(BakerBlockScreen.class, 107, 48-15, 23, 15, new RecipeType<>(BakerBakingRecipeCategory.UID, BakerBlockRecipe.class));
        registration.addRecipeClickArea(BakerBlockScreen.class, 107, 48-15, 23, 15, new RecipeType<>(BakerBroilingRecipeCategory.UID, BroilerBlockRecipe.class));

        registration.addRecipeClickArea(ButterChurnBlockScreen.class, 98, 37, 12, 27, new RecipeType<>(ChurnerChurningRecipeCategory.UID, ButterChurnBlockRecipe.class));

        registration.addRecipeClickArea(ChopperBlockScreen.class, 91, 40, 18, 18, new RecipeType<>(ChopperChoppingRecipeCategory.UID, ChopperBlockRecipe.class));

        registration.addRecipeClickArea(DistillerBlockScreen.class, 106, 32, 10, 27, new RecipeType<>(DistillerDistillingRecipeCategory.UID, DistillerBlockRecipe.class));

        registration.addRecipeClickArea(FermenterBlockScreen.class, 100, 31, 28, 20, new RecipeType<>(FermenterFermentingRecipeCategory.UID, FermenterBlockRecipe.class));

        registration.addRecipeClickArea(FreezerBlockScreen.class, 106, 34, 18, 19, new RecipeType<>(FreezerFreezingRecipeCategory.UID, FreezerBlockRecipe.class));

        registration.addRecipeClickArea(GrillerBlockScreen.class, 108, 36, 13, 14, new RecipeType<>(GrillerGrillingRecipeCategory.UID, GrillerBlockRecipe.class));

        registration.addRecipeClickArea(InfuserBlockScreen.class, 28, 37, 48, 16, new RecipeType<>(InfuserInfusingRecipeCategory.UID, InfuserBlockScreen.class));
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

        List<DistillerBlockRecipe> distillationRecipes = rm.getAllRecipesFor(DistillerBlockRecipe.Type.INSTANCE);

        List<FermenterBlockRecipe> fermentationRecipes = rm.getAllRecipesFor(FermenterBlockRecipe.Type.INSTANCE);

        List<FreezerBlockRecipe> freezingRecipes = rm.getAllRecipesFor(FreezerBlockRecipe.Type.INSTANCE);

        List<GrillerBlockRecipe> grillingRecipes = rm.getAllRecipesFor(GrillerBlockRecipe.Type.INSTANCE);

        List<InfuserBlockRecipe> infuserRecipes = rm.getAllRecipesFor(InfuserBlockRecipe.Type.INSTANCE);

        registration.addRecipes(new RecipeType<>(PotBoilingRecipeCategory.UID, PotBlockRecipe.class), potRecipes);

        registration.addRecipes(new RecipeType<>(AgerAgeingRecipeCategory.UID, AgerBlockRecipe.class), agerRecipes);

        registration.addRecipes(new RecipeType<>(BakerBakingRecipeCategory.UID, BakerBlockRecipe.class), bakerRecipes);
        registration.addRecipes(new RecipeType<>(BakerBroilingRecipeCategory.UID, BroilerBlockRecipe.class), broilerRecipes);

        registration.addRecipes(new RecipeType<>(ChurnerChurningRecipeCategory.UID, ButterChurnBlockRecipe.class), churningRecipes);

        registration.addRecipes(new RecipeType<>(ChopperChoppingRecipeCategory.UID, ChopperBlockRecipe.class), choppingRecipes);

        registration.addRecipes(new RecipeType<>(DistillerDistillingRecipeCategory.UID, DistillerBlockRecipe.class), distillationRecipes);

        registration.addRecipes(new RecipeType<>(FermenterFermentingRecipeCategory.UID, FermenterBlockRecipe.class), fermentationRecipes);

        registration.addRecipes(new RecipeType<>(FreezerFreezingRecipeCategory.UID, FreezerBlockRecipe.class), freezingRecipes);

        registration.addRecipes(new RecipeType<>(GrillerGrillingRecipeCategory.UID, GrillerBlockRecipe.class), grillingRecipes);

        registration.addRecipes(new RecipeType<>(InfuserInfusingRecipeCategory.UID, InfuserBlockRecipe.class), infuserRecipes);
    }
}
