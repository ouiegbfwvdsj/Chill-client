package me.alpha432.oyvey.features.modules.render;

// OyVey Core Imports
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.Module.Category;
import me.alpha432.oyvey.features.settings.Setting;

// Minecraft Imports
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;


public class Nametags extends Module {

    public static final Nametags INSTANCE = new Nametags();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    // --- 設定項目の定義 ---
    public Setting<Float> scale = this.register(new Setting<Float>("Scale",
            1.25f, 0.0f, 5.0f, "Scale of the NameTags"));

    public Setting<Boolean> onlyPlayers = this.register(new Setting<Boolean>("OnlyPlayers",
            false, "Whether Nametags are only enlarged for players."));

    public Setting<Boolean> alwaysVisible = this.register(new Setting<Boolean>("AlwaysVisible",
            false, "Whether Nametags will always be displayed."));


    public Nametags() {
        super("Nametags", "Scales the nametags and makes them visible through walls.", Category.RENDER, true, false, false);
    }

    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (mc.world == null || mc.player == null) return;

        boolean alwaysVisible = this.alwaysVisible.getValue();

        if (alwaysVisible) {
            glDisableDepth();
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (onlyPlayers.getValue() && !(player instanceof PlayerEntity)) continue;
            if (!alwaysVisible && !player.canSee(mc.player)) continue;

            Vec3d pos = player.getPos().add(0, player.getHeight() + 0.5, 0);
            double[] screenPos = worldToScreen(pos.x, pos.y, pos.z);

            if (screenPos == null) continue;

            double x = screenPos[0];
            double y = screenPos[1];

            if (x < 0 || y < 0 || x > mc.getWindow().getScaledWidth() || y > mc.getWindow().getScaledHeight()) {
                continue;
            }

            String text = player.getName().getString() + " §c" + Math.round(player.getHealth());
            int textWidth = mc.textRenderer.getWidth(text);

            matrices.push();
            matrices.translate(x, y, 0);
            matrices.scale(scale.getValue(), scale.getValue(), 1f);

            // ↓↓↓ 修正: 確実にキャストし、6引数形式のプレースホルダーを呼び出す ↓↓↓
            // drawRect(x1, y1, x2, y2, z, color) 形式を想定
            drawRect((int)(-textWidth / 2f - 2),
                    (int)(-mc.textRenderer.fontHeight - 2),
                    (int)(textWidth / 2f + 2),
                    2,
                    0, // Z-indexを0として渡す（6引数に対応）
                    0x80000000);

            // フォント描画: 4引数形式を維持
            drawText(text, -textWidth / 2f, -mc.textRenderer.fontHeight - 1f, 0xFFFFFF);

            matrices.pop();

            renderPlayerItems(matrices, player, (int)x, (int)y);
        }

        if (alwaysVisible) {
            glEnableDepth();
        }
    }

    // --- 【重要】クライアント独自のユーティリティ呼び出しに置き換えるメソッド ---

    private void glDisableDepth() {
        // RenderSystem.disableDepthTest() の代替。例: RenderUtil.disableDepth();
    }

    private void glEnableDepth() {
        // RenderSystem.enableDepthTest() の代替。例: RenderUtil.enableDepth();
    }

    /**
     * 四角形描画 (6つの引数で定義: x1, y1, x2, y2, z, color)
     */
    private void drawRect(int x1, int y1, int x2, int y2, int z, int color) {
        // クライアントの RenderUtil.drawRect(x1, y1, x2, y2, z, color) のようなものを呼び出す必要があります。
        // RenderUtil が存在しない場合は、独自の描画ロジックをここに記述する必要があります。
    }

    /**
     * テキスト描画 (4つの引数で定義: text, x, y, color)
     */
    private void drawText(String text, float x, float y, int color) {
        // mc.textRenderer.drawWithShadow/draw の代替。例：RenderUtil.drawTextWithShadow(text, x, y, color);
    }

    private double[] worldToScreen(double x, double y, double z) {
        // 動作する 3D -> 2D 座標変換ユーティリティの呼び出しに置き換えてください。
        // 例: return me.alpha432.oyvey.util.RenderUtil.worldToScreen(x, y, z);
        return null;
    }

    // --- アイテム描画の骨組み (省略) ---
    private void renderPlayerItems(MatrixStack matrices, PlayerEntity player, int x, int y) {
        int offset = -40;
        ItemStack main = player.getMainHandStack();
        if (!main.isEmpty()) {
            renderUtilItem(matrices, main, x + offset, y + 12);
            offset += 20;
        }
        for (int i = 3; i >= 0; i--) {
            ItemStack armorStack = player.getInventory().getStack(36 + i);
            if (!armorStack.isEmpty()) {
                renderUtilItem(matrices, armorStack, x + offset, y + 12);
                offset += 20;
            }
        }
        ItemStack off = player.getOffHandStack();
        if (!off.isEmpty()) {
            renderUtilItem(matrices, off, x + offset, y + 12);
        }
    }

    private void renderUtilItem(MatrixStack matrices, ItemStack stack, int x, int y) {
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(0.8f, 0.8f, 0.8f);
        // アイテム描画ユーティリティの呼び出しに置き換えてください
        matrices.pop();
    }

    // --- Getterメソッド ---
    public float getNametagScale() { return scale.getValue(); }
    public boolean getPlayersOnly() { return onlyPlayers.getValue(); }
    public boolean getAlwaysVisible() { return alwaysVisible.getValue(); }
}