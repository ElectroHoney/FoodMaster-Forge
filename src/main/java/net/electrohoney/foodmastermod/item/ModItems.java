package net.electrohoney.foodmastermod.item;

import net.electrohoney.foodmastermod.FoodMaster;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FoodMaster.MOD_ID);

    public static final RegistryObject<Item> TBONE_STEAK = ITEMS.register("tbone_steak",
            ()-> new Item(new Item.Properties().tab(ModCreativeModTab.FOODMASTER_TAB))
    );
    public static final RegistryObject<Item> TURMERIC = ITEMS.register("turmeric",
            ()-> new Item(new Item.Properties().tab(ModCreativeModTab.FOODMASTER_TAB).stacksTo(1).durability(4).food(new FoodProperties.Builder().saturationMod(1f).nutrition(2).fast().alwaysEat().build()))
    );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
