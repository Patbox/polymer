!!! Warning!
    These docs are written for unstable, in development version!
    You might want to check https://polymer.pb4.eu/0.1.x/ instead
    They are also quite incomplete, so you might want to look
    through api package yourself

# About Polymer
It's a library for creating server side content, that work for player's without mods or (required) resource packs!
You can create blocks, items and entities, that not only will work fully on server side (and singleplayer), but also
are represented on server the same as normal (vanilla/modded) ones (unlike bukkit/spigot ones, that are stored as vanilla block).

This library also should work correctly with other, non-polymer mods and PolyMC!

## Adding as dependency:
Add it to your dependencies like this:

```groovy
repositories {
	maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
	modImplementation include("eu.pb4:polymer:[TAG]")
}
```

For `[TAG]`/polymer version I recommend you checking [this maven](https://maven.nucleoid.xyz/eu/pb4/polymer/).

## Before starting
There are few things you need to keep in mind while using Polymer. 
All your code that interacts with Polymer should:

* Be thread safe - code can run on main server thread, player's connection thread 
  or client side rendering thread.
* Make sure to check every time you cast if it's really instance of it. Sometimes `World` won't be `ServerWorld`.
* Never implement Polymer interfaces on Vanilla Items/Blocks with mixins, it will end up really, really badly.
* Never add new BlockStates to non-polymer blocks, as it will cause desyncs (see previous point)!
* Please don't even try using registry replacement, it will break many other mods.

## Getting started

* [Items](/polymer/items)
* [Blocks](/polymer/blocks)
* [Entities](/polymer/entities)
* [Resource Packs](/polymer/resource-packs)

Other useful tools/projects compatible with Polymer

* [Server Translation API](/other/server-translation-api)