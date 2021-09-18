# Items
!!! Note
    These docs will only take care about polymer-related part of creation of items.
    You might want to see [official Fabric Wiki](https://fabricmc.net/wiki/tutorial:items_docs)
    for more in depth look into how to create items. 
    You can skip some client side specific things, as it won't take effect server side (excluding
    item groups, as they can be used by other mods)

## Creation of items

Creation of items is mostly the same as vanilla. Only real difference is that your items need to 
implement Polymer's `VirtualItem` interface. It exposes few defaulted methods for manipulation
of client side visuals.

### Default implementation
For most basic uses, there are default implementation of `VirtualItem`:

* `BasicVirtualItem` - Same as vanilla `Item`,
* `VirtualBlockItem` - Same as vanilla `BlockItem`,
* `VirtualHeadBlockItem` - Similar to `VirtualBlockItem`, but for Blocks implementing `VirtualHeadBlock` interface.

### Selecting visual item type.
To select visual item type, there are 1 required and 1 optional methods you need to override:

* `Item getVirtualItem()` - It works for everything by default, mostly in cases where ItemStack isn't available,
* `Item getVirtualItem(ItemStack itemStack, @Nullable ServerPlayerEntity player)` - Used in most cases, it includes reference to ItemStack and Player, by default it redirects to `getVirtualItem()`

Both of these methods can't return null. They can also point to other VirtualItem instance, but keep
in mind to make validation if it's configurable by user!

Example use:

Changing client-side item to diamond
```
@Override
public Item getVirtualItem() {
    return Items.DIAMOND;
}
```

Changing client-side item to diamond blocks if there is more than 32 in ItemStack, otherwise to diamonds

```
@Override
public Item getVirtualItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
    return itemStack.getCount() > 32 ? Items.DIAMOND_BLOCK : Items.DIAMOND;
}
```

### Manipulation of client side ItemStack
Sometimes it's useful to manipulate entire ItemStack, as it allows achieving better effects.
To do so, you need to override the `ItemStack getVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player)`
method. However, keep in mind that making nbt incorrect might create some issues (for example 
breaking items in creative mode)!

Ideally you should modify output of `VirtualItem.super.getVirtualItemStack(itemStack, player)`,
`ItemHelper.createBasicVirtualItemStack(itemStack, player)`
or `ItemHelper.createMinimalVirtualItemStack(itemStack, player)`, as they contain all required NBT.

Example use:

Adding enchanting glint to item.
```
@Override
public ItemStack getVirtualItemStack(ItemStack itemStack, ServerPlayerEntity player) {
    ItemStack out = VirtualItem.super.getVirtualItemStack(itemStack, player);
    out.addEnchantment(Enchantments.LURE, 0);
    return out;
}
```

### Support of models/CustomModelData
You can change custom model data of virtual model by simple 
overriding `int getCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player)`.
You can return -1 to disable it, or any number above it to set value of it.

Ideally you should return value created with [polymer's resource pack utils](/polymer/resource-packs), 
but nothing blocks you from using any other ones.

Example usage:

Changing client-side item CustomModelData to previously stored value.
```
@Override
public int getCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
    // Instance of CMDInfo, see info above
    return this.cmd.value();
}
```


## Manipulation of non-virtual items
Sometimes, you might want to manipulate other vanilla/modded items without implementing
`VirtualItem` on them. You can do it by using few events from `ItemHelper`.

### Forcing items to go through Polymer
To force items to go through polymer's client side item creation, you need to register
event handler for `ItemHelper.VIRTUAL_ITEM_CHECK` event. You can register it by using 
`ItemHelper.VIRTUAL_ITEM_CHECK.register(ItemStack -> boolean)` lambda.

Example use:

Making every item with string NBT tag of "Test" go through polymer
```
ItemHelper.VIRTUAL_ITEM_CHECK.register(
    (itemStack) -> {
        return itemStack.hasNbt() && itemStack.getNbt().contains("Test", NbtElement.STRING_TYPE);
    }
);
```

### Modification of Virtual/Client side item
After getting vanilla (or for any VirtualItem by default) you can modify any client side item
with `ItemHelper.VIRTUAL_ITEM_MODIFICATION_EVENT` event. Just keep in mind doing it incorrectly
can cause issues (mostly around creative mode, but also in case you modify original item).
You change the client side item by either directly modifying `virtual` ItemStack 
or creating new one and returning it. Ideally you should also keep previous nbt, 
just so it can work nicely, You can register this event by using
`ItemHelper.VIRTUAL_ITEM_MODIFICATION_EVENT.register(((ItemStack original, ItemStack virtual, ServerPlayerEntity player) -> ItemStack)` lambda.

Example use:

Hiding enchantment glint for items with `HideEnchantments: 1b` nbt tag
```
ItemHelper.VIRTUAL_ITEM_MODIFICATION_EVENT.register(
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
ItemHelper.VIRTUAL_ITEM_MODIFICATION_EVENT.register(
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
BlockHelper.SERVER_SIDE_MINING_CHECK.register(
    (player, pos, blockState) -> {
         var itemStack = player.getMainHandStack();
         return EnchantmentHelper.getLevel(MyEnchanments.SLOW_MINING, itemStack) > 0;
    }
);
```

## Enchantments
The only thing to make your enchantment fully server side is implementation 
of `VirtualObject` interface. You also might want to manipulate some things from Polymer Block/ItemHelper events.