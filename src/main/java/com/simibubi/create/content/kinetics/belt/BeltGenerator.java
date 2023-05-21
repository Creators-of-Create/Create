package com.simibubi.create.content.kinetics.belt;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		Direction direction = state.getValue(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = state.getValue(BeltBlock.SLOPE);
		return slope == BeltSlope.VERTICAL ? 90
			: slope == BeltSlope.SIDEWAYS && direction.getAxisDirection() == AxisDirection.NEGATIVE ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Boolean casing = state.getValue(BeltBlock.CASING);
		BeltSlope slope = state.getValue(BeltBlock.SLOPE);

		boolean flip = slope == BeltSlope.UPWARD;
		boolean rotate = casing && slope == BeltSlope.VERTICAL;
		Direction direction = state.getValue(BeltBlock.HORIZONTAL_FACING);
		return horizontalAngle(direction) + (flip ? 180 : 0) + (rotate ? 90 : 0);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		Boolean casing = state.getValue(BeltBlock.CASING);

		if (!casing)
			return prov.models()
				.getExistingFile(prov.modLoc("block/belt/particle"));
		
		BeltPart part = state.getValue(BeltBlock.PART);
		Direction direction = state.getValue(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = state.getValue(BeltBlock.SLOPE);
		boolean downward = slope == BeltSlope.DOWNWARD;
		boolean diagonal = slope == BeltSlope.UPWARD || downward;
		boolean vertical = slope == BeltSlope.VERTICAL;
		boolean pulley = part == BeltPart.PULLEY;
		boolean sideways = slope == BeltSlope.SIDEWAYS;
		boolean negative = direction.getAxisDirection() == AxisDirection.NEGATIVE;

		if (!casing && pulley)
			part = BeltPart.MIDDLE;

		if ((vertical && negative || downward || sideways && negative) && part != BeltPart.MIDDLE && !pulley)
			part = part == BeltPart.END ? BeltPart.START : BeltPart.END;

		if (!casing && vertical)
			slope = BeltSlope.HORIZONTAL;
		if (casing && vertical)
			slope = BeltSlope.SIDEWAYS;

		String path = "block/" + (casing ? "belt_casing/" : "belt/");
		String slopeName = slope.getSerializedName();
		String partName = part.getSerializedName();

		if (diagonal)
			slopeName = "diagonal";

		ResourceLocation location = prov.modLoc(path + slopeName + "_" + partName);
		return prov.models()
			.getExistingFile(location);
	}

}
