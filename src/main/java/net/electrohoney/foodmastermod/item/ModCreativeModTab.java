package net.electrohoney.foodmastermod.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModCreativeModTab {
    public static final CreativeModeTab FOODMASTER_TAB = new CreativeModeTab("foodmastermodtab") {
        @Override
        public ItemStack makeIcon(){
            return new ItemStack(ModItems.TBONE_STEAK.get());
        }
    };
}
