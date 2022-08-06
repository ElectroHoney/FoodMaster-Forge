package net.electrohoney.foodmastermod.screen;

import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.screen.menus.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>>MENUS =
    DeferredRegister.create(ForgeRegistries.CONTAINERS, FoodMaster.MOD_ID);

    public static final RegistryObject<MenuType<PotBlockMenu>> POT_BLOCK_MENU =
    registerMenuType(PotBlockMenu::new, "pot_block_menu");

    public static final RegistryObject<MenuType<AgerBlockMenu>> AGER_BLOCK_MENU =
            registerMenuType(AgerBlockMenu::new, "ager_block_menu");

    public static final RegistryObject<MenuType<BakerBlockMenu>> BAKER_BLOCK_MENU =
            registerMenuType(BakerBlockMenu::new, "baker_block_menu");

    public static final RegistryObject<MenuType<ButterChurnBlockMenu>> BUTTER_CHURNER_MENU =
            registerMenuType(ButterChurnBlockMenu::new, "churner_block_menu");

    public static final RegistryObject<MenuType<ChopperBlockMenu>> CHOPPER_BLOCK_MENU =
            registerMenuType(ChopperBlockMenu::new, "chopper_block_menu");

    public static final RegistryObject<MenuType<DistillerBlockMenu>> DISTILLER_BLOCK_MENU =
            registerMenuType(DistillerBlockMenu::new, "distiller_block_menu");

    public static final RegistryObject<MenuType<FermenterBlockMenu>> FERMENTER_BLOCK_MENU =
            registerMenuType(FermenterBlockMenu::new, "fermenter_block_menu");

    public static final RegistryObject<MenuType<FreezerBlockMenu>> FREEZER_BLOCK_MENU =
            registerMenuType(FreezerBlockMenu::new, "freezer_block_menu");

    public static final RegistryObject<MenuType<FryerBlockMenu>> FRYER_BLOCK_MENU =
            registerMenuType(FryerBlockMenu::new, "fryer_block_menu");

    public static final RegistryObject<MenuType<GrillerBlockMenu>> GRILLER_BLOCK_MENU =
            registerMenuType(GrillerBlockMenu::new, "griller_block_menu");

    public static final RegistryObject<MenuType<InfuserBlockMenu>> INFUSER_BLOCK_MENU =
            registerMenuType(InfuserBlockMenu::new, "infuser_block_menu");

    public static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name){
        return MENUS.register(name, ()-> IForgeMenuType.create(factory));
    }
    public static void register(IEventBus eventBus){
        MENUS.register(eventBus);
    }
}
