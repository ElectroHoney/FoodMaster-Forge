package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.PresserBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class PresserBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final ItemStack output1, output2;

    public Ingredient getPressedItem() {
        return pressedItem;
    }

    private final Ingredient pressedItem;
    public final FluidStack outputFluid;

    public final int percentage1, percentage2;

    public PresserBlockRecipe(ResourceLocation id, ItemStack output1, ItemStack output2,
                              Ingredient pressedItem, FluidStack outputFluid, int percentage1, int percentage2) {
        this.id = id;
        this.output1 = output1;
        this.output2 = output2;
        this.pressedItem = pressedItem;
        this.outputFluid = outputFluid;
        this.percentage1 = percentage1;
        this.percentage2 = percentage2;



    }
    @Override
    public NonNullList<Ingredient> getIngredients(){
        return null;
    }

    public FluidStack getOutputFluid(){
        return outputFluid;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {

        return pressedItem.test(pContainer.getItem(PresserBlockEntity.INPUT_SLOT_ID));
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return output1;
    }

    public ItemStack assembleSecondary(SimpleContainer pContainer) {
        return output2;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output1.copy();
    }
    public ItemStack getSecondaryResultItem() {
        return output2.copy();
    }

    public int getPercentage1() {
        return percentage1;
    }

    public int getPercentage2() {
        return percentage2;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }
    public static class Type implements RecipeType<PresserBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "pressing";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<PresserBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"pressing");

        @Override
        public PresserBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output1 = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output1"));
            ItemStack output2 = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output2"));
            JsonObject pressedItemJson = GsonHelper.getAsJsonObject(json, "pressedItem");
            Ingredient pressedItem = Ingredient.fromJson(pressedItemJson);

            JsonObject fluidJson1 = GsonHelper.getAsJsonObject(json, "outputFluid");
            Fluid fluid1 = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(
                    GsonHelper.getAsString(fluidJson1,"fluidNamespace"),
                    GsonHelper.getAsString(fluidJson1,"fluidName")));
            FluidStack outputFluid = new FluidStack(fluid1, GsonHelper.getAsInt(fluidJson1,"fluidAmount"));
            int percentageOutput1 = GsonHelper.getAsInt(json,"percentageOutput1");
            int percentageOutput2 = GsonHelper.getAsInt(json, "percentageOutput2");

            return new PresserBlockRecipe(id, output1, output2, pressedItem, outputFluid, percentageOutput1, percentageOutput2);
        }

        @Override
        public PresserBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient pressedItem = Ingredient.fromNetwork(buf);
            ItemStack output1 = buf.readItem();
            ItemStack output2 = buf.readItem();
            FluidStack outputFluid = buf.readFluidStack();
            int percentageOutput1 = buf.readInt();
            int percentageOutput2 = buf.readInt();
            return new PresserBlockRecipe(id, output1, output2, pressedItem, outputFluid, percentageOutput1, percentageOutput2);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PresserBlockRecipe recipe) {
            recipe.pressedItem.toNetwork(buf);
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeItemStack(recipe.getSecondaryResultItem(), false);
            buf.writeFluidStack(recipe.getOutputFluid());
            buf.writeInt(recipe.percentage1);
            buf.writeInt(recipe.percentage2);
        }

        @Override
        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        @Override
        public ResourceLocation getRegistryName() {
            return ID;
        }

        @Override
        public Class<RecipeSerializer<?>> getRegistryType() {
            return Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}
