package com.simibubi.create.foundation.render.light;

import com.simibubi.create.foundation.render.RenderMath;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import static com.simibubi.create.foundation.render.RenderMath.isPowerOf2;
import static com.simibubi.create.foundation.render.RenderMath.rotateSideLength;

public class GridAlignedBB {
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;

    public GridAlignedBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static GridAlignedBB copy(GridAlignedBB bb) {
        return new GridAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public static GridAlignedBB fromAABB(AxisAlignedBB aabb) {
        int minX = (int) Math.floor(aabb.minX);
        int minY = (int) Math.floor(aabb.minY);
        int minZ = (int) Math.floor(aabb.minZ);
        int maxX = (int) Math.ceil(aabb.maxX);
        int maxY = (int) Math.ceil(aabb.maxY);
        int maxZ = (int) Math.ceil(aabb.maxZ);
        return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static GridAlignedBB fromSection(SectionPos pos) {
        return new GridAlignedBB(pos.getWorldStartX(),
                                 pos.getWorldStartY(),
                                 pos.getWorldStartZ(),
                                 pos.getWorldEndX() + 1,
                                 pos.getWorldEndY() + 1,
                                 pos.getWorldEndZ() + 1);
    }

    public static AxisAlignedBB toAABB(GridAlignedBB bb) {
        return new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public int sizeX() {
        return maxX - minX;
    }

    public int sizeY() {
        return maxY - minY;
    }

    public int sizeZ() {
        return maxZ - minZ;
    }

    public int volume() {
        return sizeX() * sizeY() * sizeZ();
    }

    public boolean empty() {
        // if any dimension has side length 0 this box contains no volume
        return  minX == maxX ||
                minY == maxY ||
                minZ == maxZ;
    }

    public void translate(Vec3i by) {
        translate(by.getX(), by.getY(), by.getZ());
    }

    public void translate(int x, int y, int z) {
        minX += x;
        maxX += x;
        minY += y;
        maxY += y;
        minZ += z;
        maxZ += z;
    }

    public void rotate45(Direction.Axis axis) {
        if (axis == Direction.Axis.X) {
            this.grow(0, rotateSideLength(sizeY()), rotateSideLength(sizeZ()));
        } else if (axis == Direction.Axis.Y) {
            this.grow(rotateSideLength(sizeX()), 0, rotateSideLength(sizeZ()));
        } else if (axis == Direction.Axis.Z) {
            this.grow(rotateSideLength(sizeX()), rotateSideLength(sizeY()), 0);
        }
    }

    public void mirrorAbout(Direction.Axis axis) {
        Vec3i axisVec = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getDirectionVec();
        int flipX = axisVec.getX() - 1;
        int flipY = axisVec.getY() - 1;
        int flipZ = axisVec.getZ() - 1;

        int maxX = this.maxX * flipX;
        int maxY = this.maxY * flipY;
        int maxZ = this.maxZ * flipZ;
        this.maxX = this.minX * flipX;
        this.maxY = this.minY * flipY;
        this.maxZ = this.minZ * flipZ;
        this.minX = maxX;
        this.minY = maxY;
        this.minZ = maxZ;
    }

    public void expandAroundAxis(Direction.Axis axis) {
        int maxXDiff = Math.max(this.maxX - 1, -this.minX);
        int maxYDiff = Math.max(this.maxY - 1, -this.minY);
        int maxZDiff = Math.max(this.maxZ - 1, -this.minZ);

        int maxDiff;
        if (axis == Direction.Axis.X)
            maxDiff = Math.max(maxZDiff, maxYDiff);
        else if (axis == Direction.Axis.Y)
            maxDiff = Math.max(maxZDiff, maxXDiff);
        else if (axis == Direction.Axis.Z)
            maxDiff = Math.max(maxXDiff, maxYDiff);
        else
            maxDiff = 0;

        Vec3i axisVec = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getDirectionVec();
        int axisX = axisVec.getX();
        int axisY = axisVec.getY();
        int axisZ = axisVec.getZ();

        int planeX = 1 - axisX;
        int planeY = 1 - axisY;
        int planeZ = 1 - axisZ;

        minX = axisX * minX - maxDiff * planeX;
        minY = axisY * minY - maxDiff * planeY;
        minZ = axisZ * minZ - maxDiff * planeZ;
        maxX = axisX * maxX + (maxDiff + 1) * planeX;
        maxY = axisY * maxY + (maxDiff + 1) * planeY;
        maxZ = axisZ * maxZ + (maxDiff + 1) * planeZ;
    }

    /**
     * Grow this bounding box to have power of 2 side length, scaling from the center.
     */
    public void nextPowerOf2Centered() {
        int sizeX = sizeX();
        int sizeY = sizeY();
        int sizeZ = sizeZ();

        int newSizeX = RenderMath.nextPowerOf2(sizeX);
        int newSizeY = RenderMath.nextPowerOf2(sizeY);
        int newSizeZ = RenderMath.nextPowerOf2(sizeZ);

        int diffX = newSizeX - sizeX;
        int diffY = newSizeY - sizeY;
        int diffZ = newSizeZ - sizeZ;

        minX -= diffX / 2; // floor division for the minimums
        minY -= diffY / 2;
        minZ -= diffZ / 2;
        maxX += (diffX + 1) / 2; // ceiling divison for the maximums
        maxY += (diffY + 1) / 2;
        maxZ += (diffZ + 1) / 2;
    }

    /**
     * Grow this bounding box to have power of 2 side lengths, scaling from the minimum coords.
     */
    public void nextPowerOf2() {
        int sizeX = RenderMath.nextPowerOf2(sizeX());
        int sizeY = RenderMath.nextPowerOf2(sizeY());
        int sizeZ = RenderMath.nextPowerOf2(sizeZ());

        this.maxX = this.minX + sizeX;
        this.maxY = this.minY + sizeY;
        this.maxZ = this.minZ + sizeZ;
    }

    public boolean hasPowerOf2Sides() {
        // this is only true if all individual side lengths are powers of 2
        return isPowerOf2(volume());
    }

    public void grow(int s) {
        this.grow(s, s, s);
    }

    public void grow(int x, int y, int z) {
        minX -= x;
        minY -= y;
        minZ -= z;
        maxX += x;
        maxY += y;
        maxZ += z;
    }

    public GridAlignedBB intersect(GridAlignedBB other) {
        int minX = Math.max(this.minX, other.minX);
        int minY = Math.max(this.minY, other.minY);
        int minZ = Math.max(this.minZ, other.minZ);
        int maxX = Math.min(this.maxX, other.maxX);
        int maxY = Math.min(this.maxY, other.maxY);
        int maxZ = Math.min(this.maxZ, other.maxZ);
        return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void intersectAssign(GridAlignedBB other) {
        this.minX = Math.max(this.minX, other.minX);
        this.minY = Math.max(this.minY, other.minY);
        this.minZ = Math.max(this.minZ, other.minZ);
        this.maxX = Math.min(this.maxX, other.maxX);
        this.maxY = Math.min(this.maxY, other.maxY);
        this.maxZ = Math.min(this.maxZ, other.maxZ);
    }

    public GridAlignedBB union(GridAlignedBB other) {
        int minX = Math.min(this.minX, other.minX);
        int minY = Math.min(this.minY, other.minY);
        int minZ = Math.min(this.minZ, other.minZ);
        int maxX = Math.max(this.maxX, other.maxX);
        int maxY = Math.max(this.maxY, other.maxY);
        int maxZ = Math.max(this.maxZ, other.maxZ);
        return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void unionAssign(GridAlignedBB other) {
        this.minX = Math.min(this.minX, other.minX);
        this.minY = Math.min(this.minY, other.minY);
        this.minZ = Math.min(this.minZ, other.minZ);
        this.maxX = Math.max(this.maxX, other.maxX);
        this.maxY = Math.max(this.maxY, other.maxY);
        this.maxZ = Math.max(this.maxZ, other.maxZ);
    }

    public void unionAssign(AxisAlignedBB other) {
        this.minX = Math.min(this.minX, (int) Math.floor(other.minX));
        this.minY = Math.min(this.minY, (int) Math.floor(other.minY));
        this.minZ = Math.min(this.minZ, (int) Math.floor(other.minZ));
        this.maxX = Math.max(this.maxX, (int) Math.ceil(other.maxX));
        this.maxY = Math.max(this.maxY, (int) Math.ceil(other.maxY));
        this.maxZ = Math.max(this.maxZ, (int) Math.ceil(other.maxZ));
    }

    public boolean intersects(GridAlignedBB other) {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    public void forEachContained(CoordinateConsumer func) {
        if (empty()) return;

        for (int x = minX; x < maxX; x++) {
            for (int y = Math.max(minY, 0); y < Math.min(maxY, 255); y++) { // clamp to world height limits
                for (int z = minZ; z < maxZ; z++) {
                    func.consume(x, y, z);
                }
            }
        }
    }

}
