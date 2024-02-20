# Getting Started

This is the first and most important library for server side development.

It allows you to create blocks, items and entities, that not only will work fully on server side (and singleplayer), but also
are represented on server the same as normal (vanilla/modded) ones allowing for way better mod compatibility and way less weird edge-cases.

This library also handles all mod compatibility with client only mods (when it's present), so it can be safely used in a modpack.

### Adding to dependencies
```groovy
repositories {
	maven { url 'https://maven.nucleoid.xyz' } // You should have it
}

dependencies {
	modImplementation include("eu.pb4:polymer-core:[TAG]")
}
```

For `[TAG]`/polymer-core version I recommend you checking [this maven](https://maven.nucleoid.xyz/eu/pb4/polymer-core/).

Latest version: ![version](https://img.shields.io/maven-metadata/v?color=%23579B67&label=&metadataUrl=https://maven.nucleoid.xyz/eu/pb4/polymer-core/maven-metadata.xml)

## Before starting
There are few things you need to keep in mind while using Polymer.
All your code that interacts with Polymer should:

* Be thread safe - code can run on main server thread, player's connection thread
  or client side rendering thread.
* Make sure to check every time you cast if it's really instance of it. Sometimes `World` won't be a `ServerWorld` instance.
* Never implement Polymer interfaces on Vanilla Items/Blocks with mixins, it will end up really, really badly.
* Never add new BlockStates to non-polymer blocks, as it will cause desyncs (see previous point)!
* Please don't even try using registry replacement, it will break many other mods (and polymer itself).

Polymer is split into multiple libraries with varying functionality.