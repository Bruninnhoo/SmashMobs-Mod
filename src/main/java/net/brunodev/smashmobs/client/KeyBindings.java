package net.brunodev.smashmobs.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.brunodev.smashmobs.SmashMobs;
import net.neoforged.api.distmarker.Dist;

// O SEGREDO 1: bus = Bus.MOD (Para carregar antes do jogo abrir)
@EventBusSubscriber(modid = SmashMobs.MODID, value = Dist.CLIENT)
public class KeyBindings {

    public static final KeyMapping MORPH_MENU_KEY = new KeyMapping(
            "key.smashmobs.morph_menu",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_K, // O SEGREDO 2: O código certo da Tecla K
            KeyMapping.Category.register(Identifier.parse("key.categories.smashmobs")) // O SEGREDO 3: A sua categoria
                                                                                       // que cria o objeto correto
                                                                                       // (pode ignorar o Deprecated!)
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(MORPH_MENU_KEY);
    }
}