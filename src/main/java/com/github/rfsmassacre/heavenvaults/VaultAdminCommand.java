package com.github.rfsmassacre.heavenvaults;

import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class VaultAdminCommand extends PaperCommand
{
    public VaultAdminCommand()
    {
        super(HeavenVaults.getInstance(), "vaultadmin");
    }

    private class ReloadCommand extends PaperSubCommand
    {
        public ReloadCommand()
        {
            super("reload");
        }

        @Override
        protected void onRun(CommandSender sender, String... args)
        {
            config.reload();
            locale.reload();
            locale.sendLocale(sender, "reloaded");
        }
    }

    private class ValidateCommand extends PaperSubCommand
    {
        public ValidateCommand()
        {
            super("validate");
        }

        @Override
        protected void onRun(CommandSender sender, String... args)
        {
            locale.sendLocale(sender, "validate.starting");
            playSound(sender, SoundKey.SUCCESS);
            Vault.getAllVaults((vaults) ->
            {
                int validated = 0;
                for (Vault vault : vaults)
                {
                    Set<String> blacklistedItems = vault.validate();
                    if (!blacklistedItems.isEmpty())
                    {
                        Vault.saveVault(vault);
                        locale.sendLocale(sender, "validate.success", "{player}", vault.getName(),
                                "{items}", String.join(", ", blacklistedItems));
                        validated++;
                    }
                }

                locale.sendLocale(sender, "validate.completed", "{amount}", Integer.toString(validated));
                playSound(sender, SoundKey.SUCCESS);
            });
        }
    }
}
