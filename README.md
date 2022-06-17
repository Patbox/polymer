# Polymer
It's a library for creating server side content, that work for player's without mods or (required) resource packs!
You can create blocks, items and entities, that not only will work fully on server side (and singleplayer), but also
are represented on server the same as normal (vanilla/modded) ones (unlike bukkit/spigot ones, that are stored as vanilla block).

This library also should work correctly with other, non-polymer mods and PolyMC!

## For players:
This library/mod includes few small utilities that can be useful for playing on servers using Polymer.

Few things it does on the client:

- Added compatibility to multiple client mods (Roughly Enough Items, Just Enough Items, WTHIT, Jade, AppleSkin*)
- Correct information on F3 debug screen
- Full support for item picking
- Creative item tabs
- Fixes for server resource pack while using Canvas or Iris

## For server owners/mod pack makers:
Any mod using this library shouldn't be required on the client side! However, as stated above, there
are few small quality of life things added with it. So you might want to recommend it or include with
your mod pack.

If you have a server and polymer based mods you are using include a resource pack, you might want 
to additionally use this mod: https://github.com/aws404/polypack-host

## For mod developers:
All information about usage can be found at https://polymer.pb4.eu!

## What this library/mod doesn't do
This mod doesn't convert existing mods to server side ones. While it's possible to do so with manual coding,
for that you might want to use [PolyMC](https://github.com/TheEpicBlock/PolyMc) as long as you don't 
need to use regular mods on the client (for example with a modpack).

## Commands
- `/polymer` - Display about
- `/polymer creative` - Opens list of Polymer/Server-Side creative tabs (Available to anyone with creative)
- `/polymer generate` - Generates polymer resourcepack as `<server/client directory>/polymer-resourcepack.zip`

## Download
- [Modrinth](https://modrinth.com/mod/polymer)
- [Curseforge](https://www.curseforge.com/minecraft/mc-mods/polymer)
- [Github Releases](https://github.com/Patbox/polymer/releases)

### [Check list of known mods using Polymer](MODS.md)