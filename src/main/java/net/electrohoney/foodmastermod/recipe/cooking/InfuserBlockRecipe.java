package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.InfuserBlockEntity;
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

public class InfuserBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final ItemStack output;

    public final int minTemperature;
    public final int maxTemperature;

    public final FluidStack inputFluid;
    public final FluidStack outputFluid;

    private final Ingredient infusedIngredient;

    public InfuserBlockRecipe(ResourceLocation id, ItemStack output, Ingredient infusedIngredient, int minTemperature, int maxTemperature, FluidStack inputFluid, FluidStack outputFluid) {
        this.id = id;
        this.output = output;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.infusedIngredient = infusedIngredient;

    }
    @Override
    public NonNullList<Ingredient> getIngredients(){
        return null;
    }
    public FluidStack getInputFluidStack(){
        return inputFluid;
    }
    public FluidStack getOutputFluidStack(){
        return outputFluid;
    }
    public Ingredient getInputIngredient(){
        return infusedIngredient;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if(!infusedIngredient.test(pContainer.getItem(InfuserBlockEntity.INGREDIENT_SLOT_ID))){
            return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output.copy();
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
    public static class Type implements RecipeType<InfuserBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "infusing";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<InfuserBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"infusing");

        @Override
        public InfuserBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            int minTemperature = GsonHelper.getAsInt(json, "minTemperature");
            int maxTemperature = GsonHelper.getAsInt(json, "maxTemperature");

            JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "inputFluid");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(
                    GsonHelper.getAsString(fluidJson,"fluidNamespace"),
                    GsonHelper.getAsString(fluidJson,"fluidName")));
            FluidStack inputFluid = new FluidStack(fluid, GsonHelper.getAsInt(fluidJson,"fluidAmount"));

            JsonObject fluidJson1 = GsonHelper.getAsJsonObject(json, "outputFluid");
            Fluid fluid1 = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(
                    GsonHelper.getAsString(fluidJson1,"fluidNamespace"),
                    GsonHelper.getAsString(fluidJson1,"fluidName")));
            FluidStack outputFluid = new FluidStack(fluid1, GsonHelper.getAsInt(fluidJson1,"fluidAmount"));

            JsonObject infusedJson = GsonHelper.getAsJsonObject(json, "infusingInput");
            Ingredient input = Ingredient.fromJson(infusedJson);

            return new InfuserBlockRecipe(id, output, input, minTemperature, maxTemperature, inputFluid, outputFluid);
        }

        @Override
        public InfuserBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack output = buf.readItem();
            int minTemperature = buf.readInt();
            int maxTemperature =  buf.readInt();
            FluidStack inputFluid = buf.readFluidStack();
            FluidStack outputFluid = buf.readFluidStack();
            return new InfuserBlockRecipe(id, output, input, minTemperature, maxTemperature, inputFluid, outputFluid);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, InfuserBlockRecipe recipe) {
            recipe.infusedIngredient.toNetwork(buf);
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeInt(recipe.minTemperature);
            buf.writeInt(recipe.maxTemperature);
            buf.writeFluidStack(recipe.inputFluid);
            buf.writeFluidStack(recipe.outputFluid);
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
