package dev.tizu.headmate;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class Transformers {
    private final static List<AxisAngle4f> rotations = List.of(
            Maths.fromEuler(0, 0f, 0),
            Maths.fromEuler(0, 22.5f, 0),
            Maths.fromEuler(0, 45f, 0),
            Maths.fromEuler(0, 67.5f, 0),
            Maths.fromEuler(0, 90f, 0),
            Maths.fromEuler(0, 112.5f, 0),
            Maths.fromEuler(0, 135f, 0),
            Maths.fromEuler(0, 157.5f, 0),
            Maths.fromEuler(0, 180f, 0),
            Maths.fromEuler(0, 202.5f, 0),
            Maths.fromEuler(0, 225f, 0),
            Maths.fromEuler(0, 247.5f, 0),
            Maths.fromEuler(0, 270f, 0),
            Maths.fromEuler(0, 292.5f, 0),
            Maths.fromEuler(0, 315f, 0),
            Maths.fromEuler(0, 337.5f, 0));

    public static AxisAngle4f getRot(int index) {
        return rotations.get(index % rotations.size());
    }

    public static AxisAngle4f getRot(BlockData blockdata) {
        BlockFace face;
        if (blockdata instanceof Directional directional)
            face = directional.getFacing();
        else if (blockdata instanceof Rotatable rotatable)
            face = rotatable.getRotation().getOppositeFace();
        else
            return getRot(0);

        switch (face) {
            case NORTH:
                return rotations.get(0);
            case NORTH_NORTH_WEST:
                return rotations.get(1);
            case NORTH_WEST:
                return rotations.get(2);
            case WEST_NORTH_WEST:
                return rotations.get(3);
            case WEST:
                return rotations.get(4);
            case WEST_SOUTH_WEST:
                return rotations.get(5);
            case SOUTH_WEST:
                return rotations.get(6);
            case SOUTH_SOUTH_WEST:
                return rotations.get(7);
            case SOUTH:
                return rotations.get(8);
            case SOUTH_SOUTH_EAST:
                return rotations.get(9);
            case SOUTH_EAST:
                return rotations.get(10);
            case EAST_SOUTH_EAST:
                return rotations.get(11);
            case EAST:
                return rotations.get(12);
            case EAST_NORTH_EAST:
                return rotations.get(13);
            case NORTH_EAST:
                return rotations.get(14);
            case NORTH_NORTH_EAST:
                return rotations.get(15);
            default:
                return rotations.get(0);
        }
    }

    public static Vector3f getPos(BlockData blockdata) {
        // for wall heads, attach to the wall
        if (blockdata instanceof Directional directional)
            return directional.getFacing().getDirection().toVector3f()
                    .mul(-0.25f).add(0.5f, 0.75f, 0.5f);
        return new Vector3f(0.5f, 0.5f, 0.5f);
    }
}
