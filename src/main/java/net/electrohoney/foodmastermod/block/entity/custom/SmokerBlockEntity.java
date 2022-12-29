package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.cooking.SmokerBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.SmokerBlockMenu;
import net.electrohoney.foodmastermod.util.networking.ModMessages;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncOneFluidStackToClient;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncStringToClient;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nonnull;
import java.util.Optional;

public class SmokerBlockEntity extends BlockEntity implements MenuProvider {
    public final static int SMOKER_ENTITY_CONTAINER_SIZE = 5;
    private final ItemStackHandler itemHandler = new ItemStackHandler(SMOKER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
            assert level != null;
            if(!level.isClientSide() && slot == 0 && smokeTime >= 490) {
                ModMessages.sendToClients(new PacketSyncStringToClient(this.getStackInSlot(slot).getDisplayName().getString().replace("[", "").replace("]", ""), worldPosition));
            }
        }

        @Override
        protected void onLoad() {
            assert level != null;
            if(!level.isClientSide()) {
                ModMessages.sendToClients(new PacketSyncStringToClient(woodName, worldPosition));
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;

    public static final int FUEL_SLOT_ID = 0;
    public static final int ITEM_SLOT_ID1 = 1;
    public static final int ITEM_SLOT_ID2 = 2;
    public static final int ITEM_SLOT_ID3 = 3;
    public static final int RESULT_SLOT_ID = 4;

    private int progress = 0;
    private int maxProgress = 100;
    private int temperature = 25;
    private int minTemperature = 10;
    private int maxTemperature = 200;
    //Upper slot
    private int smokeTime = 0;
    private int smokeDuration = 0;

    private String woodName = "Generic";

    public static final int SMOKER_DATA_SIZE = 7;
    public SmokerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.SMOKER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return SmokerBlockEntity.this.progress;
                    case 1: return SmokerBlockEntity.this.maxProgress;
                    case 2: return SmokerBlockEntity.this.temperature;
                    case 3: return SmokerBlockEntity.this.maxTemperature;
                    case 4: return SmokerBlockEntity.this.minTemperature;
                    case 5: return SmokerBlockEntity.this.smokeTime;
                    case 6: return SmokerBlockEntity.this.smokeDuration;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: SmokerBlockEntity.this.progress = value; break;
                    case 1: SmokerBlockEntity.this.maxProgress = value; break;
                    case 2: SmokerBlockEntity.this.temperature = value; break;
                    case 3: SmokerBlockEntity.this.maxTemperature = value;break;
                    case 4: SmokerBlockEntity.this.minTemperature = value;break;
                    case 5: SmokerBlockEntity.this.smokeTime = value;break;
                    case 6: SmokerBlockEntity.this.smokeDuration = value;break;
                }
            }

            public int getCount() {
                return SMOKER_DATA_SIZE;
            }
        };
    }


    @Override
    public Component getDisplayName() {
        return new TextComponent("Smoker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new SmokerBlockMenu(pContainerId, pPlayerInventory, this, this.data);
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
        tag.putInt("smoker_block.progress", progress);
        tag.putInt("smoker_block.maxProgress", maxProgress);
        tag.putInt("smoker_block.temperature", temperature);
        tag.putInt("smoker_block.maxTemperature", maxTemperature);
        tag.putInt("smoker_block.minTemperature", minTemperature);
        tag.putInt("smoker_block.smokeTime", smokeTime);
        tag.putInt("smoker_block.smokeDuration", smokeDuration);
        tag.putString("smoker_block.woodName", woodName);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("smoker_block.progress");
        maxProgress = nbt.getInt("smoker_block.maxProgress");
        temperature = nbt.getInt("smoker_block.temperature");
        maxTemperature = nbt.getInt("smoker_block.maxTemperature");
        minTemperature = nbt.getInt("smoker_block.minTemperature");
        smokeTime = nbt.getInt("smoker_block.smokeTime");
        smokeDuration = nbt.getInt("smoker_block.smokeDuration");
        woodName = nbt.getString("smoker_block.woodName");

    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, SmokerBlockEntity pBlockEntity) {
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
    }

    private static boolean hasRecipe(SmokerBlockEntity entity) {

        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<SmokerBlockRecipe> matchSmoke = level.getRecipeManager()
                .getRecipeFor(SmokerBlockRecipe.Type.INSTANCE, inventory, level);

        //smoke time decrease
        if (isSmoking(entity)) {
            --entity.smokeTime;
        }

        //matchSmoke.ifPresent(smokerBlockRecipe -> entity.woodName = smokerBlockRecipe.getWoodLog().getItems()[0].getDisplayName().getString());

//        System.out.println("isSmoking: " + isSmoking(entity));
//        System.out.println("isPresent: " + matchSmoke.isPresent());
//        System.out.println("canInsertAmountIntoOutputSlot: " + canInsertAmountIntoOutputSlot(inventory));
//        System.out.println("canInsertItemIntoOutputSlot: " + canInsertItemIntoOutputSlot(inventory, matchSmoke.get().getResultItem()));

        if(!isSmoking(entity) && matchSmoke.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, matchSmoke.get().getResultItem()))
        {
            entity.smokeTime = 500;
            //TODO get a way to obtain smoke time from fuels
            if(entity.smokeTime >= 0 && entity.itemHandler.getStackInSlot(FUEL_SLOT_ID)!=ItemStack.EMPTY){
                entity.itemHandler.extractItem(FUEL_SLOT_ID, 1, false);
            }
            entity.smokeDuration = entity.smokeTime;
        }

        return (matchSmoke.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, matchSmoke.get().getResultItem())
                && isInTemperatureRange(entity, matchSmoke) && isSmoking(entity));
    }

    private static boolean isSmoking(SmokerBlockEntity entity) {
        return entity.smokeTime > 0;
    }

    private static boolean isFuelInSmokeSlot(SmokerBlockEntity entity, SimpleContainer inventory){
//      return ForgeHooks.getBurnTime(inventory.getItem(BAKE_SLOT_ID), RecipeType.SMELTING) > 0;
        //TODO add a filter if the fuel is part of the recipe
        return true;
    }
    private static boolean isInTemperatureRange(SmokerBlockEntity entity, Optional<SmokerBlockRecipe> match){
        if(match.isPresent()){
            int minTemperature = match.get().getMinTemperature();
            int maxTemperature = match.get().getMaxTemperature();
            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
    }
    private static void craftItem(SmokerBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<SmokerBlockRecipe> matchSmoker = level.getRecipeManager()
                .getRecipeFor(SmokerBlockRecipe.Type.INSTANCE, inventory, level);
        if(matchSmoker.isPresent()) {
            for(int i = 1;i <= 3; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(matchSmoker.get().getResultItem().getItem(),
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
    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public String getWoodName(){
        return woodName;
    }
    public void setWoodName(String woodName){
         this.woodName = woodName;
    }

}


