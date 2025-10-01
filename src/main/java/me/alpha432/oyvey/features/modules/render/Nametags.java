package me.alpha432.oyvey.features.modules.render;

// 必要なMinecraftクラスのインポート
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.awt.Color;

// 依存クラス (me.alpha432.oyvey クライアントの既存クラス)
// 【修正】 Render3DEvent のインポートを削除（解決できないため）
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.Module.Category;
import me.alpha432.oyvey.features.settings.Setting;

public class Nametags extends Module {

    private final Setting<Integer> distance = register(new Setting<>("DistanceChunks", 5, 1, 10, "Distance in chunks to render nametags."));
    private final Setting<Float> scale = register(new Setting<>("Scale", 2.0f, 0.5f, 5.0f, "GUI scale factor."));

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public Nametags() {
        super("Nametags", "Displays nameと health.", Category.RENDER, true, false, false);
    }

    // 【修正】 @Override と引数 (Render3DEvent) を削除
    public void onRender3D() {
        if (fullNullCheck()) return;

        float partialTicks = 0.0f;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player.equals(mc.player) || player.isDead()) continue;

            if (mc.player.distanceTo(player) > distance.getValue() * 16) continue;

            Vec3d interpPos = getInterpolatedPos(player, partialTicks);

            double x = interpPos.x - cameraPos.x;
            double y = interpPos.y + player.getHeight() + 0.5 - cameraPos.y;
            double z = interpPos.getZ() - cameraPos.z;

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

        // スケーリング
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

        // 【修正】最も互換性の低い、引数が多いシグネチャを使用（以前のエラーを無視して再試行）
        mc.textRenderer.draw(displayText, 2.0F, 1.0F, Color.WHITE.getRGB(), false);

        matrixStack.pop();

        matrixStack.translate(boxWidth / 2, 0, 0);

        matrixStack.pop();
    }

    // 【修正】 drawRect: レガシーな getBuffer() と startDrawing(7) に戻す
    private void drawRect(MatrixStack matrices, float x1, float y1, float x2, float y2, int color) {

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();

        // getBuffer() を再試行
        BufferBuilder buffer = tessellator.getBuffer();

        // startDrawing(7) を再試行
        buffer.startDrawing(7);

        // 【修正】 .next() を削除
        Matrix4f currentMatrix = matrices.peek().getPositionMatrix();
        buffer.vertex(currentMatrix, x1, y2, 0).color(r, g, b, a);
        buffer.vertex(currentMatrix, x2, y2, 0).color(r, g, b, a);
        buffer.vertex(currentMatrix, x2, y1, 0).color(r, g, b, a);
        buffer.vertex(currentMatrix, x1, y1, 0).color(r, g, b, a);

        // end() を使用
        buffer.end();
    }

    // プレイヤー位置補間ユーティリティ
    private Vec3d getInterpolatedPos(PlayerEntity entity, float partialTicks) {
        // partialTicks は 0.0f の固定値
        return new Vec3d(
                entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks,
                entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks,
                entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks
        );
    }
}