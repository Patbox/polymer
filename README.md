# Polymer
It's a library for creating server side content, that work for player's without mods or (required) resource packs!
You can create blocks, items and entities, that not only will work fully on server side (and singleplayer), but also
are represented on server the same as normal (vanilla/modded) ones (unlike bukkit/spigot ones, that are stored as vanilla block).

This library also should work correctly with other, non-polymer mods and PolyMC!

## For players:
This library/mod includes few small utilities that can be useful for playing on servers using Polymer.

Few things it does on the client:

- Added compatibility to multiple client side mods ([EMI](https://modrinth.com/mod/emi),
  [Just Enough Items](https://www.curseforge.com/minecraft/mc-mods/jei),
  [Roughly Enough Items](https://modrinth.com/mod/roughly-enough-items), [WTHIT](https://modrinth.com/mod/wthit), 
  [Jade](https://www.curseforge.com/minecraft/mc-mods/jade), [AppleSkin](https://modrinth.com/mod/appleskin))
- Correct information on F3 debug screen
- Full support for Polymer quick block picking (middle mouse click)
- Creative item tabs synced with server
- Fixes (some) server resource pack features broken by [Canvas or Iris](https://github.com/IrisShaders/Iris/issues/1042)
  (custom armor texutes)

## For server owners/mod pack makers:
Any mod using this library shouldn't be required on the client side! However, as stated above, there
are few small quality of life things added with it. So you might want to recommend it or include with
your mod pack.

If you have a server and polymer based mods you are using include a resource pack, you might want since version 0.2.5
"packed/all" version of polymer contains AutoHost module, allowing you to setup automatic building and sending of packs
to clients.

If you use 1.18.x or older, use this instead: https://github.com/aws404/polypack-host

## For mod developers:
All information about usage can be found at https://polymer.pb4.eu!

## What this library/mod doesn't do
This mod doesn't convert existing mods to server side ones. While it's possible to do so with manual coding,
for that you might want to use [PolyMC](https://github.com/TheEpicBlock/PolyMc) as long as you don't 
need to use regular mods on the client (for example with a modpack).

## Commands
- `/polymer` - Display about
- `/polymer creative` - Opens list of Polymer/Server-Side creative tabs (Available to anyone with creative)
- `/polymer generate-pack` - Generates polymer resourcepack as `<server/client directory>/polymer-resourcepack.zip`
- `/polymer stats` - Shows server side statistics
- `/polymer effects` - Shows server side status effects
- `/polymer client-item` - Displays client side nbt of held item
- `/polymer client-item get` - Gives player client side copy of held item
- `/polymer export-registry` - Exports registry information (debug command)

## Download
- [Modrinth](https://modrinth.com/mod/polymer)
- [Curseforge](https://www.curseforge.com/minecraft/mc-mods/polymer)
- [Github Releases](https://github.com/Patbox/polymer/releases)

### [Check list of known mods using Polymer](MODS.md)