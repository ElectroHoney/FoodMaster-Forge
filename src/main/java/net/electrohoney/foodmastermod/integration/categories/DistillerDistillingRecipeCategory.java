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
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.DistillerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
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
import java.util.*;

//bad idea but I hate to see those warnings!
@SuppressWarnings("removal")
public class DistillerDistillingRecipeCategory implements IRecipeCategory<DistillerBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "distilling");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/distiller_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    private IDrawable temperatureBar;

    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer inputRenderer;
    private FluidStackRenderer outputRenderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public DistillerDistillingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.DISTILLER_BLOCK.get()));

        this.temperatureBar = helper.createDrawable(TEXTURE, 176, 45, 7, 55);

        this.inputRenderer = new FluidStackRenderer(DistillerBlockEntity.DISTILLER_MAX_FLUID_CAPACITY, true, 52, 52);
        this.outputRenderer = new FluidStackRenderer(DistillerBlockEntity.DISTILLER_RESULT_FLUID_CAPACITY, true, 24, 24);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends DistillerBlockRecipe> getRecipeClass() {
        return DistillerBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Distilling");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull DistillerBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {

        int ingredientIndex = 0;
        if(recipe.getIngredients().size() > 0){
            for(int i = 1; i<=3; i++){
                if(ingredientIndex < recipe.getIngredients().size()){
                    builder.addSlot(RecipeIngredientRole.INPUT, 85+18*i, 11).addIngredients(recipe.getIngredients().get(ingredientIndex));
                    ingredientIndex++;
                }

            }
        }
        builder.setShapeless();
        builder.addSlot(RecipeIngredientRole.INPUT, 77, 44).addIngredients(recipe.getMush());
    }

    @Override
    public void draw(DistillerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        if(recipe.maxTemperature > 0){
            int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
            int maxPossibleTemperature = 125; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
            int tMax = recipe.maxTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            int tMin = recipe.minTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 70-tMax, 7, tMax);
            this.temperatureBar.draw(stack, 9-7, 45 + 53 - tMax); //it is drawn correctly but I can't set it a varible size

            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 70-tMin, 7, tMin);
            this.temperatureBar.draw(stack, 9+7, 45 + 53 - tMin); //it is drawn correctly but I can't set it a varible size
        }
        //the xPosition and yPosition are the pixel location on the gui texture
        inputRenderer.render(stack, 26, 15,recipe.getInputFluidStack());
        outputRenderer.render(stack, 130, 43,recipe.getOutputFluidStack());

    }

    @Override
    public List<Component> getTooltipStrings(DistillerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
        int maxPossibleTemperature = 125; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
        int tMax = recipe.maxTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
        int tMin = recipe.minTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function

        if (mouseX >= 26 && mouseX <= 26+inputRenderer.getWidth() && mouseY >= 15 && mouseY <= 15+inputRenderer.getHeight()){
            return inputRenderer.getTooltip(recipe.getInputFluidStack(), TooltipFlag.Default.NORMAL);
        }

        if (mouseX >= 130 && mouseX <= 130+outputRenderer.getWidth() && mouseY >= 43 && mouseY <= 43+outputRenderer.getHeight()){
            return outputRenderer.getTooltip(recipe.getOutputFluidStack(), TooltipFlag.Default.NORMAL);
        }


        if(mouseX >= 9-7 && mouseX <= 9 && mouseY >= 14 && mouseY <= 14+54){
            Component displayMaxTemperature =
                    new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(recipe.maxTemperature), nf.format(maxPossibleTemperature));
            List<Component> componentList = new ArrayList<>(Collections.emptyList());
            componentList.add(displayMaxTemperature);
            return componentList;
        }
        if(mouseX >= 9+7 && mouseX <= 9+14 && mouseY >= 14 && mouseY <= 14+54){
            Component displayMinTemperature =
                    new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(recipe.minTemperature), nf.format(maxPossibleTemperature));
            List<Component> componentList = new ArrayList<>(Collections.emptyList());
            componentList.add(displayMinTemperature);
            return componentList;
        }

        return new ArrayList<>();


    }

}
