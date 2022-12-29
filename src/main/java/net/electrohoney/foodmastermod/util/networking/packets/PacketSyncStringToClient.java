package net.electrohoney.foodmastermod.util.networking.packets;

import net.electrohoney.foodmastermod.block.entity.custom.*;
import net.electrohoney.foodmastermod.screen.menus.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncStringToClient {
    private String sentWoodName;
    private final BlockPos pos;

    public PacketSyncStringToClient(String WoodName, BlockPos pos){
        this.sentWoodName = WoodName;
        this.pos = pos;
    }

    public PacketSyncStringToClient(FriendlyByteBuf buf){
        this.sentWoodName = buf.readNbt().getString("woodName");
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf){
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("woodName", this.sentWoodName);
        buf.writeNbt(compoundTag);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(()->{
            //setting the data for the client to read
            //POT
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof SmokerBlockEntity smokerBlockEntity){
                smokerBlockEntity.setWoodName(this.sentWoodName);
            }
            if(Minecraft.getInstance().player.containerMenu instanceof SmokerBlockMenu menu && menu.blockEntity.getBlockPos().equals(pos)){
                menu.setWoodName(this.sentWoodName);
            }
        });
        return true;
    }


}
