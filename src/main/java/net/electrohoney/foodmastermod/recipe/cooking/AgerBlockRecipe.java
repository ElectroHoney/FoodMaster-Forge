package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
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

public class AgerBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final FluidStack output;

    public final FluidStack input;

    public final Ingredient timePiece;


    public AgerBlockRecipe(ResourceLocation id, FluidStack output, FluidStack input, Ingredient timePiece) {
        this.id = id;
        this.output = output;
        this.input = input;
        this.timePiece = timePiece;
    }

    public FluidStack getInput(){
        return input;
    }
    public FluidStack getOutput(){
        return output;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return timePiece.test(pContainer.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return null;
    }


    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return null;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return Recipe.super.getIngredients();
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

    public Ingredient getTimePiece() {
        return timePiece;
    }

    public static class Type implements RecipeType<AgerBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "ageing";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<AgerBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"ageing");

        @Override
        public AgerBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonObject timePieceJson = GsonHelper.getAsJsonObject(json, "timePiece");
            //Might be a "catalyst"
            Ingredient timePiece = Ingredient.fromJson(timePieceJson);

            JsonObject inputFluidJson = GsonHelper.getAsJsonObject(json, "input_fluid");
            Fluid inputFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(inputFluidJson,"fluid_namespace"), GsonHelper.getAsString(inputFluidJson,"fluid_name")));
            assert inputFluid != null;
            FluidStack fluidStackInput = new FluidStack(inputFluid, GsonHelper.getAsInt(inputFluidJson,"fluid_amount"));

            JsonObject outputFluidJson = GsonHelper.getAsJsonObject(json, "output_fluid");
            Fluid outputFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(outputFluidJson,"fluid_namespace"), GsonHelper.getAsString(outputFluidJson,"fluid_name")));
            assert outputFluid != null;
            FluidStack fluidStackOutput = new FluidStack(outputFluid, GsonHelper.getAsInt(outputFluidJson,"fluid_amount"));

            return new AgerBlockRecipe(id, fluidStackOutput, fluidStackInput, timePiece);
        }

        @Override
        public AgerBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient timePiece = Ingredient.fromNetwork(buf);
            FluidStack outputFluidStack = buf.readFluidStack();
            FluidStack inputFluidStack = buf.readFluidStack();
            return new AgerBlockRecipe(id, outputFluidStack, inputFluidStack, timePiece);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AgerBlockRecipe recipe) {
            recipe.timePiece.toNetwork(buf);
            buf.writeFluidStack(recipe.input);
            buf.writeFluidStack(recipe.output);
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
