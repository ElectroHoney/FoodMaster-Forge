package net.electrohoney.foodmastermod.integration;

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
import net.electrohoney.foodmastermod.item.ModItems;
import net.electrohoney.foodmastermod.recipe.PotBlockRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
//bad idea but I hate to see those warnings!
@SuppressWarnings("removal")
public class PotBoilingRecipeCategory implements IRecipeCategory<PotBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "boiling");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/pot_block_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    private IDrawable temperatureBar;

    private final IGuiHelper iGuiHelper;


    public PotBoilingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 194, 85);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.POT_BLOCK.get()));

        this.temperatureBar = helper.createDrawable(TEXTURE, 195, 16, 7, 55);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends PotBlockRecipe> getRecipeClass() {
        return PotBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Pot Boiling");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull PotBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 53).addIngredients(Ingredient.of((Items.POTION).getDefaultInstance()));

        int ingredientIndex = 0;
        for(int i = 0; i<=2; i++){
            for(int j = 0; j<=2; j++){
                if(ingredientIndex < recipe.getIngredients().size()){
                    builder.addSlot(RecipeIngredientRole.INPUT, 53+18*(i%3), 17+18*(j%3)).addIngredients(recipe.getIngredients().get(ingredientIndex));
                    ingredientIndex++;
                }

            }

        }

        builder.setShapeless();

        builder.addSlot(RecipeIngredientRole.INPUT, 143, 59).addIngredients(Ingredient.of(ModItems.TURMERIC.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 143, 35).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(PotBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        if(recipe.maxTemperature > 0){
            int temperatureBarHeight = 55; //pixels, using I in comments is fun. I did stuff lol
            int maxPossibleTemperature = 200; //degrees? I just set an arbitrary amount identical to the block entity, also it has nothing to do with the maxTemperature from the recipe
            int tMax = recipe.maxTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            int tMin = recipe.minTemperature * temperatureBarHeight / maxPossibleTemperature; //i stole this from my menu function
            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 195, 70-tMax, 7, tMax);
            this.temperatureBar.draw(stack, 35-7, 16 + 53 - tMax); //it is drawn correctly but I can't set it a varible size

            temperatureBar = iGuiHelper.createDrawable(TEXTURE, 195, 70-tMin, 7, tMin);
            this.temperatureBar.draw(stack, 35+7, 16 + 53 - tMin); //it is drawn correctly but I can't set it a varible size
        }
    }
}
