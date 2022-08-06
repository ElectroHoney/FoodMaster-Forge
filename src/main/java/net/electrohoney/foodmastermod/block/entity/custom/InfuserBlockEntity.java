package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.cooking.InfuserBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.InfuserBlockMenu;
import net.electrohoney.foodmastermod.util.networking.ModMessages;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncOneFluidStackToClient;
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

public class InfuserBlockEntity extends BlockEntity implements MenuProvider {
    public final static int INFUSER_ENTITY_CONTAINER_SIZE = 2;
    public final static int INFUSER_MAX_FLUID_CAPACITY = 2000;
    private final ItemStackHandler itemHandler = new ItemStackHandler(INFUSER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };


    final int INPUT = 1, OUTPUT = 0;
    private final FluidTank inputFluidTank = new FluidTank(INFUSER_MAX_FLUID_CAPACITY){
        @Override
        protected void onContentsChanged() {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                ModMessages.sendToClients(new PacketSyncTwoFluidStacksToClient(this.fluid, INPUT, worldPosition));
            }
        }

    };
    private final FluidTank outputFluidTank = new FluidTank(INFUSER_MAX_FLUID_CAPACITY){
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

    public static final int INGREDIENT_SLOT_ID = 0;
    public static final int RESULT_SLOT_ID = 1;

    private int progress = 0;
    private int maxProgress = 100;

    private int temperature = 25;
    private final int minTemperature = 25;
    private int maxTemperature = 100;

    public static final int INFUSER_DATA_SIZE = 4;
    private int fluidFillAmount = 0;
    public InfuserBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.INFUSER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return InfuserBlockEntity.this.progress;
                    case 1: return InfuserBlockEntity.this.maxProgress;
                    case 2: return InfuserBlockEntity.this.temperature;
                    case 3: return InfuserBlockEntity.this.maxTemperature;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: InfuserBlockEntity.this.progress = value; break;
                    case 1: InfuserBlockEntity.this.maxProgress = value; break;
                    case 2: InfuserBlockEntity.this.temperature = value; break;
                    case 3: InfuserBlockEntity.this.maxTemperature = value;break;

                }
            }

            public int getCount() {
                return INFUSER_DATA_SIZE;
            }
        };
    }

    public FluidStack getInputFluid(){
        return this.inputFluidTank.getFluid();
    }
    public void setInputFluid(FluidStack fluidStack){
        this.inputFluidTank.setFluid(fluidStack);
    }
    public FluidStack getOutputFluid(){
        return this.outputFluidTank.getFluid();
    }
    public void setOutputFluid(FluidStack fluidStack){
        this.outputFluidTank.setFluid(fluidStack);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Infuser");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new InfuserBlockMenu(pContainerId, pPlayerInventory, this, this.data);
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
        lazyFluidHandler = LazyOptional.of(()-> inputFluidTank);
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
        tag.putInt("infuser.progress", progress);
        tag.putInt("infuser.maxProgress", maxProgress);
        tag.putInt("infuser.temperature", temperature);
        CompoundTag inputFluidTag = new CompoundTag();
        CompoundTag outputFluidTag = new CompoundTag();
        inputFluidTank.writeToNBT(inputFluidTag);
        outputFluidTank.writeToNBT(outputFluidTag);
        tag.put("infuser.inputFluidTag", inputFluidTag);
        tag.put("infuser.outputFluidTag", outputFluidTag);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("infuser.progress");
        maxProgress = nbt.getInt("infuser.maxProgress");
        temperature = nbt.getInt("infuser.temperature");
        inputFluidTank.readFromNBT(nbt);

    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, InfuserBlockEntity pBlockEntity) {
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

        if(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature < 0.6*pBlockEntity.maxTemperature){
            pBlockEntity.temperature++;
        }
        if(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature < pBlockEntity.maxTemperature){
            pBlockEntity.temperature++;
        }
        else if(!(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock()) && !(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock()) && pBlockEntity.temperature > pBlockEntity.minTemperature){
            pBlockEntity.temperature--;
        }
    }

    private static boolean hasRecipe(InfuserBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<InfuserBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(InfuserBlockRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem())
                && hasRecipeFluidInTank(entity, match) && isInTemperatureRange(entity, match) && OutputTankIsNotFull(entity);
    }

    private  static boolean OutputTankIsNotFull(InfuserBlockEntity entity){
        return entity.getOutputFluid().getAmount() < InfuserBlockEntity.INFUSER_MAX_FLUID_CAPACITY;
    }
    private static boolean isInTemperatureRange(InfuserBlockEntity entity, Optional<InfuserBlockRecipe> match){
        if(match.isPresent()){
            int minTemperature = match.get().minTemperature;
            int maxTemperature = match.get().maxTemperature;
            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
    }

    private static boolean hasRecipeFluidInTank(InfuserBlockEntity entity, Optional<InfuserBlockRecipe> recipe) {
        return entity.getInputFluid().getAmount() >= recipe.get().inputFluid.getAmount()
                && entity.getInputFluid().getFluid().equals(recipe.get().inputFluid.getFluid());
    }


    private static void craftItem(InfuserBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<InfuserBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(InfuserBlockRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            if(entity.itemHandler.getStackInSlot(0)!=ItemStack.EMPTY){
                entity.itemHandler.extractItem(0, 1, false);
            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(match.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + match.get().getResultItem().getCount()));
            //todo remember to remove this if it becomes annoying
            entity.inputFluidTank.drain(match.get().getInputFluidStack().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            entity.outputFluidTank.fill(match.get().getOutputFluidStack(), IFluidHandler.FluidAction.EXECUTE);
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
}

