package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.cooking.GrillerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.PotBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.GrillerBlockMenu;
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

public class GrillerBlockEntity extends BlockEntity implements MenuProvider {
    public final static int GRILLER_ENTITY_CONTAINER_SIZE = 11;
    private final ItemStackHandler itemHandler = new ItemStackHandler(GRILLER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;

    public static final int UTENSIL_SLOT_ID = 9;
    private static final int RESULT_SLOT_ID = 10;

    private int progress = 0;
    private int maxProgress = 100;

    public int getTemperature() {
        return temperature;
    }

    //my own variables
    private int temperature = 25;
    private int minTemperature = 25;
    private int maxTemperature = 300;

    public static final int GRILLER_DATA_SIZE = 4;
    private int fluidFillAmount = 0;
    public GrillerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.GRILLER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return GrillerBlockEntity.this.progress;
                    case 1: return GrillerBlockEntity.this.maxProgress;
                    case 2: return GrillerBlockEntity.this.temperature;
                    case 3: return GrillerBlockEntity.this.maxTemperature;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: GrillerBlockEntity.this.progress = value; break;
                    case 1: GrillerBlockEntity.this.maxProgress = value; break;
                    case 2: GrillerBlockEntity.this.temperature = value; break;
                    case 3: GrillerBlockEntity.this.maxTemperature = value;break;

                }
            }

            public int getCount() {
                return GRILLER_DATA_SIZE;
            }
        };
    }


    @Override
    public Component getDisplayName() {
        return new TextComponent("Grill");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new GrillerBlockMenu(pContainerId, pPlayerInventory, this, this.data);
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
        tag.putInt("griller.progress", progress);
        tag.putInt("griller.temperature", temperature);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("griller.progress");
        temperature = nbt.getInt("griller.temperature");

    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GrillerBlockEntity pBlockEntity) {
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

    private static boolean hasRecipe(GrillerBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<GrillerBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(GrillerBlockRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem())
                && isInTemperatureRange(entity, match);
    }

    private static boolean isInTemperatureRange(GrillerBlockEntity entity, Optional<GrillerBlockRecipe> match){
        if(match.isPresent()){
            int minTemperature = match.get().minTemperature;
            int maxTemperature = match.get().maxTemperature;
            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
    }

    private static void craftItem(GrillerBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<GrillerBlockRecipe> match = level.getRecipeManager()
                .getRecipeFor(GrillerBlockRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            for(int i = 0;i <= 8; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }

            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(match.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + match.get().getResultItem().getCount()));
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

