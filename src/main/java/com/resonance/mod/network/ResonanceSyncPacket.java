package com.resonance.mod.network;

import com.resonance.mod.ClientResonanceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import com.resonance.mod.network.ResonanceSyncPacket;

import java.util.function.Supplier;

public class ResonanceSyncPacket {
    private final float resonance;
    private final boolean marked;

    public ResonanceSyncPacket(float resonance, boolean marked) {
        this.resonance = resonance;
        this.marked = marked;
    }

    public static void encode(ResonanceSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.resonance);
        buf.writeBoolean(packet.marked);
    }

    public static ResonanceSyncPacket decode(FriendlyByteBuf buf) {
        return new ResonanceSyncPacket(buf.readFloat(), buf.readBoolean());
    }

    public static void handle(ResonanceSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                System.out.println("DEBUG: Resonancia recibida = " + packet.resonance + ", marked = " + packet.marked);
                ClientResonanceData.setResonance(packet.resonance);
                ClientResonanceData.setMarked(packet.marked);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}