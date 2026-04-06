package com.resonance.mod.network;

import com.resonance.mod.ResonanceMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ResonanceMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // Paquete de sincronización de resonancia
        CHANNEL.registerMessage(
                packetId++,
                ResonanceSyncPacket.class,
                ResonanceSyncPacket::encode,
                ResonanceSyncPacket::decode,
                ResonanceSyncPacket::handle
        );

        // Paquete de sincronización de infección
        CHANNEL.registerMessage(
                packetId++,
                InfectionSyncPacket.class,
                InfectionSyncPacket::encode,
                InfectionSyncPacket::decode,
                InfectionSyncPacket::handle
        );

        // Paquete de cambio de fase
        CHANNEL.registerMessage(
                packetId++,
                PhaseChangePacket.class,
                PhaseChangePacket::encode,
                PhaseChangePacket::decode,
                PhaseChangePacket::handle
        );
    }

    public static void sendToClient(ResonanceSyncPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllClients(InfectionSyncPacket packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}