package dev.tizu.headmate.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Equals {
	private Equals() {
	}

	private static final float EPSILON = 0.0001f;

	public static boolean nearlyEqual(Vector3f a, Vector3f b) {
		return a.distance(b) < EPSILON;
	}

	public static boolean nearlyEqual(Quaternionf a, Quaternionf b) {
		return Math.abs(a.x - b.x) < EPSILON &&
				Math.abs(a.y - b.y) < EPSILON &&
				Math.abs(a.z - b.z) < EPSILON &&
				Math.abs(a.w - b.w) < EPSILON;
	}
}
