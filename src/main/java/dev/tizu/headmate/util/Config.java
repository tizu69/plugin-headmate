package dev.tizu.headmate.util;

import dev.tizu.headmate.ThisPlugin;

public class Config {
	public static float anchorDistanceLimit() {
		return (float) ThisPlugin.instance.getConfig().getDouble("anchorDistanceLimit");
	};

	public static float maxSideLength() {
		return (float) ThisPlugin.instance.getConfig().getDouble("maxSideLength");
	};

	public static float minSideLength() {
		return (float) ThisPlugin.instance.getConfig().getDouble("minSideLength");
	};

	public static float movementStepSize() {
		return (float) ThisPlugin.instance.getConfig().getDouble("movementStepSize");
	};

	public static float scaleStepSize() {
		return (float) ThisPlugin.instance.getConfig().getDouble("scaleStepSize");
	};

	public static int maxHeads() {
		return ThisPlugin.instance.getConfig().getInt("maxHeads");
	};
}
