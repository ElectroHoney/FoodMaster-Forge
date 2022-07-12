//package net.electrohoney.foodmastermod.block.entity.custom.abstracts;
//
//import net.electrohoney.foodmastermod.block.entity.ModBlockEntities;
//import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
//import net.electrohoney.foodmastermod.recipe.PotBlockRecipe;
//import net.electrohoney.foodmastermod.screen.menus.PotBlockMenu;
//import net.electrohoney.foodmastermod.util.networking.ModMessages;
//import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncFluidStackToClient;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.Connection;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.network.protocol.Packet;
//import net.minecraft.network.protocol.game.ClientGamePacketListener;
//import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
//import net.minecraft.world.Containers;
//import net.minecraft.world.MenuProvider;
//import net.minecraft.world.SimpleContainer;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.ContainerData;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.util.LazyOptional;
//import net.minecraftforge.fluids.FluidStack;
//import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
//import net.minecraftforge.fluids.capability.IFluidHandler;
//import net.minecraftforge.fluids.capability.templates.FluidTank;
//import net.minecraftforge.items.CapabilityItemHandler;
//import net.minecraftforge.items.IItemHandler;
//import net.minecraftforge.items.ItemStackHandler;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import javax.annotation.Nonnull;
//import java.util.Optional;
//
//public class AbstractCookingBlockEntity extends BlockEntity implements MenuProvider {
//    public AbstractCookingBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
//        super(pType, pWorldPosition, pBlockState);
//    }
//    public final static int ENTITY_CONTAINER_SIZE = 1;
//    public final static int MAX_FLUID_CAPACITY = 4000;
//    public final int DATA_SIZE = 1;
//    public ContainerData data;
//
//    private final ItemStackHandler itemHandler = new ItemStackHandler(ENTITY_CONTAINER_SIZE){
//        @Override
//        protected void onContentsChanged(int slot){
//            setChanged();
//        }
//    };
//
//    private final FluidTank fluidTank = new FluidTank(MAX_FLUID_CAPACITY){
//        @Override
//        protected void onContentsChanged() {
//            setChanged();
//            assert level != null;
//            if(!level.isClientSide()){
//                ModMessages.sendToClients(new PacketSyncFluidStackToClient(this.fluid, worldPosition));
//            }
//        }
//
//    };
//
//    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
//    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();
//    private static final int RESULT_SLOT_ID = 1;
//
//    public AbstractCookingBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
//        super(ModBlockEntities.POT_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
//        //@todo change how this works, this fields are only saved in the server
//        this.data = new ContainerData() {
//            public int get(int index) {
//                switch (index) {
//                    default: return 0;
//                }
//            }
//
//            public void set(int index, int value) {
//                switch(index) {
//                }
//            }
//
//            public int getCount() {
//                return DATA_SIZE;
//            }
//        };
//    }
//
//    public FluidStack getFluidStack() {
//        return this.fluidTank.getFluid();
//    }
//
//    public void setFluid(FluidStack fluidStack) {
//        this.fluidTank.setFluid(fluidStack);
//    }
//    @Override
//    public Component getDisplayName() {
//        return new TextComponent("Abstract Cooking Block");
//    }
//
//    @Nullable
//    @Override
//    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
//        return new PotBlockMenu(pContainerId, pPlayerInventory, this, this.data);
//    }
//
//    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
//        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//            return lazyItemHandler.cast();
//        }
//
//        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
//            return lazyFluidHandler.cast();
//        }
//
//        return super.getCapability(cap, side);
//    }
//
//    @Override
//    public void onLoad() {
//        super.onLoad();
//        lazyItemHandler = LazyOptional.of(() -> itemHandler);
//        lazyFluidHandler = LazyOptional.of(()->fluidTank);
//    }
//
//    @Override
//    public void invalidateCaps()  {
//        super.invalidateCaps();
//        lazyItemHandler.invalidate();
//        lazyFluidHandler.invalidate();
//    }
//
//    @Override
//    protected void saveAdditional(@NotNull CompoundTag tag) {
//        super.saveAdditional(tag);
//    }
//
//    @Override
//    public void load(CompoundTag nbt) {
//        super.load(nbt);
//    }
//
//    public void drops() {
//        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
//        for (int i = 0; i < itemHandler.getSlots(); i++) {
//            inventory.setItem(i, itemHandler.getStackInSlot(i));
//        }
//
//        Containers.dropContents(this.level, this.worldPosition, inventory);
//    }
//
//    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, PotBlockEntity pBlockEntity) {
//    }
//
//    private static boolean hasRecipe(PotBlockEntity entity) {
//        return true;
//    }
//
//    private static boolean hasRecipeFluidInTank(PotBlockEntity entity, Optional<PotBlockRecipe> recipe){
//        return true;
//    }
//
//    private static void craftItem(PotBlockEntity entity) {
//    }
//
//    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory, ItemStack output) {
//        return inventory.getItem(RESULT_SLOT_ID).getItem() == output.getItem() || inventory.getItem(RESULT_SLOT_ID).isEmpty();
//    }
//
//    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory) {
//        return inventory.getItem(RESULT_SLOT_ID).getMaxStackSize() > inventory.getItem(RESULT_SLOT_ID).getCount();
//    }
//
//    @Nullable
//    @Override
//    public Packet<ClientGamePacketListener> getUpdatePacket() {
//        return ClientboundBlockEntityDataPacket.create(this);
//    }
//
//    @Override
//    public CompoundTag getUpdateTag() {
//        CompoundTag compoundTag = saveWithoutMetadata();
//        load(compoundTag);
//        return compoundTag;
//    }
//}
