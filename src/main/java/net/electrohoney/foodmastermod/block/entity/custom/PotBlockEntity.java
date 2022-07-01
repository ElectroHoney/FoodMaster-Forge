package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.item.ModItems;
import net.electrohoney.foodmastermod.recipe.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.PotBlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;

public class PotBlockEntity extends BlockEntity implements MenuProvider {
    public final static int POT_ENTITY_CONTAINER_SIZE = 12;
    private final ItemStackHandler itemHandler = new ItemStackHandler(POT_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;

    private static final int SPICE_SLOT_ID = 1;
    public static final int RECIPE_SLOT_ID = 2;
    private static final int WATER_SLOT_ID = 0;
    private static final int RESULT_SLOT_ID = 11;

    private int progress = 0;
    private int maxProgress = 72;
    //my own variables
    private int temperature = 0;
    private int maxTemperature = 200;

    public static final int POT_DATA_SIZE = 4;
    private Fluid fluid = null;
    public PotBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.POT_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return PotBlockEntity.this.progress;
                    case 1: return PotBlockEntity.this.maxProgress;
                    case 2: return PotBlockEntity.this.temperature;
                    case 3: return PotBlockEntity.this.maxTemperature;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: PotBlockEntity.this.progress = value; break;
                    case 1: PotBlockEntity.this.maxProgress = value; break;
                    case 2: PotBlockEntity.this.temperature = value; break;
                    case 3: PotBlockEntity.this.maxTemperature = value;

                }
            }

            public int getCount() {
                return POT_DATA_SIZE;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Pot Block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PotBlockMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("pot_block.progress", progress);
        tag.putInt("pot_block.temperature", temperature);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("pot_block.progress");
        temperature = nbt.getInt("pot_block.temperature");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, PotBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity)) {
            pBlockEntity.progress++;
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.progress > pBlockEntity.maxProgress) {
                craftItem(pBlockEntity);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
        }

        if(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature <= pBlockEntity.maxTemperature){
            pBlockEntity.temperature++;
        }
        else if(!(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock()) && pBlockEntity.temperature >= 0){
            pBlockEntity.temperature--;
        }
    }

    private static boolean hasRecipe(PotBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<PotBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(PotBlockRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem())
                && hasWaterInWaterSlot(entity) && hasToolsInToolSlot(entity);
    }

    private static boolean hasWaterInWaterSlot(PotBlockEntity entity) {
        return PotionUtils.getPotion(entity.itemHandler.getStackInSlot(WATER_SLOT_ID)) == Potions.WATER;
    }

    private static boolean hasToolsInToolSlot(PotBlockEntity entity) {
        return entity.itemHandler.getStackInSlot(SPICE_SLOT_ID).getItem() == ModItems.TURMERIC.get();
    }

    private static void craftItem(PotBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<PotBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(PotBlockRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            entity.itemHandler.extractItem(WATER_SLOT_ID,1, false);
            entity.itemHandler.extractItem(RECIPE_SLOT_ID,1, false);
            //entity.itemHandler.getStackInSlot(2).hurt(1, new Random(), null);
            if(entity.itemHandler.getStackInSlot(SPICE_SLOT_ID).getDamageValue()>=entity.itemHandler.getStackInSlot(SPICE_SLOT_ID).getMaxDamage()){
                    entity.itemHandler.extractItem(SPICE_SLOT_ID, 1, false);
            }
            else {
                    entity.itemHandler.getStackInSlot(SPICE_SLOT_ID).hurt(1, new Random(), null);
            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(match.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + 1));

            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory, ItemStack output) {
        return inventory.getItem(RESULT_SLOT_ID).getItem() == output.getItem() || inventory.getItem(RESULT_SLOT_ID).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(RESULT_SLOT_ID).getMaxStackSize() > inventory.getItem(RESULT_SLOT_ID).getCount();
    }
//    hardcoded
//public static void tick(Level pLevel, BlockPos pPos, BlockState pState, PotBlockEntity pBlockEntity) {
//    if(hasRecipe(pBlockEntity) && hasNotReachedStackLimit(pBlockEntity)) {
//        craftItem(pBlockEntity);
//    }
//}
//
//    private static void craftItem(PotBlockEntity entity) {
//
//        entity.itemHandler.extractItem(0, 1, false);
//        entity.itemHandler.extractItem(1, 1, false);
//
//        if(entity.itemHandler.getStackInSlot(2).getDamageValue()>=entity.itemHandler.getStackInSlot(2).getMaxDamage()){
//            entity.itemHandler.extractItem(2, 1, false);
//        }
//        else {
//            entity.itemHandler.getStackInSlot(2).hurt(1, new Random(), null);
//        }
//
//        entity.itemHandler.setStackInSlot(11, new ItemStack(ModItems.TBONE_STEAK.get(),
//                entity.itemHandler.getStackInSlot(11).getCount() + 1));
//    }
//
//    private static boolean hasRecipe(PotBlockEntity entity) {
//        boolean hasItemInWaterSlot = PotionUtils.getPotion(entity.itemHandler.getStackInSlot(0)) == Potions.WATER;
//        boolean hasItemInFirstSlot = entity.itemHandler.getStackInSlot(1).getItem() == Items.STICK.asItem();
//        boolean hasItemInSecondSlot = entity.itemHandler.getStackInSlot(2).getItem() == ModItems.TURMERIC.get();
//
//        return hasItemInWaterSlot && hasItemInFirstSlot && hasItemInSecondSlot;
//    }
//
//    private static boolean hasNotReachedStackLimit(PotBlockEntity entity) {
//        return entity.itemHandler.getStackInSlot(3).getCount() < entity.itemHandler.getStackInSlot(3).getMaxStackSize();
//    }
}

