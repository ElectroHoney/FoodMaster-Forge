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
import net.electrohoney.foodmastermod.block.entity.custom.DistillerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.FermenterBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.DistillerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.FermenterBlockRecipe;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//bad idea but I hate to see those warnings!
@SuppressWarnings("removal")
public class FermenterFermentingRecipeCategory implements IRecipeCategory<FermenterBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "fermenting");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/fermenter_gui.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer inputRenderer;
    private FluidStackRenderer outputRenderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public FermenterFermentingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 194, 85);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.FERMENTER_BLOCK.get()));

        this.inputRenderer = new FluidStackRenderer(FermenterBlockEntity.FERMENTER_MAX_FLUID_CAPACITY, true, 12, 52);
        this.outputRenderer = new FluidStackRenderer(FermenterBlockEntity.FERMENTER_RESULT_FLUID_CAPACITY, true, 12, 52);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends FermenterBlockRecipe> getRecipeClass() {
        return FermenterBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Fermenting");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull FermenterBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {

        int ingredientIndex = 0;
        for(int i = 0; i<=2; i++){
            for(int j = 0; j<=2; j++){
                if(ingredientIndex < recipe.getIngredients().size()) {
                    builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            53 + 18 * (j % 3), 17 + 18 * (i % 3)).addIngredients(recipe.getIngredients().get(ingredientIndex));
                    ingredientIndex++;
                }
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 161, 35).addItemStack(recipe.getResultItem());
        builder.setShapeless();
    }

    @Override
    public void draw(FermenterBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        inputRenderer.render(stack, 32, 17,recipe.getInputFluid());
        outputRenderer.render(stack, 141, 17,recipe.getOutputFluid());

    }

    @Override
    public List<Component> getTooltipStrings(FermenterBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        if (mouseX >= 32 && mouseX <= 32+inputRenderer.getWidth() && mouseY >= 17 && mouseY <= 17+inputRenderer.getHeight()){
            return inputRenderer.getTooltip(recipe.getInputFluid(), TooltipFlag.Default.NORMAL);
        }

        if (mouseX >= 141 && mouseX <= 141+outputRenderer.getWidth() && mouseY >= 17 && mouseY <= 17+outputRenderer.getHeight()){
            return outputRenderer.getTooltip(recipe.getOutputFluid(), TooltipFlag.Default.NORMAL);
        }

        return new ArrayList<>();


    }

}
