package com.simibubi.create.lib.mixin.client;

import com.simibubi.create.lib.extensions.AbstractTextureExtension;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.texture.AbstractTexture;

@Mixin(AbstractTexture.class)
public abstract class AbstractTextureMixin implements AbstractTextureExtension {
	@Shadow
	public abstract void setFilter(boolean blur, boolean mipmap);

	@Shadow
	protected boolean mipmap;
	@Shadow
	protected boolean blur;
	@Unique
	private boolean lastBlur;
	@Unique
	private boolean lastMipmap;

	@Unique
	@Override
	public void setBlurMipmap(boolean blur, boolean mipmap) {
		this.lastBlur = this.blur;
		this.lastMipmap = this.mipmap;
		setFilter(blur, mipmap);
	}

	@Unique
	@Override
	public void restoreLastBlurMipmap() {
		setFilter(this.lastBlur, this.lastMipmap);
	}
}
