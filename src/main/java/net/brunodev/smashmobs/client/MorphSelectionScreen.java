package net.brunodev.smashmobs.client;

import net.brunodev.smashmobs.network.MorphPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MorphSelectionScreen extends Screen {

    private Button creeperButton;
    private Button ironGolemButton;
    private Button goatButton;

    public MorphSelectionScreen() {
        super(Component.literal("Selecione seu Mob"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // --- BOTÃO DO CREEPER ---
        this.creeperButton = Button.builder(Component.empty(), (btn) -> {
                    if (net.minecraft.client.Minecraft.getInstance().getConnection() != null) {
                        net.minecraft.client.Minecraft.getInstance().getConnection().send(
                                new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new MorphPacket("creeper"))
                        );
                    }
                    this.onClose();
                })
                .bounds(centerX - 45, centerY - 20, 40, 40)
                .tooltip(Tooltip.create(Component.literal("CREEPER")))
                .build();

        // A MÁGICA 1: Tira a textura feia do Minecraft deixando o botão invisível!
        this.creeperButton.setAlpha(0.0F);
        this.addRenderableWidget(creeperButton);

        // --- BOTÃO DO IRON GOLEM ---
        this.ironGolemButton = Button.builder(Component.empty(), (btn) -> {
                    if (net.minecraft.client.Minecraft.getInstance().getConnection() != null) {
                        net.minecraft.client.Minecraft.getInstance().getConnection().send(
                                new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new MorphPacket("iron_golem"))
                        );
                    }
                    this.onClose();
                })
                .bounds(centerX + 5, centerY - 20, 40, 40)
                .tooltip(Tooltip.create(Component.literal("IRON GOLEM")))
                .build();

        // Deixa invisível também
        this.ironGolemButton.setAlpha(0.0F);
        this.addRenderableWidget(ironGolemButton);

        // --- BOTÃO GOAT ---
        this.goatButton = Button.builder(Component.empty(), (btn) -> {
                    if (net.minecraft.client.Minecraft.getInstance().getConnection() != null) {
                        net.minecraft.client.Minecraft.getInstance().getConnection().send(
                                new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(new MorphPacket("goat"))
                        );
                    }
                    this.onClose();
                })
                .bounds(centerX + 30, centerY - 20, 40, 40) // <--- MUDOU AQUI
                .tooltip(Tooltip.create(Component.literal("GOAT")))
                .build();
        this.goatButton.setAlpha(0.0F);
        this.addRenderableWidget(goatButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Pede pro Minecraft processar os nossos botões invisíveis
        super.render(graphics, mouseX, mouseY, partialTick);

        // ==========================================================
        // A MÁGICA 2: Desenhamos o nosso próprio estilo Smash Bros!
        // ==========================================================

        // --- VISUAL DO CREEPER ---
        int cx = this.creeperButton.getX();
        int cy = this.creeperButton.getY();

        if (this.creeperButton.isHovered()) {
            // Efeito Brilhante de Seleção: Borda Branca + Fundo Verde Transparente
            graphics.fill(cx - 2, cy - 2, cx + 42, cy + 42, 0xFFFFFFFF); // Desenha uma borda maior por trás
            graphics.fill(cx, cy, cx + 40, cy + 40, 0xAA22CC22);         // Desenha o fundo verde por cima
        } else {
            // Efeito Normal: Um bloco de vidro escuro e estiloso
            graphics.fill(cx, cy, cx + 40, cy + 40, 0xAA000000);
        }

        // Coloca a cabeça em cima da pintura
        graphics.renderItem(new ItemStack(Items.CREEPER_HEAD), cx + 12, cy + 12);


        // --- VISUAL DO IRON GOLEM ---
        int ix = this.ironGolemButton.getX();
        int iy = this.ironGolemButton.getY();

        if (this.ironGolemButton.isHovered()) {
            // Efeito Brilhante de Seleção: Borda Branca + Fundo Cinza/Azulado Transparente
            graphics.fill(ix - 2, iy - 2, ix + 42, iy + 42, 0xFFFFFFFF);
            graphics.fill(ix, iy, ix + 40, iy + 40, 0xAA8888AA);
        } else {
            // Efeito Normal: Vidro escuro
            graphics.fill(ix, iy, ix + 40, iy + 40, 0xAA000000);
        }

        graphics.renderItem(new ItemStack(Items.IRON_BLOCK), ix + 12, iy + 12);

        // --- VISUAL GOAT ---
        int gx = this.goatButton.getX();
        int gy = this.goatButton.getY();

        if (this.goatButton.isHovered()) {
            // Efeito Brilhante de Seleção: Borda Branca + Fundo Cinza/Azulado Transparente
            graphics.fill(gx - 2, gy - 2, gx + 42, gy + 42, 0xFFFFFFFF);
            graphics.fill(gx, gy, gx + 40, gy + 40, 0xAAAA7744);
        } else {
            // Efeito Normal: Vidro escuro
            graphics.fill(gx, gy, gx + 40, gy + 40, 0xAA000000);
        }

        graphics.renderItem(new ItemStack(Items.GOAT_HORN), gx + 12, gy + 12);
    }
}