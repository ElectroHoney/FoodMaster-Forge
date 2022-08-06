package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.FryerBlockEntity;
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

public class FryerBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeIngredients;

    private final static int listSize = 9;

    public final int minTemperature;
    public final int maxTemperature;

    public final FluidStack fluidStack;

    private final Ingredient tool;

    public FryerBlockRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, int minTemperature, int maxTemperature, FluidStack fluidStack, Ingredient tool) {
        this.id = id;
        this.output = output;
        this.recipeIngredients = recipeItems;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.fluidStack = fluidStack;
        this.tool = tool;
    }
    public Ingredient getUtensil() {
        return this.tool;
    }
    @Override
    public NonNullList<Ingredient> getIngredients(){
        return recipeIngredients;
    }
    public FluidStack getFluidStack(){
        return fluidStack;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {

        if(!tool.test(pContainer.getItem(FryerBlockEntity.UTENSIL_SLOT_ID))){
            return false;
        }

        boolean isFilled = false;
        for(int g = 1; g<= 5; g++){

            if(pContainer.getItem(g) != ItemStack.EMPTY){
                isFilled = true;
            }
        }
        if(!isFilled){
            return false;
        }

        int allIngredients = 0;
        for (int i = 0; i < recipeIngredients.size(); i++){
            for (int g = 1; g <= 5; g++){
                if(recipeIngredients.get(i).test(pContainer.getItem(g))) {
                    allIngredients ++;
                }
            }
        }
        if(allIngredients != recipeIngredients.size()){
            return false;
        }

        for (int g = 1; g <= 5; g++){
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
    public static class Type implements RecipeType<FryerBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "frying";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<FryerBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"frying");

        @Override
        public FryerBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            int minTemperature = GsonHelper.getAsInt(json, "minTemperature");
            int maxTemperature = GsonHelper.getAsInt(json, "maxTemperature");
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }
            JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "fluid");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(fluidJson,"fluidNamespace"), GsonHelper.getAsString(fluidJson,"fluidName")));
            FluidStack fluidStack1 = new FluidStack(fluid, GsonHelper.getAsInt(fluidJson,"fluidAmount"));
            JsonObject toolJson = GsonHelper.getAsJsonObject(json, "tool");
            Ingredient tool = Ingredient.fromJson(toolJson);

            return new FryerBlockRecipe(id, output, inputs, minTemperature, maxTemperature, fluidStack1, tool);
        }

        @Override
        public FryerBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
            int minTemperature = buf.readInt();
            int maxTemperature =  buf.readInt();
            FluidStack fluidStack1 = buf.readFluidStack();
            Ingredient tool = Ingredient.fromNetwork(buf);
            return new FryerBlockRecipe(id, output, inputs, minTemperature, maxTemperature, fluidStack1, tool);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FryerBlockRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeInt(recipe.minTemperature);
            buf.writeInt(recipe.maxTemperature);
            buf.writeFluidStack(recipe.fluidStack);
            recipe.tool.toNetwork(buf);
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
