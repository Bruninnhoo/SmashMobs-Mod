package net.brunodev.smashmobs.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;

public record AnvilAnimPayload(UUID playerId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AnvilAnimPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("smashmobs", "anvil_anim"));

    public static final StreamCodec<FriendlyByteBuf, AnvilAnimPayload> STREAM_CODEC = CustomPacketPayload.codec(
            AnvilAnimPayload::write, AnvilAnimPayload::new
    );

    public AnvilAnimPayload(FriendlyByteBuf buffer) {
        this(buffer.readUUID());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerId);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Quando o cliente dos seus amigos receber o aviso, eles rodam a animação!
    public static void handle(AnvilAnimPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.brunodev.smashmobs.client.ClientEvents.playAnvilAnimation(payload.playerId());
        });
    }
}