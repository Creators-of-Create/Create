package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.lib.extensions.AbstractTextureExtension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;

@Environment(EnvType.CLIENT)
@Mixin(AbstractTexture.class)
public abstract class AbstractTextureMixin implements AbstractTextureExtension {
	@Shadow
	protected boolean blur;
	@Shadow
	protected boolean mipmap;
	@Unique
	private boolean create$lastBlur;
	@Unique
	private boolean create$lastMipmap;

	@Shadow
	public abstract void setFilter(boolean blur, boolean mipmap);

	@Unique
	@Override
	public void create$setBlurMipmap(boolean blur, boolean mipmap) {
		this.create$lastBlur = this.blur;
		this.create$lastMipmap = this.mipmap;
		setFilter(blur, mipmap);
	}

	@Unique
	@Override
	public void create$restoreLastBlurMipmap() {
		setFilter(this.create$lastBlur, this.create$lastMipmap);
	}
}
