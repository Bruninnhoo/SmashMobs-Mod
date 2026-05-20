package net.brunodev.smashmobs.client;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SmashMobs.MODID, value = Dist.CLIENT)
public class SmashHudOverlay {

    private static final Identifier HUD_TEX = Identifier.parse("smashmobs:textures/gui/hud_player.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;

        List<Player> activePlayers = new ArrayList<>();
        boolean isMatchStarted = false;
        
        for (Player p : mc.level.players()) {
            String morph = p.getData(ModAttachments.MORPH_DATA.get());
            if (!"none".equals(morph)) {
                activePlayers.add(p);
            }
            
            // HEURÍSTICA INTELIGENTE: Se alguém na sala tem vidas > 0, a partida já começou!
            // Isso se integra perfeitamente com o seu GameManager que define 3 vidas no Início e 0 no Fim!
            if (p.getData(ModAttachments.PLAYER_LIVES.get()) > 0) {
                isMatchStarted = true;
            }
        }

        // Só exibe o HUD se houver uma partida ATIVA e jogadores selecionados!
        if (!isMatchStarted || activePlayers.isEmpty()) return;

        GuiGraphics gui = event.getGuiGraphics();
        Font font = mc.font;

        int w = gui.guiWidth();
        int h = gui.guiHeight();
        
        float scaleFactor = 0.75F; 
        
        int baseImgW = 162;
        int baseImgH = 67;

        float drawW = baseImgW * scaleFactor;
        float drawH = baseImgH * scaleFactor;
        
        float spacing = drawW + 8.0F;
        float totalWidth = activePlayers.size() * spacing - 8.0F;
        float startX = (w - totalWidth) / 2.0F;
        float baseY = h - drawH - 25.0F;

        for (int i = 0; i < activePlayers.size(); i++) {
            Player p = activePlayers.get(i);
            float x = startX + (i * spacing);
            
            float pct = p.getData(ModAttachments.DAMAGE_PERCENT.get());
            int lives = p.getData(ModAttachments.PLAYER_LIVES.get());
            String morphId = p.getData(ModAttachments.MORPH_DATA.get());

            // ESCOPO ESCALONADO
            gui.pose().pushMatrix();
            gui.pose().translate(x, baseY);
            gui.pose().scale(scaleFactor); 

            // --- 1. CAMADA DE FUNDO ---
            gui.blit(HUD_TEX, 0, 0, baseImgW, baseImgH, 0.0F, 1.0F, 0.0F, 1.0F);

            // --- 2. CAMADA DE TEXTOS (Desenhar ANTES do Item Rendering para evitar bugs de estado de render) ---
            
            // a) Nome do Jogador (Cálculo limpo com Escala 1.3F)
            String name = p.getGameProfile().name();
            if (name == null || name.isEmpty()) name = "Player";
            
            if (name.length() > 14) name = name.substring(0, 12) + "..";
            int nameW = font.width(name);
            float scaledWidth = nameW * 1.3F; // Largura total projetada após escala
            
            int stripCenterX = 65 + (baseImgW - 65) / 2;
            float nameDrawX = stripCenterX - (scaledWidth / 2.0F); // Início perfeito baseado no tamanho final!
            
            gui.pose().pushMatrix();
            gui.pose().translate(nameDrawX, 46.0F); // Altura calibrada
            gui.pose().scale(1.3F); // Aumenta a fonte
            gui.drawString(font, name, 0, 0, 0xFFFFFFFF, true); // Branco OPACO absoluto
            gui.pose().popMatrix();

            // b) Porcentagem Gigante
            if (lives > 0) {
                int pctColor = 0xFFFFFFFF; // CORRIGIDO: 8 DÍGITOS (Branco Opaco)
                if (pct >= 150) pctColor = 0xFFFF2222;
                else if (pct >= 100) pctColor = 0xFFFF9922;
                else if (pct >= 50) pctColor = 0xFFFFFF33;

                int intPart = (int) pct;
                int decPart = (int) ((pct - intPart) * 10);
                String intTxt = String.valueOf(intPart);
                String decTxt = "." + decPart + "%";

                int intWidth = (int) (font.width(intTxt) * 2.0F);
                int decWidth = (int) (font.width(decTxt) * 0.9F);
                int totalTextWidth = intWidth + decWidth;

                gui.pose().pushMatrix();
                float txtX = stripCenterX - (totalTextWidth / 2.0F);
                float txtY = 10;
                
                gui.pose().translate(txtX, txtY);
                gui.pose().pushMatrix();
                gui.pose().scale(2.0F);
                gui.drawString(font, intTxt, 0, 0, pctColor, true);
                gui.pose().popMatrix();

                gui.pose().pushMatrix();
                gui.pose().translate((float) intWidth, 8.0F);
                gui.pose().scale(0.9F);
                gui.drawString(font, decTxt, 0, 0, pctColor, true);
                gui.pose().popMatrix();
                
                gui.pose().popMatrix();
            } else {
                int elimW = font.width("ELIMINADO");
                gui.drawString(font, "ELIMINADO", stripCenterX - (elimW / 2), 18, 0xFFAAAAAA, true);
            }
            
            // c) Vidas
            String vTxt = "❤ x" + lives;
            gui.drawString(font, vTxt, 5, -12, lives > 0 ? 0xFFFF4444 : 0xFF666666, true);

            // --- 3. CAMADA DE ITENS (Renderizar por ÚLTIMO pois altera estados GL/Shader!) ---
            ItemStack iconStack = new ItemStack(getClassIcon(morphId));
            gui.pose().pushMatrix();
            gui.pose().translate(32.0F, 34.0F);
            gui.pose().scale(1.8F); 
            gui.pose().translate(-8.0F, -8.0F);
            gui.renderItem(iconStack, 0, 0);
            gui.pose().popMatrix();

            gui.pose().popMatrix();
        }
    }

    private static Item getClassIcon(String morphId) {
        return switch (morphId) {
            case "minecraft:creeper" -> Items.TNT;
            case "minecraft:chicken" -> Items.FEATHER;
            case "minecraft:iron_golem" -> Items.ANVIL;
            case "minecraft:skeleton" -> Items.BOW;
            case "minecraft:goat" -> Items.GOAT_HORN;
            default -> Items.BARRIER;
        };
    }
}
