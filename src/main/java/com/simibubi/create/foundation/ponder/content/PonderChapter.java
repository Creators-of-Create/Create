package com.simibubi.create.foundation.ponder.content;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.IScreenRenderable;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class PonderChapter implements IScreenRenderable {

	private final String id;
	private final ResourceLocation icon;

	private PonderChapter(String id) {
		this.id = id;
		icon = new ResourceLocation(Create.ID, "textures/ponder/chapter/" + id + ".png");
	}

	@Override
	public void draw(MatrixStack ms, AbstractGui screen, int x, int y) {
		ms.push();
		Minecraft.getInstance().getTextureManager().bindTexture(icon);
		ms.scale(0.25f, 0.25f, 1);
		//x and y offset, blit z offset, tex x and y, tex width and height, entire tex sheet width and height
		AbstractGui.drawTexture(ms, x, y, 0, 0, 0, 64, 64, 64, 64);
		ms.pop();
	}

	@Nonnull
	public static PonderChapter of(String id) {
		PonderChapter chapter = PonderRegistry.chapters.getChapter(id);
		if (chapter == null) {
			 chapter = PonderRegistry.chapters.addChapter(new PonderChapter(id));
		}

		return chapter;
	}

	public PonderChapter addTagsToChapter(PonderTag... tags) {
		for (PonderTag t : tags)
			PonderRegistry.tags.add(t, this);
		return this;
	}

	public String getId() {
		return id;
	}
}
