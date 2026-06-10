package com.duox.easysunlitvalley.mixin;

import com.bonker.stardewfishing.client.FishingMinigame;
import com.bonker.stardewfishing.client.FishingScreen;
import com.duox.easysunlitvalley.config.ESVConfig;
import com.duox.easysunlitvalley.fishing.EasyFishing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FishingScreen.class)
public abstract class EasyFishingMixin {

    @Shadow private FishingMinigame minigame;
    @Shadow public abstract void setInputDown(boolean down);

    @Unique private boolean esv$isTargetingChest;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (!ESVConfig.INSTANCE.easyMinigame.get() || minigame == null) return;

        float fishPos = minigame.getFishPos();
        float bobberPos = minigame.getBobberPos();
        int barSize = minigame.getBarSize();
        float barCenter = bobberPos + (barSize / 2.0f);
        float fishCenter = fishPos + 8.0f;
        float targetCenter = fishCenter;

        boolean catchTreasureEnabled = ESVConfig.INSTANCE.catchTreasure.get();
        if (catchTreasureEnabled && minigame.isChestVisible() && minigame.getChestProgress() < 1.0f) {
            float progress = minigame.getProgress();
            if (!this.esv$isTargetingChest && progress > 0.80f) this.esv$isTargetingChest = true;
            if (this.esv$isTargetingChest && progress < 0.50f) this.esv$isTargetingChest = false;
        } else {
            this.esv$isTargetingChest = false;
        }

        if (this.esv$isTargetingChest) {
            float chestCenter = minigame.getChestPos() + 6.5f;
            targetCenter = chestCenter;
        }

        float delta = targetCenter - barCenter;
        if (Math.abs(delta) < 3.0f) return;
        this.setInputDown(delta > 0);
    }

    @Inject(method = "setResult(ZDZZ)V", at = @At("HEAD"), remap = false)
    private void onSetResult(boolean success, double accuracy, boolean gotChest, boolean goldenChest, CallbackInfo ci) {
        if (success) EasyFishing.caughtCount++;
    }
}
