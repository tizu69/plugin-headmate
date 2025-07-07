package dev.tizu.headmate.headmate;

import java.util.UUID;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.util.Transformers;

public class HeadmateMigrators {
	/** Migrate away from chunk PDC to entity self-contained storage. */
	public static void m0_selfContainedStorage(ChunkLoadEvent event) {
		var pdc = event.getChunk().getPersistentDataContainer();
		for (var key : pdc.getKeys()) {
			if (!key.asString().startsWith("headmate:hm-"))
				continue;
			var list = pdc.get(key, PersistentDataType.LIST.strings());
			if (list == null || list.isEmpty())
				continue;

			ThisPlugin.instance.getLogger().info("Starting migration for " + key.getKey() +
					" in " + event.getWorld().getName() + " (" + list.size() + " heads)");

			for (var uuid : list) {
				var entity = event.getWorld().getEntity(UUID.fromString(uuid));
				if (entity == null)
					continue;
				ThisPlugin.instance.getLogger().info("Migrating " + uuid + " in " +
						event.getWorld().getName());
				var de = (ItemDisplay) entity;
				de.addScoreboardTag("headmate");
				de.addScoreboardTag("headmate_migrated");
				de.teleport(de.getLocation().add(-0.5, -0.5, -0.5));

				var transforms = de.getTransformation();
				var trans = transforms.getTranslation();
				var yaw = Transformers.getRotIndex(transforms.getLeftRotation());
				var scale = transforms.getScale().y / 2f;
				var inst = new HeadmateInstance(trans.x, trans.y - (scale / 2f), trans.z,
						scale, yaw, 0);
				HeadmateStore.set(de, inst);
			}
			pdc.remove(key);
		}
	}

	public static class Listeners implements Listener {
		@EventHandler
		public void onChunkLoad(ChunkLoadEvent event) {
			m0_selfContainedStorage(event);
		}
	}
}
