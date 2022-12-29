package net.electrohoney.foodmastermod;

import com.mojang.logging.LogUtils;
import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.item.ModItems;
import net.electrohoney.foodmastermod.recipe.ModRecipes;
import net.electrohoney.foodmastermod.screen.ModMenuTypes;
import net.electrohoney.foodmastermod.screen.menus.ChopperBlockMenu;
import net.electrohoney.foodmastermod.screen.screens.*;
import net.electrohoney.foodmastermod.util.networking.ModMessages;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
//Special Thanks to Kaupeunjoe,
@Mod(FoodMaster.MOD_ID)
public class FoodMaster
{
    public static final String MOD_ID = "foodmastermod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public FoodMaster()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(eventBus);
        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModRecipes.register(eventBus);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event){
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.POT_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.AGER_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BUTTER_CHURN.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHOPPER_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PRESSER_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.DISTILLER_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SMOKER_BLOCK.get(), RenderType.translucent());

        MenuScreens.register(ModMenuTypes.POT_BLOCK_MENU.get(), PotBlockScreen::new);
        MenuScreens.register(ModMenuTypes.AGER_BLOCK_MENU.get(), AgerBlockScreen::new);
        MenuScreens.register(ModMenuTypes.BAKER_BLOCK_MENU.get(), BakerBlockScreen::new);
        MenuScreens.register(ModMenuTypes.BUTTER_CHURNER_MENU.get(), ButterChurnBlockScreen::new);
        MenuScreens.register(ModMenuTypes.CHOPPER_BLOCK_MENU.get(), ChopperBlockScreen::new);
        MenuScreens.register(ModMenuTypes.DISTILLER_BLOCK_MENU.get(), DistillerBlockScreen::new);
        MenuScreens.register(ModMenuTypes.FERMENTER_BLOCK_MENU.get(), FermenterBlockScreen::new);
        MenuScreens.register(ModMenuTypes.FREEZER_BLOCK_MENU.get(), FreezerBlockScreen::new);
        MenuScreens.register(ModMenuTypes.FRYER_BLOCK_MENU.get(), FryerBlockScreen::new);
        MenuScreens.register(ModMenuTypes.GRILLER_BLOCK_MENU.get(), GrillerBlockScreen::new);
        MenuScreens.register(ModMenuTypes.INFUSER_BLOCK_MENU.get(), InfuserBlockScreen::new);
        MenuScreens.register(ModMenuTypes.PRESSER_BLOCK_MENU.get(), PresserBlockScreen::new);
        MenuScreens.register(ModMenuTypes.SMOKER_BLOCK_MENU.get(), SmokerBlockScreen::new);
    }

    private void setup(final FMLCommonSetupEvent event)
    {

        ModMessages.register();
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }


}
