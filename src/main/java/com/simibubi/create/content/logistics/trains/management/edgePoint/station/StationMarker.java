package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import java.util.Objects;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class StationMarker {

	private final BlockPos source;
	private final BlockPos target;
	private final Component name;

	public StationMarker(BlockPos source, BlockPos target, Component name) {
		this.source = source;
		this.target = target;
		this.name = name;
	}

	public static StationMarker load(CompoundTag tag) {
		BlockPos source = NbtUtils.readBlockPos(tag.getCompound("source"));
		BlockPos target = NbtUtils.readBlockPos(tag.getCompound("target"));
		Component name = Component.Serializer.fromJson(tag.getString("name"));
		if (name == null) name = Components.immutableEmpty();

		return new StationMarker(source, target, name);
	}

	public static StationMarker fromWorld(BlockGetter level, BlockPos pos) {
		Optional<StationTileEntity> stationOption = AllTileEntities.TRACK_STATION.get(level, pos);

		if (stationOption.isEmpty() || stationOption.get().getStation() == null)
			return null;

		String name = stationOption.get()
			.getStation().name;
		return new StationMarker(pos, TileEntityBehaviour.get(stationOption.get(), TrackTargetingBehaviour.TYPE)
			.getPositionForMapMarker(), Components.literal(name));
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("source", NbtUtils.writeBlockPos(this.source));
		tag.put("target", NbtUtils.writeBlockPos(this.target));
		tag.putString("name", Component.Serializer.toJson(this.name));

		return tag;
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
		return Objects.hash(this.target, this.name);
	}

	public BlockPos getTarget() {
		return this.target;
	}

	public BlockPos getSource() {
		return this.source;
	}

	public Component getName() {
		return name;
	}

	public MapDecoration.Type getType() {
		return MapDecoration.Type.MANSION;
	}

	public String getId() {
		return "create:station-" + this.target.getX() + "," + this.target.getY() + "," + this.target.getZ();
	}

	public static class Decoration extends MapDecoration {

		public Decoration(byte pX, byte pY, Component pName) {
			super(Type.MANSION, pX, pY, (byte) 0, pName);
		}

		@Override
		public boolean renderOnFrame() {
			return true;
		}

		@Override
		public boolean render(int index) {
			return true;
		}

		public boolean render(PoseStack ms, MultiBufferSource bufferSource, int mapId, MapItemSavedData mapData, boolean active, int packedLight, int index) {
			ms.pushPose();

			ms.translate(getX() / 2D + 64.0, getY() / 2D + 64.0, -0.02D);

			ms.pushPose();

			ms.translate(0.5f, 0f, 0);
			ms.scale(4.5F, 4.5F, 3.0F);

			VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(Create.asResource("textures/gui/station_map_icon.png")));

			float zOffset = -0.001f;
			float alpha = 1f;

			Matrix4f mat = ms.last().pose();
			buffer.vertex(mat, -1, -1, zOffset * index).color(1f, 1f, 1f, alpha).uv(0.0f       , 0.0f       ).uv2(packedLight).endVertex();
			buffer.vertex(mat, -1,  1, zOffset * index).color(1f, 1f, 1f, alpha).uv(0.0f       , 0.0f + 1.0f).uv2(packedLight).endVertex();
			buffer.vertex(mat,  1,  1, zOffset * index).color(1f, 1f, 1f, alpha).uv(0.0f + 1.0f, 0.0f + 1.0f).uv2(packedLight).endVertex();
			buffer.vertex(mat,  1, -1, zOffset * index).color(1f, 1f, 1f, alpha).uv(0.0f + 1.0f, 0.0f       ).uv2(packedLight).endVertex();

			ms.popPose();

			if (getName() != null) {
				Font font = Minecraft.getInstance().font;
				Component component = getName();
				float f6 = (float)font.width(component);
//				float f7 = Mth.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
				ms.pushPose();
				//ms.translate((double)(0.0F + (float)getX() / 2.0F + 64.0F / 2.0F), (double)(0.0F + (float)getY() / 2.0F + 64.0F + 4.0F), (double)-0.025F);
				ms.translate(0, 6.0D, -0.005F);

				ms.scale(0.8f, 0.8f, 1.0F);
				ms.translate(-f6 / 2f + .5f, 0, 0);
				//ms.scale(f7, f7, 1.0F);
				font.drawInBatch(component, 0.0F, 0.0F, -1, false, ms.last().pose(), bufferSource, false, Integer.MIN_VALUE, 15728880);
				ms.popPose();
			}

			ms.popPose();

			return false;
		}
	}
}
