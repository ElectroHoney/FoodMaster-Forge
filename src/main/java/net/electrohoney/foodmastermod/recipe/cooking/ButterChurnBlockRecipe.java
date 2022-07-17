package net.electrohoney.foodmastermod.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
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

public class ButterChurnBlockRecipe implements Recipe<SimpleContainer> {
//    @todo I have to modify the recipe so its not shapeless
    private final ResourceLocation id;
    private final ItemStack output;

    private final static int listSize = 9;


    public final FluidStack fluidStack;


    public ButterChurnBlockRecipe(ResourceLocation id, ItemStack output, FluidStack fluidStack) {
        this.id = id;
        this.output = output;
        this.fluidStack = fluidStack;
    }
    public FluidStack getFluidStack(){
        return fluidStack;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
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
    public static class Type implements RecipeType<ButterChurnBlockRecipe>{
        private Type(){}
            public static final Type INSTANCE = new Type();
            public static final String ID = "churning";
    }

    //thanks a lot Kaupenjoe!! :)
    //a modified version of this from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/blob/45-recipeTypes/src/main/java/net/kaupenjoe/tutorialmod/recipe/GemCuttingStationRecipe.java
    public static class Serializer implements RecipeSerializer<ButterChurnBlockRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(FoodMaster.MOD_ID,"churning");

        @Override
        public ButterChurnBlockRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "fluid");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(fluidJson,"fluidNamespace"), GsonHelper.getAsString(fluidJson,"fluidName")));
            FluidStack fluidStack1 = new FluidStack(fluid, GsonHelper.getAsInt(fluidJson,"fluidAmount"));

            return new ButterChurnBlockRecipe(id, output, fluidStack1);
        }

        @Override
        public ButterChurnBlockRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ItemStack output = buf.readItem();
            FluidStack fluidStack1 = buf.readFluidStack();

            return new ButterChurnBlockRecipe(id, output, fluidStack1);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ButterChurnBlockRecipe recipe) {
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeFluidStack(recipe.fluidStack);
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
