package dev.francies.betterduels.Duels;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class DuelRequest {
    private final Player requester;
    private final Player targetPlayer;
    private final BukkitTask expiryTask;

    public DuelRequest(Player requester, Player targetPlayer, BukkitTask expiryTask) {
        this.requester = requester;
        this.targetPlayer = targetPlayer;
        this.expiryTask = expiryTask;
    }

    public Player getRequester() {
        return this.requester;
    }

    public Player getTargetPlayer() {
        return this.targetPlayer;
    }

    public void cancelExpiryTask() {
        if (this.expiryTask != null && !this.expiryTask.isCancelled()) {
            this.expiryTask.cancel();
        }
    }
}
