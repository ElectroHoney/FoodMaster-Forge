package net.electrohoney.foodmastermod.util.networking.packets;

import net.electrohoney.foodmastermod.block.entity.custom.AgerBlockEntity;
import net.electrohoney.foodmastermod.screen.menus.AgerBlockMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//Credit https://github.com/Kaupenjoe/Resource-Slimes by kaupenjoe
//Under MIT Licence https://github.com/Kaupenjoe/Resource-Slimes/blob/master/LICENSE
public class AgerPacketSyncFluidStackToClient {

    private FluidStack AgerFluidStack;
    private final BlockPos pos;

    // 0 for output and 1 for input
    private final int fluidInputType;

    public AgerPacketSyncFluidStackToClient(FluidStack fluidStack, int fluidInputType, BlockPos pos){
        this.fluidInputType = fluidInputType;
        this.AgerFluidStack = fluidStack;
        this.pos = pos;
    }
    public AgerPacketSyncFluidStackToClient(FriendlyByteBuf buf){
        this.fluidInputType = buf.readInt();
        this.AgerFluidStack = buf.readFluidStack();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(fluidInputType);
        buf.writeFluidStack(AgerFluidStack);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        System.out.println("Network " + Minecraft.getInstance().screen);

        context.enqueueWork(()->{
            //setting the data for the client to read
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof AgerBlockEntity blockEntity){
                if(fluidInputType == 0) {
                    blockEntity.setOutputFluid(this.AgerFluidStack);
                }
                else{
                    blockEntity.setInputFluid(this.AgerFluidStack);
                }
            }

            if(Minecraft.getInstance().player.containerMenu instanceof AgerBlockMenu menu && menu.blockEntity.getBlockPos().equals(pos)){
                if(fluidInputType == 0) {
                    menu.setOutputFluid(this.AgerFluidStack);
                }
                else{
                    menu.setInputFluid(this.AgerFluidStack);
                }
            }
        });
        return true;
    }


}
