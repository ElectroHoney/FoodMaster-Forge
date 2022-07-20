package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.cooking.DistillerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.FermenterBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.DistillerBlockMenu;
import net.electrohoney.foodmastermod.screen.menus.FermenterBlockMenu;
import net.electrohoney.foodmastermod.util.networking.ModMessages;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncTwoFluidStacksToClient;
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
import net.minecraft.world.level.block.Blocks;
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

public class FermenterBlockEntity extends BlockEntity implements MenuProvider {
    public final static int FERMENTER_ENTITY_CONTAINER_SIZE = 10;
    public final static int FERMENTER_MAX_FLUID_CAPACITY = 8000;
    public final static int FERMENTER_RESULT_FLUID_CAPACITY = 8000;

    private final ItemStackHandler itemHandler = new ItemStackHandler(FERMENTER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    final int INPUT = 1, OUTPUT = 0;
    private final FluidTank inputFluidTank = new FluidTank(FERMENTER_MAX_FLUID_CAPACITY){
        @Override
        protected void onContentsChanged() {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                ModMessages.sendToClients(new PacketSyncTwoFluidStacksToClient(this.fluid, INPUT, worldPosition));
            }
        }

    };
    private final FluidTank outputFluidTank = new FluidTank(FERMENTER_RESULT_FLUID_CAPACITY){
        @Override
        protected void onContentsChanged() {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                ModMessages.sendToClients(new PacketSyncTwoFluidStacksToClient(this.fluid, OUTPUT, worldPosition));
            }
        }

    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int fermentingProgress = 0;
    private int maxFermentingProgress = 100;

    public static final int FERMENTER_DATA_SIZE = 2;
    public static final int RESULT_SLOT_ID = 9;

    public FermenterBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.FERMENTER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return FermenterBlockEntity.this.fermentingProgress;
                    case 1: return FermenterBlockEntity.this.maxFermentingProgress;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: FermenterBlockEntity.this.fermentingProgress = value; break;
                    case 1: FermenterBlockEntity.this.maxFermentingProgress = value; break;
                }
            }

            public int getCount() {
                return FERMENTER_DATA_SIZE;
            }
        };
    }


    @Override
    public Component getDisplayName() {
        return new TextComponent("Clay Pot");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new FermenterBlockMenu(pContainerId, pPlayerInventory, this, this.data);
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
        lazyFluidHandler = LazyOptional.of(()->inputFluidTank);
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
        tag.putInt("fermenter.progress", fermentingProgress);
        tag.putInt("fermenter.maxSteamProgress", maxFermentingProgress);
        CompoundTag inputFluidTag = new CompoundTag();
        CompoundTag outputFluidTag = new CompoundTag();
        inputFluidTank.writeToNBT(inputFluidTag);
        outputFluidTank.writeToNBT(outputFluidTag);
        tag.put("fermenter.inputFluidTag", inputFluidTag);
        tag.put("fermenter.outputFluidTag", outputFluidTag);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        fermentingProgress = nbt.getInt("fermenter.progress");
        maxFermentingProgress = nbt.getInt("fermenter.maxSteamProgress");
        CompoundTag inputFluidTag = (CompoundTag) nbt.get("fermenter.inputFluidTag");
        CompoundTag outputFluidTag = (CompoundTag) nbt.get("fermenter.outputFluidTag");
        inputFluidTank.readFromNBT(inputFluidTag);
        outputFluidTank.readFromNBT(outputFluidTag);

    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FermenterBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity)) {
            pBlockEntity.fermentingProgress++;
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.fermentingProgress > pBlockEntity.maxFermentingProgress) {
                craftItem(pBlockEntity);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
        }
    }

    private static boolean hasRecipe(FermenterBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<FermenterBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(FermenterBlockRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && hasRecipeFluidInTank(entity, match) && OutputTankIsNotFull(entity) &&
                canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem());
    }
    private  static boolean OutputTankIsNotFull(FermenterBlockEntity entity){
        return entity.getOutputFluidTank().getAmount() < FermenterBlockEntity.FERMENTER_RESULT_FLUID_CAPACITY;
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory, ItemStack output) {
        return inventory.getItem(RESULT_SLOT_ID).getItem() == output.getItem() || inventory.getItem(RESULT_SLOT_ID).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(RESULT_SLOT_ID).getMaxStackSize() > inventory.getItem(RESULT_SLOT_ID).getCount();
    }

    private static boolean hasRecipeFluidInTank(FermenterBlockEntity entity, Optional<FermenterBlockRecipe> recipe) {
        return entity.getInputFluidTank().getAmount() >= recipe.get().getInputFluid().getAmount()
                && entity.getInputFluidTank().getFluid().equals(recipe.get().getInputFluid().getFluid());
    }

    private static void craftItem(FermenterBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<FermenterBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(FermenterBlockRecipe.Type.INSTANCE, inventory, level);



        if(match.isPresent()) {
            for(int i = 0;i <= 8; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(match.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + match.get().getResultItem().getCount()));
            entity.inputFluidTank.drain(match.get().getInputFluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            entity.outputFluidTank.fill(match.get().getOutputFluid(), IFluidHandler.FluidAction.EXECUTE);
            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.fermentingProgress = 0;
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

    public FluidStack getInputFluidTank() {
        return inputFluidTank.getFluid();
    }
    public void setInputFluid(FluidStack input) {
        this.inputFluidTank.setFluid(input);
    }

    public FluidStack getOutputFluidTank() {
        return outputFluidTank.getFluid();
    }
    public void setOutputFluid(FluidStack output) {
        this.outputFluidTank.setFluid(output);
    }
}

