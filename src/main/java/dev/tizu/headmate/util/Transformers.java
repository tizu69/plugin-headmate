package dev.tizu.headmate.util;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Transformers {
    public static int getRotIndex(Quaternionf rot) {
        var yaw = (float) Math.toDegrees(Math.atan2(2.0f * (rot.w() * rot.y() + rot.x() * rot.z()),
                1.0f - 2.0f * (rot.y() * rot.y() + rot.z() * rot.z())));
        if (yaw < 0)
            yaw += 360;
        return (int) Math.round(yaw / 22.5f) + 8;
    }

    public static Quaternionf getRot(int index) {
        return new Quaternionf().rotationY((float) Math.toRadians(((index * 22.5f) % 360) - 180));
    }

    public static Quaternionf getRot(BlockData blockdata) {
        BlockFace face;
        if (blockdata instanceof Directional directional)
            face = directional.getFacing();
        else if (blockdata instanceof Rotatable rotatable)
            face = rotatable.getRotation().getOppositeFace();
        else
            return getRot(0);

        return switch (face) {
            case SOUTH -> getRot(0);
            case SOUTH_SOUTH_EAST -> getRot(1);
            case SOUTH_EAST -> getRot(2);
            case EAST_SOUTH_EAST -> getRot(3);
            case EAST -> getRot(4);
            case EAST_NORTH_EAST -> getRot(5);
            case NORTH_EAST -> getRot(6);
            case NORTH_NORTH_EAST -> getRot(7);
            case NORTH -> getRot(8);
            case NORTH_NORTH_WEST -> getRot(9);
            case NORTH_WEST -> getRot(10);
            case WEST_NORTH_WEST -> getRot(11);
            case WEST -> getRot(12);
            case WEST_SOUTH_WEST -> getRot(13);
            case SOUTH_WEST -> getRot(14);
            case SOUTH_SOUTH_WEST -> getRot(15);
            default -> getRot(0);
        };
    }

    public static Quaternionf getRot(float yaw) {
        // being drunk is fun (i wasnt drunk but feels like it, wtf did i do)
        float normalizedYaw = yaw < 0 ? yaw + 360 : yaw;
        float flippedYaw = (540 - normalizedYaw) % 360;
        int index = (int) Math.floor((flippedYaw + 11.25f) / 22.5f) % 16;
        return getRot(index);
    }

    public static Vector3f getPos(BlockData blockdata) {
        // for wall heads, attach to the wall
        if (blockdata instanceof Directional directional)
            return directional.getFacing().getDirection().toVector3f()
                    .mul(-0.25f).add(0.0f, 0.25f, 0.0f);
        return new Vector3f(00f, 00f, 00f);
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
