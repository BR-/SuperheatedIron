package me.br_.minecraft.bukkit.superheatediron;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SIMain extends JavaPlugin {
	public void onDisable() {
		System.out.println("[SuperheatedIron] Disabled.");
	}

	public void onEnable() {
		PluginManager pm = this.getServer().getPluginManager();
		SIListener listener = new SIListener(this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, listener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, listener,
				Event.Priority.Normal, this);
		System.out.println("[SuperheatedIron] Enabled.");
	}
}
