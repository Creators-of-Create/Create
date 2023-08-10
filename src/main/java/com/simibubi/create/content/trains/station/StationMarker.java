package com.simibubi.create.content.trains.station;

import java.util.Objects;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.map.CustomRenderedMapDecoration;

import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class StationMarker {
	// Not MANSION or MONUMENT to allow map extending
	public static final MapDecoration.Type TYPE = MapDecoration.Type.RED_MARKER;

	private final BlockPos source;
	private final BlockPos target;
	private final Component name;
	private final String id;

	public StationMarker(BlockPos source, BlockPos target, Component name) {
		this.source = source;
		this.target = target;
		this.name = name;
		id = "create:station-" + target.getX() + "," + target.getY() + "," + target.getZ();
	}

	public static StationMarker load(CompoundTag tag) {
		BlockPos source = NbtUtils.readBlockPos(tag.getCompound("source"));
		BlockPos target = NbtUtils.readBlockPos(tag.getCompound("target"));
		Component name = Component.Serializer.fromJson(tag.getString("name"));
		if (name == null) name = Components.immutableEmpty();

		return new StationMarker(source, target, name);
	}

	public static StationMarker fromWorld(BlockGetter level, BlockPos pos) {
		Optional<StationBlockEntity> stationOption = AllBlockEntityTypes.TRACK_STATION.get(level, pos);

		if (stationOption.isEmpty() || stationOption.get().getStation() == null)
			return null;

		String name = stationOption.get()
			.getStation().name;
		return new StationMarker(pos, BlockEntityBehaviour.get(stationOption.get(), TrackTargetingBehaviour.TYPE)
			.getPositionForMapMarker(), Components.literal(name));
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("source", NbtUtils.writeBlockPos(source));
		tag.put("target", NbtUtils.writeBlockPos(target));
		tag.putString("name", Component.Serializer.toJson(name));

		return tag;
	}

	public BlockPos getSource() {
		return source;
	}

	public BlockPos getTarget() {
		return target;
	}

	public Component getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StationMarker that = (StationMarker) o;

		if (!target.equals(that.target)) return false;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(target, name);
	}

	public static class Decoration extends MapDecoration implements CustomRenderedMapDecoration {
		private static final ResourceLocation TEXTURE = Create.asResource("textures/gui/station_map_icon.png");

		public Decoration(byte x, byte y, Component name) {
			super(TYPE, x, y, (byte) 0, name);
		}

		public static Decoration from(MapDecoration decoration) {
			return new StationMarker.Decoration(decoration.getX(), decoration.getY(), decoration.getName());
		}

		@Override
		public boolean renderOnFrame() {
			return true;
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight, MapItemSavedData mapData, int index) {
			poseStack.pushPose();

			poseStack.translate(getX() / 2D + 64.0, getY() / 2D + 64.0, -0.02D);

			poseStack.pushPose();

			poseStack.translate(0.5f, 0f, 0);
			poseStack.scale(4.5F, 4.5F, 3.0F);

			VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(TEXTURE));
			Matrix4f mat = poseStack.last().pose();
			float zOffset = -0.001f;
			buffer.vertex(mat, -1, -1, zOffset * index).color(255, 255, 255, 255).uv(0.0f		, 0.0f		 ).uv2(packedLight).endVertex();
			buffer.vertex(mat, -1,  1, zOffset * index).color(255, 255, 255, 255).uv(0.0f		, 0.0f + 1.0f).uv2(packedLight).endVertex();
			buffer.vertex(mat,  1,  1, zOffset * index).color(255, 255, 255, 255).uv(0.0f + 1.0f, 0.0f + 1.0f).uv2(packedLight).endVertex();
			buffer.vertex(mat,  1, -1, zOffset * index).color(255, 255, 255, 255).uv(0.0f + 1.0f, 0.0f		 ).uv2(packedLight).endVertex();

			poseStack.popPose();

			if (getName() != null) {
				Font font = Minecraft.getInstance().font;
				Component component = getName();
				float f6 = (float)font.width(component);
//				float f7 = Mth.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
				poseStack.pushPose();
//				poseStack.translate((double)(0.0F + (float)getX() / 2.0F + 64.0F / 2.0F), (double)(0.0F + (float)getY() / 2.0F + 64.0F + 4.0F), (double)-0.025F);
				poseStack.translate(0, 6.0D, -0.005F);

				poseStack.scale(0.8f, 0.8f, 1.0F);
				poseStack.translate(-f6 / 2f + .5f, 0, 0);
//				poseStack.scale(f7, f7, 1.0F);
				font.drawInBatch(component, 0.0F, 0.0F, -1, false, poseStack.last().pose(), bufferSource, false, Integer.MIN_VALUE, 15728880);
				poseStack.popPose();
			}

			poseStack.popPose();
		}

		@Override
		public boolean render(int index) {
			return true;
		}
	}
}
