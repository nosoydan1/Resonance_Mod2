package com.resonance.mod.network;

import com.resonance.mod.ClientInfectionData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PhaseChangePacket {

    private final int newPhase;
    private final int points;

    public PhaseChangePacket(int newPhase, int points) {
        this.newPhase = newPhase;
        this.points = points;
    }

    public static void encode(PhaseChangePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.newPhase);
        buf.writeInt(packet.points);
    }

    public static PhaseChangePacket decode(FriendlyByteBuf buf) {
        return new PhaseChangePacket(buf.readInt(), buf.readInt());
    }

    public static void handle(PhaseChangePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Solo ejecutar en el lado del cliente
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                int oldPhase = ClientInfectionData.phase;
                ClientInfectionData.phase = packet.newPhase;
                ClientInfectionData.points = packet.points;

                // Reproducir sonido de cambio de fase si cambió la fase
                if (oldPhase != packet.newPhase) {
                    Minecraft.getInstance().player.playNotifySound(
                            SoundEvents.WARDEN_NEARBY_CLOSE,
                            SoundSource.AMBIENT,
                            1.0f, 1.0f
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}