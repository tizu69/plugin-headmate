package dev.tizu.headmate.util;

import dev.tizu.headmate.headmate.HeadmateStore;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.joml.Vector3d;

public class Locator {

	private Locator() {}

	private static boolean isHead(ItemDisplay head) {
		return head.getItemStack().getType() == Material.PLAYER_HEAD;
	}

	public static Vector3d centerOfHead(ItemDisplay head) {
		var inst = HeadmateStore.get(head);
		return head
			.getLocation()
			.toVector()
			.toVector3d()
			.add(0.5f, 0.5f, 0.5f)
			.add(inst.offsetX, inst.offsetY, inst.offsetZ);
	}

	public static float diameterOfHead(ItemDisplay head) {
		var inst = HeadmateStore.get(head);
		return isHead(head) ? inst.scale : inst.scale * 2;
	}

	public static ItemDisplay lookingAt(
		Location player,
		List<ItemDisplay> considerations
	) {
		var ppos = player.toVector().toVector3d();
		var pdir = player.getDirection().toVector3d().normalize();

		ItemDisplay target = null;
		var closestDistance = Double.MAX_VALUE;

		for (var head : considerations) {
			var hpos = centerOfHead(head);
			var hrad = diameterOfHead(head) / Math.sqrt(Math.PI);
			var oc = ppos.sub(hpos, new Vector3d());

			var a = 1.0;
			var b = 2.0 * oc.dot(pdir);
			var c = oc.dot(oc) - hrad * hrad;
			var discriminant = b * b - 4 * a * c;
			if (discriminant < 0) continue;

			var sqrtDiscriminant = Math.sqrt(discriminant);
			var t1 = (-b - sqrtDiscriminant) / (2 * a);
			var t2 = (-b + sqrtDiscriminant) / (2 * a);
			var t = Math.max(Math.min(t1, t2), 0);
			if (t > 0 && t < closestDistance) {
				closestDistance = t;
				target = head;
			}
		}

		return target;
	}
}
