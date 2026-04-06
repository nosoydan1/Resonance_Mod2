package com.resonance.mod.network;

import com.resonance.mod.ClientInfectionData;
import net.minecraft.network.FriendlyByteBuf;
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
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    int oldPhase = ClientInfectionData.phase;
                    ClientInfectionData.phase = packet.newPhase;
                    ClientInfectionData.points = packet.points;

                    // Reproducir sonido de cambio de fase
                    if (oldPhase != packet.newPhase) {
                        net.minecraft.client.Minecraft.getInstance().getSoundManager()
                                .play(new net.minecraft.client.resources.sounds.SimpleSoundInstance(
                                        new net.minecraft.resources.ResourceLocation("minecraft", "entity.warden.nearby_close"),
                                        net.minecraft.sounds.SoundSource.AMBIENT,
                                        1.0f, 1.0f,
                                        net.minecraft.client.Minecraft.getInstance().player
                                ));
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }
}