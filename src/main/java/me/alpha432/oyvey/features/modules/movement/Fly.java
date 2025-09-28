package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
// ★★★ ここを修正: settings フォルダーへの正しいインポートパス ★★★
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.features.modules.Module.Category;

public class Fly extends Module {

    // Setting のクラス名は正しい (大文字 S)
    public Setting<Float> speed = this.register(new Setting<Float>("Speed", 0.1f, 0.01f, 1.0f));

    public Fly() {
        super("Fly", "Allows you to fly in survival mode.", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.getAbilities().allowFlying = true;
            // setFlying() のエラー回避のため、フィールドに直接代入
            mc.player.getAbilities().flying = true;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player != null && mc.player.getAbilities().flying) {
            // getValue() を呼び出す
            mc.player.getAbilities().setFlySpeed(this.speed.getValue());
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            // 速度をデフォルト値にリセット
            mc.player.getAbilities().setFlySpeed(0.05f);

            // 飛行を停止
            mc.player.getAbilities().flying = false;

            // クリエイティブモード/スペクテイターモードでない場合のみ、飛行能力を無効にリセット
            if (!mc.player.isCreative() && !mc.player.isSpectator()) {
                mc.player.getAbilities().allowFlying = false;
            }
        }
    }
}