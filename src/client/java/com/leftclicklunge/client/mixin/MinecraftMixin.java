package com.leftclicklunge.client.mixin;

import com.leftclicklunge.client.LungeController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	/**
	 * Every real left click ends up here. If the lunge macro wants this click, it does the swap-lunge
	 * and we cancel the normal click. Otherwise the click goes through like normal (so fighting
	 * and mining work as usual whenever the macro is off, or someone is within the safe radius).
	 */
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void leftclicklunge$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
		if (LungeController.handleLeftClick((Minecraft) (Object) this)) {
			cir.setReturnValue(true);
		}
	}

	/**
	 * While holding left click, vanilla would start mining whatever block you are looking at.
	 * Stop that while the macro is active so lunging doesn't also dig with your carrier item.
	 * (require = 0: if this ever fails to apply, the mod still works, you would just also mine.)
	 */
	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true, require = 0)
	private void leftclicklunge$onContinueAttack(boolean leftClick, CallbackInfo ci) {
		if (leftClick && LungeController.shouldBlockMining((Minecraft) (Object) this)) {
			ci.cancel();
		}
	}
}
