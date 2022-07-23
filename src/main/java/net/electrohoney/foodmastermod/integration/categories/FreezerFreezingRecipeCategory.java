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
import net.electrohoney.foodmastermod.block.entity.custom.FreezerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.FreezerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.electrohoney.foodmastermod.screen.slot.ModResultSlot;
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
public class FreezerFreezingRecipeCategory implements IRecipeCategory<FreezerBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "freezing");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/freezer_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    private IDrawable temperatureBar;

    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer renderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public FreezerFreezingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.FREEZER_BLOCK.get()));

        this.temperatureBar = helper.createDrawable(TEXTURE, 176, 19, 7, 54);

        this.renderer = new FluidStackRenderer(FreezerBlockEntity.FREEZER_MAX_FLUID_CAPACITY, true, 12, 53);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends FreezerBlockRecipe> getRecipeClass() {
        return FreezerBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Freezing");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull FreezerBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {

        int ingredientIndex = 0;
        for(int i = 0; i<=2; i++){
            for(int j = 0; j<=2; j++){
                if(!(i==1 && j==1)) {
                    if (ingredientIndex < recipe.getIngredients().size()) {
                        builder.addSlot(RecipeIngredientRole.INPUT, 48 + 18 * (i % 3), 17 + 18 * (j % 3)).addIngredients(recipe.getIngredients().get(ingredientIndex));
                        ingredientIndex++;
                    }
                }

            }

        }

        builder.setShapeless();

        builder.addSlot(RecipeIngredientRole.CATALYST, 134, 59).addIngredients(recipe.getUtensil());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 35).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(FreezerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
            int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
            int maxPossibleTemperature = 125; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
            int tMax = Math.abs(recipe.maxTemperature-25) * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            int tMin = Math.abs(recipe.minTemperature-25) * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 19, 7, tMax);
            this.temperatureBar.draw(stack, 29-7, 16);
            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 176, 19, 7, tMin);
            this.temperatureBar.draw(stack, 29+7, 16); //it is drawn correctly but I can't set it a varible size
        //the xPosition and yPosition are the pixel location on the gui texture
        renderer.render(stack, 8, 16,recipe.getFluidStack());

    }

    @Override
    public List<Component> getTooltipStrings(FreezerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
        int maxPossibleTemperature = 125; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
        int tMax = Math.abs(recipe.maxTemperature) * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
        int tMin = Math.abs(recipe.minTemperature) * temperatureBarHeight / maxPossibleTemperature; ///i stole this from my menu function

        if (mouseX >= 8 && mouseX <= 8+12 && mouseY >= 17 && mouseY <= 17+52){
            return renderer.getTooltip(recipe.getFluidStack(), TooltipFlag.Default.NORMAL);
        }

        if(mouseX >= 29-7 && mouseX <= 29 && mouseY >= 16 && mouseY <= 69){
            Component displayMaxTemperature =
                    new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(recipe.maxTemperature), nf.format(maxPossibleTemperature));
            List<Component> componentList = new ArrayList<>(Collections.emptyList());
            componentList.add(displayMaxTemperature);
            return componentList;
        }
        if(mouseX >= 29+7 && mouseX <= 29+14 && mouseY >= 16 && mouseY <= 69){
            Component displayMinTemperature =
                    new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(recipe.minTemperature), nf.format(maxPossibleTemperature));
            List<Component> componentList = new ArrayList<>(Collections.emptyList());
            componentList.add(displayMinTemperature);
            return componentList;
        }

        return new ArrayList<>();


    }

}
