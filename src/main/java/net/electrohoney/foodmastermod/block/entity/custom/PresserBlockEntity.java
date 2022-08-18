package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.cooking.PresserBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.PresserBlockMenu;
import net.electrohoney.foodmastermod.util.networking.ModMessages;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncOneFluidStackToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class PresserBlockEntity extends BlockEntity implements MenuProvider {
    public final static int PRESSER_ENTITY_CONTAINER_SIZE = 3;
    public final static int PRESSER_RESULT_FLUID_CAPACITY = 12000;

    private final ItemStackHandler itemHandler = new ItemStackHandler(PRESSER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    private final FluidTank outputFluidTank = new FluidTank(PRESSER_RESULT_FLUID_CAPACITY){
        @Override
        protected void onContentsChanged() {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                ModMessages.sendToClients(new PacketSyncOneFluidStackToClient(this.fluid, worldPosition));
            }
        }

    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int pressingProgress = 0;
    private int maxPressingProgress = 101;

    private int outputPercentage1;
    private int outputPercentage2;

    public static final int PRESSER_DATA_SIZE = 5;
    public static final int RESULT_SLOT_ID1 = 1;
    public static final int RESULT_SLOT_ID2 = 2;
    public static final int INPUT_SLOT_ID = 0;

    public PresserBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.PRESSER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return PresserBlockEntity.this.pressingProgress;
                    case 1: return PresserBlockEntity.this.maxPressingProgress;
                    case 2: return PresserBlockEntity.this.getOutputPercentage1();
                    case 3: return PresserBlockEntity.this.getOutputPercentage2();
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: PresserBlockEntity.this.pressingProgress = value; break;
                    case 1: PresserBlockEntity.this.maxPressingProgress = value; break;
                    case 2: PresserBlockEntity.this.setOutputPercentage1(value); break;
                    case 3: PresserBlockEntity.this.setOutputPercentage2(value); break;
                }
            }

            public int getCount() {
                return PRESSER_DATA_SIZE;
            }
        };
    }


    @Override
    public Component getDisplayName() {
        return new TextComponent("Piston Press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PresserBlockMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }

        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            return lazyFluidHandler.cast();
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
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("presser.progress", pressingProgress);
        tag.putInt("presser.maxSteamProgress", maxPressingProgress);
        tag.putInt("presser.outputPercentage1", getOutputPercentage1());
        tag.putInt("presser.outputPercentage2", getOutputPercentage2());
        CompoundTag outputFluidTag = new CompoundTag();
        outputFluidTank.writeToNBT(outputFluidTag);
        tag.put("presser.outputFluidTag", outputFluidTag);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        pressingProgress = nbt.getInt("presser.progress");
        maxPressingProgress = nbt.getInt("presser.maxSteamProgress");
        setOutputPercentage1(nbt.getInt("presser.outputPercentage1"));
        setOutputPercentage2(nbt.getInt("presser.outputPercentage2"));
        CompoundTag outputFluidTag = (CompoundTag) nbt.get("presser.outputFluidTag");
        outputFluidTank.readFromNBT(outputFluidTag);

    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, PresserBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity)) {
            pBlockEntity.pressingProgress++;
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.pressingProgress > pBlockEntity.maxPressingProgress) {
                craftItem(pBlockEntity);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
        }
    }

    private static boolean hasRecipe(PresserBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<PresserBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(PresserBlockRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && OutputTankIsNotFull(entity) &&
                canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem(), match.get().getSecondaryResultItem());
    }
    private  static boolean OutputTankIsNotFull(PresserBlockEntity entity){
        return entity.getOutputFluidTank().getAmount() < PresserBlockEntity.PRESSER_RESULT_FLUID_CAPACITY;
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory, ItemStack output1, ItemStack output2) {
        return inventory.getItem(RESULT_SLOT_ID1).getItem() == output1.getItem() || inventory.getItem(RESULT_SLOT_ID1).isEmpty()
        && inventory.getItem(RESULT_SLOT_ID2).getItem() == output2.getItem() || inventory.getItem(RESULT_SLOT_ID2).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(RESULT_SLOT_ID1).getMaxStackSize() > inventory.getItem(RESULT_SLOT_ID1).getCount()
        && inventory.getItem(RESULT_SLOT_ID2).getMaxStackSize() > inventory.getItem(RESULT_SLOT_ID2).getCount();
    }

    private static void craftItem(PresserBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<PresserBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(PresserBlockRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            entity.setOutputPercentage1(match.get().getPercentage1());
            entity.setOutputPercentage2(match.get().getPercentage2());

            if(entity.itemHandler.getStackInSlot(0)!=ItemStack.EMPTY){
                entity.itemHandler.extractItem(0, 1, false);
            }
            int probability = (int) ((Math.random()*100)+1);
            System.out.println("Probability: " + probability + "Vs" + entity.getOutputPercentage1() + "and" + entity.getOutputPercentage2());
            if(probability <= entity.getOutputPercentage1()){
                entity.itemHandler.setStackInSlot(RESULT_SLOT_ID1, new ItemStack(match.get().getResultItem().getItem(),
                        entity.itemHandler.getStackInSlot(RESULT_SLOT_ID1).getCount() + match.get().getResultItem().getCount()));
            }

            if(probability <= entity.getOutputPercentage2()){
                entity.itemHandler.setStackInSlot(RESULT_SLOT_ID2, new ItemStack(match.get().getSecondaryResultItem().getItem(),
                        entity.itemHandler.getStackInSlot(RESULT_SLOT_ID2).getCount() + match.get().getSecondaryResultItem().getCount()));
            }
            entity.outputFluidTank.fill(match.get().getOutputFluid(), IFluidHandler.FluidAction.EXECUTE);
            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.pressingProgress = 0;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = saveWithoutMetadata();
        load(compoundTag);
        return compoundTag;
    }

    public FluidStack getOutputFluidTank() {
        return outputFluidTank.getFluid();
    }
    public void setOutputFluid(FluidStack output) {
        this.outputFluidTank.setFluid(output);
    }

    public int getOutputPercentage1() {
        return outputPercentage1;
    }

    public void setOutputPercentage1(int outputPercentage1) {
        this.outputPercentage1 = outputPercentage1;
    }

    public int getOutputPercentage2() {
        return outputPercentage2;
    }

    public void setOutputPercentage2(int outputPercentage2) {
        this.outputPercentage2 = outputPercentage2;
    }
}

