# About Polymer
It's a set of libraries designed for creation of server side content, that work for player's without mods or (required) resource packs!
You can create blocks, items and entities, that not only will work fully on server side (and singleplayer), but also
are represented on server the same as normal (vanilla/modded) ones allowing for way better mod compatibility and way less weird edge-cases.

It is also should be fully compatible with close to all other mods and in case of found issues they are patched as soon as possible.

## Adding as dependency:
Add it to your dependencies like this:

```groovy
repositories {
	maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
	modImplementation include("eu.pb4:polymer-core:[TAG]")
	modImplementation include("eu.pb4:polymer-resource-pack:[TAG]")
	modImplementation include("eu.pb4:polymer-blocks:[TAG]")
}
```

For `[TAG]`/polymer version I recommend you checking [this maven](https://maven.nucleoid.xyz/eu/pb4/polymer-core/).

Latest version: ![version](https://img.shields.io/maven-metadata/v?color=%23579B67&label=&metadataUrl=https://maven.nucleoid.xyz/eu/pb4/polymer-core/maven-metadata.xml)

All modules use the same version numbers.

### Updating from 0.2.x
With Minecraft 1.19.3 being globally breaking update. I took this as a good time to make breaking changes to polymer itself.

[See this update guide for more informations!](/other/updating-0.2.x-to-0.3)

## Before starting
There are few things you need to keep in mind while using Polymer. 
All your code that interacts with Polymer should:

* Be thread safe - code can run on main server thread, player's connection thread 
  or client side rendering thread.
* Make sure to check every time you cast if it's really instance of it. Sometimes `World` won't be `ServerWorld`.
* Never implement Polymer interfaces on Vanilla Items/Blocks with mixins, it will end up really, really badly.
* Never add new BlockStates to non-polymer blocks, as it will cause desyncs (see previous point)!
* Please don't even try using registry replacement, it will break many other mods (and polymer itself).

Polymer is split into multiple libraries with varying functionality.

## Modules

### Polymer Core
`eu.pb4:polymer-core`

It's a heart of Polymer. It allows you to create server side content. It also contains lots of extra mod compatibility for client mods,
to make your mod better fit for any modpack.

* [Items](/polymer/items)
* [Blocks](/polymer/blocks)
* [Entities](/polymer/entities)
* [Other custom features](/polymer/other)
* [(Optional) Client Side features](/polymer/client-side)

### Polymer Resource Pack
`eu.pb4:polymer-resource-pack`

Allows creating global (and mod specific) resource packs. It also patches PolyMc to make it's resource generation
work with polymer.

* [Basics](/docs/polymer-resource-pack/basics.md)

### Polymer Blocks
`eu.pb4:polymer-blocks`

Extension of Polymer Core and Resource Pack. Allows creation of textured blocks.

Depends on Polymer Core and Polymer Resource Pack.
* [Basics](/docs/polymer-blocks/basics.md)

## Other useful tools/projects compatible with Polymer
* [Server Translation API](/other/server-translation-api)
