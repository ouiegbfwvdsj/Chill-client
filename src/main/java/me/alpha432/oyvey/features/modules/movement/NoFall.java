package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.OyVey;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Prevents all fall damage.", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onTick() {
        if (this.isOn() && OyVey.mc.player != null) {

            if (OyVey.mc.player.fallDistance > 2.5f) {

                // ★★★ PlayerMoveC2SPacket.PositionAndOnGround に5つの引数を渡す ★★★
                // (double x, double y, double z, boolean onGround, boolean changesPosition/rotation/isMoving)

                // ここでは、一般的な 5引数パターン (X, Y, Z, boolean, boolean) を試みます
                // Y座標のわずかな変化を含まないため、最も安全なのは PositionAndOnGround です。

                // 案C: Y座標の変更を含まない OnGroundOnly ではなく、位置を含まないパケットを送信
                // これは PositionAndOnGround が最も近いですが、引数が多いと仮定します。

                // 最終の推測: PositionAndOnGround は (X, Y, Z, boolean onGround, boolean changesPosition) の5引数である
                OyVey.mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(
                                OyVey.mc.player.getX(),
                                OyVey.mc.player.getY() - 0.0001, // わずかな Y 座標のずれ
                                OyVey.mc.player.getZ(),
                                true,  // onGround を true にする
                                false  // 5つ目の引数（通常は isMoving など）を false に設定
                        )
                );

                // クライアント側の落下距離をリセット
                OyVey.mc.player.fallDistance = 0.0f;
            }
        }
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}


