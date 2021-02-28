package com.simibubi.create.foundation.ponder.elements;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.client.renderer.IRenderTypeBuffer;
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
	public void renderFirst(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float pt) {}

	@Override
	public void renderLayer(PonderWorld world, IRenderTypeBuffer buffer, RenderType type, MatrixStack ms, float pt) {}

	@Override
	public void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float pt) {}

}
