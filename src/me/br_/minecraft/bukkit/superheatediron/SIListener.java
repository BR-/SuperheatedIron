package me.br_.minecraft.bukkit.superheatediron;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class SIListener extends PlayerListener {
	private HashMap<Player, HurtTimer> timers = new HashMap<Player, HurtTimer>();
	private HashMap<Player, Integer> ids = new HashMap<Player, Integer>();
	private SIMain main;
	private BukkitScheduler timer;

	public SIListener(SIMain main) {
		this.main = main;
		timer = main.getServer().getScheduler();
	}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		if (timers.containsKey(p)) {
			cancel(p);
			timers.remove(p);
		}
	}

	private int delay(Runnable task, long delay) {
		return timer.scheduleSyncDelayedTask(main, task, delay / 50L);
	}

	private int repeat(Runnable task, long delay, long interval) {
		return timer.scheduleSyncRepeatingTask(main, task, delay / 50L,
				interval / 50L);
	}

	private void cancel(Player p) {
		try {
			timer.cancelTask(ids.get(p));
		} catch (Exception e) {
		}
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		Heat to = Heat.NONE;
		Block b = event.getTo().getBlock();
		if (b.getFace(BlockFace.DOWN, 2).getType() == Material.LAVA
				|| b.getFace(BlockFace.DOWN, 2).getType() == Material.STATIONARY_LAVA) {
			if (b.getFace(BlockFace.DOWN).getType() == Material.IRON_ORE) {
				to = Heat.HOT;
			} else if (b.getFace(BlockFace.DOWN).getType() == Material.IRON_BLOCK) {
				to = Heat.SUPERHOT;
			}
		}
		Player player = event.getPlayer();
		if (!timers.containsKey(player)) {
			timers.put(player, new HurtTimer(player, 0, Heat.NONE));
		}
		switch (to) {
		case NONE:
			if (timers.get(player).heat == Heat.NONE) {
				break;
			}
			HurtTimer cool = new HurtTimer(player, 1, Heat.NONE);
			if (timers.get(player).times > 2) {
				if (timers.get(player).heat == Heat.HOT) {
					cancel(player);
					timers.put(player, cool);
					ids.put(player, repeat(cool, 500, 1000));
					delay(new CancelTimer(ids.get(player), timer), 3000);
				} else if (timers.get(player).heat == Heat.SUPERHOT) {
					cancel(player);
					timers.put(player, cool);
					ids.put(player, repeat(cool, 100, 750));
					delay(new CancelTimer(ids.get(player), timer), 3000);
				}
			} else {
				cancel(player);
				timers.remove(player);
			}
			break;
		case HOT:
			HurtTimer hot = new HurtTimer(player, 2, Heat.HOT);
			if (timers.get(player).heat == Heat.NONE) {
				timers.put(player, hot);
				ids.put(player, repeat(hot, 750, 500));
			} else if (timers.get(player).heat == Heat.SUPERHOT) {
				delay(new CancelTimer(ids.get(player), timer), 500);
				timers.put(player, hot);
				ids.put(player, repeat(hot, 1000, 500));
			}
			break;
		case SUPERHOT:
			HurtTimer superhot = new HurtTimer(player, 1, Heat.SUPERHOT);
			if (timers.get(player).heat == Heat.NONE) {
				timers.put(player, superhot);
				ids.put(player, repeat(superhot, 200, 1000 / 6L));
			} else if (timers.get(player).heat == Heat.HOT) {
				cancel(player);
				timers.put(player, superhot);
				ids.put(player, repeat(superhot, 1000 / 6L, 1000 / 6L));
			}
			break;
		}
	}

	private class HurtTimer implements Runnable {
		private final Player player;
		private final int damage;
		public final Heat heat;
		public int times = 0;

		public HurtTimer(Player p, int d, Heat h) {
			player = p;
			damage = d;
			heat = h;
		}

		public void run() {
			player.damage(damage);
			times++;
		}
	}

	private class CancelTimer implements Runnable {
		private final int id;
		private final BukkitScheduler s;

		public CancelTimer(int id, BukkitScheduler s) {
			this.id = id;
			this.s = s;
		}

		public void run() {
			s.cancelTask(id);
		}
	}

	private enum Heat {
		NONE, HOT, SUPERHOT
	}
}
