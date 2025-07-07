package dev.tizu.headmate.util;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Transformers {
    public static float getRot(int index) {
        return (float) Math.toRadians(index * 22.5f);
    }

    public static int getRot(BlockData blockdata) {
        BlockFace face;
        if (blockdata instanceof Directional directional)
            face = directional.getFacing();
        else if (blockdata instanceof Rotatable rotatable)
            face = rotatable.getRotation().getOppositeFace();
        else
            return 0;

        return switch (face) {
            case NORTH -> 0;
            case NORTH_NORTH_WEST -> 1;
            case NORTH_WEST -> 2;
            case WEST_NORTH_WEST -> 3;
            case WEST -> 4;
            case WEST_SOUTH_WEST -> 5;
            case SOUTH_WEST -> 6;
            case SOUTH_SOUTH_WEST -> 7;
            case SOUTH -> 8;
            case SOUTH_SOUTH_EAST -> 9;
            case SOUTH_EAST -> 10;
            case EAST_SOUTH_EAST -> 11;
            case EAST -> 12;
            case EAST_NORTH_EAST -> 13;
            case NORTH_EAST -> 14;
            case NORTH_NORTH_EAST -> 15;
            default -> 0;
        };
    }

    public static int getRotIndex(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        int index = Math.round(yaw / 22.5f);
        return index % 16;
    }

    // TODO: I don't know why or how I am doing this the way I am. Clean up.
    public static int getRotIndex(Quaternionf rotation) {
        Vector3f forward = rotation.transform(new Vector3f(0, 0, -1));
        float yaw = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));
        yaw = (yaw % 360 + 360) % 360;
        int index = Math.round(yaw / 22.5f) % 16;
        return (index + 8) % 16;
    }

    public static Vector3f getPos(BlockData blockdata) {
        // for wall heads, attach to the wall
        if (blockdata instanceof Directional directional)
            return directional.getFacing().getDirection().toVector3f().mul(-0.25f);
        return new Vector3f(0f, -0.25f, 0f);
    }

    /**
     * Grabs the largest component of the vector, either negative or positive, and
     * returns offset or -offset at its place.
     */
    public static Vector3f turnIntoGenericOffset(Vector3d in, float offset) {
        var out = new Vector3f();
        var max = in.maxComponent();
        out.setComponent(max, offset * (float) Math.signum(in.get(max)));
        return out;
    }
}
