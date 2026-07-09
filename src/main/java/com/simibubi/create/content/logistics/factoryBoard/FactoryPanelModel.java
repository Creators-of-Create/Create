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
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.simibubi.create.foundation.model.BakedQuad;
import com.simibubi.create.foundation.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

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
	}

	private static class FactoryPanelModelData {
		public PanelType type;
		public EnumMap<PanelSlot, PanelState> states = new EnumMap<>(PanelSlot.class);
		private boolean ponder;
	}

}
