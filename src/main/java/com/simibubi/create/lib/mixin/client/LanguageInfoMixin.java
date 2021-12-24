package com.simibubi.create.lib.mixin.client;

import java.util.Locale;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.LanguageInfoExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.LanguageInfo;

@Environment(EnvType.CLIENT)
@Mixin(LanguageInfo.class)
public abstract class LanguageInfoMixin implements LanguageInfoExtensions {
	@Shadow
	@Final
	private String code;
	@Unique
	private Locale create$javaLocale;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void create$addLocale(String string, String string2, String string3, boolean bl, CallbackInfo ci) {
		String[] splitLangCode = code.split("_", 2);
		if (splitLangCode.length == 1) { // Vanilla has some languages without underscores
			this.create$javaLocale = new Locale(code);
		} else {
			this.create$javaLocale = new Locale(splitLangCode[0], splitLangCode[1]);
		}
	}

	@Override
	public Locale create$getJavaLocale() {
		return create$javaLocale;
	}
}
