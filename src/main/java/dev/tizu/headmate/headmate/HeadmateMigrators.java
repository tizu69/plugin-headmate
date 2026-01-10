package dev.tizu.headmate.headmate;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.util.Transformers;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Vector3f;

public class HeadmateMigrators {

	private HeadmateMigrators() {}

	/** Migrate away from chunk PDC to entity self-contained storage. */
	public static void selfContainedStorage(ChunkLoadEvent event) {
		var pdc = event.getChunk().getPersistentDataContainer();
		for (var key : pdc.getKeys()) {
			if (!key.asString().startsWith("headmate:hm-")) continue;
			var list = pdc.get(key, PersistentDataType.LIST.strings());
			if (list == null || list.isEmpty()) continue;

			ThisPlugin.i()
				.getLogger()
				.info(
					"Starting migration for " +
						key.getKey() +
						" in " +
						event.getWorld().getName() +
						" (" +
						list.size() +
						" heads)"
				);

			for (var uuid : list) {
				var entity = event.getWorld().getEntity(UUID.fromString(uuid));
				if (entity == null) continue;
				ThisPlugin.i()
					.getLogger()
					.info(
						"Migrating " +
							uuid +
							" in " +
							event.getWorld().getName()
					);
				var de = (ItemDisplay) entity;
				de.addScoreboardTag("headmate");
				de.addScoreboardTag("headmate_migrated");
				de.teleport(de.getLocation().add(-0.5, -0.5, -0.5));

				var transforms = de.getTransformation();
				var trans = transforms.getTranslation();
				var yaw = Transformers.getRotIndex(
					transforms.getLeftRotation()
				);
				var scale = transforms.getScale().y / 2f;
				var inst = new HeadmateInstance(
					trans.x,
					trans.y - (scale / 2f),
					trans.z,
					scale,
					yaw,
					0
				);
				HeadmateStore.set(de, inst);
			}
			pdc.remove(key);
		}
	}

	/**
	 * Migrate one or more heads to fix rotation & position, if applicable. This may
	 * happen due to WorldEdit or other tools that rotate heads by rotating the
	 * entity itself.
	 */
	public static int rotationFixes(ItemDisplay[] heads) {
		var yawOffsets = Map.of(
			0,
			new Vector3f(0f, 0f, 0f),
			90,
			new Vector3f(-1f, 0f, 0f),
			180,
			new Vector3f(-1f, 0f, -1f),
			270,
			new Vector3f(0f, 0f, -1f)
		);

		var applied = 0;
		for (var head : heads) {
			var inst = HeadmateStore.get(head);
			if (inst == null) continue;

			var posOkay = head
				.getLocation()
				.equals(head.getLocation().toBlockLocation());
			var realYaw = (int) head.getYaw();
			if (posOkay && realYaw == 0f) continue;

			ThisPlugin.i()
				.getLogger()
				.info("Fixing rotation of head at " + head.getLocation());
			applied++;
			if (!yawOffsets.containsKey(realYaw)) {
				ThisPlugin.i().getLogger().info("Unknown yaw: " + realYaw);
				HeadmateStore.remove(
					head
						.getWorld()
						.getBlockAt(
							(int) head.getX(),
							(int) head.getY(),
							(int) head.getZ()
						),
					head.getUniqueId()
				);
				continue;
			}

			if (!posOkay) head.teleport(head.getLocation().toBlockLocation());
			if (realYaw != 0) {
				// HACK: a special case for 180 degrees is NOT a good idea probably
				var yaw =
					inst.rotH * 22.5f + (realYaw == 180 ? 180 : realYaw - 180);

				// we may need to rotate the offsets for custom rotations (say 45 degrees) too?
				var newX = switch (realYaw) {
					case 90 -> -inst.offsetZ;
					case 180 -> -inst.offsetX;
					case 270 -> inst.offsetZ;
					default -> inst.offsetX;
				};
				var newZ = switch (realYaw) {
					case 90 -> inst.offsetX;
					case 180 -> -inst.offsetZ;
					case 270 -> -inst.offsetX;
					default -> inst.offsetZ;
				};

				var after = new HeadmateInstance(
					newX,
					inst.offsetY,
					newZ,
					inst.scale,
					Transformers.getRotIndex(yaw),
					0
				);
				HeadmateStore.set(head, after);
				head.setRotation(0, 0);
				var offset = yawOffsets.get(realYaw);
				head.teleport(
					head.getLocation().add(offset.x, offset.y, offset.z)
				);
			}
		}
		return applied;
	}

	public static class Listeners implements Listener {

		@EventHandler
		public void onChunkLoad(ChunkLoadEvent event) {
			selfContainedStorage(event);
		}
	}
}
