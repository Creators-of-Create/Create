package com.simibubi.create.foundation.ponder.ui;

import net.minecraft.client.renderer.Rect2i;

public interface LayoutHelper {

	static LayoutHelper centeredHorizontal(int itemCount, int rows, int width, int height, int spacing) {
		return new CenteredHorizontalLayoutHelper(itemCount, rows, width, height, spacing);
	}

	int getX();

	int getY();

	void next();

	int getTotalWidth();

	int getTotalHeight();

	default Rect2i getArea() {
		int lWidth = getTotalWidth();
		int lHeight = getTotalHeight();
		return new Rect2i(-lWidth / 2, -lHeight / 2, lWidth, lHeight);
	}

	class CenteredHorizontalLayoutHelper implements LayoutHelper {

		int itemCount;
		int rows;
		int width;
		int height;
		int spacing;

		int currentColumn = 0;
		int currentRow = 0;
		int[] rowCounts;
		int x = 0, y = 0;

		CenteredHorizontalLayoutHelper(int itemCount, int rows, int width, int height, int spacing) {
			this.itemCount = itemCount;
			this.rows = rows;
			this.width = width;
			this.height = height;
			this.spacing = spacing;

			rowCounts = new int[rows];
			int itemsPerRow = itemCount / rows;
			int itemDiff = itemCount - itemsPerRow * rows;
			for (int i = 0; i < rows; i++) {
				rowCounts[i] = itemsPerRow;
				if (itemDiff > 0) {
					rowCounts[i]++;
					itemDiff--;
				}
			}

			init();
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public void next() {
			currentColumn++;
			if (currentColumn >= rowCounts[currentRow]) {
				// nextRow
				if (++currentRow >= rows) {
					x = 0;
					y = 0;
					return;
				}

				currentColumn = 0;
				prepareX();
				y += height + spacing;
				return;
			}

			x += width + spacing;
		}

		private void init() {
			prepareX();
			prepareY();
		}

		private void prepareX() {
			int rowWidth = rowCounts[currentRow] * width + (rowCounts[currentRow] - 1) * spacing;
			x = -(rowWidth / 2);
		}

		private void prepareY() {
			int totalHeight = rows * height + (rows > 1 ? ((rows - 1) * spacing) : 0);
			y = -(totalHeight / 2);
		}

		@Override
		public int getTotalWidth() {
			return rowCounts[0] * width + (rowCounts[0] - 1) * spacing;
		}

		@Override
		public int getTotalHeight() {
			return rows * height + (rows > 1 ? ((rows - 1) * spacing) : 0);
		}

	}

}
