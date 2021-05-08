# Polymer
It's a library for creating server side content, that work for player's without mods or (required) resource packs!

This library is its alpha stages, however it should be stable. Feel free to suggest changes/improvements!

## Usage:
Add it to your dependencies like this:

```
repositories {
	maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
	modImplementation include("eu.pb4.polymer:[TAG]").
}
```

After that, it's as easy as making your items implement `VirtualItem`, blocks `VirtualBlock` or 
`VirtualHeadBlock` and entities `VirtualEntity`. Additionally, you need to implement `VirtualObject` on your enchantments and recipe serializers.
It's also recommended registering block entities with `PolymerMod.registerVirtualBlockEntity(Identifier)`.

### If you are a server owner, you most likely wanted to get [PolyMC](https://github.com/TheEpicBlock/PolyMc)

Some code in this library is based on [PolyMC](https://github.com/TheEpicBlock/PolyMc). So if you are using Polymer, give a star to PolyMC too! 