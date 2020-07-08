package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		Direction direction = state.get(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = state.get(BeltBlock.SLOPE);
		return slope == BeltSlope.VERTICAL ? 90
			: slope == BeltSlope.SIDEWAYS && direction.getAxisDirection() == AxisDirection.NEGATIVE ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Boolean casing = state.get(BeltBlock.CASING);
		BeltSlope slope = state.get(BeltBlock.SLOPE);

		boolean flip = casing && slope == BeltSlope.UPWARD;
		boolean rotate = casing && slope == BeltSlope.VERTICAL;
		Direction direction = state.get(BeltBlock.HORIZONTAL_FACING);
		return horizontalAngle(direction) + (flip ? 180 : 0) + (rotate ? 90 : 0);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		Boolean casing = state.get(BeltBlock.CASING);
		BeltPart part = state.get(BeltBlock.PART);
		Direction direction = state.get(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = state.get(BeltBlock.SLOPE);
		boolean downward = slope == BeltSlope.DOWNWARD;
		boolean diagonal = slope == BeltSlope.UPWARD || downward;
		boolean vertical = slope == BeltSlope.VERTICAL;
		boolean pulley = part == BeltPart.PULLEY;
		boolean sideways = slope == BeltSlope.SIDEWAYS;
		boolean negative = direction.getAxisDirection() == AxisDirection.NEGATIVE;

		if (!casing && pulley)
			part = BeltPart.MIDDLE;

		if ((vertical && negative || casing && downward || sideways && negative) && part != BeltPart.MIDDLE && !pulley)
			part = part == BeltPart.END ? BeltPart.START : BeltPart.END;

		if (!casing && vertical)
			slope = BeltSlope.HORIZONTAL;
		if (casing && vertical)
			slope = BeltSlope.SIDEWAYS;

		String path = "block/" + (casing ? "belt_casing/" : "belt/");
		String slopeName = slope.getName();
		String partName = part.getName();

		if (casing && diagonal)
			slopeName = "diagonal";

		ResourceLocation location = prov.modLoc(path + slopeName + "_" + partName);
		return prov.models()
			.getExistingFile(location);
	}

}
