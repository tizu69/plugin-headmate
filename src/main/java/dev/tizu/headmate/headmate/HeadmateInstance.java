package dev.tizu.headmate.headmate;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.tizu.headmate.util.Config;
import dev.tizu.headmate.util.Transformers;

public class HeadmateInstance {
    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public float scale = 1;
    public int rotH = 0;
    public int rotV = 0;

    protected HeadmateInstance() {
    }

    /**
     * This should only be used for importing old data, V-rotation is not supported!
     */
    public HeadmateInstance(Vector3f pos, Quaternionf rot, float scale) {
        this.offsetX = pos.x;
        this.offsetY = pos.y;
        this.offsetZ = pos.z;
        this.scale = scale;
        this.rotH = Transformers.getRotIndex(rot);
        this.rotV = 0;
    }

    public HeadmateInstance(float offsetX, float offsetY, float offsetZ, float scale, int rotH, int rotV) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.scale = scale;
        this.rotH = rotH;
        this.rotV = rotV;
    }

    public Transformation getTransformation() {
        return new Transformation(
                new Vector3f(offsetX, offsetY, offsetZ),
                new Quaternionf()
                        .rotateY(Transformers.getRotation(rotH))
                        .rotateX(Transformers.getRotation(rotV)),
                new Vector3f(scale, scale, scale),
                new Quaternionf());
    }

    /**
     * Returns true if the transformation has been externally tampered with, used
     * for warning the user that external changes will be lost if modified with
     * the Headmate wand.
     */
    public boolean isModified(Transformation trans) {
        var own = getTransformation();
        return !own.getTranslation().equals(trans.getTranslation())
                || !own.getScale().equals(trans.getScale())
                || !own.getLeftRotation().equals(trans.getLeftRotation())
                || !own.getRightRotation().equals(trans.getRightRotation());
    }

    public HeadmateInstance move(float x, float y, float z) {
        var max = Config.anchorDistanceLimit();
        this.offsetX = Math.min(Math.max(this.offsetX + x, -max), max);
        this.offsetY = Math.min(Math.max(this.offsetY + y, -max), max);
        this.offsetZ = Math.min(Math.max(this.offsetZ + z, -max), max);
        return this;
    }

    /** If xyz is positive, add one step, if negative, subtract one step */
    public HeadmateInstance move(int x, int y, int z) {
        var one = Config.movementStepSize();
        return this.move(x * one, y * one, z * one);
    }

    public HeadmateInstance rotateSet(int h, int v) {
        this.rotH = h % 16;
        this.rotV = v % 16;
        return this;
    }

    public HeadmateInstance rotateRight() {
        return this.rotateSet(this.rotH + 1, this.rotV);
    }

    public HeadmateInstance rotateLeft() {
        return this.rotateSet(this.rotH + 15, this.rotV);
    }

    public HeadmateInstance rotateUp() {
        return this.rotateSet(this.rotH, this.rotV + 1);
    }

    public HeadmateInstance rotateDown() {
        return this.rotateSet(this.rotH, this.rotV + 15);
    }

    public HeadmateInstance scale(float by) {
        this.scale += by;
        return this;
    }

    public HeadmateInstance scaleDown() {
        return this.scale(-Config.scaleStepSize());
    }

    public HeadmateInstance scaleUp() {
        return this.scale(Config.scaleStepSize());
    }
}