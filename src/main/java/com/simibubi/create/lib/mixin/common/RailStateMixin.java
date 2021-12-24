package com.simibubi.create.lib.mixin.common;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.block.SlopeCreationCheckingRail;
import com.simibubi.create.lib.mixin.accessor.RailStateAccessor;
import com.simibubi.create.lib.util.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

@Mixin(value = RailState.class, priority = 1501) // bigger number is applied first right?
//I'm pretty sure lower priorities are applied first but I'm not sure :(
public abstract class RailStateMixin {
	// I hate everything about this file so much
	@Unique
	public boolean create$canMakeSlopes;
	@Final
	@Shadow
	private Level level;
	@Final
	@Shadow
	private BaseRailBlock block;
	@Final
	@Shadow
	private BlockPos pos;
	@Final
	@Shadow
	private boolean isStraight;
	@Shadow
	private BlockState state;
	@Final
	@Shadow
	private List<BlockPos> connections;

	@Shadow
	protected abstract boolean hasNeighborRail(BlockPos blockPos);

	@Shadow
	protected abstract void updateConnections(RailShape railShape);

	@Shadow
	protected abstract @Nullable RailState getRail(BlockPos blockPos);

	@Shadow
	protected abstract boolean hasConnection(BlockPos blockPos);

	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/RailState;updateConnections(Lnet/minecraft/world/level/block/state/properties/RailShape;)V",
					shift = At.Shift.BEFORE
			)
	)
	public void create$RailState(Level world, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		create$canMakeSlopes = true;
		if (block instanceof SlopeCreationCheckingRail) {
			create$canMakeSlopes = ((SlopeCreationCheckingRail) block).canMakeSlopes(blockState, world, pos);
		}
	}

	/**
	 * In the event of catastrophic failure, look here first. <br>
	 * This is hopefully pretty much identical to the original, so ideally this won't conflict.
	 *
	 * @reason I don't see any other way to do a change like this. If you figure out another way, please fix it. <br>
	 * @author Tropheus Jay
	 */
	@Overwrite
	private void connectTo(RailState railState) {
		this.connections.add(((RailStateAccessor) railState).create$getPos());
		BlockPos blockPos = this.pos.north();
		BlockPos blockPos2 = this.pos.south();
		BlockPos blockPos3 = this.pos.west();
		BlockPos blockPos4 = this.pos.east();
		boolean bl = this.hasConnection(blockPos);
		boolean bl2 = this.hasConnection(blockPos2);
		boolean bl3 = this.hasConnection(blockPos3);
		boolean bl4 = this.hasConnection(blockPos4);
		RailShape railShape = null;
		if (bl || bl2) {
			railShape = RailShape.NORTH_SOUTH;
		}

		if (bl3 || bl4) {
			railShape = RailShape.EAST_WEST;
		}

		if (!this.isStraight) {
			if (bl2 && bl4 && !bl && !bl3) {
				railShape = RailShape.SOUTH_EAST;
			}

			if (bl2 && bl3 && !bl && !bl4) {
				railShape = RailShape.SOUTH_WEST;
			}

			if (bl && bl3 && !bl2 && !bl4) {
				railShape = RailShape.NORTH_WEST;
			}

			if (bl && bl4 && !bl2 && !bl3) {
				railShape = RailShape.NORTH_EAST;
			}
		}

		if (railShape == RailShape.NORTH_SOUTH && create$canMakeSlopes) {
			if (BaseRailBlock.isRail(this.level, blockPos.above())) {
				railShape = RailShape.ASCENDING_NORTH;
			}

			if (BaseRailBlock.isRail(this.level, blockPos2.above())) {
				railShape = RailShape.ASCENDING_SOUTH;
			}
		}

		if (railShape == RailShape.EAST_WEST && create$canMakeSlopes) {
			if (BaseRailBlock.isRail(this.level, blockPos4.above())) {
				railShape = RailShape.ASCENDING_EAST;
			}

			if (BaseRailBlock.isRail(this.level, blockPos3.above())) {
				railShape = RailShape.ASCENDING_WEST;
			}
		}

		if (railShape == null) {
			railShape = RailShape.NORTH_SOUTH;
		}

		this.state = this.state.setValue(this.block.getShapeProperty(), railShape);
		this.level.setBlock(this.pos, this.state, 3);
	}

	/**
	 * @reason See method above
	 *
	 * @author Tropheus Jay
	 */
	@Overwrite
	public RailState place(boolean bl, boolean bl2, RailShape railShape) {
		BlockPos blockPos = this.pos.north();
		BlockPos blockPos2 = this.pos.south();
		BlockPos blockPos3 = this.pos.west();
		BlockPos blockPos4 = this.pos.east();
		boolean bl3 = this.hasNeighborRail(blockPos);
		boolean bl4 = this.hasNeighborRail(blockPos2);
		boolean bl5 = this.hasNeighborRail(blockPos3);
		boolean bl6 = this.hasNeighborRail(blockPos4);
		RailShape railShape2 = null;
		boolean bl7 = bl3 || bl4;
		boolean bl8 = bl5 || bl6;
		if (bl7 && !bl8) {
			railShape2 = RailShape.NORTH_SOUTH;
		}

		if (bl8 && !bl7) {
			railShape2 = RailShape.EAST_WEST;
		}

		boolean bl9 = bl4 && bl6;
		boolean bl10 = bl4 && bl5;
		boolean bl11 = bl3 && bl6;
		boolean bl12 = bl3 && bl5;
		if (!this.isStraight) {
			if (bl9 && !bl3 && !bl5) {
				railShape2 = RailShape.SOUTH_EAST;
			}

			if (bl10 && !bl3 && !bl6) {
				railShape2 = RailShape.SOUTH_WEST;
			}

			if (bl12 && !bl4 && !bl6) {
				railShape2 = RailShape.NORTH_WEST;
			}

			if (bl11 && !bl4 && !bl5) {
				railShape2 = RailShape.NORTH_EAST;
			}
		}

		if (railShape2 == null) {
			if (bl7 && bl8) {
				railShape2 = railShape;
			} else if (bl7) {
				railShape2 = RailShape.NORTH_SOUTH;
			} else if (bl8) {
				railShape2 = RailShape.EAST_WEST;
			}

			if (!this.isStraight) {
				if (bl) {
					if (bl9) {
						railShape2 = RailShape.SOUTH_EAST;
					}

					if (bl10) {
						railShape2 = RailShape.SOUTH_WEST;
					}

					if (bl11) {
						railShape2 = RailShape.NORTH_EAST;
					}

					if (bl12) {
						railShape2 = RailShape.NORTH_WEST;
					}
				} else {
					if (bl12) {
						railShape2 = RailShape.NORTH_WEST;
					}

					if (bl11) {
						railShape2 = RailShape.NORTH_EAST;
					}

					if (bl10) {
						railShape2 = RailShape.SOUTH_WEST;
					}

					if (bl9) {
						railShape2 = RailShape.SOUTH_EAST;
					}
				}
			}
		}

		if (railShape2 == RailShape.NORTH_SOUTH && create$canMakeSlopes) {
			if (BaseRailBlock.isRail(this.level, blockPos.above())) {
				railShape2 = RailShape.ASCENDING_NORTH;
			}

			if (BaseRailBlock.isRail(this.level, blockPos2.above())) {
				railShape2 = RailShape.ASCENDING_SOUTH;
			}
		}

		if (railShape2 == RailShape.EAST_WEST && create$canMakeSlopes) {
			if (BaseRailBlock.isRail(this.level, blockPos4.above())) {
				railShape2 = RailShape.ASCENDING_EAST;
			}

			if (BaseRailBlock.isRail(this.level, blockPos3.above())) {
				railShape2 = RailShape.ASCENDING_WEST;
			}
		}

		if (railShape2 == null) {
			railShape2 = railShape;
		}

		this.updateConnections(railShape2);
		this.state = this.state.setValue(this.block.getShapeProperty(), railShape2);
		if (bl2 || this.level.getBlockState(this.pos) != this.state) {
			this.level.setBlock(this.pos, this.state, 3);

			for(int i = 0; i < this.connections.size(); ++i) {
				RailState railState = this.getRail(this.connections.get(i));
				if (railState != null) {
					((RailStateAccessor) railState).create$removeSoftConnections();
					if (((RailStateAccessor) railState).create$canConnectTo(MixinHelper.cast(this))) {
						((RailStateAccessor) railState).create$canConnectTo(MixinHelper.cast(this));
					}
				}
			}
		}

		return MixinHelper.cast(this);
	}
}
