package com.simibubi.create.foundation.utility;

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.MinecraftForgeClient;

public final class FontHelper {

	private FontHelper() {
	}

	public static List<String> cutString(FontRenderer font, String text, int maxWidthPerLine) {
		// Split words
		List<String> words = new LinkedList<>();
		BreakIterator iterator = BreakIterator.getLineInstance(MinecraftForgeClient.getLocale());
		iterator.setText(text);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String word = text.substring(start, end);
			words.add(word);
		}
		// Apply hard wrap
		List<String> lines = new LinkedList<>();
		StringBuilder currentLine = new StringBuilder();
		int width = 0;
		for (String word : words) {
			int newWidth = font.getStringWidth(word);
			if (width + newWidth > maxWidthPerLine) {
				if (width > 0) {
					String line = currentLine.toString();
					lines.add(line);
					currentLine = new StringBuilder();
					width = 0;
				} else {
					lines.add(word);
					continue;
				}
			}
			currentLine.append(word);
			width += newWidth;
		}
		if (width > 0) {
			lines.add(currentLine.toString());
		}
		return lines;
	}

	public static void drawSplitString(MatrixStack ms, FontRenderer font, String text, int x, int y, int width, int color) {
		List<String> list = cutString(font, text, width);
		Matrix4f matrix4f = ms.peek().getModel();

		for (String s : list) {
			float f = (float) x;
			if (font.getBidiFlag()) {
				int i = font.getStringWidth(font.bidiReorder(s));
				f += (float) (width - i);
			}

			draw(font, s, f, (float) y, color, matrix4f, false);
			y += 9;
		}
	}

	private static int draw(FontRenderer font, String p_228078_1_, float p_228078_2_, float p_228078_3_,
			int p_228078_4_, Matrix4f p_228078_5_, boolean p_228078_6_) {
		if (p_228078_1_ == null) {
			return 0;
		} else {
			IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer
					.immediate(Tessellator.getInstance().getBuffer());
			int i = font.draw(p_228078_1_, p_228078_2_, p_228078_3_, p_228078_4_, p_228078_6_, p_228078_5_,
					irendertypebuffer$impl, false, 0, 15728880);
			irendertypebuffer$impl.draw();
			return i;
		}
	}

}
