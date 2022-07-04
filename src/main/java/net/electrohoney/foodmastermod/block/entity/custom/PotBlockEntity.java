package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.item.ModItems;
import net.electrohoney.foodmastermod.recipe.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.PotBlockMenu;
import net.electrohoney.foodmastermod.util.networking.ModMessages;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncFluidStackToClient;
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
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
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
import java.util.Random;

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
            ModMessages.sendToClients(new PacketSyncFluidStackToClient(this.fluid, worldPosition));
        }

    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    protected final ContainerData data;

    private static final int SPICE_SLOT_ID = 10;
    //public static final int RECIPE_SLOT_ID = 2;
    private static final int WATER_SLOT_ID = 0;
    private static final int RESULT_SLOT_ID = 11;

    private int progress = 0;
    private int maxProgress = 72;

    public int getTemperature() {
        return temperature;
    }

    //my own variables
    private int temperature = 0;
    private int maxTemperature = 199;

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

    public FluidStack getFluid() {
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

        if(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature <= 125){
            pBlockEntity.temperature++;
        }
        if(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock() && pBlockEntity.temperature <= pBlockEntity.maxTemperature){
            pBlockEntity.temperature++;
        }
        else if(!(Blocks.FIRE == pLevel.getBlockState(pPos.below()).getBlock()) && !(Blocks.LAVA == pLevel.getBlockState(pPos.below()).getBlock()) && pBlockEntity.temperature >= 0){
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
                && hasWaterInWaterSlot(entity) && hasToolsInToolSlot(entity) && isInTemperatureRange(entity, match);
    }

    private static boolean isInTemperatureRange(PotBlockEntity entity, Optional<PotBlockRecipe> match){
        if(match.isPresent()){
            System.out.println(match.get().minTemperature);
            System.out.println(match.get().maxTemperature);
            System.out.println(entity.temperature);
            int minTemperature = match.get().minTemperature;
            int maxTemperature = match.get().maxTemperature;
            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
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
            for(int i = 1;i <= 9; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    System.out.println("PotBlockEntity:" + entity.itemHandler.getStackInSlot(i));
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }
            //entity.itemHandler.getStackInSlot(2).hurt(1, new Random(), null);
            if(entity.itemHandler.getStackInSlot(SPICE_SLOT_ID).getDamageValue()>=entity.itemHandler.getStackInSlot(SPICE_SLOT_ID).getMaxDamage()){
                    entity.itemHandler.extractItem(SPICE_SLOT_ID, 1, false);
            }
            else {
                    entity.itemHandler.getStackInSlot(SPICE_SLOT_ID).hurt(1, new Random(), null);
            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(match.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + 1));
            //todo remember to remove this if it becomes annoying
            entity.temperature-=10;
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

