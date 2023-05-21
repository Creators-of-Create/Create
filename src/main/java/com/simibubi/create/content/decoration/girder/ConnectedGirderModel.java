package com.simibubi.create.content.decoration.girder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class ConnectedGirderModel extends CTModel {

	protected static final ModelProperty<ConnectionData> CONNECTION_PROPERTY = new ModelProperty<>();

	public ConnectedGirderModel(BakedModel originalModel) {
		super(originalModel, new GirderCTBehaviour());
	}

	@Override
	protected void gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		IModelData blockEntityData) {
		super.gatherModelData(builder, world, pos, state, blockEntityData);
		ConnectionData connectionData = new ConnectionData();
		for (Direction d : Iterate.horizontalDirections)
			connectionData.setConnected(d, GirderBlock.isConnected(world, pos, state, d));
		builder.withInitial(CONNECTION_PROPERTY, connectionData);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		List<BakedQuad> superQuads = super.getQuads(state, side, rand, extraData);
		if (side != null || !extraData.hasProperty(CONNECTION_PROPERTY))
			return superQuads;
		List<BakedQuad> quads = new ArrayList<>(superQuads);
		ConnectionData data = extraData.getData(CONNECTION_PROPERTY);
		for (Direction d : Iterate.horizontalDirections)
			if (data.isConnected(d))
				quads.addAll(AllPartialModels.METAL_GIRDER_BRACKETS.get(d)
					.get()
					.getQuads(state, side, rand, extraData));
		return quads;
	}

	private static class ConnectionData {
		boolean[] connectedFaces;

		public ConnectionData() {
			connectedFaces = new boolean[4];
			Arrays.fill(connectedFaces, false);
		}

		void setConnected(Direction face, boolean connected) {
			connectedFaces[face.get2DDataValue()] = connected;
		}

		boolean isConnected(Direction face) {
			return connectedFaces[face.get2DDataValue()];
		}
	}

}
