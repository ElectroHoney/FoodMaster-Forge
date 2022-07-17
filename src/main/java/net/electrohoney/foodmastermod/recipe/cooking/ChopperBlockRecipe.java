package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.ChopperBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
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

import static net.electrohoney.foodmastermod.block.entity.custom.ChopperBlockEntity.INPUT_SLOT_ID;

public class ChopperBlockRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final Ingredient input;
    private final Ingredient tool;
    private final int choppingTime;

    public int getChoppingTime() {
        return choppingTime;
    }

    public ChopperBlockRecipe(ResourceLocation id, ItemStack output, Ingredient input, Ingredient tool, int choppingTime) {
        this.id = id;
        this.output = output;
        this.input = input;
        this.tool = tool;
        this.choppingTime = choppingTime;
    }
    public Ingredient getUtensil() {
        return this.tool;
    }

    public Ingredient getIngredient(){
        return input;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {

        if(!tool.test(pContainer.getItem(ChopperBlockEntity.UTENSIL_SLOT_ID))){
            return false;
        }

        return input.test(pContainer.getItem(INPUT_SLOT_ID));
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
    public static class Type implements RecipeType<ChopperBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "chopping";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<ChopperBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"chopping");

        @Override
        public ChopperBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            JsonObject toolJson = GsonHelper.getAsJsonObject(json, "tool");
            Ingredient tool = Ingredient.fromJson(toolJson);
            int choppingTime = GsonHelper.getAsInt(json, "choppingTime", 350);
            return new ChopperBlockRecipe(id, output, input, tool, choppingTime);
        }

        @Override
        public ChopperBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack output = buf.readItem();
            Ingredient tool = Ingredient.fromNetwork(buf);
            int time = buf.readInt();
            return new ChopperBlockRecipe(id, output, input, tool, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ChopperBlockRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItemStack(recipe.getResultItem(), false);
            recipe.tool.toNetwork(buf);
            buf.writeInt(recipe.choppingTime);
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
