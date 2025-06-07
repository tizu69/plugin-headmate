package dev.tizu.headmate;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class Maths {
    public static AxisAngle4f fromEuler(float pitch, float yaw, float roll) {
        var xRad = (float) Math.toRadians(pitch);
        var yRad = (float) Math.toRadians(yaw);
        var zRad = (float) Math.toRadians(roll);

        var qX = new Quaternionf().rotationX(xRad);
        var qY = new Quaternionf().rotationY(yRad);
        var qZ = new Quaternionf().rotationZ(zRad);
        var combined = new Quaternionf();
        combined.set(qY).mul(qX).mul(qZ);

        var result = new AxisAngle4f();
        combined.get(result);
        return result;
    }
}
