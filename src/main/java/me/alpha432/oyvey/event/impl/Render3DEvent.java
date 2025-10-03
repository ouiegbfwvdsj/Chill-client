package me.alpha432.oyvey.event.impl;

import me.alpha432.oyvey.event.Event; // OyVeyの基本Eventクラスをインポート
import net.minecraft.client.util.math.MatrixStack;

/**
 * 3D空間描画用のカスタムイベント。
 * Render3DモジュールやNametagsモジュールがWorldRendererMixinから呼び出されることを想定。
 */
public class Render3DEvent extends Event {

    // Minecraftの3D描画コンテキストを保持
    private final MatrixStack matrixStack;
    private final float partialTicks;
    private boolean canceled;

    public Render3DEvent(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
