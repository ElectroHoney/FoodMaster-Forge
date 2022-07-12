package net.electrohoney.foodmastermod.util.networking;

import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.util.networking.packets.AgerPacketSyncFluidStackToClient;
import net.electrohoney.foodmastermod.util.networking.packets.PotPacketSyncFluidStackToClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
//Credit https://github.com/Kaupenjoe/Resource-Slimes by kaupenjoe
//Under MIT Licence https://github.com/Kaupenjoe/Resource-Slimes/blob/master/LICENSE
public class ModMessages {

    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int agerPacketId = 1;
    private static int id(){
        return packetId++;
    }

    private static int idAger(){
        return agerPacketId++;
    }

    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(FoodMaster.MOD_ID, "messages"))
                .networkProtocolVersion(()->"1.0")
                .clientAcceptedVersions(s->true)
                .serverAcceptedVersions(s->true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(PotPacketSyncFluidStackToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PotPacketSyncFluidStackToClient::new)
                .encoder(PotPacketSyncFluidStackToClient::toBytes)
                .consumer(PotPacketSyncFluidStackToClient::handle)
                .add();

        net.messageBuilder(AgerPacketSyncFluidStackToClient.class, idAger(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AgerPacketSyncFluidStackToClient::new)
                .encoder(AgerPacketSyncFluidStackToClient::toBytes)
                .consumer(AgerPacketSyncFluidStackToClient::handle)
                .add();

    }

    public static <MSG> void sendToClients(MSG message){
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
