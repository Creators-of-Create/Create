package com.simibubi.create.lib.extensions;

public interface AbstractTextureExtension {
	void create$setBlurMipmap(boolean blur, boolean mipmap);
	void create$restoreLastBlurMipmap();
}
