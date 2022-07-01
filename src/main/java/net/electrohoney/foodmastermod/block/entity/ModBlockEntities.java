package net.electrohoney.foodmastermod.block.entity;

import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, FoodMaster.MOD_ID);

    public static final RegistryObject<BlockEntityType<PotBlockEntity>> POT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pot_block_enitity",
                    ()-> BlockEntityType.Builder.of(PotBlockEntity::new,
                            ModBlocks.POT_BLOCK.get()).build(null));
    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
