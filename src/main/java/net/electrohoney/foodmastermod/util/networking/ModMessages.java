package net.electrohoney.foodmastermod.util.networking;

import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncStringToClient;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncTwoFluidStacksToClient;
import net.electrohoney.foodmastermod.util.networking.packets.PacketSyncOneFluidStackToClient;
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

    private static int stringPacketId = 2;
    private static int id(){
        return packetId++;
    }

    private static int idAger(){
        return agerPacketId++;
    }
    private static int idString(){
        return stringPacketId++;
    }

    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(FoodMaster.MOD_ID, "messages"))
                .networkProtocolVersion(()->"1.0")
                .clientAcceptedVersions(s->true)
                .serverAcceptedVersions(s->true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(PacketSyncOneFluidStackToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncOneFluidStackToClient::new)
                .encoder(PacketSyncOneFluidStackToClient::toBytes)
                .consumer(PacketSyncOneFluidStackToClient::handle)
                .add();

        net.messageBuilder(PacketSyncTwoFluidStacksToClient.class, idAger(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncTwoFluidStacksToClient::new)
                .encoder(PacketSyncTwoFluidStacksToClient::toBytes)
                .consumer(PacketSyncTwoFluidStacksToClient::handle)
                .add();

        net.messageBuilder(PacketSyncStringToClient.class, idString(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncStringToClient::new)
                .encoder(PacketSyncStringToClient::toBytes)
                .consumer(PacketSyncStringToClient::handle)
                .add();
    }

    public static <MSG> void sendToClients(MSG message){
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
