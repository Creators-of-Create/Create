package com.simibubi.create.foundation.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.inventory.IClearable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class CloneCommand {

	private static final Dynamic2CommandExceptionType CLONE_TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((arg1, arg2) -> new TranslationTextComponent("commands.clone.toobig", arg1, arg2));

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("clone")
				.requires(cs -> cs.hasPermissionLevel(2))
				.then(Commands.argument("begin", BlockPosArgument.blockPos())
						.then(Commands.argument("end", BlockPosArgument.blockPos())
								.then(Commands.argument("destination", BlockPosArgument.blockPos())
										.then(Commands.literal("skipBlocks")
												.executes(ctx -> doClone(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "begin"), BlockPosArgument.getLoadedBlockPos(ctx, "end"), BlockPosArgument.getLoadedBlockPos(ctx, "destination"), false))
										)
										.executes(ctx -> doClone(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "begin"), BlockPosArgument.getLoadedBlockPos(ctx, "end"), BlockPosArgument.getLoadedBlockPos(ctx, "destination"), true))
								)
						)
				)
				.executes(ctx -> {
					ctx.getSource().sendFeedback(new StringTextComponent("Clones all blocks as well as super glue from the specified area to the target destination"), true);

					return Command.SINGLE_SUCCESS;
				});


	}

	private static int doClone(CommandSource source, BlockPos begin, BlockPos end, BlockPos destination, boolean cloneBlocks) throws CommandSyntaxException {
		MutableBoundingBox sourceArea = new MutableBoundingBox(begin, end);
		BlockPos destinationEnd = destination.add(sourceArea.getLength());
		MutableBoundingBox destinationArea = new MutableBoundingBox(destination, destinationEnd);

		int i = sourceArea.getXSize() * sourceArea.getYSize() * sourceArea.getZSize();
		if (i > 32768)
			throw CLONE_TOO_BIG_EXCEPTION.create(32768, i);

		ServerWorld world = source.getWorld();

		if (!world.isAreaLoaded(begin, end) || !world.isAreaLoaded(destination, destinationEnd))
			throw BlockPosArgument.POS_UNLOADED.create();

		BlockPos diffToTarget = new BlockPos(destinationArea.minX - sourceArea.minX, destinationArea.minY - sourceArea.minY, destinationArea.minZ - sourceArea.minZ);

		int blockPastes = cloneBlocks ? cloneBlocks(sourceArea, world, diffToTarget) : 0;
		int gluePastes = cloneGlue(sourceArea, world, diffToTarget);

		if (cloneBlocks)
			source.sendFeedback(new StringTextComponent("Successfully cloned " + blockPastes + " Blocks"), true);

		source.sendFeedback(new StringTextComponent("Successfully applied glue " + gluePastes + " times"), true);
		return blockPastes + gluePastes;

	}

	private static int cloneGlue(MutableBoundingBox sourceArea, ServerWorld world, BlockPos diffToTarget) {
		int gluePastes = 0;

		List<SuperGlueEntity> glue = world.getEntitiesWithinAABB(SuperGlueEntity.class, AxisAlignedBB.func_216363_a(sourceArea));
		List<Pair<BlockPos, Direction>> newGlue = Lists.newArrayList();

		for (SuperGlueEntity g : glue) {
			BlockPos pos = g.getHangingPosition();
			Direction direction = g.getFacingDirection();
			newGlue.add(Pair.of(pos.add(diffToTarget), direction));
		}

		for (Pair<BlockPos, Direction> p : newGlue) {
			SuperGlueEntity g = new SuperGlueEntity(world, p.getFirst(), p.getSecond());
			if (g.onValidSurface()){
				world.addEntity(g);
				gluePastes++;
			}
		}
		return gluePastes;
	}

	private static int cloneBlocks(MutableBoundingBox sourceArea, ServerWorld world, BlockPos diffToTarget) {
		int blockPastes = 0;

		List<Template.BlockInfo> blocks = Lists.newArrayList();
		List<Template.BlockInfo> tileBlocks = Lists.newArrayList();

		for (int z = sourceArea.minZ; z <= sourceArea.maxZ; ++z) {
			for (int y = sourceArea.minY; y <= sourceArea.maxY; ++y) {
				for (int x = sourceArea.minX; x <= sourceArea.maxX; ++x) {
					BlockPos currentPos = new BlockPos(x, y, z);
					BlockPos newPos = currentPos.add(diffToTarget);
					CachedBlockInfo cached = new CachedBlockInfo(world, currentPos, false);
					BlockState state = cached.getBlockState();
					TileEntity te = world.getTileEntity(currentPos);
					if (te != null) {
						CompoundNBT nbt = te.write(new CompoundNBT());
						tileBlocks.add(new Template.BlockInfo(newPos, state, nbt));
					} else {
						blocks.add(new Template.BlockInfo(newPos, state, null));
					}
				}
			}
		}

		List<Template.BlockInfo> allBlocks = Lists.newArrayList();
		allBlocks.addAll(blocks);
		allBlocks.addAll(tileBlocks);

		List<Template.BlockInfo> reverse = Lists.reverse(allBlocks);

		for (Template.BlockInfo info : reverse) {
			TileEntity te = world.getTileEntity(info.pos);
			IClearable.clearObj(te);
			world.setBlockState(info.pos, Blocks.BARRIER.getDefaultState(), 2);
		}

		for (Template.BlockInfo info : allBlocks) {
			if (world.setBlockState(info.pos, info.state, 2))
				blockPastes++;
		}

		for (Template.BlockInfo info : tileBlocks) {
			TileEntity te = world.getTileEntity(info.pos);
			if (te != null && info.nbt != null) {
				info.nbt.putInt("x", info.pos.getX());
				info.nbt.putInt("y", info.pos.getY());
				info.nbt.putInt("z", info.pos.getZ());
				te.read(info.nbt);
				te.markDirty();
			}

			//idk why the state is set twice for a te, but its done like this in the original clone command
			world.setBlockState(info.pos, info.state, 2);
		}

		for (Template.BlockInfo info : reverse) {
			world.notifyNeighbors(info.pos, info.state.getBlock());
		}


		world.getPendingBlockTicks().copyTicks(sourceArea, diffToTarget);

		return blockPastes;
	}

}
