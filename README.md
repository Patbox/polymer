# Polymer
It's a library for creating server side content, that work for player's without mods or (required) resource packs!
You can create blocks, items and entities, that not only will work fully on server side (and singleplayer), but also
are represented on server the same as normal (vanilla/modded) ones (unlike bukkit/spigot ones, that are stored as vanilla block).

This library also should work correctly with other, non-polymer mods and PolyMC!

## For players:
This library/mod includes few small utilities that can be useful for playing on servers using Polymer.

Few things it does on the client:
- Added compatibility with mods like Roughly Enough Items and WTHIT
- Correct information on F3 debug screen
- Full support for item picking
- Fixes for server resource pack while using Canvas or Iris

## For mod developers:
All information about usage can be found at https://polymer.pb4.eu/!

## What this library/mod doesn't do
This mod doesn't convert existing mods to server side ones. While it's possible to do so with manual coding,
for that you might want to use [PolyMC](https://github.com/TheEpicBlock/PolyMc) as long as you don't 
need to use regular mods on the client (for example with a modpack).