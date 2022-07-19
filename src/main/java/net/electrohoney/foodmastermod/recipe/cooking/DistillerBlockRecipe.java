package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.DistillerBlockEntity;
import net.electrohoney.foodmastermod.screen.screens.DistillerBlockScreen;
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

public class DistillerBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final NonNullList<Ingredient> recipeIngredients;

    public final int minTemperature;
    public final int maxTemperature;

    public final FluidStack inputFluidStack, outputFluidStack;

    private final Ingredient mush;

    public DistillerBlockRecipe(ResourceLocation id, NonNullList<Ingredient> recipeItems, int minTemperature, int maxTemperature, FluidStack inputFluidStack, FluidStack outputFluidStack, Ingredient mush) {
        this.id = id;
        this.recipeIngredients = recipeItems;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.inputFluidStack = inputFluidStack;
        this.outputFluidStack = outputFluidStack;
        this.mush = mush;
    }
    public Ingredient getMush() {
        return this.mush;
    }
    @Override
    public NonNullList<Ingredient> getIngredients(){
        return recipeIngredients;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {

        if(!mush.test(pContainer.getItem(DistillerBlockEntity.MUSH_SLOT_ID))){
            return false;
        }

        if(recipeIngredients.size() == 0){
            return true;
        }

        boolean isFilled = false;
        for(int g = 1; g<= 3; g++){

            if(pContainer.getItem(g) != ItemStack.EMPTY){
                isFilled = true;
            }
        }

        if(!isFilled){
            return false;
        }

        int allIngredients = 0;
        for (int i = 0; i < recipeIngredients.size(); i++){
            for (int g = 1; g <= 3; g++){
                if(recipeIngredients.get(i).test(pContainer.getItem(g))) {
                    allIngredients ++;
                }
            }
        }

        if(allIngredients != recipeIngredients.size()){
            return false;
        }

        for (int g = 1; g <= 3; g++){
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

    public FluidStack getInputFluidStack() {
        return inputFluidStack;
    }

    public FluidStack getOutputFluidStack() {
        return outputFluidStack;
    }

    public static class Type implements RecipeType<DistillerBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "distilling";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<DistillerBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"distilling");

        @Override
        public DistillerBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            int minTemperature = GsonHelper.getAsInt(json, "minTemperature");
            int maxTemperature = GsonHelper.getAsInt(json, "maxTemperature");
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients", new JsonArray());
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

            JsonObject mushJson = GsonHelper.getAsJsonObject(json, "mush");
            Ingredient mush = Ingredient.fromJson(mushJson);

            return new DistillerBlockRecipe(id, inputs, minTemperature, maxTemperature, inputFluid, outputFluid, mush);
        }

        @Override
        public DistillerBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
            int minTemperature = buf.readInt();
            int maxTemperature =  buf.readInt();
            FluidStack inputFluid = buf.readFluidStack();
            FluidStack outputFluid = buf.readFluidStack();
            Ingredient mush = Ingredient.fromNetwork(buf);
            return new DistillerBlockRecipe(id, inputs, minTemperature, maxTemperature, inputFluid, outputFluid, mush);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DistillerBlockRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeInt(recipe.minTemperature);
            buf.writeInt(recipe.maxTemperature);
            buf.writeFluidStack(recipe.inputFluidStack);
            buf.writeFluidStack(recipe.outputFluidStack);
            recipe.mush.toNetwork(buf);
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
