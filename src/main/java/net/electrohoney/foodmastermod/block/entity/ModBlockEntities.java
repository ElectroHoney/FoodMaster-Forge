package net.electrohoney.foodmastermod.block.entity;

import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.block.entity.custom.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, FoodMaster.MOD_ID);

    public static final RegistryObject<BlockEntityType<PotBlockEntity>> POT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pot_block_entity",
                    ()-> BlockEntityType.Builder.of(PotBlockEntity::new,
                            ModBlocks.POT_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<AgerBlockEntity>> AGER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ager_block_entity",
                    ()-> BlockEntityType.Builder.of(AgerBlockEntity::new,
                            ModBlocks.AGER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<BakerBlockEntity>> BAKER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("baker_block_entity",
                    ()-> BlockEntityType.Builder.of(BakerBlockEntity::new,
                            ModBlocks.BAKER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ButterChurnBlockEntity>> BUTTER_CHURN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("butter_churn_block_entity",
                    ()-> BlockEntityType.Builder.of(ButterChurnBlockEntity::new,
                            ModBlocks.BUTTER_CHURN.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChopperBlockEntity>> CHOPPER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("chopper_block_entity",
                    ()-> BlockEntityType.Builder.of(ChopperBlockEntity::new,
                            ModBlocks.CHOPPER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<DistillerBlockEntity>> DISTILLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("distiller_block_entity",
                    ()-> BlockEntityType.Builder.of(DistillerBlockEntity::new,
                            ModBlocks.DISTILLER_BLOCK.get()).build(null));


    public static final RegistryObject<BlockEntityType<FermenterBlockEntity>> FERMENTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("fermenter_block_entity",
                    ()-> BlockEntityType.Builder.of(FermenterBlockEntity::new,
                            ModBlocks.FERMENTER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<FreezerBlockEntity>> FREEZER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("freezer_block_entity",
                    ()-> BlockEntityType.Builder.of(FreezerBlockEntity::new,
                            ModBlocks.FREEZER_BLOCK.get()).build(null));


    public static final RegistryObject<BlockEntityType<GrillerBlockEntity>> GRILLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("griller_block_entity",
                    ()-> BlockEntityType.Builder.of(GrillerBlockEntity::new,
                            ModBlocks.GRILLER_BLOCK.get()).build(null));


    public static final RegistryObject<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infuser_block_entity",
                    ()-> BlockEntityType.Builder.of(InfuserBlockEntity::new,
                            ModBlocks.INFUSER_BLOCK.get()).build(null));


    public static final RegistryObject<BlockEntityType<FryerBlockEntity>> FRYER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("fryer_block_entity",
                    ()-> BlockEntityType.Builder.of(FryerBlockEntity::new,
                            ModBlocks.FRYER_BLOCK.get()).build(null));
    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
