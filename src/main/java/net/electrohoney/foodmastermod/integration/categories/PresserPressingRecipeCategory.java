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
import net.electrohoney.foodmastermod.block.entity.custom.PresserBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.PresserBlockRecipe;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.event.RenderTooltipEvent;

import javax.annotation.Nonnull;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//bad idea but I hate to see those warnings!
@SuppressWarnings("removal")
public class PresserPressingRecipeCategory implements IRecipeCategory<PresserBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "pressing");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/presser_gui.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer inputRenderer;
    private FluidStackRenderer outputRenderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public PresserPressingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.PRESSER_BLOCK.get()));

        this.outputRenderer = new FluidStackRenderer(PresserBlockEntity.PRESSER_RESULT_FLUID_CAPACITY, true, 29, 52);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends PresserBlockRecipe> getRecipeClass() {
        return PresserBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Pressing");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull PresserBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {

        builder.addSlot(RecipeIngredientRole.INPUT, 42, 31).addIngredients(recipe.getPressedItem());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 121, 17).addItemStack(recipe.getResultItem());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 121, 45).addItemStack(recipe.getSecondaryResultItem());
        builder.setShapeless();
    }

    @Override
    public void draw(PresserBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        outputRenderer.render(stack, 82, 13, recipe.getOutputFluid());

        Minecraft.getInstance().font.draw(stack, String.valueOf(recipe.percentage1)+"%", 150F, 21F, 1);
        Minecraft.getInstance().font.draw(stack, String.valueOf(recipe.percentage2)+"%", 150F, 49F, 1);
    }

    @Override
    public List<Component> getTooltipStrings(PresserBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        if (mouseX >= 82 && mouseX <= 82+outputRenderer.getWidth() && mouseY >= 13 && mouseY <= 13+outputRenderer.getHeight()){
            return outputRenderer.getTooltip(recipe.getOutputFluid(), TooltipFlag.Default.NORMAL);
        }
        return new ArrayList<>();
    }

}
