package dev.tizu.headmate.headmate;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.tizu.headmate.util.Config;
import static dev.tizu.headmate.util.Equals.*;
import dev.tizu.headmate.util.Transformers;

public class HeadmateInstance {
	public float offsetX = 0;
	public float offsetY = 0;
	public float offsetZ = 0;
	/**
	 * A head is only half the size of the actual entity, so this scale is divided
	 * by 2
	 */
	public float scale = 0.5f;
	public int rotH = 0;
	public int rotV = 0;

	protected HeadmateInstance() {
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
		var yaw = Transformers.getRot(rotH);
		var pitch = Transformers.getRot(rotV);
		float x = (float) (Math.sin(yaw) * Math.sin(pitch) * (scale / 2f));
		float y = (float) (Math.cos(pitch) * (scale / 2f));
		float z = (float) (Math.cos(yaw) * Math.sin(pitch) * (scale / 2f));

		return new Transformation(
				new Vector3f(offsetX + 0.5f + x, offsetY + 0.5f + y, offsetZ + 0.5f + z),
				new Quaternionf().rotateY(yaw).rotateX(pitch),
				new Vector3f(scale * 2, scale * 2, scale * 2),
				new Quaternionf());
	}

	/**
	 * Returns true if the transformation has been externally tampered with, used
	 * for warning the user that external changes will be lost if modified with
	 * the Headmate wand.
	 */
	public boolean isModified(Transformation trans) {
		var own = getTransformation();
		return !nearlyEqual(own.getTranslation(), trans.getTranslation())
				|| !nearlyEqual(own.getScale(), trans.getScale())
				|| !nearlyEqual(own.getLeftRotation(), trans.getLeftRotation())
				|| !nearlyEqual(own.getRightRotation(), trans.getRightRotation());
	}

	public HeadmateInstance move(float x, float y, float z) {
		var max = Config.anchorDistanceLimit();
		this.offsetX = Math.clamp(this.offsetX + x, -max, max);
		this.offsetY = Math.clamp(this.offsetY + y, -max, max);
		this.offsetZ = Math.clamp(this.offsetZ + z, -max, max);
		return this;
	}

	/**
	 * If xyz is positive, add one step, if negative, subtract one step.
	 * 
	 * @param largestOnly If true, only move the largest component.
	 */
	public HeadmateInstance move(float x, float y, float z, boolean largestOnly) {
		var one = Config.movementStepSize();

		if (!largestOnly)
			return this.move(Math.signum(x) * one, Math.signum(y) * one, Math.signum(z) * one);

		var absX = Math.abs(x);
		var absY = Math.abs(y);
		var absZ = Math.abs(z);

		if (absX >= absY && absX >= absZ)
			return this.move(Math.signum(x) * one, 0, 0);
		else if (absY >= absX && absY >= absZ)
			return this.move(0, Math.signum(y) * one, 0);
		else
			return this.move(0, 0, Math.signum(z) * one);
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
		this.scale = Math.clamp(this.scale + by, Config.minSideLength(), Config.maxSideLength());
		return this;
	}

	public HeadmateInstance scaleDown() {
		return this.scale(-Config.scaleStepSize());
	}

	public HeadmateInstance scaleUp() {
		return this.scale(Config.scaleStepSize());
	}

	public String toString() {
		return "Head{ oX=" + this.offsetX + ", oY=" + this.offsetY + ", oZ=" + this.offsetZ +
				", scale=" + this.scale + ", rotH=" + this.rotH + ", rotV=" + this.rotV + " }";
	}
}
