package com.simibubi.create.foundation.gui;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.element.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AllIcons implements ScreenElement {

	public static final ResourceLocation ICON_ATLAS = Create.asResource("textures/gui/icons.png");
	public static final int ICON_ATLAS_SIZE = 256;

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
		I_SKIP_BLOCK_ENTITIES = next(),
		I_DICE = next(),
		I_TUNNEL_SPLIT = next(),
		I_TUNNEL_FORCED_SPLIT = next(),
		I_TUNNEL_ROUND_ROBIN = next(),
		I_TUNNEL_FORCED_ROUND_ROBIN = next(),
		I_TUNNEL_PREFER_NEAREST = next(),
		I_TUNNEL_RANDOMIZE = next(),
		I_TUNNEL_SYNCHRONIZE = next(),
		I_TOOLBOX = next(),
		I_VIEW_SCHEDULE = next(),

		I_TOOL_MOVE_XZ = newRow(),
		I_TOOL_MOVE_Y = next(),
		I_TOOL_ROTATE = next(),
		I_TOOL_MIRROR = next(),
		I_ARM_ROUND_ROBIN = next(),
		I_ARM_FORCED_ROUND_ROBIN = next(),
		I_ARM_PREFER_FIRST = next(),

		I_ADD_INVERTED_ATTRIBUTE = next(),
		I_FLIP = next(),
		
		I_ROLLER_PAVE = next(),
		I_ROLLER_FILL = next(),
		I_ROLLER_WIDE_FILL = next(),

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
		
		I_CLEAR_CHECKED = next(),

		I_SCHEMATIC = newRow(),
		I_SEQ_REPEAT = next(),
		VALUE_BOX_HOVER_6PX = next(),
		VALUE_BOX_HOVER_4PX = next(),
		VALUE_BOX_HOVER_8PX = next(),

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
		RenderSystem.setShaderTexture(0, ICON_ATLAS);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void render(PoseStack matrixStack, int x, int y) {
		bind();
		GuiGraphics.blit(matrixStack, x, y, 0, iconX, iconY, 16, 16, 256, 256);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack matrixStack, int x, int y, GuiComponent component) {
		bind();
		component.blit(matrixStack, x, y, iconX, iconY, 16, 16);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack ms, MultiBufferSource buffer, int color) {
		VertexConsumer builder = buffer.getBuffer(RenderType.text(ICON_ATLAS));
		Matrix4f matrix = ms.last().pose();
		Color rgb = new Color(color);
		int light = LightTexture.FULL_BRIGHT;

		Vec3 vec1 = new Vec3(0, 0, 0);
		Vec3 vec2 = new Vec3(0, 1, 0);
		Vec3 vec3 = new Vec3(1, 1, 0);
		Vec3 vec4 = new Vec3(1, 0, 0);

		float u1 = iconX * 1f / ICON_ATLAS_SIZE;
		float u2 = (iconX + 16) * 1f / ICON_ATLAS_SIZE;
		float v1 = iconY * 1f / ICON_ATLAS_SIZE;
		float v2 = (iconY + 16) * 1f / ICON_ATLAS_SIZE;

		vertex(builder, matrix, vec1, rgb, u1, v1, light);
		vertex(builder, matrix, vec2, rgb, u1, v2, light);
		vertex(builder, matrix, vec3, rgb, u2, v2, light);
		vertex(builder, matrix, vec4, rgb, u2, v1, light);
	}

	@OnlyIn(Dist.CLIENT)
	private void vertex(VertexConsumer builder, Matrix4f matrix, Vec3 vec, Color rgb, float u, float v, int light) {
		builder.vertex(matrix, (float) vec.x, (float) vec.y, (float) vec.z)
			.color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255)
			.uv(u, v)
			.uv2(light)
			.endVertex();
	}

	@OnlyIn(Dist.CLIENT)
	public DelegatedStencilElement asStencil() {
		return new DelegatedStencilElement().withStencilRenderer((ms, w, h, alpha) -> this.render(ms, 0, 0)).withBounds(16, 16);
	}

}
