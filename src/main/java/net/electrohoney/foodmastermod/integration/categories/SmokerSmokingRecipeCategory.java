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
import net.electrohoney.foodmastermod.block.entity.custom.SmokerBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.SmokerBlockRecipe;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//bad idea but I hate to see those warnings!
@SuppressWarnings("removal")
public class SmokerSmokingRecipeCategory implements IRecipeCategory<SmokerBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "smoking");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/smoker_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    private IDrawable temperatureBar;

    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer renderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public SmokerSmokingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 78);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.SMOKER_BLOCK.get()));

        this.temperatureBar = helper.createDrawable(TEXTURE, 176, 31, 7, 55);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends SmokerBlockRecipe> getRecipeClass() {
        return SmokerBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Smoking");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull SmokerBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {

        int ingredientIndex = 0;
        for(int j = 1; j<=3; j++){
            if(ingredientIndex < recipe.getIngredients().size()){
                builder.addSlot(RecipeIngredientRole.INPUT, 72, 13+18*j).addIngredients(recipe.getIngredients().get(ingredientIndex));
                ingredientIndex++;
            }

        }

        builder.setShapeless();

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 17, 57).addIngredients(recipe.getWoodLog());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 35).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(SmokerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        if(recipe.getMaxTemperature() > 0){
            int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
            int maxPossibleTemperature = 500; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
            int tMax = recipe.getMaxTemperature() * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            int tMin = recipe.getMinTemperature() * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 85-tMax, 7, tMax);
            this.temperatureBar.draw(stack, 30-4, 29 + 53 - tMax); //it is drawn correctly but I can't set it a varible size

            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 85-tMin, 7, tMin);
            this.temperatureBar.draw(stack, 30+4, 29 + 53 - tMin); //it is drawn correctly but I can't set it a varible size
        }

    }

    @Override
    public List<Component> getTooltipStrings(SmokerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
        int maxPossibleTemperature = 500; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
        int tMax = recipe.getMaxTemperature() * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
        int tMin = recipe.getMinTemperature() * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function

        if(mouseX >= 30-4 && mouseX <= 30 && mouseY >= 31 && mouseY <= 31+52){
            return displayTemperature(recipe.getMaxTemperature(), maxPossibleTemperature);
        }
        if(mouseX >= 30+4 && mouseX <= 30+4+7 && mouseY >= 31 && mouseY <= 31+52){
            return displayTemperature(recipe.getMinTemperature(), maxPossibleTemperature);
        }

        return new ArrayList<>();


    }

    private List<Component> displayTemperature(int currentTemp, int maxPossibleTemperature){
        Component displayTemperature =
                new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(currentTemp), nf.format(maxPossibleTemperature));
        List<Component> componentList = new ArrayList<>(Collections.emptyList());
        componentList.add(displayTemperature);
        return componentList;
    }

}
