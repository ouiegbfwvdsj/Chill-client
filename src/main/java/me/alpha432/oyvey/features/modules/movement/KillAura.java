package me.alpha432.oyvey.features.modules.movement;


import me.alpha432.oyvey.features.modules.Module;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {

    private static final float RANGE = 4.5f;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities.", Category.COMBAT, true, false, false);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // 範囲内のLivingEntityのみを取得
        List<LivingEntity> targets = mc.world.getEntitiesByClass(
                LivingEntity.class,
                mc.player.getBoundingBox().expand(RANGE),
                this::isValidTarget
        );

        if (targets.isEmpty()) {
            return;
        }

        // 一番近いターゲットを選択
        targets.sort(Comparator.comparingDouble(entity -> mc.player.distanceTo(entity)));
        LivingEntity target = targets.get(0);

        // クールダウン完了で攻撃
        if (mc.player.getAttackCooldownProgress(0.0f) >= 1.0f) {
            mc.interactionManager.attackEntity(mc.player, target); // ← Fabric 1.20 正しい呼び出し
            mc.player.swingHand(Hand.MAIN_HAND); // 攻撃アニメーション
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == mc.player) return false;       // 自分は攻撃しない
        if (entity.isSpectator()) return false;      // 観戦モード除外
        if (entity.isDead() || entity.getHealth() <= 0) return false; // 死んでるやつ除外
        return true; // モブ・プレイヤー含め有効
    }
}