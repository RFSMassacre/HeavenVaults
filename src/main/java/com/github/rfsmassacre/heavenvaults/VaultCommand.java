package com.github.rfsmassacre.heavenvaults;

import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VaultCommand extends SimplePaperCommand
{
    public VaultCommand()
    {
        super(HeavenVaults.getInstance(), "playervault");
    }

    @Override
    protected void onRun(CommandSender sender, String... args)
    {
        if (!(sender instanceof Player player))
        {
            onConsole(sender);
            return;
        }

        if (args.length > 0 && !hasPermission(player, "heavenvaults.admin"))
        {
            locale.sendLocale(player, "no-admin-perm");
            playSound(sender, SoundKey.NO_PERM);
            return;
        }

        String playerName = player.getName();
        if (args.length > 0)
        {
            playerName = args[0];
        }

        String finalPlayerName = playerName;
        Vault.getVault(playerName, (vault) ->
        {
            if (vault == null)
            {
                locale.sendLocale(sender, "no-vault-found", "{player}", finalPlayerName);
                playSound(sender, SoundKey.INCOMPLETE);
                return;
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(HeavenVaults.getInstance(), () ->
            {
                VaultMenu menu = new VaultMenu(vault, 1);
                Menu.addView(player.getUniqueId(), menu);
                player.openInventory(menu.createInventory(player));
                playSound(player, SoundKey.SUCCESS);
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args)
    {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1)
        {
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
        }

        return suggestions;
    }
}
