package com.simibubi.create.lib.extensions;

public interface AbstractTextureExtension {
	void setBlurMipmap(boolean blur, boolean mipmap);
	void restoreLastBlurMipmap();
}
