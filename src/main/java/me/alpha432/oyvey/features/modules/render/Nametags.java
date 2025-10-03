package me.alpha432.oyvey.features.modules.render;

// 1.21.5で必要なインポートをすべて含む
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat; // これが正しく解決されることを期待

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.awt.Color;

// 依存クラス (あなたのクライアントの既存クラス)
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.Module.Category;
import me.alpha432.oyvey.features.settings.Setting;

public class Nametags extends Module {

    public Setting<Integer> distance = register(new Setting("DistanceChunks", 5, 1, 10));
    public Setting<Float> scale = register(new Setting("Scale", 2.0f, 0.5f, 5.0f));

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public Nametags() {
        super("Nametags", "Displays name and health.", Category.RENDER, true, false, false);
    }

    public void onRender3D() {
        if (fullNullCheck()) return;

        float partialTicks = 0.0f;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player.equals(mc.player) || player.isDead()) continue;

            if (mc.player.distanceTo(player) > distance.getValue() * 16) continue;

            Vec3d interpPos = getInterpolatedPos(player, partialTicks);

            double x = interpPos.getX() - cameraPos.getX();
            double y = interpPos.getY() + player.getHeight() + 0.5 - cameraPos.getY();
            double z = interpPos.getZ() - cameraPos.getZ();

            renderNametag(player, x, y, z);
        }
    }

    private void renderNametag(PlayerEntity player, double finalX, double finalY, double finalZ) {
        MatrixStack matrixStack = new MatrixStack();
        Text name = player.getDisplayName();

        float baseScale = 0.025f;

        matrixStack.push();
        matrixStack.translate(finalX, finalY, finalZ);

        // ビルボーディング
        matrixStack.multiply(mc.getEntityRenderDispatcher().getRotation());

        // スケーリングの調整
        double dist = Math.sqrt(finalX * finalX + finalY * finalY + finalZ * finalZ);
        double finalScale = baseScale * dist;
        float fixedScale = scale.getValue() / 2.0f;
        finalScale = finalScale * fixedScale;

        matrixStack.scale(-((float)finalScale), -((float)finalScale), (float)finalScale);

        // --- 描画ロジック ---
        String healthText = String.format(" §c[%.1f]", player.getHealth() + player.getAbsorptionAmount());
        String displayText = name.getString() + healthText;

        float textWidth = mc.textRenderer.getWidth(displayText);
        float boxWidth = textWidth + 4;

        matrixStack.translate(-boxWidth / 2, 0, 0);

        // 背景の描画
        drawRect(matrixStack, 0, 0, boxWidth, 11, 0x80000000);

        // テキストレンダリング
        matrixStack.push();
        // 1.21.5標準のTextRenderer.draw()を使用
        mc.textRenderer.draw(matrixStack, Text.of(displayText), 2.0F, 1.0F, Color.WHITE.getRGB());
        matrixStack.pop();

        matrixStack.pop();
    }

    // 1.21.5標準の矩形描画メソッド
    private void drawRect(MatrixStack matrices, float x1, float y1, float x2, float y2, int color) {

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        // VertexFormatとVertexFormatsを使用
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // 頂点の定義
        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a).next();

        // 描画の実行
        buffer.end();
        Tessellator.getInstance().draw();
    }

    private Vec3d getInterpolatedPos(PlayerEntity entity, float partialTicks) {
        return new Vec3d(
                entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks,
                entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks,
                entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks
        );
    }
}
