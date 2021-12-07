package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.SuperByteBuffer;

public abstract class CTSpriteShiftEntry extends SpriteShiftEntry {

	protected int textureSheetSize;

	public CTSpriteShiftEntry(int sheetSize) {
		this.textureSheetSize = sheetSize;
	}

	public float getTargetU(float localU, int index) {
		float uOffset = (index % textureSheetSize);
		return getTarget().getU(
			(SuperByteBuffer.getUnInterpolatedU(getOriginal(), localU) + (uOffset * 16)) / ((float) textureSheetSize));
	}

	public float getTargetV(float localV, int index) {
		float vOffset = (index / textureSheetSize);
		return getTarget().getV(
			(SuperByteBuffer.getUnInterpolatedV(getOriginal(), localV) + (vOffset * 16)) / ((float) textureSheetSize));
	}

	public abstract int getTextureIndex(CTContext context);

	public abstract CTType getType();

	public static class Horizontal extends CTSpriteShiftEntry {

		// Different sheet arrangement
		private boolean kryppers;

		public Horizontal(boolean kryppers) {
			super(2);
			this.kryppers = kryppers;
		}

		@Override
		public int getTextureIndex(CTContext context) {
			if (kryppers)
				return !context.right && !context.left ? 0 : !context.right ? 3 : !context.left ? 2 : 1;
			return (context.right ? 1 : 0) + (context.left ? 2 : 0);
		}

		@Override
		public CTType getType() {
			return CTType.HORIZONTAL;
		}

	}

	public static class Vertical extends CTSpriteShiftEntry {

		public Vertical() {
			super(2);
		}

		@Override
		public int getTextureIndex(CTContext context) {
			return (context.up ? 1 : 0) + (context.down ? 2 : 0);
		}

		@Override
		public CTType getType() {
			return CTType.VERTICAL;
		}

	}

	public static class Cross extends CTSpriteShiftEntry {

		public Cross() {
			super(4);
		}

		@Override
		public int getTextureIndex(CTContext context) {
			return (context.up ? 1 : 0) + (context.down ? 2 : 0) + (context.left ? 4 : 0) + (context.right ? 8 : 0);
		}

		@Override
		public CTType getType() {
			return CTType.CROSS;
		}

	}
	
	// Same as cross with different sheet arrangement
	public static class Rect extends CTSpriteShiftEntry {

		public Rect() {
			super(4);
		}

		@Override
		public int getTextureIndex(CTContext context) {
			int x = context.left && context.right ? 2 : context.left ? 3 : context.right ? 1 : 0;
			int y = context.up && context.down ? 1 : context.up ? 2 : context.down ? 0 : 3;
			return x + y * 4;
		}

		@Override
		public CTType getType() {
			return CTType.RECTANGLE;
		}

	}

	public static class Omnidirectional extends CTSpriteShiftEntry {

		public Omnidirectional() {
			super(8);
		}

		@Override
		public int getTextureIndex(CTContext context) {
			CTContext c = context;
			int tileX = 0, tileY = 0;
			int borders = (!c.up ? 1 : 0) + (!c.down ? 1 : 0) + (!c.left ? 1 : 0) + (!c.right ? 1 : 0);

			if (c.up)
				tileX++;
			if (c.down)
				tileX += 2;
			if (c.left)
				tileY++;
			if (c.right)
				tileY += 2;

			if (borders == 0) {
				if (c.topRight)
					tileX++;
				if (c.topLeft)
					tileX += 2;
				if (c.bottomRight)
					tileY += 2;
				if (c.bottomLeft)
					tileY++;
			}

			if (borders == 1) {
				if (!c.right) {
					if (c.topLeft || c.bottomLeft) {
						tileY = 4;
						tileX = -1 + (c.bottomLeft ? 1 : 0) + (c.topLeft ? 1 : 0) * 2;
					}
				}
				if (!c.left) {
					if (c.topRight || c.bottomRight) {
						tileY = 5;
						tileX = -1 + (c.bottomRight ? 1 : 0) + (c.topRight ? 1 : 0) * 2;
					}
				}
				if (!c.down) {
					if (c.topLeft || c.topRight) {
						tileY = 6;
						tileX = -1 + (c.topLeft ? 1 : 0) + (c.topRight ? 1 : 0) * 2;
					}
				}
				if (!c.up) {
					if (c.bottomLeft || c.bottomRight) {
						tileY = 7;
						tileX = -1 + (c.bottomLeft ? 1 : 0) + (c.bottomRight ? 1 : 0) * 2;
					}
				}
			}

			if (borders == 2) {
				if ((c.up && c.left && c.topLeft) || (c.down && c.left && c.bottomLeft)
					|| (c.up && c.right && c.topRight) || (c.down && c.right && c.bottomRight))
					tileX += 3;
			}

			return tileX + 8 * tileY;
		}

		@Override
		public CTType getType() {
			return CTType.OMNIDIRECTIONAL;
		}

	}

}
