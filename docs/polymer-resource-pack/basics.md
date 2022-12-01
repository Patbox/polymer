# Basics of Polymer Resource Pack


While Polymer wasn't originally designed around resource pack, it provides resource pack 
utilities to make it easier for mods to stay compatible with each other. Currently, it's limited
only to item models, as Vanilla only allows these (as of 1.17.1 version). 

## Registering assets
This is quite simple. You just need to do things written below. Ideally it all should run at
your mod's initialization.

### Adding mod assets to resource pack
First step for adding assets to resource pack is marking mod as asset source. To do it you just
need to call `boolean PolymerResourcePackUtils.addModAssets(String modid)`, which 
returns `true` if `modid` is valid.

This should be called ideally in your mod initializer.

Additionally, you can add assets manually by calling `PolymerRPBuilder.addData(String path, byte[] data)`.
You can get instance of it by listening to `PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT`.
Just keep in minds that new one will be created every time resource pack is generated.

### Requesting model for item
After that you can register your models by calling 
`PolymerModelData PolymerResourcePackUtils.requestModel(Item vanillaItem, Identifier modelPath)`.
It returns `PolymerModelData` with contains all information you need for applying custom model data
to your items. You need to keep in mind, that modelPath needs to contain main directory (in similar way
to vanilla models). While model is created, all it's overrides are copied and applied, so you don't need to
request them manually (useful for bows).

You can execute this function before making your mod an asset source, but it should be run before
resource pack is build.

Example use:

```
PolymerModelData modelData = PolymerResourcePackUtils.requestModel(Items.IRON_SWORD, new Identifier("mymod", "item/silver_sword"));
```

### Requesting armor textures
Polymer supports custom armor textures thanks to usage of [Ancientkingg's fancyPants resource pack](https://github.com/Ancientkingg/fancyPants).

To request it you need to use `PolymerResourcePackUtils.requestArmor(Identifier)`. 
It will automatically create variant of every armor peace, however you aren't 
required to use/define them all.

To apply it to your armor, you need to set your client side item to leather armor peace.
Then you need to override `PolymerItem.getPolymerArmorColor()` method and return used color.

```
PolymerArmorModel armorModel = PolymerResourcePackUtils.requestArmor(new Identifier("mymod", "silver"));
```

## Checking players
Checking if player has resource pack is quite simple. 
You just need to call `boolean PolymerResourcePackUtils.hasPack(ServerPlayerEntity player)`.

Example use:

```
Identifier font;

if (PolymerResourcePackUtils.hasPack(player)) {
    font = new Identifier("mymod", "myfont");
} else {
    font = new Identifier("minecraft", "default");
}
```

### Making pack required
To make font required, you just need to call `PolymerResourcePackUtil.markAsRequired()`.

However, Polymer doesn't contain any utilities for sending packs, as it should be implemented by other mods (or use vanilla one).
One exception is resource pack on client, which will get effected by that.

I also recommend you to keep it optional if it's possible.

## Building resource pack
To create resource pack you only need to execute `/polymer generate-pack` command. Resource pack will be located in your server folder as `polymer-resourcepack.zip`.
