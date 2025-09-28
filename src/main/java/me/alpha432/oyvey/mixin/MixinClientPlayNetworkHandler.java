package me.alpha432.oyvey.mixin;

import me.alpha432.oyvey.OyVey;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import me.alpha432.oyvey.event.impl.ChatEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static me.alpha432.oyvey.util.traits.Util.EVENT_BUS;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessageHook(String content, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(content);
        EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    // ★★★ リスポーン完了フック：モジュール状態の復元 ★★★
    @Inject(method = "onPlayerRespawn", at = @At("RETURN"))
    private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        // リスポーン処理が完了した後、モジュールマネージャーの復元メソッドを呼び出す
        if (OyVey.moduleManager != null) {
            OyVey.moduleManager.restoreModulesAfterDeath();
        }
    } // ← ここに閉じ括弧が必要ですが、お送りいただいたコードでは適切に閉じられています。
}