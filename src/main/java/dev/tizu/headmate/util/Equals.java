package dev.tizu.headmate.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Equals {
	private static final float epsilon = 0.0001f;

	public static boolean nearlyEqual(Vector3f a, Vector3f b) {
		return a.distance(b) < epsilon;
	}

	public static boolean nearlyEqual(Quaternionf a, Quaternionf b) {
		return Math.abs(a.x - b.x) < epsilon &&
				Math.abs(a.y - b.y) < epsilon &&
				Math.abs(a.z - b.z) < epsilon &&
				Math.abs(a.w - b.w) < epsilon;
	}
}
