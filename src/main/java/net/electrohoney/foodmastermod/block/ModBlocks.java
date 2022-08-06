package net.electrohoney.foodmastermod.block;

import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.custom.*;
import net.electrohoney.foodmastermod.item.ModCreativeModTab;
import net.electrohoney.foodmastermod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FoodMaster.MOD_ID);

    public static List<RegistryObject<Item>> itemsList = new ArrayList<>();
    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }


    public static final RegistryObject<Block> POT_BLOCK = registerBlock("pot_block",
            () -> new PotBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> AGER_BLOCK = registerBlock("ager",
            () -> new AgerBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> BAKER_BLOCK = registerBlock("baker",
            () -> new BakerBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> BUTTER_CHURN = registerBlock("butter_churn",
            () -> new ButterChurn(BlockBehaviour.Properties.of(Material.METAL).strength(1f).noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> CHOPPER_BLOCK = registerBlock("chopper",
            () -> new ChopperBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(0.2f).noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> DISTILLER_BLOCK = registerBlock("distiller",
            () -> new DistillerBlock(BlockBehaviour.Properties.of(Material.METAL).strength(0.8f).noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> FERMENTER_BLOCK = registerBlock("fermenter",
            () -> new FermenterBlock(BlockBehaviour.Properties.of(Material.CLAY).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> FREEZER_BLOCK = registerBlock("freezer",
            () -> new FreezerBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> FRYER_BLOCK = registerBlock("fryer",
            () -> new FryerBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);
    public static final RegistryObject<Block> GRILLER_BLOCK = registerBlock("griller",
            () -> new GrillerBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> INFUSER_BLOCK = registerBlock("infuser",
            () -> new InfuserBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> MIXER = registerBlock("mixer",
            () -> new MixerBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);

    public static final RegistryObject<Block> PRESSER_BLOCK = registerBlock("presser",
            () -> new Presser(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);




    public static final RegistryObject<Block> SMOKER_BLOCK = registerBlock("smoker",
            () -> new SmokerBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion()),
            ModCreativeModTab.FOODMASTER_TAB);



    public static <T extends Block>RegistryObject<Item>registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab){
        //I have a list of all the block items that is used for optional JEI support
        itemsList.add(ModItems.ITEMS.register(name, ()-> new BlockItem(block.get(),
                new Item.Properties().tab(tab))));
        return itemsList.get(itemsList.size()-1);
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
