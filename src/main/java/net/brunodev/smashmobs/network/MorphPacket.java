package net.brunodev.smashmobs.network;

import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
// O import correto da classe nova:
import net.minecraft.resources.Identifier;

public record MorphPacket(String mobType) implements CustomPacketPayload {

    // A MÁGICA FINAL: Usando a classe nova (Identifier) com o método correto (fromNamespaceAndPath)
    public static final CustomPacketPayload.Type<MorphPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SmashMobs.MODID, "morph_packet"));

    public static final StreamCodec<FriendlyByteBuf, MorphPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MorphPacket::mobType,
            MorphPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}