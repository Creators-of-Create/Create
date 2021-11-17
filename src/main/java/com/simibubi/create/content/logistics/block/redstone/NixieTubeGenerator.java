//package com.simibubi.create.content.logistics.block.redstone;
//
//import com.simibubi.create.foundation.data.SpecialBlockStateGen;
//import com.tterrag.registrate.providers.DataGenContext;
//import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
//
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.client.model.generators.ModelFile;
//
//public class NixieTubeGenerator extends SpecialBlockStateGen {
//
//	@Override
//	protected int getXRotation(BlockState state) {
//		return state.getValue(NixieTubeBlock.CEILING) ? 180 : 0;
//	}
//
//	@Override
//	protected int getYRotation(BlockState state) {
//		return horizontalAngle(state.getValue(NixieTubeBlock.FACING));
//	}
//
//	@Override
//	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
//		BlockState state) {
//		return prov.models()
//			.withExistingParent(ctx.getName(), prov.modLoc("block/nixie_tube/block"));
//	}
//
//}
