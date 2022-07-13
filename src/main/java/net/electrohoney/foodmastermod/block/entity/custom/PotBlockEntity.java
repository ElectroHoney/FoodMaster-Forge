package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.PotBlockMenu;
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

public class PotBlockEntity extends BlockEntity implements MenuProvider {
    public final static int POT_ENTITY_CONTAINER_SIZE = 12;
    public final static int POT_MAX_FLUID_CAPACITY = 4000;
    private final ItemStackHandler itemHandler = new ItemStackHandler(POT_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    private final FluidTank fluidTank = new FluidTank(POT_MAX_FLUID_CAPACITY){
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

    public static final int UTENSIL_SLOT_ID = 10;
    //public static final int RECIPE_SLOT_ID = 2;
    private static final int WATER_SLOT_ID = 0;
    private static final int RESULT_SLOT_ID = 11;

    private int progress = 0;
    private int maxProgress = 72;

    public int getTemperature() {
        return temperature;
    }

    //my own variables
    private int temperature = 25;
    private int minTemperature = 25;
    private int maxTemperature = 200;

    public static final int POT_DATA_SIZE = 5;
    private int fluidFillAmount = 0;
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
                    case 4: return PotBlockEntity.this.fluidFillAmount;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: PotBlockEntity.this.progress = value; break;
                    case 1: PotBlockEntity.this.maxProgress = value; break;
                    case 2: PotBlockEntity.this.temperature = value; break;
                    case 3: PotBlockEntity.this.maxTemperature = value;break;
                    case 4: PotBlockEntity.this.fluidFillAmount = value;

                }
            }

            public int getCount() {
                return POT_DATA_SIZE;
            }
        };
    }

    public FluidStack getFluidStack() {
        return this.fluidTank.getFluid();
    }

    public void setFluid(FluidStack fluidStack) {
        this.fluidTank.setFluid(fluidStack);
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

        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(()->fluidTank);
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
        tag.putInt("pot_block.progress", progress);
        tag.putInt("pot_block.temperature", temperature);
        tag = fluidTank.writeToNBT(tag);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("pot_block.progress");
        temperature = nbt.getInt("pot_block.temperature");
        fluidTank.readFromNBT(nbt);

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
        //not good!!!
        pBlockEntity.fluidFillAmount = pBlockEntity.fluidTank.getFluidAmount();

        if(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature < 125){
            pBlockEntity.temperature++;
        }
        if(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature < pBlockEntity.maxTemperature){
            pBlockEntity.temperature++;
        }
        else if(!(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock()) && !(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock()) && pBlockEntity.temperature > pBlockEntity.minTemperature){
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
                && hasRecipeFluidInTank(entity, match) && isInTemperatureRange(entity, match);
    }

    private static boolean isInTemperatureRange(PotBlockEntity entity, Optional<PotBlockRecipe> match){
        if(match.isPresent()){
            int minTemperature = match.get().minTemperature;
            int maxTemperature = match.get().maxTemperature;
            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
    }

    private static boolean hasRecipeFluidInTank(PotBlockEntity entity, Optional<PotBlockRecipe> recipe) {
        return entity.getFluidStack().getAmount() >= recipe.get().fluidStack.getAmount()
                && entity.getFluidStack().getFluid().equals(recipe.get().fluidStack.getFluid());
    }

//    private static boolean hasToolsInToolSlot(PotBlockEntity entity) {
//        return entity.itemHandler.getStackInSlot(UTENSIL_SLOT_ID).getItem() == ModItems.TURMERIC.get();
//    }

    private static void craftItem(PotBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<PotBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(PotBlockRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            for(int i = 1;i <= 9; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }
//            if(entity.itemHandler.getStackInSlot(UTENSIL_SLOT_ID).getDamageValue()>=entity.itemHandler.getStackInSlot(UTENSIL_SLOT_ID).getMaxDamage()){
//                    entity.itemHandler.extractItem(UTENSIL_SLOT_ID, 1, false);
//            }
//            else {
//                    entity.itemHandler.getStackInSlot(UTENSIL_SLOT_ID).hurt(1, new Random(), null);
//            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(match.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + 1));
            //todo remember to remove this if it becomes annoying
            entity.temperature-=5;
            entity.fluidTank.drain(match.get().getFluidStack().getAmount(), IFluidHandler.FluidAction.EXECUTE);
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

