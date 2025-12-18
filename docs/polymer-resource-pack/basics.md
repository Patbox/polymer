# Basics

## Registering assets
This is quite simple. You just need to do things written below. Ideally it all should run at
your mod's initialization.

### Adding mod assets to resource pack
First step for adding assets to resource pack is marking mod as asset source. To do it you just
need to call `boolean PolymerResourcePackUtils.addModAssets(String modid)`, which 
returns `true` if `modid` is valid.

This should be called ideally in your mod initializer.

Additionally, you can add assets manually by calling `ResourcePackBuilder.addData(String path, byte[] data)`.
You can get instance of it by listening to `PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT`.
Just keep in minds that new one will be created every time resource pack is generated.

## Checking players
Checking if player has resource pack is quite simple. 
You just need to call `boolean PolymerResourcePackUtils.hasPack(ServerPlayerEntity player)`.

Example use:

```
Identifier font;

if (PolymerResourcePackUtils.hasPack(player)) {
    font = Identifier.of("mymod", "myfont");
} else {
    font = Identifier.of("minecraft", "default");
}
```

### Making pack required
To make font required, you just need to call `PolymerResourcePackUtil.markAsRequired()`.

However, Polymer doesn't contain any utilities for sending packs, as it should be implemented by other mods (or use vanilla one).
One exception is resource pack on client, which will get effected by that.

I also recommend you to keep it optional if it's possible.

## Building resource pack
To create resource pack you only need to execute `/polymer generate-pack` command.
Resource pack will be located in your server folder as `polymer/resourcepack.zip`.

## Auto-host
If you'd like Polymer to automatically serve the resource pack to players, make sure to call `PolymerResourcePackUtils.addModAssets` or add your assets manually as shown before, and make sure to add the following as a dependency:
```groovy
modImplementation include("eu.pb4:polymer-autohost:[TAG]")
```
For `[TAG]`/autohost version I recommend you checking [this maven](https://maven.nucleoid.xyz/eu/pb4/polymer-autohost/).

Latest version: ![version](https://img.shields.io/maven-metadata/v?color=%23579B67&label=&metadataUrl=https://maven.nucleoid.xyz/eu/pb4/polymer-autohost/maven-metadata.xml)
