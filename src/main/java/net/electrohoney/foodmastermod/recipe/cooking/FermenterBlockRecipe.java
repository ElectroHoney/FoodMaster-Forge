package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
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

public class FermenterBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeIngredients;


    public final FluidStack inputFluid, outputFluid;

    public FermenterBlockRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, FluidStack inputFluid, FluidStack outputFluid) {
        this.id = id;
        this.output = output;
        this.recipeIngredients = recipeItems;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
    }
    @Override
    public NonNullList<Ingredient> getIngredients(){
        return recipeIngredients;
    }

    public FluidStack getInputFluid(){
        return inputFluid;
    }
    public FluidStack getOutputFluid(){
        return outputFluid;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {

        boolean isFilled = false;
        for(int g = 0; g<= 8; g++){

            if(pContainer.getItem(g) != ItemStack.EMPTY){
                isFilled = true;
            }
        }
        if(!isFilled){
            return false;
        }

        int allIngredients = 0;
        for (int i = 0; i < recipeIngredients.size(); i++){
            for (int g = 0; g <= 8; g++){
                if(recipeIngredients.get(i).test(pContainer.getItem(g))) {
                    allIngredients ++;
                }
            }
        }
        if(allIngredients != recipeIngredients.size()){
            return false;
        }

        for (int g = 0; g <= 8; g++){
            if(pContainer.getItem(g) != ItemStack.EMPTY){
                boolean doesItemExist = false;
                for( int i = 0; i < recipeIngredients.size(); ++ i){
                    if(recipeIngredients.get(i).test(pContainer.getItem(g))){
                        doesItemExist = recipeIngredients.get(i).test(pContainer.getItem(g));
                    }
                }
                if(!doesItemExist){
                    return false;
                }

            }

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
    public static class Type implements RecipeType<FermenterBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "fermenting";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<FermenterBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"fermenting");

        @Override
        public FermenterBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

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


            return new FermenterBlockRecipe(id, output, inputs, inputFluid, outputFluid);
        }

        @Override
        public FermenterBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }
            ItemStack output = buf.readItem();
            FluidStack inputFluid = buf.readFluidStack();
            FluidStack outputFluid = buf.readFluidStack();
            return new FermenterBlockRecipe(id, output, inputs, inputFluid, outputFluid);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FermenterBlockRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeFluidStack(recipe.getInputFluid());
            buf.writeFluidStack(recipe.getOutputFluid());
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
