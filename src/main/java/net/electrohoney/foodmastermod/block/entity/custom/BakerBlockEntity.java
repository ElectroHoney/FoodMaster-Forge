package net.electrohoney.foodmastermod.block.entity.custom;

import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
import net.electrohoney.foodmastermod.recipe.cooking.baker.BakerBlockRecipe;
import net.electrohoney.foodmastermod.recipe.cooking.baker.BroilerBlockRecipe;
import net.electrohoney.foodmastermod.screen.menus.BakerBlockMenu;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
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
import java.util.Objects;
import java.util.Optional;

public class BakerBlockEntity extends BlockEntity implements MenuProvider {
    public final static int BAKER_ENTITY_CONTAINER_SIZE = 10;
    public final static int BAKER_MAX_FLUID_CAPACITY = 8000;
    private final ItemStackHandler itemHandler = new ItemStackHandler(BAKER_ENTITY_CONTAINER_SIZE){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
        }
    };

    private final FluidTank fluidTank = new FluidTank(BAKER_MAX_FLUID_CAPACITY){
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

    public static final int BROIL_SLOT_ID = 6;
    public static final int BAKE_SLOT_ID = 7;

    public static final int UTENSIL_SLOT_ID = 8;
    public static final int RESULT_SLOT_ID = 9;


    private int progress = 0;
    private int maxProgress = 100;

    public int getTemperature() {
        return temperature;
    }

    //my own variables
    private int temperature = 25;
    private final int minTemperature = 25;
    private int maxTemperature = 500;
    //Lower slot
    private int bakeTime = 0;
    private int bakeDuration = 0;
    //Upper slot
    private int broilTime = 0;
    private int broilDuration = 0;

    public static final int BAKER_DATA_SIZE = 8;
    public BakerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.BAKER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        //@todo change how this works, this fields are only saved in the server
        this.data = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0: return BakerBlockEntity.this.progress;
                    case 1: return BakerBlockEntity.this.maxProgress;
                    case 2: return BakerBlockEntity.this.temperature;
                    case 3: return BakerBlockEntity.this.maxTemperature;
                    case 4: return BakerBlockEntity.this.bakeTime;
                    case 5: return BakerBlockEntity.this.bakeDuration;
                    case 6: return BakerBlockEntity.this.broilTime;
                    case 7: return BakerBlockEntity.this.broilDuration;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: BakerBlockEntity.this.progress = value; break;
                    case 1: BakerBlockEntity.this.maxProgress = value; break;
                    case 2: BakerBlockEntity.this.temperature = value; break;
                    case 3: BakerBlockEntity.this.maxTemperature = value;break;
                    case 4: BakerBlockEntity.this.bakeTime = value;break;
                    case 5: BakerBlockEntity.this.bakeDuration = value;break;
                    case 6: BakerBlockEntity.this.broilTime = value;break;
                    case 7: BakerBlockEntity.this.broilDuration = value;break;
                }
            }

            public int getCount() {
                return BAKER_DATA_SIZE;
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
        return new TextComponent("Brick Oven");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new BakerBlockMenu(pContainerId, pPlayerInventory, this, this.data);
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
        tag.putInt("baker_block.progress", progress);
        tag.putInt("baker_block.temperature", temperature);
        tag.putInt("baker_block.bakeTime", bakeTime);
        tag.putInt("baker_block.bakeDuration", bakeDuration);
        tag.putInt("baker_block.broilTime", broilTime);
        tag.putInt("baker_block.broilDuration", broilDuration);
        tag = fluidTank.writeToNBT(tag);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("baker_block.progress");
        temperature = nbt.getInt("baker_block.temperature");
        bakeTime = nbt.getInt("baker_block.bakeTime");
        bakeDuration = nbt.getInt("baker_block.bakeDuration");
        broilTime = nbt.getInt("baker_block.broilTime");
        broilDuration = nbt.getInt("baker_block.broilDuration");
        fluidTank.readFromNBT(nbt);

    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BakerBlockEntity pBlockEntity) {
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

    private static boolean hasRecipe(BakerBlockEntity entity) {
        if (isBaking(entity)) {
            --entity.bakeTime;
        }

        if (isBroiling(entity)) {
            --entity.broilTime;
        }

        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<BakerBlockRecipe> matchBake = level.getRecipeManager()
                .getRecipeFor(BakerBlockRecipe.Type.INSTANCE, inventory, level);

        Optional<BroilerBlockRecipe> matchBroil = level.getRecipeManager()
                .getRecipeFor(BroilerBlockRecipe.Type.INSTANCE, inventory, level);

        if(!isBaking(entity) && matchBake.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, matchBake.get().getResultItem())
                && hasRecipeFluidInTank(entity, matchBake) && isInTemperatureRangeBaker(entity, matchBake))
        {
            entity.bakeTime = ForgeHooks.getBurnTime(inventory.getItem(BAKE_SLOT_ID), RecipeType.SMELTING);
            if(entity.bakeTime > 0 && entity.itemHandler.getStackInSlot(BAKE_SLOT_ID)!=ItemStack.EMPTY){
                entity.itemHandler.extractItem(BAKE_SLOT_ID, 1, false);
            }
            entity.bakeDuration = entity.bakeTime;
        }

        if(!isBroiling(entity) && matchBroil.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, matchBroil.get().getResultItem())
                && hasRecipeFluidInTankBroiler(entity, matchBroil) && isInTemperatureRangeBroiler(entity, matchBroil))
        {
            entity.broilTime = ForgeHooks.getBurnTime(inventory.getItem(BROIL_SLOT_ID), RecipeType.SMELTING);
            if(entity.broilTime > 0 && entity.itemHandler.getStackInSlot(BROIL_SLOT_ID)!=ItemStack.EMPTY){
                entity.itemHandler.extractItem(BROIL_SLOT_ID, 1, false);
            }
            entity.broilDuration = entity.broilTime;
        }

        return (matchBake.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, matchBake.get().getResultItem())
                && hasRecipeFluidInTank(entity, matchBake) && isInTemperatureRangeBaker(entity, matchBake) && isBaking(entity))

                ||

                (matchBroil.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                        && canInsertItemIntoOutputSlot(inventory, matchBroil.get().getResultItem())
                        && hasRecipeFluidInTankBroiler(entity, matchBroil) && isInTemperatureRangeBroiler(entity, matchBroil) && isBroiling(entity));
    }

    private static boolean isBaking(BakerBlockEntity entity) {
        return entity.bakeTime > 0;
    }
    private static boolean isBroiling(BakerBlockEntity entity) {
        return entity.broilTime > 0;
    }

    protected static int getBurnDuration(ItemStack pFuel) {
        if (pFuel.isEmpty()) {
            return 0;
        } else {
            Item item = pFuel.getItem();
            return ForgeHooks.getBurnTime(pFuel, RecipeType.SMELTING);
        }
    }

    private static boolean isFuelInBroilSlot(BakerBlockEntity entity, SimpleContainer inventory){;
        return ForgeHooks.getBurnTime(inventory.getItem(BROIL_SLOT_ID), RecipeType.SMELTING) > 0;
    }

    private static boolean isFuelInBakeSlot(BakerBlockEntity entity, SimpleContainer inventory){
        System.out.println("ForgeHooks Works?-bake");
        System.out.println(ForgeHooks.getBurnTime(inventory.getItem(BAKE_SLOT_ID), RecipeType.SMELTING));
        System.out.println(inventory.getItem(BAKE_SLOT_ID));
        return ForgeHooks.getBurnTime(inventory.getItem(BAKE_SLOT_ID), RecipeType.SMELTING) > 0;
    }

    private static boolean isInTemperatureRangeBaker(BakerBlockEntity entity, Optional<BakerBlockRecipe> match){
        if(match.isPresent()){
            int minTemperature = match.get().minTemperature;
            int maxTemperature = match.get().maxTemperature;
            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
    }
    private static boolean isInTemperatureRangeBroiler(BakerBlockEntity entity, Optional<BroilerBlockRecipe> match){
        if(match.isPresent()){
            int minTemperature = match.get().minTemperature;
            int maxTemperature = match.get().maxTemperature;
            return minTemperature <= entity.temperature && entity.temperature <= maxTemperature;
        }
        else return false;
    }

    private static boolean hasRecipeFluidInTank(BakerBlockEntity entity, Optional<BakerBlockRecipe> recipe) {
        return entity.getFluidStack().getAmount() >= recipe.get().fluidStack.getAmount()
                && entity.getFluidStack().getFluid().equals(recipe.get().fluidStack.getFluid());
    }

    private static boolean hasRecipeFluidInTankBroiler(BakerBlockEntity entity, Optional<BroilerBlockRecipe> recipe) {
        return entity.getFluidStack().getAmount() >= recipe.get().fluidStack.getAmount()
                && entity.getFluidStack().getFluid().equals(recipe.get().fluidStack.getFluid());
    }

    private static void craftItem(BakerBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<BakerBlockRecipe> matchBaker = level.getRecipeManager()
                .getRecipeFor(BakerBlockRecipe.Type.INSTANCE, inventory, level);
        Optional<BroilerBlockRecipe> matchBroiler = level.getRecipeManager()
                .getRecipeFor(BroilerBlockRecipe.Type.INSTANCE, inventory, level);
        if(matchBaker.isPresent()) {
            for(int i = 0;i < 6; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(matchBaker.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + 1));
            entity.fluidTank.drain(matchBaker.get().getFluidStack().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            entity.resetProgress();
        }
        else if(matchBroiler.isPresent()){
            for(int i = 0;i < 6; ++i){
                if(entity.itemHandler.getStackInSlot(i)!=ItemStack.EMPTY){
                    entity.itemHandler.extractItem(i, 1, false);
                }
            }
            entity.itemHandler.setStackInSlot(RESULT_SLOT_ID, new ItemStack(matchBroiler.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(RESULT_SLOT_ID).getCount() + 1));
            entity.fluidTank.drain(matchBroiler.get().getFluidStack().getAmount(), IFluidHandler.FluidAction.EXECUTE);
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

    public int getBakeTime() {
        return bakeTime;
    }

    public void setBakeTime(int bakeTime) {
        this.bakeTime = bakeTime;
    }

    public int getBroilTime() {
        return broilTime;
    }

    public void setBroilTime(int broilTime) {
        this.broilTime = broilTime;
    }
}


