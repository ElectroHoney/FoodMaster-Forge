package net.electrohoney.foodmastermod.util.networking.packets;

import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.screen.menus.PotBlockMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
//Credit https://github.com/Kaupenjoe/Resource-Slimes by kaupenjoe
//Under MIT Licence https://github.com/Kaupenjoe/Resource-Slimes/blob/master/LICENSE
public class PacketSyncOneFluidStackToClient {
    private FluidStack fluidStack;
    private final BlockPos pos;

    public PacketSyncOneFluidStackToClient(FluidStack stack, BlockPos pos){
        this.fluidStack = stack;
        this.pos = pos;
    }

    public PacketSyncOneFluidStackToClient(FriendlyByteBuf buf){
       this.fluidStack = buf.readFluidStack();
       this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeFluidStack(fluidStack);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
//        System.out.println("Network " + Minecraft.getInstance().screen);

        context.enqueueWork(()->{
            //setting the data for the client to read
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof PotBlockEntity blockEntity){
                blockEntity.setFluid(this.fluidStack);
            }

            if(Minecraft.getInstance().player.containerMenu instanceof PotBlockMenu menu && menu.blockEntity.getBlockPos().equals(pos)){
                menu.setFluid(this.fluidStack);
            }

        });
        return true;
    }


}
