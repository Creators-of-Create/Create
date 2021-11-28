package com.simibubi.create.content.palettes;

import java.util.Map;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ConnectedOreCTBehaviour extends ConnectedTextureBehaviour {

	private CTSpriteShiftEntry entry;

	public ConnectedOreCTBehaviour(CTSpriteShiftEntry entry) {
		this.entry = entry;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {

		if (state.getBlock() instanceof ConnectedSixwayBlock)
			return true;

		ConnectedSixwayBlock block = (ConnectedSixwayBlock) state.getBlock();
		Map<Direction, BooleanProperty> props = ConnectedSixwayBlock.PROPERTY_BY_DIRECTION;
		BooleanProperty faceProp = props.get(face);

		// Diagonal Connection
		if (secondaryOffset != null && primaryOffset != null) {
			BooleanProperty primaryProp = props.get(primaryOffset);
			BooleanProperty secondaryProp = props.get(secondaryOffset);
			BooleanProperty primaryOppositeProp = props.get(primaryOffset.getOpposite());
			BooleanProperty secondaryOppositeProp = props.get(secondaryOffset.getOpposite());
			BooleanProperty faceOppositeProp = props.get(face.getOpposite());

			BlockState adjAbove1 = reader.getBlockState(pos.relative(primaryOffset)
				.relative(face));
			BlockState adjAbove2 = reader.getBlockState(pos.relative(secondaryOffset)
				.relative(face));

			boolean adjAbove1Connects = block.connectsToState(adjAbove1);
			boolean adjAbove2Connects = block.connectsToState(adjAbove2);

			if (adjAbove1Connects && adjAbove2Connects)
				return true;

			if (isBeingBlocked(state, reader, pos, otherPos, face))
				return false;
			if (adjAbove1Connects && !(adjAbove1.getValue(secondaryProp)))
				return false;
			if (adjAbove2Connects && !(adjAbove2.getValue(primaryProp)))
				return false;

			BlockState diagAbove = reader.getBlockState(pos.relative(primaryOffset)
				.relative(secondaryOffset)
				.relative(face));
			boolean diagAboveConnects = block.connectsToState(diagAbove);
			if (diagAboveConnects
				&& (!diagAbove.getValue(primaryOppositeProp) || !diagAbove.getValue(secondaryOppositeProp)))
				return false;

			BlockState diagSameY = reader.getBlockState(pos.relative(primaryOffset)
				.relative(secondaryOffset));
			boolean diagSameYConnects = block.connectsToState(diagSameY);

			BlockState adj1 = reader.getBlockState(pos.relative(primaryOffset));
			BlockState adj2 = reader.getBlockState(pos.relative(secondaryOffset));

			boolean adj1Connects = block.connectsToState(adj1);
			boolean adj2Connects = block.connectsToState(adj2);
			boolean coveredDiag = diagAboveConnects || diagSameYConnects;
			boolean covered1 = adj1Connects || adjAbove1Connects;
			boolean covered2 = adj2Connects || adjAbove2Connects;
			boolean fullCover = covered1 && covered2 && coveredDiag;

			if (!fullCover && block.disconnectsFromState(diagSameY))
				return false;
			if (!fullCover && diagAboveConnects && !diagAbove.getValue(faceOppositeProp))
				return false;
			if (!(fullCover || diagSameYConnects) && adjAbove1Connects && !adjAbove1.getValue(faceOppositeProp))
				return false;
			if (!(fullCover || diagSameYConnects) && adjAbove2Connects && !adjAbove2.getValue(faceOppositeProp))
				return false;

			if (diagSameYConnects)
				return diagSameY.getValue(faceProp) && (adjAbove1Connects || diagSameY.getValue(secondaryOppositeProp))
					&& (adjAbove2Connects || diagSameY.getValue(primaryOppositeProp));

			if (!fullCover && adj1Connects && !adj1.getValue(secondaryProp))
				return false;
			if (!fullCover && adj2Connects && !adj2.getValue(primaryProp))
				return false;
			if (!fullCover && !state.getValue(primaryProp))
				return false;
			if (!fullCover && !state.getValue(secondaryProp))
				return false;

			return true;
		}

		// Orthogonal Connection
		else if (secondaryOffset != null || primaryOffset != null) {

			if (isBeingBlocked(state, reader, pos, otherPos, face))
				return false;

			Direction offset = secondaryOffset == null ? primaryOffset : secondaryOffset;
			BlockState checkedState = reader.getBlockState(pos.relative(offset)
				.relative(face));
			if (block.connectsToState(checkedState))
				return checkedState.getValue(props.get(offset.getOpposite()));
			checkedState = reader.getBlockState(pos.relative(offset));
			if (block.connectsToState(checkedState))
				return checkedState.getValue(faceProp);
			return state.getValue(props.get(offset));
		}

		return false;
	}

	@Override
	protected boolean isBeingBlocked(BlockState state, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		ConnectedSixwayBlock block = (ConnectedSixwayBlock) state.getBlock();
		BlockState blockingState = reader.getBlockState(otherPos.relative(face));
		return block.disconnectsFromState(blockingState);
	}

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		return state.getValue(ConnectedSixwayBlock.PROPERTY_BY_DIRECTION.get(direction)) ? entry : null;
	}

}
