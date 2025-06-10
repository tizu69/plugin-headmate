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

    public static float diameterOfHead(ItemDisplay head) {
        var trans = head.getTransformation();
        return trans.getScale().y / 2f;
    }

    public static ItemDisplay lookingAt(Location player, List<ItemDisplay> considerations) {
        var ppos = player.toVector().toVector3d();
        var pdir = player.getDirection().toVector3d().normalize();

        ItemDisplay target = null;
        var closestDistance = Double.MAX_VALUE;

        for (var head : considerations) {
            var hpos = centerOfHead(head);
            var hrad = diameterOfHead(head) / Math.sqrt(Math.PI);
            var oc = ppos.sub(hpos, new Vector3d());

            var b = 2.0 * oc.dot(pdir);
            var c = oc.dot(oc) - hrad * hrad;
            var discriminant = b * b - 4 * c;
            if (discriminant < 0)
                continue;

            var t1 = (-b - Math.sqrt(discriminant)) / 2;
            var t2 = (-b + Math.sqrt(discriminant)) / 2;
            var t = Math.max(Math.min(t1, t2), 0);
            if (t > 0 && t < closestDistance) {
                closestDistance = t;
                target = head;
            }
        }

        return target;
    }

}
