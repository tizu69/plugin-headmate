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
	 * by 2. As such, for Blockmates, this is only half of the actual size of the
	 * displayed block.
	 */
	public float scale = 0.5f;
	public int rotH = 0;
	public int rotV = 0;
	public boolean miniX = false;
	public boolean miniY = false;
	public boolean miniZ = false;

	protected HeadmateInstance() {
	}

	public HeadmateInstance(float offsetX, float offsetY, float offsetZ, float scale,
			int rotH, int rotV, boolean miniX, boolean miniY, boolean miniZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.scale = scale;
		this.rotH = rotH;
		this.rotV = rotV;
		this.miniX = miniX;
		this.miniY = miniY;
		this.miniZ = miniZ;
	}

	public HeadmateInstance(float offsetX, float offsetY, float offsetZ, float scale,
			int rotH, int rotV) {
		this(offsetX, offsetY, offsetZ, scale, rotH, rotV, false, false, false);
	}

	public Transformation getTransformation(boolean isBlock) {
		var yaw = Transformers.getRot(rotH);
		var pitch = Transformers.getRot(rotV);
		float x = (float) (Math.sin(yaw) * Math.sin(pitch) * (scale / 2f));
		float y = (float) (Math.cos(pitch) * (scale / 2f));
		float z = (float) (Math.cos(yaw) * Math.sin(pitch) * (scale / 2f));

		// if a mini-mode dimension is enabled and no rotation is applied, we
		// fix Z-fighting so that you can place a real slab etc on the same
		// block and have it render the blockmate. This is a hack, but it works
		// just fine, and doesn't mess with headmates, as those can't have mini
		// mode applied to them.
		var zFightFix = scale == 0.5f && (miniX || miniY || miniZ)
				&& rotH % 4 == 0 && rotV % 4 == 0 ? 0.001f : 0;
		float scaleX = scale * (miniX ? 0.5f : 1) * (isBlock ? .5f : 1) + zFightFix;
		float scaleY = scale * (miniY ? 0.5f : 1) * (isBlock ? .5f : 1) + zFightFix;
		float scaleZ = scale * (miniZ ? 0.5f : 1) * (isBlock ? .5f : 1) + zFightFix;

		return new Transformation(
				new Vector3f(offsetX + 0.5f + x, offsetY + 0.5f + y, offsetZ + 0.5f + z),
				new Quaternionf().rotateY(yaw).rotateX(pitch),
				new Vector3f(scaleX * 2, scaleY * 2, scaleZ * 2),
				new Quaternionf());
	}

	/**
	 * Returns true if the transformation has been externally tampered with, used
	 * for warning the user that external changes will be lost if modified with
	 * the Headmate wand.
	 */
	public boolean isModified(Transformation trans, boolean isBlock) {
		var own = getTransformation(isBlock);
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

	public HeadmateInstance toggleMiniX() {
		this.miniX = !this.miniX;
		if (this.miniX && this.miniY && this.miniZ)
			this.miniZ = false;
		return this;
	}

	public HeadmateInstance toggleMiniY() {
		this.miniY = !this.miniY;
		if (this.miniX && this.miniY && this.miniZ)
			this.miniX = false;
		return this;
	}

	public HeadmateInstance toggleMiniZ() {
		this.miniZ = !this.miniZ;
		if (this.miniX && this.miniY && this.miniZ)
			this.miniY = false;
		return this;
	}

	public String toString() {
		return "Head{ oX=" + this.offsetX + ", oY=" + this.offsetY + ", oZ=" + this.offsetZ +
				", scale=" + this.scale + ", rotH=" + this.rotH + ", rotV=" + this.rotV +
				", miniX=" + this.miniX + ", miniY=" + this.miniY + ", miniZ=" + this.miniZ + " }";
	}
}
