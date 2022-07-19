package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.cooking.DistillerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.DistillerBlockMenu;
import net.electrohoney.foodmastermod.screen.menus.PotBlockMenu;
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
import net.minecraft.world.level.material.Fluid;
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

public class DistillerBlockEntity extends BlockEntity implements MenuProvider {
    public final static int DISTILLER_ENTITY_CONTAINER_SIZE = 4;
    public final static int DISTILLER_MAX_FLUID_CAPACITY = 16000;
    public final static int DISTILLER_RESULT_FLUID_CAPACITY = 4000;

    public final static int MUSH_SLOT_ID = 0;
    private final ItemStackHandler itemHandler = new ItemStackHandler(DISTILLER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    final int INPUT = 1, OUTPUT = 0;
    private final FluidTank inputFluidTank = new FluidTank(DISTILLER_MAX_FLUID_CAPACITY){
        @Override
        protected void onContentsChanged() {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                ModMessages.sendToClients(new PacketSyncTwoFluidStacksToClient(this.fluid, INPUT, worldPosition));
            }
        }

    };
    private final FluidTank outputFluidTank = new FluidTank(DISTILLER_RESULT_FLUID_CAPACITY){
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
    private int steamProgress = 0;
    private int maxSteamProgress = 100;
    private int temperature = 25;
    private final int minTemperature = 25;
    private int maxTemperature = 125;

    public int getTemperature() {
        return temperature;
    }

    public static final int DISTILLER_DATA_SIZE = 4;

    public DistillerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.DISTILLER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return DistillerBlockEntity.this.steamProgress;
                    case 1: return DistillerBlockEntity.this.maxSteamProgress;
                    case 2: return DistillerBlockEntity.this.temperature;
                    case 3: return DistillerBlockEntity.this.maxTemperature;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: DistillerBlockEntity.this.steamProgress = value; break;
                    case 1: DistillerBlockEntity.this.maxSteamProgress = value; break;
                    case 2: DistillerBlockEntity.this.temperature = value; break;
                    case 3: DistillerBlockEntity.this.maxTemperature = value;break;
                }
            }

            public int getCount() {
                return DISTILLER_DATA_SIZE;
            }
        };
    }


    @Override
    public Component getDisplayName() {
        return new TextComponent("Distillery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new DistillerBlockMenu(pContainerId, pPlayerInventory, this, this.data);
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
        tag.putInt("distiller.progress", steamProgress);
        tag.putInt("distiller.maxSteamProgress", maxSteamProgress);
        tag.putInt("distiller.temperature", temperature);
        tag.putInt("distiller.maxTemperature", maxTemperature);
        CompoundTag inputFluidTag = new CompoundTag();
        CompoundTag outputFluidTag = new CompoundTag();
        inputFluidTank.writeToNBT(inputFluidTag);
        outputFluidTank.writeToNBT(outputFluidTag);
        tag.put("distiller.inputFluidTag", inputFluidTag);
        tag.put("distiller.outputFluidTag", outputFluidTag);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        steamProgress = nbt.getInt("distiller.progress");
        maxSteamProgress = nbt.getInt("distiller.maxSteamProgress");
        temperature = nbt.getInt("distiller.temperature");
        maxTemperature = nbt.getInt("distiller.maxTemperature");
        CompoundTag inputFluidTag = (CompoundTag) nbt.get("distiller.inputFluidTag");
        CompoundTag outputFluidTag = (CompoundTag) nbt.get("distiller.outputFluidTag");
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

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DistillerBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity)) {
            pBlockEntity.steamProgress++;
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.steamProgress > pBlockEntity.maxSteamProgress) {
                craftItem(pBlockEntity);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
        }
        //not good!!!
        if(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature < 90){
            pBlockEntity.temperature++;
        }
        if(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature < pBlockEntity.maxTemperature){
            pBlockEntity.temperature++;
        }
        else if(!(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock()) && !(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock()) && pBlockEntity.temperature > pBlockEntity.minTemperature){
            pBlockEntity.temperature--;
        }
    }

    private static boolean hasRecipe(DistillerBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<DistillerBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(DistillerBlockRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && hasRecipeFluidInTank(entity, match) && isInTemperatureRange(entity, match) && OutputTankIsNotFull(entity);
    }
    private  static boolean OutputTankIsNotFull(DistillerBlockEntity entity){
        return entity.getOutputFluidTank().getAmount() < DistillerBlockEntity.DISTILLER_RESULT_FLUID_CAPACITY;
    }
    private static boolean isInTemperatureRange(DistillerBlockEntity entity, Optional<DistillerBlockRecipe> match){
        if(match.isPresent()){
            int minTemperature = match.get().minTemperature;
            int maxTemperature = match.get().maxTemperature;

            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
    }

    private static boolean hasRecipeFluidInTank(DistillerBlockEntity entity, Optional<DistillerBlockRecipe> recipe) {
        return entity.getInputFluidTank().getAmount() >= recipe.get().getInputFluidStack().getAmount()
                && entity.getInputFluidTank().getFluid().equals(recipe.get().getInputFluidStack().getFluid());
    }

    private static void craftItem(DistillerBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<DistillerBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(DistillerBlockRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            for(int i = 0;i <= 2; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }
            entity.inputFluidTank.drain(match.get().getInputFluidStack().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            entity.outputFluidTank.fill(match.get().getOutputFluidStack(), IFluidHandler.FluidAction.EXECUTE);
            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.steamProgress = 0;
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

