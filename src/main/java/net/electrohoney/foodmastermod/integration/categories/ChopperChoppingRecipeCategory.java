package net.electrohoney.foodmastermod.integration.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.ChopperBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//bad idea but I hate to see those warnings!
@SuppressWarnings("removal")
public class ChopperChoppingRecipeCategory implements IRecipeCategory<ChopperBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "chopping");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/chopper_gui.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer renderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public ChopperChoppingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.CHOPPER_BLOCK.get()));

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends ChopperBlockRecipe> getRecipeClass() {
        return ChopperBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Chopping");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }



    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull ChopperBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {

        builder.addSlot(RecipeIngredientRole.CATALYST, 37, 19).addIngredients(recipe.getUtensil());

        builder.addSlot(RecipeIngredientRole.INPUT, 37, 40).addIngredients(recipe.getIngredient());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 41).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(ChopperBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
    }

    @Override
    public List<Component> getTooltipStrings(ChopperBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return new ArrayList<>();


    }

}
