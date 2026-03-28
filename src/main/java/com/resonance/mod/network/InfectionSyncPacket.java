package com.resonance.mod.network;

import com.resonance.mod.ClientInfectionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InfectionSyncPacket {

    private final int points;
    private final int phase;

    public InfectionSyncPacket(int points, int phase) {
        this.points = points;
        this.phase = phase;
    }

    public static void encode(InfectionSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.points);
        buf.writeInt(packet.phase);
    }

    public static InfectionSyncPacket decode(FriendlyByteBuf buf) {
        return new InfectionSyncPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(InfectionSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    ClientInfectionData.points = packet.points;
                    ClientInfectionData.phase = packet.phase;
                })
        );
        ctx.get().setPacketHandled(true);
    }
}