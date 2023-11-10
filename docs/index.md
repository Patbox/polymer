# About Polymer
It's a set of libraries designed for creation of server side content, that work for player's without mods or (required) resource packs!
You can create blocks, items and entities, that not only will work fully on server side (and singleplayer), but also
are represented on server the same as normal (vanilla/modded) ones allowing for way better mod compatibility and way less weird edge-cases.

It is also should be fully compatible with close to all other mods and in case of found issues they are patched as soon as possible.

## Modules

### Polymer Core
`eu.pb4:polymer-core`

It's the heart of Polymer. It allows you to create server side content. It also contains lots of extra mod compatibility for client mods,
to make your mod better fit for any modpack.

* [Getting Started](polymer-core/getting-started)
* [Items](polymer-core/items)
* [Blocks](polymer-core/blocks)
* [Entities](polymer-core/entities)
* [Other custom features](polymer-core/other)
* [(Optional) Client Side features](polymer-core/client-side)

### Polymer Resource Pack
`eu.pb4:polymer-resource-pack`

Allows creating global (and mod specific) resource packs. It also patches PolyMc to make it's resource generation
work with polymer.

* [Getting Started](polymer-resource-pack/getting-started)
* [Basics](polymer-resource-pack/basics)

### Polymer Networking
`eu.pb4:polymer-networking`

Polymer's Networking API. Uses its own custom synchronization code. Contains extra API, more specific that aren't available in fabric.

* [Getting Started](polymer-networking/getting-started)

### Polymer Virtual Entity
`eu.pb4:polymer-virtual-entity`

Allows you to create virtual/packet based entities in a quick and simple way, with support for
attaching to any entity and chunks.

* [Getting Started](polymer-virtual-entity/getting-started)
* [Basics](polymer-virtual-entity/basics)

### Polymer Blocks
`eu.pb4:polymer-blocks`

Extension of Polymer Core and Resource Pack. Allows creation of textured blocks.

* [Getting Started](polymer-blocks/getting-started)
* [Basics](polymer-blocks/basics)

## Other useful tools/projects compatible with Polymer
* [Server Translation API](https://github.com/NucleoidMC/Server-Translations)
