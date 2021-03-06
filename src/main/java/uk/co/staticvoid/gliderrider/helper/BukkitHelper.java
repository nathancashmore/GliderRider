package uk.co.staticvoid.gliderrider.helper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BukkitHelper {

    public void broadcastMessage(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> consoleNotification(p, message));
    }

    public void consoleNotification(Player player, String message) {
        player.sendMessage(message);
    }

    public void titleNotification(Player player, String title, String subtitle) {
        player.sendTitle(title, subtitle, 20, 100, 20 );
    }

    public Logger getLogger() {
        return Bukkit.getLogger();
    }

    public List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

}
