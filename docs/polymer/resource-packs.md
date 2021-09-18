# Resource packs
While Polymer wasn't designed around resource pack, it provides simple resource pack 
utilities to make it easier for mods to stay compatible with each other. Currently, it's limited
only to item models, as Vanilla only allows these (as of 1.17.1 version). 

## Registering assets
This is quite simple. You just need to do things written below. Ideally it all should run at
your mod's initialization.

### Adding mod assets to resource pack
First step for adding assets to resource pack is marking mod as asset source. To do it you just
need to call `boolean ResourcePackUtils.addModAsAssetsSource(String modid)`, which 
returns `true` if `modid` is valid.

This should be called ideally in your mod initializer.

### Requesting model for item
After that you can register your models by calling 
`CMDInfo ResourcePackUtils.requestCustomModelData(Item vanillaItem, Identifier modelPath)`.
It returns `CMDInfo` with contains all information you need for applying custom model data
to your items.

You can execute this function before making your mod an asset source, but it should be run before
resource pack is build.

Example use:

```
CMDInfo cmdInfo = ResourcePackUtils.requestCustomModelData(Items.IRON_SWORD, new Identifier("mymod", "silver_sword"));
```

## Checking players
Checking if player has resource pack is quite simple. 
You just need to call `boolean ResourcePackUtils.hasPack(ServerPlayerEntity player)`.

Example use:

```
Identifier font;

if (ResourcePackUtils.hasPack(player)) {
    font = new Identifier("mymod", "myfont");
} else {
    font = new Identifier("minecraft", "default");
}
```

### Making pack required
To make font required, you just need to call `ResourcePackUtil.markAsRequired()`.

However, Polymer doesn't contain any utilities for sending packs, as it should be implemented by other mods (or use vanilla one).
One exception is resource pack on client, which will get effected by that.

I also recommend you to keep it optional if it's possible.

## Building resource pack
To create resource pack you only need to execute `/polymer generate` command. Resource pack will be located in `polymer-resourcepack/output` folder.
