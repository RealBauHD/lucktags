# LuckTags
LuckPerms addon for tab and chat prefixes

## Benefits
- easily managed through LuckPerms Meta
- integration with other LuckPerms Addons
- no Vault plugin needed
- no scoreboard teams (no issues with other plugins using the scoreboard)
- [MiniMessage](https://docs.advntr.dev/minimessage/format.html) support
- [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) support (e. g. ``<papi:player_x>``)
- [MiniPlaceholders](https://modrinth.com/plugin/miniplaceholders) support

## Example Commands
- ``/lucktags reload`` - reloads the configuration - Permission: ``lucktags.reload``


- ``/lp group admin meta setprefix 100 "<dark_red>Admin </dark_red>"``
- ``/lp group <group> meta setprefix <priority> <prefix>``
- ``/lp group <group> meta setsuffix <priority> <suffix>``
- ``/lp user <user> meta setprefix <priority> <prefix>``
- ``/lp user <user> meta setsuffix <priority> <suffix>``

## Requirements
- Java 21+
- Paper or Paper fork 1.21.7+
- [LuckPerms](https://luckperms.net/)

## Default configuration

```
# LuckTags plugin v1.4 by BauHD
# Modrinth: https://modrinth.com/project/lucktags
# Discord: https://discord.gg/Gmxwzz2rA9

# Supports MiniMessage: https://docs.advntr.dev/minimessage/format.html
chat-format: "<prefix><player><suffix><dark_gray>: <gray><message>"
tab-format: "<prefix><player><suffix>"

override-display-name: false
```