package com.simibubi.create.foundation.ponder.element;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public abstract class TrackedElement<T> extends PonderSceneElement {

	private WeakReference<T> reference;

	public TrackedElement(T wrapped) {
		this.reference = new WeakReference<>(wrapped);
	}

	public void ifPresent(Consumer<T> func) {
		if (reference == null)
			return;
		T resolved = reference.get();
		if (resolved == null)
			return;
		func.accept(resolved);
	}
	
	protected boolean isStillValid(T element) { 
		return true;
	}
	
	@Override
	public void renderFirst(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float pt) {}

	@Override
	public void renderLayer(PonderWorld world, MultiBufferSource buffer, RenderType type, PoseStack ms, float pt) {}

	@Override
	public void renderLast(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float pt) {}

}
