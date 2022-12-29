package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.SmokerBlockEntity;
import net.electrohoney.foodmastermod.screen.screens.SmokerBlockScreen;
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

public class SmokerBlockRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeIngredients;
    private final Ingredient woodLog;
    private final int maxProgress;
    private final int minTemperature;
    private final int maxTemperature;
    private final int smokeTime;

    public SmokerBlockRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, Ingredient woodLog, int minTemperature, int maxTemperature, int maxProgress, int smokeTime) {
        this.id = id;
        this.output = output;
        this.recipeIngredients = recipeItems;
        this.woodLog = woodLog;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.maxProgress = maxProgress;
        this.smokeTime = smokeTime;
    }
    public int getMinTemperature() {
        return minTemperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }
    public Ingredient getWoodLog() {
        return woodLog;
    }

    @Override
    public NonNullList<Ingredient> getIngredients(){
        return recipeIngredients;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {

        if(!woodLog.test(pContainer.getItem(SmokerBlockEntity.FUEL_SLOT_ID))){
            return false;
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
    public static class Type implements RecipeType<SmokerBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "smoking";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<SmokerBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"smoking");

        @Override
        public SmokerBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            int minTemperature = GsonHelper.getAsInt(json, "minTemperature");
            int maxTemperature = GsonHelper.getAsInt(json, "maxTemperature");
            int maxProgress = GsonHelper.getAsInt(json, "maxProgress");

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }
            JsonObject woodLogJson = json.getAsJsonObject("woodLog");
            Ingredient woodLog = Ingredient.fromJson(woodLogJson);

            //this has fallback so I should implement one for everything
            int smokeTime = GsonHelper.getAsInt(json, "smokeTime", 200);

            //System.out.println("fluid->" + fluid + "tool" + tool + "cooking" + cookingTime);
            return new SmokerBlockRecipe(id, output, inputs, woodLog, minTemperature, maxTemperature, maxProgress, smokeTime);
        }

        @Override
        public SmokerBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }
            Ingredient woodLog = Ingredient.fromNetwork(buf);
            ItemStack output = buf.readItem();
            int minTemperature = buf.readInt();
            int maxTemperature =  buf.readInt();
            int maxProgress = buf.readInt();
            int smokeTime = buf.readInt();
            return new SmokerBlockRecipe(id, output, inputs, woodLog, minTemperature, maxTemperature, maxProgress, smokeTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SmokerBlockRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            recipe.woodLog.toNetwork(buf);
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeInt(recipe.minTemperature);
            buf.writeInt(recipe.maxTemperature);
            buf.writeInt(recipe.maxProgress);
            buf.writeInt(recipe.smokeTime);
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

