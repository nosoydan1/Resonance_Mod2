package com.resonance.mod.network;

import com.resonance.mod.ResonanceData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResonanceSyncPacket {

    private final float resonanceValue;
    private final boolean isMarked;

    public ResonanceSyncPacket(float resonanceValue, boolean isMarked) {
        this.resonanceValue = resonanceValue;
        this.isMarked = isMarked;
    }

    public static void encode(ResonanceSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.resonanceValue);
        buf.writeBoolean(packet.isMarked);
    }

    public static ResonanceSyncPacket decode(FriendlyByteBuf buf) {
        return new ResonanceSyncPacket(buf.readFloat(), buf.readBoolean());
    }

    public static void handle(ResonanceSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Player player = Minecraft.getInstance().player;
                    if (player != null) {
                        ResonanceData.setResonance(player, packet.resonanceValue);
                        if (packet.isMarked) {
                            ResonanceData.markPlayer(player);
                        }
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }
}