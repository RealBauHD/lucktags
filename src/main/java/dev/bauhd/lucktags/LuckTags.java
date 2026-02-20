package dev.bauhd.lucktags;

import com.mojang.brigadier.Command;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.Objects;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LuckTags extends JavaPlugin implements Listener {

  private LuckPerms luckPerms;
  private boolean miniPlaceholders;
  private TagResolver placeholderApiResolver;

  @Override
  public void onEnable() {
    this.saveDefaultConfig();
    this.luckPerms = LuckPermsProvider.get();

    this.luckPerms.getEventBus().subscribe(this, NodeMutateEvent.class, event -> {
      if (event.getTarget() instanceof User user) {
        final Player player = this.getServer().getPlayer(user.getUniqueId());
        if (player != null) {
          this.updateUser(player, user);
        }
      }
    });

    final PluginManager pluginManager = this.getServer().getPluginManager();
    pluginManager.registerEvents(this, this);

    this.miniPlaceholders = pluginManager.isPluginEnabled("MiniPlaceholderAPI");
    if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
      this.placeholderApiResolver = TagResolver.resolver("papi", (argument, context) -> {
        final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(
            (OfflinePlayer) context.target(),
            '%' + argument.popOr("papi tag requires an argument").value() + '%');
        return Tag.selfClosingInserting(
            LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder));
      });
    }

    this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
        event.registrar().register(Commands.literal("lucktags")
            .requires(source -> source.getSender().hasPermission("lucktags.reload"))
            .then(Commands.literal("reload")
                .executes(context -> {
                  this.reloadConfig();
                  for (final Player player : this.getServer().getOnlinePlayers()) {
                    final User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                      this.updateUser(player, user);
                    }
                  }
                  context.getSource().getSender().sendRichMessage(
                      "<dark_gray>[<#528175>LuckTags</#528175>] <#65AE51>Configuration successfully reloaded.");
                  return Command.SINGLE_SUCCESS;
                }))
            .build()));
  }

  @EventHandler
  public void handleJoin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    this.updateUser(player,
        Objects.requireNonNull(this.luckPerms.getUserManager().getUser(player.getUniqueId())));
  }

  @EventHandler
  public void handleChat(final AsyncChatEvent event) {
    final CachedMetaData meta = this.luckPerms.getUserManager()
        .getUser(event.getPlayer().getUniqueId())
        .getCachedData().getMetaData();

    final Component format = MiniMessage.miniMessage().deserialize(
        Objects.requireNonNull(this.getConfig().getString("chat-format")),
        event.getPlayer(),
        this.resolver(meta, event.getPlayer())
            .resolver(Placeholder.component("message", event.message()))
            .build());

    event.renderer(((source, displayName, message, viewer) -> format));
  }

  private void updateUser(final Player player, final User user) {
    final CachedMetaData meta = user.getCachedData().getMetaData();

    final Component playerListName = MiniMessage.miniMessage().deserialize(
        Objects.requireNonNull(this.getConfig().getString("tab-format")),
        player,
        this.resolver(meta, player).build());
    player.playerListName(playerListName);
    player.setPlayerListOrder(meta.getWeight());

    if (this.getConfig().getBoolean("override-display-name")) {
      player.displayName(playerListName);
    }
  }

  private TagResolver.Builder resolver(final CachedMetaData meta, final Player player) {
    final TagResolver.Builder builder = TagResolver.builder()
        .resolver(Placeholder.parsed("prefix", this.format(meta.getPrefix())))
        .resolver(Placeholder.unparsed("player", player.getName()))
        .resolver(Placeholder.parsed("suffix", this.format(meta.getSuffix())));
    if (this.miniPlaceholders) {
      builder.resolver(MiniPlaceholders.audienceGlobalPlaceholders());
    }
    if (this.placeholderApiResolver != null) {
      builder.resolver(this.placeholderApiResolver);
    }
    return builder;
  }

  private String format(final String value) {
    if (value == null) {
      return "";
    }
    final boolean legacy = value.indexOf(LegacyComponentSerializer.AMPERSAND_CHAR) != -1;
    if (legacy) {
      return MiniMessage.miniMessage()
          .serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(value));
    } else {
      return value;
    }
  }
}
