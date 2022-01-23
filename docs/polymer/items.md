# Items
!!! Note
    These docs will only take care about polymer-related part of creation of items.
    You might want to see [official Fabric Wiki](https://fabricmc.net/wiki/tutorial:items_docs)
    for more in depth look into how to create items. 
    You can skip some client side specific things, as it won't take effect server side (excluding
    item groups, as they can be used by other mods)

## Creation of items

Creation of items is mostly the same as vanilla. Only real difference is that your items need to 
implement Polymer's `PolymerItem` interface. It exposes few defaulted methods for manipulation
of client side visuals.

### Default implementation
For most basic uses, there are default implementation of `PolymerItem`:

* `SimplePolymerItem` - Same as vanilla `Item`,
* `PolymerSpawnEggItem` - Same as vanilla `SpawnEggItem`,
* `PolymerBlockItem` - Same as vanilla `BlockItem`,
* `PolymerHeadBlockItem` - Similar to `VirtualBlockItem`, but for Blocks implementing `VirtualHeadBlock` interface.

### Selecting visual item type.
To select visual item type, you need to implement this method
* `Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player)`

They can't return nulls. They can also point to other PolymerItem instance, but keep
in mind to make validation if it's configurable by user!

Example use:

Changing client-side item to diamond
```
@Override
public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
    return itemStack.getCount() > 32 ? Items.DIAMOND_BLOCK : Items.DIAMOND;
}
```

### Manipulation of client side ItemStack
Sometimes it's useful to manipulate entire ItemStack, as it allows achieving better effects.
To do so, you need to override the `ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player)`
method. However, keep in mind that making nbt incorrect might create some issues (for example 
breaking items in creative mode)!

Ideally you should modify output of `PolymerItem.super.getPolymerItemStack(itemStack, player)`,
`PolymerItemUtils.createItemStack(itemStack, player)`
or `PolymerItemUtils.createMinimalItemStack(itemStack, player)`, as they contain all required NBT.

Example use:

Adding enchanting glint to item.
```
@Override
public ItemStack getPolymerItemStack(ItemStack itemStack, ServerPlayerEntity player) {
    ItemStack out = PolymerItemUtils.createItemStack(itemStack, player);
    out.addEnchantment(Enchantments.LURE, 0);
    return out;
}
```

### Support of models/CustomModelData
You can change custom model data of virtual model by simple 
overriding `int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player)`.
You can return -1 to disable it, or any number above it to set value of it.

Ideally you should return value created with [polymer's resource pack utils](/polymer/resource-packs), 
but nothing blocks you from using any other ones.

Example usage:

Changing client-side item CustomModelData to previously stored value.
```
@Override
public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
    // Instance of PolymerModelData, see info above
    return this.cmd.value();
}
```

## Item Groups support
You can create server side Item Groups, which will be later synced with Polymer-compatible clients.
They also allow you to create server side Creative categories, that are accessible via `/polymer creative` command.

To create, it, you just need to call one of provided `PolymerItemGroup.create(Identifier, Text, [Supplier<ItemStack>])` 
static method. Then you can use it like regular ItemGroup.

## Manipulation of non-polymer items
Sometimes, you might want to manipulate other vanilla/modded items without implementing
`PolymerItem` on them. You can do it by using few events from `PolymerItemUtils`.

### Forcing items to go through Polymer
To force items to go through polymer's client side item creation, you need to register
event handler for `PolymerItemUtils.ITEM_CHECK` event. You can register it by using 
`PolymerItemUtils.ITEM_CHECK.register(ItemStack -> boolean)` lambda.

Example use:

Making every item with string NBT tag of "Test" go through polymer
```
PolymerItemUtils.ITEM_CHECK.register(
    (itemStack) -> {
        return itemStack.hasNbt() && itemStack.getNbt().contains("Test", NbtElement.STRING_TYPE);
    }
);
```

### Modification of Client side item
After getting vanilla (or for any VirtualItem by default) you can modify any client side item
with `PolymerItemUtils.ITEM_MODIFICATION_EVENT` event. Just keep in mind doing it incorrectly
can cause issues (mostly around creative mode, but also in case you modify original item).
You change the client side item by either directly modifying `virtual` ItemStack 
or creating new one and returning it. Ideally you should also keep previous nbt, 
just so it can work nicely, You can register this event by using
`PolymerItemUtils.ITEM_MODIFICATION_EVENT.register(((ItemStack original, ItemStack client, ServerPlayerEntity player) -> ItemStack)` lambda.

Example use:

Hiding enchantment glint for items with `HideEnchantments: 1b` nbt tag
```
PolymerItemUtils.ITEM_MODIFICATION_EVENT.register(
    (original, virtual, player) -> {
         if (original.hasNbt() && original.getNbt().getBoolean("HideEnchantments")) {
             virtual.getNbt().remove("Enchantments");

         }
         return virtual;
    }
);
```

Replacing look/name of ItemStack with "Test" NBT tag
```
PolymerItemUtils.ITEM_MODIFICATION_EVENT.register(
    (original, virtual, player) -> {
         if (original.hasNbt() && original.getNbt().contains("Test", NbtElement.STRING_TYPE)) {
             ItemStack out = new ItemStack(Items.DIAMOND_SWORD, virtual.getCount());
             out.setNbt(virtual.getNbt());
             out.setCustomName(new LiteralText("TEST VALUE: " + original.getNbt().getString("Test")).formatted(Formatting.WHITE));
             return out;
         }
         return virtual;
    }
);
```

### Making items mining calculated on server side
You can also force item's mining speed to be calculated server side 
(which happens by default to every VirtualItem).

Only thing you need to do is just listening to `BlockHelper.SERVER_SIDE_MINING_CHECK` event.

Example use:
```
PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.register(
    (player, pos, blockState) -> {
         var itemStack = player.getMainHandStack();
         return EnchantmentHelper.getLevel(MyEnchanments.SLOW_MINING, itemStack) > 0;
    }
);
```

## Enchantments
The only thing to make your enchantment fully server side is implementation 
of `PolymerObject` interface. You also might want to manipulate some things from Polymer Block/Item events.