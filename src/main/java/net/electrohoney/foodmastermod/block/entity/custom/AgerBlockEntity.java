package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.AgerBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.AgerBlockMenu;
import net.electrohoney.foodmastermod.util.networking.ModMessages;
import net.electrohoney.foodmastermod.util.networking.packets.AgerPacketSyncFluidStackToClient;
import net.electrohoney.foodmastermod.util.networking.packets.PotPacketSyncFluidStackToClient;
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

public class AgerBlockEntity extends BlockEntity implements MenuProvider {
    public final static int AGER_MAX_FLUID_CAPACITY = 8000;
    public final static int AGER_ENTITY_CONTAINER_SIZE = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(AGER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    final int INPUT = 1, OUTPUT = 0;
    private final FluidTank inputFluidTank = new FluidTank(AGER_MAX_FLUID_CAPACITY){
        @Override
        protected void onContentsChanged() {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                ModMessages.sendToClients(new AgerPacketSyncFluidStackToClient(this.fluid, INPUT, worldPosition));
            }
        }

    };
    private final FluidTank outputFluidTank = new FluidTank(AGER_MAX_FLUID_CAPACITY){
        @Override
        protected void onContentsChanged() {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                ModMessages.sendToClients(new AgerPacketSyncFluidStackToClient(this.fluid, OUTPUT, worldPosition));
            }
        }

    };

    private LazyOptional<IFluidHandler> lazyInputFluidHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyOutputFluidHandler = LazyOptional.empty();

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;

    public static final int TIME_PIECE_SLOT = 0;
    private int ageing = 0;
    private int maxAgeing = 500;

    private int ageingRate = 1;

    public static final int AGER_DATA_SIZE = 3;
    public AgerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.AGER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return AgerBlockEntity.this.ageing;
                    case 1: return AgerBlockEntity.this.maxAgeing;
                    case 2: return AgerBlockEntity.this.ageingRate;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: AgerBlockEntity.this.ageing = value; break;
                    case 1: AgerBlockEntity.this.maxAgeing = value; break;
                    case 2: AgerBlockEntity.this.ageingRate = value; break;
                }
            }

            public int getCount() {
                return AGER_DATA_SIZE;
            }
        };
    }

    public FluidStack getInputFluidStack() {
        return this.inputFluidTank.getFluid();
    }
    public FluidStack getOutputFluidStack() {
        return this.outputFluidTank.getFluid();
    }
    public void setInputFluid(FluidStack fluidStack) {
        this.inputFluidTank.setFluid(fluidStack);
    }
    public void setOutputFluid(FluidStack fluidStack) {
        this.outputFluidTank.setFluid(fluidStack);
    }
    @Override
    public Component getDisplayName() {
        return new TextComponent("Aging Barrels");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new AgerBlockMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }

        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            return lazyInputFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyInputFluidHandler = LazyOptional.of(()->inputFluidTank);
//        lazyOutputFluidHandler = LazyOptional.of(()->outputFluidTank);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyInputFluidHandler.invalidate();
        //lazyOutputFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("ager_block.ageing", ageing);
        tag.putInt("ager_block.maxAgeing", maxAgeing);
        CompoundTag inputFluidTag = new CompoundTag();
        CompoundTag outputFluidTag = new CompoundTag();
        inputFluidTank.writeToNBT(inputFluidTag);
        outputFluidTank.writeToNBT(outputFluidTag);
        tag.put("ager_block.inputFluidTag", inputFluidTag);
        tag.put("ager_block.outputFluidTag", outputFluidTag);

        super.saveAdditional(tag);
    }



    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        ageing = nbt.getInt("ager_block.ageing");
        maxAgeing = nbt.getInt("ager_block.maxAgeing");
        CompoundTag inputFluidTag = (CompoundTag) nbt.get("ager_block.inputFluidTag");
        CompoundTag outputFluidTag = (CompoundTag) nbt.get("ager_block.outputFluidTag");
        inputFluidTank.readFromNBT(inputFluidTag);
        outputFluidTank.readFromNBT(outputFluidTag);

    }
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        //might have more than 1 slot
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AgerBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity)) {
            pBlockEntity.ageing+=50;
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.ageing > pBlockEntity.maxAgeing) {
                craftItem(pBlockEntity);
            }
        } else {
            pBlockEntity.resetAgeing();
            setChanged(pLevel, pPos, pState);
        }

    }

    private static boolean hasRecipe(AgerBlockEntity entity) {
        Level level = entity.level;
        //this wont work 100%, like how will I detect what liquid is inside??? will it match??
        //simple container
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }
        System.out.println("ITEMS???");
        System.out.println(inventory.getItem(0));
        System.out.println(entity.itemHandler.getStackInSlot(0));
        assert level != null;
        Optional<AgerBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(AgerBlockRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && hasRecipeFluidInTank(entity, match) && OutputTankIsNotFull(entity);
    }
    private  static boolean OutputTankIsNotFull(AgerBlockEntity entity){
        return entity.getOutputFluidStack().getAmount() < AgerBlockEntity.AGER_MAX_FLUID_CAPACITY;

//        entity.getFluidStack().getAmount() >= recipe.get().fluidStack.getAmount()
//                && entity.getFluidStack().getFluid().equals(recipe.get().fluidStack.getFluid());
    }
    private static boolean hasRecipeFluidInTank(AgerBlockEntity entity, Optional<AgerBlockRecipe> recipe) {
        System.out.println("hasRecipeInTank");
        System.out.println(entity.getInputFluidStack().getFluid().equals(recipe.get().input.getFluid()));
        System.out.println(entity.getInputFluidStack().getFluid());
        System.out.println(recipe.get().input.getFluid());
        return entity.getInputFluidStack().getFluid().equals(recipe.get().input.getFluid());
    }


    private static void craftItem(AgerBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<AgerBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(AgerBlockRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            entity.inputFluidTank.drain(match.get().getInput().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            entity.outputFluidTank.fill(match.get().getOutput(), IFluidHandler.FluidAction.EXECUTE);
            entity.resetAgeing();
        }
    }

    private void resetAgeing() {
        this.ageing = 0;
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

