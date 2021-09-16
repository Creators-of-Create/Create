package com.simibubi.create.foundation.ponder.content;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.IScreenRenderable;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public class PonderChapter implements IScreenRenderable {

	private final ResourceLocation id;
	private final ResourceLocation icon;

	private PonderChapter(ResourceLocation id) {
		this.id = id;
		icon = new ResourceLocation(id.getNamespace(), "textures/ponder/chapter/" + id.getPath() + ".png");
	}

	public ResourceLocation getId() {
		return id;
	}

	public String getTitle() {
		return PonderLocalization.getChapter(id);
	}

	public PonderChapter addTagsToChapter(PonderTag... tags) {
		for (PonderTag t : tags)
			PonderRegistry.TAGS.add(t, this);
		return this;
	}

	@Override
	public void draw(PoseStack ms, GuiComponent screen, int x, int y) {
		ms.pushPose();
		Minecraft.getInstance().getTextureManager().bind(icon);
		ms.scale(0.25f, 0.25f, 1);
		//x and y offset, blit z offset, tex x and y, tex width and height, entire tex sheet width and height
		GuiComponent.blit(ms, x, y, 0, 0, 0, 64, 64, 64, 64);
		ms.popPose();
	}

	@Nonnull
	public static PonderChapter of(ResourceLocation id) {
		PonderChapter chapter = PonderRegistry.CHAPTERS.getChapter(id);
		if (chapter == null) {
			 chapter = PonderRegistry.CHAPTERS.addChapter(new PonderChapter(id));
		}

		return chapter;
	}
}
