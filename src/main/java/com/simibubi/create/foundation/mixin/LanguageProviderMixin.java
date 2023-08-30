package com.simibubi.create.foundation.mixin;

import java.util.Map;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.data.LanguageProviderExtension;

import net.minecraftforge.common.data.LanguageProvider;

@Mixin(LanguageProvider.class)
public class LanguageProviderMixin implements LanguageProviderExtension {
	@Shadow
	@Final
	@Mutable
	private Map<String, String> data;

	@Unique
	@Nullable
	private UnaryOperator<Map<String, String>> postprocessor;

	@Override
	public void create$addPostprocessor(UnaryOperator<Map<String, String>> postprocessor) {
		if (this.postprocessor == null) {
			this.postprocessor = postprocessor;
		} else {
			UnaryOperator<Map<String, String>> current = this.postprocessor;
			this.postprocessor = entries -> postprocessor.apply(current.apply(entries));
		}
	}

	@Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/data/LanguageProvider;addTranslations()V", shift = Shift.AFTER, remap = false))
	private void create$afterAddTranslations(CallbackInfo ci) {
		if (postprocessor != null) {
			data = postprocessor.apply(data);
		}
	}
}
