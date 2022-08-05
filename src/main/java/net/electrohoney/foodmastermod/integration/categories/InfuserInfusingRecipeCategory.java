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
import net.electrohoney.foodmastermod.block.entity.custom.InfuserBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.InfuserBlockRecipe;
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
public class InfuserInfusingRecipeCategory implements IRecipeCategory<InfuserBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "infusing");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/infuser_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    private IDrawable temperatureBar;

    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer outputRenderer, inputRenderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public InfuserInfusingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.INFUSER_BLOCK.get()));

        this.temperatureBar = helper.createDrawable(TEXTURE, 176, 18, 7, 54);

        this.inputRenderer = new FluidStackRenderer(InfuserBlockEntity.INFUSER_MAX_FLUID_CAPACITY, true, 48, 14);
        this.outputRenderer = new FluidStackRenderer(InfuserBlockEntity.INFUSER_MAX_FLUID_CAPACITY, true, 24, 24);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends InfuserBlockRecipe> getRecipeClass() {
        return InfuserBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Infusing");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull InfuserBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.setShapeless();

        builder.addSlot(RecipeIngredientRole.INPUT, 44, 17).addIngredients(recipe.getInputIngredient());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 49).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(InfuserBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        if(recipe.maxTemperature > 0){
            int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
            int maxPossibleTemperature = 100; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
            int tMax = recipe.maxTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            int tMin = recipe.minTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 70-tMax, 7, tMax);
            this.temperatureBar.draw(stack, 12-7, 16 + 53 - tMax); //it is drawn correctly, but I can't set it a variable size

            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 70-tMin, 7, tMin);
            this.temperatureBar.draw(stack, 12+7, 16 + 53 - tMin); //it is drawn correctly, but I can't set it a variable size
        }
        //the xPosition and yPosition are the pixel location on the gui texture
        inputRenderer.render(stack, 28, 55,recipe.getInputFluidStack());
        outputRenderer.render(stack, 112, 45, recipe.getOutputFluidStack());

    }

    @Override
    public List<Component> getTooltipStrings(InfuserBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
        int maxPossibleTemperature = 100; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
        int tMax = recipe.maxTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
        int tMin = recipe.minTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function

        if (mouseX >= 28 && mouseX <= 55+inputRenderer.getWidth() && mouseY >= 55 && mouseY <= 55+inputRenderer.getHeight()){
            return inputRenderer.getTooltip(recipe.getInputFluidStack(), TooltipFlag.Default.NORMAL);
        }
        if (mouseX >= 112 && mouseX <= 112+outputRenderer.getWidth() && mouseY >= 45 && mouseY <= 45+outputRenderer.getHeight()){
            return outputRenderer.getTooltip(recipe.getOutputFluidStack(), TooltipFlag.Default.NORMAL);
        }

        if(mouseX >= 11-7 && mouseX <= 11 && mouseY >= 16 && mouseY <= 16 + temperatureBar.getHeight()){
            Component displayMaxTemperature =
                    new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(recipe.maxTemperature), nf.format(maxPossibleTemperature));
            List<Component> componentList = new ArrayList<>(Collections.emptyList());
            componentList.add(displayMaxTemperature);
            return componentList;
        }
        if(mouseX >= 11+7 && mouseX <= 11+14 && mouseY >= 16 && mouseY <= 16 + temperatureBar.getHeight()){
            Component displayMinTemperature =
                    new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(recipe.minTemperature), nf.format(maxPossibleTemperature));
            List<Component> componentList = new ArrayList<>(Collections.emptyList());
            componentList.add(displayMinTemperature);
            return componentList;
        }

        return new ArrayList<>();


    }

}
