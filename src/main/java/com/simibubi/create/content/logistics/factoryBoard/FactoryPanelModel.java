package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelState;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelType;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.VecHelper;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class FactoryPanelModel extends BakedModelWrapperWithData {

	private static final ModelProperty<FactoryPanelModelData> PANEL_PROPERTY = new ModelProperty<>();

	public FactoryPanelModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		ModelData blockEntityData) {
		FactoryPanelModelData data = new FactoryPanelModelData();
		for (PanelSlot slot : PanelSlot.values()) {
			FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(world, new FactoryPanelPosition(pos, slot));
			if (behaviour == null)
				continue;
			data.states.put(slot, behaviour.count == 0 ? PanelState.PASSIVE : PanelState.ACTIVE);
			data.type = behaviour.panelBE().restocker ? PanelType.PACKAGER : PanelType.NETWORK;
		}
		data.ponder = world instanceof PonderLevel;
		return builder.with(PANEL_PROPERTY, data);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
		RenderType renderType) {
		if (side != null || !data.has(PANEL_PROPERTY))
			return Collections.emptyList();
		FactoryPanelModelData modelData = data.get(PANEL_PROPERTY);
		List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, null, rand, data, renderType));
		for (PanelSlot panelSlot : PanelSlot.values())
			if (modelData.states.containsKey(panelSlot))
				addPanel(quads, state, panelSlot, modelData.type, modelData.states.get(panelSlot), rand, data,
					renderType, modelData.ponder);
		return quads;
	}

	public void addPanel(List<BakedQuad> quads, BlockState state, PanelSlot slot, PanelType type, PanelState panelState,
		RandomSource rand, ModelData data, RenderType renderType, boolean ponder) {
		PartialModel factoryPanel = panelState == PanelState.PASSIVE
			? type == PanelType.NETWORK ? AllPartialModels.FACTORY_PANEL : AllPartialModels.FACTORY_PANEL_RESTOCKER
			: type == PanelType.NETWORK ? AllPartialModels.FACTORY_PANEL_WITH_BULB
				: AllPartialModels.FACTORY_PANEL_RESTOCKER_WITH_BULB;

		List<BakedQuad> quadsToAdd = factoryPanel.get()
			.getQuads(state, null, rand, data, RenderType.solid());

		float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
		float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);

		for (BakedQuad bakedQuad : quadsToAdd) {
			int[] vertices = bakedQuad.getVertices();
			int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

			Vec3 quadNormal = Vec3.atLowerCornerOf(bakedQuad.getDirection()
				.getNormal());
			quadNormal = VecHelper.rotate(quadNormal, 180, Axis.Y);
			quadNormal = VecHelper.rotate(quadNormal, xRot + 90, Axis.X);
			quadNormal = VecHelper.rotate(quadNormal, yRot, Axis.Y);

			for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
				Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);
				Vec3 normal = BakedQuadHelper.getNormalXYZ(vertices, i);

				vertex = vertex.add(slot.xOffset * .5, 0, slot.yOffset * .5);
				vertex = VecHelper.rotateCentered(vertex, 180, Axis.Y);
				vertex = VecHelper.rotateCentered(vertex, xRot + 90, Axis.X);
				vertex = VecHelper.rotateCentered(vertex, yRot, Axis.Y);

				normal = VecHelper.rotate(normal, 180, Axis.Y);
				normal = VecHelper.rotate(normal, xRot + 90, Axis.X);
				normal = VecHelper.rotate(normal, yRot, Axis.Y);

				BakedQuadHelper.setXYZ(transformedVertices, i, vertex);
				BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3(0, 1, 0));
			}

			Direction newNormal = Direction.fromDelta((int) Math.round(quadNormal.x), (int) Math.round(quadNormal.y),
				(int) Math.round(quadNormal.z));
			quads.add(new BakedQuad(transformedVertices, bakedQuad.getTintIndex(), newNormal, bakedQuad.getSprite(),
				!ponder && bakedQuad.isShade()));
		}

	}

	private static class FactoryPanelModelData {
		public PanelType type;
		public EnumMap<PanelSlot, PanelState> states = new EnumMap<>(PanelSlot.class);
		private boolean ponder;
	}

}
