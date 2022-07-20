package net.electrohoney.foodmastermod.util.networking.packets;

import net.electrohoney.foodmastermod.block.entity.custom.AgerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.DistillerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.FermenterBlockEntity;
import net.electrohoney.foodmastermod.screen.menus.AgerBlockMenu;
import net.electrohoney.foodmastermod.screen.menus.DistillerBlockMenu;
import net.electrohoney.foodmastermod.screen.menus.FermenterBlockMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//Credit https://github.com/Kaupenjoe/Resource-Slimes by kaupenjoe
//Under MIT Licence https://github.com/Kaupenjoe/Resource-Slimes/blob/master/LICENSE
public class PacketSyncTwoFluidStacksToClient {

    private FluidStack sentFluidStack;
    private final BlockPos pos;

    // 0 for output and 1 for input
    private final int fluidInputType;

    public PacketSyncTwoFluidStacksToClient(FluidStack fluidStack, int fluidInputType, BlockPos pos){
        this.fluidInputType = fluidInputType;
        this.sentFluidStack = fluidStack;
        this.pos = pos;
    }
    public PacketSyncTwoFluidStacksToClient(FriendlyByteBuf buf){
        this.fluidInputType = buf.readInt();
        this.sentFluidStack = buf.readFluidStack();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(fluidInputType);
        buf.writeFluidStack(sentFluidStack);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(()->{
            //setting the data for the client to read
            //ageing barrels
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof AgerBlockEntity blockEntity){
                if(fluidInputType == 0) {
                    blockEntity.setOutputFluid(this.sentFluidStack);
                }
                else{
                    blockEntity.setInputFluid(this.sentFluidStack);
                }
            }
            if(Minecraft.getInstance().player.containerMenu instanceof AgerBlockMenu menu && menu.blockEntity.getBlockPos().equals(pos)){
                if(fluidInputType == 0) {
                    menu.setOutputFluid(this.sentFluidStack);
                }
                else{
                    menu.setInputFluid(this.sentFluidStack);
                }
            }
            //distiller
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof DistillerBlockEntity blockEntity){
                if(fluidInputType == 0) {
                    blockEntity.setOutputFluid(this.sentFluidStack);
                }
                else{
                    blockEntity.setInputFluid(this.sentFluidStack);
                }
            }
            if(Minecraft.getInstance().player.containerMenu instanceof DistillerBlockMenu menu && menu.blockEntity.getBlockPos().equals(pos)){
                if(fluidInputType == 0) {
                    menu.setOutputFluid(this.sentFluidStack);
                }
                else{
                    menu.setInputFluid(this.sentFluidStack);
                }
            }

            //fermenter
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof FermenterBlockEntity blockEntity){
                if(fluidInputType == 0) {
                    blockEntity.setOutputFluid(this.sentFluidStack);
                }
                else{
                    blockEntity.setInputFluid(this.sentFluidStack);
                }
            }
            if(Minecraft.getInstance().player.containerMenu instanceof FermenterBlockMenu menu && menu.blockEntity.getBlockPos().equals(pos)){
                if(fluidInputType == 0) {
                    menu.setOutputFluid(this.sentFluidStack);
                }
                else{
                    menu.setInputFluid(this.sentFluidStack);
                }
            }
        });
        return true;
    }

}
