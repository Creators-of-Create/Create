package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AllIcons implements IScreenRenderable {

	public static final ResourceLocation ICON_ATLAS = Create.asResource("textures/gui/icons.png");
	private static int x = 0, y = -1;
	private int iconX;
	private int iconY;

	public static final AllIcons
		I_ADD = newRow(),
		I_TRASH = next(),
		I_3x3 = next(),
		I_TARGET = next(),
		I_PRIORITY_VERY_LOW = next(),
		I_PRIORITY_LOW = next(),
		I_PRIORITY_HIGH = next(),
		I_PRIORITY_VERY_HIGH = next(),
		I_BLACKLIST = next(),
		I_WHITELIST = next(),
		I_WHITELIST_OR = next(),
		I_WHITELIST_AND = next(),
		I_WHITELIST_NOT = next(),
		I_RESPECT_NBT = next(),
		I_IGNORE_NBT = next();

	public static final AllIcons
		I_CONFIRM = newRow(),
		I_NONE = next(),
		I_OPEN_FOLDER = next(),
		I_REFRESH = next(),
		I_ACTIVE = next(),
		I_PASSIVE = next(),
		I_ROTATE_PLACE = next(),
		I_ROTATE_PLACE_RETURNED = next(),
		I_ROTATE_NEVER_PLACE = next(),
		I_MOVE_PLACE = next(),
		I_MOVE_PLACE_RETURNED = next(),
		I_MOVE_NEVER_PLACE = next(),
		I_CART_ROTATE = next(),
		I_CART_ROTATE_PAUSED = next(),
		I_CART_ROTATE_LOCKED = next();

	public static final AllIcons
		I_DONT_REPLACE = newRow(),
		I_REPLACE_SOLID = next(),
		I_REPLACE_ANY = next(),
		I_REPLACE_EMPTY = next(),
		I_CENTERED = next(),
		I_ATTACHED = next(),
		I_INSERTED = next(),
		I_FILL = next(),
		I_PLACE = next(),
		I_REPLACE = next(),
		I_CLEAR = next(),
		I_OVERLAY = next(),
		I_FLATTEN = next(),
		I_LMB = next(),
		I_SCROLL = next(),
		I_RMB = next();

	public static final AllIcons
		I_TOOL_DEPLOY = newRow(),
		I_SKIP_MISSING = next(),
		I_SKIP_TILES = next(),
		I_DICE = next(),
		I_TUNNEL_SPLIT = next(),
		I_TUNNEL_FORCED_SPLIT = next(),
		I_TUNNEL_ROUND_ROBIN = next(),
		I_TUNNEL_FORCED_ROUND_ROBIN = next(),
		I_TUNNEL_PREFER_NEAREST = next(),
		I_TUNNEL_RANDOMIZE = next(),
		I_TUNNEL_SYNCHRONIZE = next(),

		I_TOOL_MOVE_XZ = newRow(),
		I_TOOL_MOVE_Y = next(),
		I_TOOL_ROTATE = next(),
		I_TOOL_MIRROR = next(),
		I_ARM_ROUND_ROBIN = next(),
		I_ARM_FORCED_ROUND_ROBIN = next(),
		I_ARM_PREFER_FIRST = next(),

		I_ADD_INVERTED_ATTRIBUTE = next(),
		I_FLIP = next(),

		I_PLAY = newRow(),
		I_PAUSE = next(),
		I_STOP = next(),
		I_PLACEMENT_SETTINGS = next(),
		I_ROTATE_CCW = next(),
		I_HOUR_HAND_FIRST = next(),
		I_MINUTE_HAND_FIRST = next(),
		I_HOUR_HAND_FIRST_24 = next(),

		I_PATTERN_SOLID = newRow(),
		I_PATTERN_CHECKERED = next(),
		I_PATTERN_CHECKERED_INVERSED = next(),
		I_PATTERN_CHANCE_25 = next(),

		I_PATTERN_CHANCE_50 = newRow(),
		I_PATTERN_CHANCE_75 = next(),
		I_FOLLOW_DIAGONAL = next(),
		I_FOLLOW_MATERIAL = next(),

		I_SCHEMATIC = newRow(),

		I_MTD_LEFT = newRow(),
		I_MTD_CLOSE = next(),
		I_MTD_RIGHT = next(),
		I_MTD_SCAN = next(),
		I_MTD_REPLAY = next(),
		I_MTD_USER_MODE = next(),
		I_MTD_SLOW_MODE = next(),

		I_CONFIG_UNLOCKED = newRow(),
		I_CONFIG_LOCKED = next(),
		I_CONFIG_DISCARD = next(),
		I_CONFIG_SAVE = next(),
		I_CONFIG_RESET = next(),
		I_CONFIG_BACK = next(),
		I_CONFIG_PREV = next(),
		I_CONFIG_NEXT = next(),
		I_DISABLE = next(),
		I_CONFIG_OPEN = next(),

		I_FX_SURFACE_OFF = newRow(),
		I_FX_SURFACE_ON = next(),
		I_FX_FIELD_OFF = next(),
		I_FX_FIELD_ON = next(),
		I_FX_BLEND = next(),
		I_FX_BLEND_OFF = next();
	;

	public AllIcons(int x, int y) {
		iconX = x * 16;
		iconY = y * 16;
	}

	private static AllIcons next() {
		return new AllIcons(++x, y);
	}

	private static AllIcons newRow() {
		return new AllIcons(x = 0, ++y);
	}

	@OnlyIn(Dist.CLIENT)
	public void bind() {
		Minecraft.getInstance()
			.getTextureManager()
			.bind(ICON_ATLAS);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(MatrixStack matrixStack, AbstractGui screen, int x, int y) {
		bind();
		screen.blit(matrixStack, x, y, iconX, iconY, 16, 16);
	}

	@OnlyIn(Dist.CLIENT)
	public void draw(MatrixStack ms, IRenderTypeBuffer buffer, int color) {
		IVertexBuilder builder = buffer.getBuffer(RenderType.textSeeThrough(ICON_ATLAS));
		float sheetSize = 256;
		int i = 15 << 20 | 15 << 4;
		int j = i >> 16 & '\uffff';
		int k = i & '\uffff';
		Entry peek = ms.last();
		Vector3d rgb = ColorHelper.getRGB(color);

		Vector3d vec4 = new Vector3d(1, 1, 0);
		Vector3d vec3 = new Vector3d(0, 1, 0);
		Vector3d vec2 = new Vector3d(0, 0, 0);
		Vector3d vec1 = new Vector3d(1, 0, 0);

		float u1 = (iconX + 16) / sheetSize;
		float u2 = iconX / sheetSize;
		float v1 = iconY / sheetSize;
		float v2 = (iconY + 16) / sheetSize;

		vertex(peek, builder, j, k, rgb, vec1, u1, v1);
		vertex(peek, builder, j, k, rgb, vec2, u2, v1);
		vertex(peek, builder, j, k, rgb, vec3, u2, v2);
		vertex(peek, builder, j, k, rgb, vec4, u1, v2);
	}

	@OnlyIn(Dist.CLIENT)
	public DelegatedStencilElement asStencil() {
		return new DelegatedStencilElement().withStencilRenderer((ms, w, h, alpha) -> this.draw(ms, 0, 0)).withBounds(16, 16);
	}

	@OnlyIn(Dist.CLIENT)
	private void vertex(Entry peek, IVertexBuilder builder, int j, int k, Vector3d rgb, Vector3d vec, float u, float v) {
		builder.vertex(peek.pose(), (float) vec.x, (float) vec.y, (float) vec.z)
			.color((float) rgb.x, (float) rgb.y, (float) rgb.z, 1)
			.uv(u, v)
			.uv2(j, k)
			.endVertex();
	}

}
