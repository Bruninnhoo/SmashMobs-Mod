package net.brunodev.smashmobs.registration;
 
import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.mojang.serialization.Codec;
 
import java.util.function.Supplier;
 
public class ModAttachments {
 
        public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
                        .create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SmashMobs.MODID);
 
        public static final Supplier<AttachmentType<String>> MORPH_DATA = ATTACHMENT_TYPES.register(
                        "morph_data", () -> AttachmentType.builder(() -> "none")
                                        .serialize(Codec.STRING.fieldOf("id"), s -> true)
                                        .copyOnDeath()
                                        .sync(ByteBufCodecs.STRING_UTF8)
                                        .build());
 
        public static final Supplier<AttachmentType<Float>> DAMAGE_PERCENT = ATTACHMENT_TYPES.register(
                        "damage_percent", () -> AttachmentType.builder(() -> 0.0f)
                                        .serialize(Codec.FLOAT.fieldOf("value"), f -> true)
                                        .copyOnDeath()
                                        .sync(ByteBufCodecs.FLOAT)
                                        .build());
 
        public static final Supplier<AttachmentType<Integer>> PLAYER_LIVES = ATTACHMENT_TYPES.register(
                        "player_lives", () -> AttachmentType.builder(() -> 0) 
                                        .serialize(Codec.INT.fieldOf("value"), i -> true)
                                        .copyOnDeath()
                                        .sync(ByteBufCodecs.INT)
                                        .build());
}
