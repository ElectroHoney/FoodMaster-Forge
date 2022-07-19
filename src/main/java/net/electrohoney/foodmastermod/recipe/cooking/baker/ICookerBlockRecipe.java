package net.electrohoney.foodmastermod.recipe.cooking.baker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.BakerBlockEntity;
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

public interface ICookerBlockRecipe {
     Ingredient getUtensil();
     int getMinTemperature();
     int getMaxTemperature();
     NonNullList<Ingredient> getIngredients();
     FluidStack getFluidStack();
     boolean matches(SimpleContainer pContainer, Level pLevel);
     ItemStack assemble(SimpleContainer pContainer);
     boolean canCraftInDimensions(int pWidth, int pHeight);
     ItemStack getResultItem();
     ResourceLocation getId();
     RecipeSerializer<?> getSerializer();
     RecipeType<?> getType();

    static interface ISerializer<T extends Recipe<SimpleContainer> > extends RecipeSerializer<T> {
         T fromJson(ResourceLocation id, JsonObject json);
         T fromNetwork(ResourceLocation id, FriendlyByteBuf buf);
         void toNetwork(FriendlyByteBuf buf, T recipe) ;
         ResourceLocation getRegistryName();
         Class<RecipeSerializer<?>> getRegistryType();
         @SuppressWarnings("unchecked") // Need this wrapper, because generics
         private static <G> Class<G> castClass(Class<?> cls) {
             return (Class<G>)cls;
         }
    }
}
