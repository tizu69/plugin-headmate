package dev.tizu.headmate.util;

import dev.tizu.headmate.ThisPlugin;

public class Config {

	private Config() {}

	public static float anchorDistanceLimit() {
		return (float) ThisPlugin.i()
			.getConfig()
			.getDouble("anchorDistanceLimit");
	}

	public static float maxSideLength() {
		return (float) ThisPlugin.i().getConfig().getDouble("maxSideLength");
	}

	public static float minSideLength() {
		return (float) ThisPlugin.i().getConfig().getDouble("minSideLength");
	}

	public static float movementStepSize() {
		return (float) ThisPlugin.i().getConfig().getDouble("movementStepSize");
	}

	public static float scaleStepSize() {
		return (float) ThisPlugin.i().getConfig().getDouble("scaleStepSize");
	}

	public static int maxHeads() {
		return ThisPlugin.i().getConfig().getInt("maxHeads");
	}

	public static boolean allowBlockmating() {
		return ThisPlugin.i().getConfig().getBoolean("allowBlockmating");
	}
}
