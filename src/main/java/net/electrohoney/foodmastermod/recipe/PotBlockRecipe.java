package net.electrohoney.foodmastermod.recipe;

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

import javax.annotation.Nullable;

public class PotBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeIngredients;

    private final static int listSize = 9;

    public final int minTemperature;
    public final int maxTemperature;

    public PotBlockRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, int minTemperature, int maxTemperature) {
        this.id = id;
        this.output = output;
        this.recipeIngredients = recipeItems;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    @Override
    public NonNullList<Ingredient> getIngredients(){
        return recipeIngredients;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
//        NonNullList<ItemStack> recipeItems = NonNullList.withSize(recipeIngredients.size(), ItemStack.EMPTY);
//        for (int i = 0; i < recipeIngredients.size(); i++) {
//            recipeItems.set(i, recipeIngredients.get(i).getItems()[0]);
//        }

        boolean isFilled = false;
        for(int g = 1; g<= 9; g++){

            if(pContainer.getItem(g) != ItemStack.EMPTY){
                isFilled = true;
            }
        }
        if(!isFilled){
            return false;
        }
        int allIngredients = 0;
        for (int i = 0; i < recipeIngredients.size(); i++){
            for (int g = 1; g <= 9; g++){
                if(recipeIngredients.get(i).test(pContainer.getItem(g))) {
                    allIngredients ++;
                }
            }
        }
        if(allIngredients != recipeIngredients.size()){
            return false;
        }

        for (int g = 1; g <= 9; g++){
            //System.out.println("________________");
            //System.out.println(pContainer.getItem(g) + "<->" + g);
            //System.out.println("@@@@@@@@@@@@@@@@");

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

//            boolean checkOnlyItemsInRecipe = false;
//            for (Ingredient recipeIngredient : recipeIngredients) {
//                if (pContainer.getItem(g) != ItemStack.EMPTY) {
//                    System.out.println("____________________");
//                    System.out.println(pContainer.getItem(g));
//                    System.out.println(recipeIngredient.test(pContainer.getItem(g)));
//                    System.out.println("____________________");
//
//                    checkOnlyItemsInRecipe = checkOnlyItemsInRecipe || recipeIngredient.test(pContainer.getItem(g));
//                }
//            }
//            System.out.println("FINAL TEST:" + checkOnlyItemsInRecipe);
//            if(checkOnlyItemsInRecipe == false){
//                return false;
//            }
        }
        return true;

//        for (int i = 0; i < recipeIngredients.size(); i++) {
//            boolean findIfItemInContainer = false;
//            for(int g=0; g <= 9; g++) {
//                if(recipeIngredients.get(i).test(pContainer.getItem(g))){
//                    findIfItemInContainer = true;
//                }
//            }
//
////            System.out.println(findIfItemInContainer);
//            allItemsInContainer = allItemsInContainer && findIfItemInContainer;
//        }
//
//
//        return allItemsInContainer;
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
    public static class Type implements RecipeType<PotBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "boiling";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<PotBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"boiling");

        @Override
        public PotBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            int minTemperature = GsonHelper.getAsInt(json, "min_temperature");
            int maxTemperature = GsonHelper.getAsInt(json, "max_temperature");
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
//            System.out.println("Inputs:" + inputs);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }
//            System.out.println("Ingredients:" +  ingredients);

            return new PotBlockRecipe(id, output, inputs, minTemperature, maxTemperature);
        }

        @Override
        public PotBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
            int minTemperature = buf.readInt();
            int maxTemperature =  buf.readInt();
            return new PotBlockRecipe(id, output, inputs, minTemperature, maxTemperature);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PotBlockRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeInt(recipe.minTemperature);
            buf.writeInt(recipe.maxTemperature);
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
