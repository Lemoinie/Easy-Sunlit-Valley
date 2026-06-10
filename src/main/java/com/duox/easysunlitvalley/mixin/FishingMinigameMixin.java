package com.duox.easysunlitvalley.mixin;

import com.bonker.stardewfishing.client.FishingMinigame;
import com.duox.easysunlitvalley.config.ESVConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FishingMinigame.class)
public class FishingMinigameMixin {

    @Shadow private double bobberPos;
    @Shadow private double fishPos;
    @Shadow private int barSize;
    @Shadow private double bobberVelocity;
    @Shadow private int maxBobberHeight;
    @Shadow private boolean chestVisible;
    @Shadow private int chestPos;
    @Shadow private float points;
    @Shadow private float chestTimer;

    @Unique private boolean esv$isTargetingChest;

    @Inject(method = "tick(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I"))
    private void onTickLogic(boolean mouseDown, CallbackInfo ci) {
        if (ESVConfig.INSTANCE.easyMinigame.get()) {
            double targetBobberPos = this.fishPos - (this.barSize / 2.0) + 7.0;

            boolean catchTreasureEnabled = ESVConfig.INSTANCE.catchTreasure.get();
            if (catchTreasureEnabled && this.chestVisible && this.chestTimer < 30.0f) {
                double progress = this.points / 120.0;
                if (!this.esv$isTargetingChest && progress > 0.80) this.esv$isTargetingChest = true;
                if (this.esv$isTargetingChest && progress < 0.50) this.esv$isTargetingChest = false;
            } else {
                this.esv$isTargetingChest = false;
            }

            if (this.esv$isTargetingChest) {
                targetBobberPos = this.chestPos + 6.5 - (this.barSize / 2.0);
            }

            this.bobberPos = targetBobberPos;
            this.bobberVelocity = 0;
            if (this.bobberPos > this.maxBobberHeight) this.bobberPos = this.maxBobberHeight;
            else if (this.bobberPos < 0) this.bobberPos = 0;
        }
    }
}
