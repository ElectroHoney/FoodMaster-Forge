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
import net.electrohoney.foodmastermod.block.entity.custom.AgerBlockEntity;
import net.electrohoney.foodmastermod.recipe.cooking.AgerBlockRecipe;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//bad idea but I hate to see those warnings!
@SuppressWarnings("removal")
public class AgerAgeingRecipeCategory implements IRecipeCategory<AgerBlockRecipe> {
    public final static ResourceLocation UID = new ResourceLocation(FoodMaster.MOD_ID, "ageing");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/ager_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    private IDrawable clockBar;

    private final IGuiHelper iGuiHelper;

    private FluidStackRenderer renderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();



    public AgerAgeingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 70);

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.AGER_BLOCK.get()));

        this.clockBar = helper.createDrawable(TEXTURE, 177, 0, 16, 16);

        this.renderer =new FluidStackRenderer(AgerBlockEntity.AGER_MAX_FLUID_CAPACITY, true, 52, 52);

        iGuiHelper = helper;
    }


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends AgerBlockRecipe> getRecipeClass() {
        return AgerBlockRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Ageing");
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull AgerBlockRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 80, 35).addIngredients(recipe.getTimePiece());
    }

    @Override
    public void draw(AgerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        this.clockBar.draw(stack, 81, 16);
        //the xPosition and yPosition are the pixel location on the gui texture
        renderer.render(stack, 8, 17,recipe.getInput());
        renderer.render(stack, 116, 17,recipe.getOutput());

    }

    @Override
    public List<Component> getTooltipStrings(AgerBlockRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        if (mouseX >= 8 && mouseX <= 10+52 && mouseY >= 17 && mouseY <= 17+52){
            return renderer.getTooltip(recipe.getInput(), TooltipFlag.Default.NORMAL);
        }
        //52 is the size of the renderer box
        if (mouseX >= 116 && mouseX <= 116+52 && mouseY >= 17 && mouseY <= 17+52){
            return renderer.getTooltip(recipe.getOutput(), TooltipFlag.Default.NORMAL);
        }

//        if(mouseX >= 35+7 && mouseX <= 35+14 && mouseY >= 16 && mouseY <= 69){
//            Component displayMinTemperature =
//                    new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(recipe.minTemperature), nf.format(maxPossibleTemperature));
//            List<Component> componentList = new ArrayList<>(Collections.emptyList());
//            componentList.add(displayMinTemperature);
//            return componentList;
//        }

        return new ArrayList<>();


    }

}
