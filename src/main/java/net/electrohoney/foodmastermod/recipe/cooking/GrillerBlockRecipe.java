package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.GrillerBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class GrillerBlockRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeIngredients;

    public final int minTemperature;
    public final int maxTemperature;

    private final Ingredient tool;

    public GrillerBlockRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, int minTemperature, int maxTemperature, Ingredient tool) {
        this.id = id;
        this.output = output;
        this.recipeIngredients = recipeItems;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.tool = tool;
    }
    public Ingredient getUtensil() {
        return this.tool;
    }
    @Override
    public NonNullList<Ingredient> getIngredients(){
        return recipeIngredients;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {

        if(!tool.test(pContainer.getItem(GrillerBlockEntity.UTENSIL_SLOT_ID))){
            return false;
        }

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
    public static class Type implements RecipeType<GrillerBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "grilling";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<GrillerBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"grilling");

        @Override
        public GrillerBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            int minTemperature = GsonHelper.getAsInt(json, "minTemperature");
            int maxTemperature = GsonHelper.getAsInt(json, "maxTemperature");
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            JsonObject toolJson = GsonHelper.getAsJsonObject(json, "tool");
            Ingredient tool = Ingredient.fromJson(toolJson);

            return new GrillerBlockRecipe(id, output, inputs, minTemperature, maxTemperature, tool);
        }

        @Override
        public GrillerBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
            int minTemperature = buf.readInt();
            int maxTemperature =  buf.readInt();
            Ingredient tool = Ingredient.fromNetwork(buf);
            return new GrillerBlockRecipe(id, output, inputs, minTemperature, maxTemperature, tool);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, GrillerBlockRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeInt(recipe.minTemperature);
            buf.writeInt(recipe.maxTemperature);
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
