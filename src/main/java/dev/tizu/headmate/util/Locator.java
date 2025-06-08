package dev.tizu.headmate.util;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.joml.Vector3d;

public class Locator {
    public static Vector3d centerOfHead(ItemDisplay head) {
        var trans = head.getTransformation();
        var offset = trans.getTranslation();
        offset.sub(0, trans.getScale().y / 4, 0);
        return head.getLocation().toVector().toVector3d().add(offset);
    }

    public static ItemDisplay lookingAt(Location player, List<ItemDisplay> considerations) {
        var playerLoc = player.toVector().toVector3d();
        var direction = player.getDirection().toVector3d().normalize();

        double maxDistance = 10.0;
        ItemDisplay bestHead = null;
        var closestDistance = Double.MAX_VALUE;

        for (var head : considerations) {
            var headLoc = centerOfHead(head);
            var toHead = headLoc.sub(playerLoc, new Vector3d());

            var projection = toHead.dot(direction);
            if (projection < 0 || projection > maxDistance)
                continue;

            var closestPoint = direction.mul(projection, new Vector3d()).add(playerLoc);
            var distanceSquared = headLoc.distanceSquared(closestPoint);

            if (distanceSquared < 0.5 * 0.5 && projection < closestDistance) {
                closestDistance = projection;
                bestHead = head;
            }
        }

        return bestHead;
    }

}
