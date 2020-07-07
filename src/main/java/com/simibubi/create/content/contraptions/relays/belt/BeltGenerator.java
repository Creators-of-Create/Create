package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.content.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock.Slope;
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
		Slope slope = state.get(BeltBlock.SLOPE);
		return slope == Slope.VERTICAL ? 90
			: slope == Slope.SIDEWAYS && direction.getAxisDirection() == AxisDirection.NEGATIVE ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Boolean casing = state.get(BeltBlock.CASING);
		Slope slope = state.get(BeltBlock.SLOPE);

		boolean flip = casing && slope == Slope.UPWARD;
		boolean rotate = casing && slope == Slope.VERTICAL;
		Direction direction = state.get(BeltBlock.HORIZONTAL_FACING);
		return horizontalAngle(direction) + (flip ? 180 : 0) + (rotate ? 90 : 0);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		Boolean casing = state.get(BeltBlock.CASING);
		BeltBlock.Part part = state.get(BeltBlock.PART);
		Direction direction = state.get(BeltBlock.HORIZONTAL_FACING);
		Slope slope = state.get(BeltBlock.SLOPE);
		boolean downward = slope == Slope.DOWNWARD;
		boolean diagonal = slope == Slope.UPWARD || downward;
		boolean vertical = slope == Slope.VERTICAL;
		boolean pulley = part == Part.PULLEY;
		boolean sideways = slope == Slope.SIDEWAYS;
		boolean negative = direction.getAxisDirection() == AxisDirection.NEGATIVE;

		if (!casing && pulley)
			part = Part.MIDDLE;

		if ((vertical && negative || casing && downward || sideways && negative) && part != Part.MIDDLE && !pulley)
			part = part == Part.END ? Part.START : Part.END;

		if (!casing && vertical)
			slope = Slope.HORIZONTAL;
		if (casing && vertical)
			slope = Slope.SIDEWAYS;

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
