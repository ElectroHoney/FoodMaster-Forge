package net.electrohoney.foodmastermod.screen.menus;

import net.electrohoney.foodmastermod.block.ModBlocks;
import net.electrohoney.foodmastermod.block.entity.custom.DistillerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.screen.ModMenuTypes;
import net.electrohoney.foodmastermod.screen.slot.ModResultSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import static net.electrohoney.foodmastermod.block.entity.custom.DistillerBlockEntity.DISTILLER_DATA_SIZE;
import static net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity.POT_DATA_SIZE;

public class DistillerBlockMenu extends AbstractContainerMenu {

    public final DistillerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private FluidStack inputFluid;
    private FluidStack outputFluid;

    public DistillerBlockMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()),new SimpleContainerData(DISTILLER_DATA_SIZE));
    }

    public DistillerBlockMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DISTILLER_BLOCK_MENU.get(), pContainerId);
        checkContainerSize(inv, DistillerBlockEntity.DISTILLER_ENTITY_CONTAINER_SIZE);
        blockEntity = ((DistillerBlockEntity) entity);
        this.level = inv.player.level;
        this.data = data;
        this.inputFluid = blockEntity.getInputFluidTank();
        this.outputFluid = blockEntity.getOutputFluidTank();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
//          // Mush Slot
            this.addSlot(new SlotItemHandler(handler, 0, 77, 44));

            //Item Extra Grid
            int gridIndex = 1;
            for(int i = 0; i<=2; i++){
                    this.addSlot(
                            new SlotItemHandler(handler, gridIndex,
                                    85+18*i, 11));
                    gridIndex += 1;
            }
        });

        //very important!
        addDataSlots(data);
    }

    public boolean isCrafting(){
        return data.get(0) > 0;
    }

    public void setInputFluid(FluidStack fluidStack){
        this.inputFluid = fluidStack;
    }
    public void setOutputFluid(FluidStack fluidStack){
        this.outputFluid = fluidStack;
    }

    public FluidStack getInputFluid(){
        return this.inputFluid;
    }
    public FluidStack getOutputFluid(){
        return this.outputFluid;
    }

    public int getScaledProgress(){
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 31;//31 //bubble height/(in the video it was downwards mine is sideways) in pixels

        //System.out.print("Hey progress! " + ((maxProgress != 0 && progress !=0) ? progress * progressArrowSize / maxProgress : 0));
        return (maxProgress != 0 && progress !=0) ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getScaledTemperature(){
        int temperature = this.data.get(2);
        int maxTemperature = this.data.get(3);
        int temperatureBarHeight = 54; //size in pixels of the gray bar

        return (maxTemperature != 0 && temperature !=0) ? temperature * temperatureBarHeight / maxTemperature : 0;

    }

    public int getTemperature(){
        return this.data.get(2);
    }

    public int getMaxTemperature(){
        return this.data.get(3);
    }


    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!@todo change number
    private static final int TE_INVENTORY_SLOT_COUNT = DistillerBlockEntity.DISTILLER_ENTITY_CONTAINER_SIZE;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.DISTILLER_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 17-9 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 17-9 + i * 18, 142));
        }
    }
}

