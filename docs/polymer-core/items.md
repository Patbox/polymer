# Items
!!! Note
    These docs will only take care about polymer-related part of creation of items.
    You might want to see [official Fabric Docs](https://docs.fabricmc.net/develop/items/first-item)
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
* `PolymerHeadBlockItem` - Similar to `PolymerBlockItem`, but for Blocks implementing `PolymerHeadBlock` interface.

### Selecting base item type.
To select base item type, you need to implement this method
* `Item getPolymerItem(ItemStack itemStack, PacketContext context)`

It can't return nulls. They can also point to other PolymerItem instance, but keep
in mind to make validation if it's configurable by user!

Example use:

Changing client-side item to diamond
```
@Override
public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
    return itemStack.getCount() > 32 ? Items.DIAMOND_BLOCK : Items.DIAMOND;
}
```

### Changing item model
By default, Polymer will use whatever the `item_model` component is set for the item, which is  equal to the value 
you provided in Item.Settings, as long as ItemStack doesn't override it.

For better control over it, you can override `Identifier getPolymerItemModel(ItemStack itemStack, PacketContext context)`
method.

### Manipulation of client side ItemStack
Sometimes it's useful to manipulate entire ItemStack, as it allows achieving better effects.
To do so, you need to override the `void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context)`
method. You can only modify the `out` item stack, as it's item that gets sent to the client.
The `stack` item is what server sends by default and should never be modified.

If you need more control, you can also override `ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType type, PacketContext context)`,
however doing it incorrectly will result in items converting to vanilla ones in creative and desyncing in inventory.

## Item Groups support
You can create server side Item Groups, which will be later synced with Polymer-compatible clients.
They also allow you to create server side Creative categories, that are accessible via `/polymer creative` command.

To create, it, you just need to call one of provided `PolymerItemGroupUtils.builder()` static method.
Then you can create it just like regular ItemGroup, but instead of registering into vanilla registry, you use
`PolymerItemGroupUtils.registerPolymerItemGroup(Identifier id, ItemGroup group)

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
After getting vanilla (or for any PolymerItem by default) you can modify any client side item
with `PolymerItemUtils.ITEM_MODIFICATION_EVENT` event. Just keep in mind doing it incorrectly
can cause issues (mostly around creative mode, but also in case you modify original item).
You change the client side item by either directly modifying client ItemStack 
or creating new one and returning it. Ideally you should also keep previous nbt, 
just so it can work nicely, You can register this event by using
`PolymerItemUtils.ITEM_MODIFICATION_EVENT.register(((ItemStack original, ItemStack client, PacketContext context) -> ItemStack)` lambda.

Example use:

Hiding enchantment glint for items with `HideEnchantments: 1b` nbt tag
```
PolymerItemUtils.ITEM_MODIFICATION_EVENT.register(
    (original, client, context) -> {
         if (original.hasNbt() && original.getNbt().getBoolean("HideEnchantments")) {
             client.getNbt().remove("Enchantments");

         }
         return client;
    }
);
```

Replacing look/name of ItemStack with "Test" NBT tag
```
PolymerItemUtils.ITEM_MODIFICATION_EVENT.register(
    (original, client, context) -> {
         if (original.hasNbt() && original.getNbt().contains("Test", NbtElement.STRING_TYPE)) {
             ItemStack out = new ItemStack(Items.DIAMOND_SWORD, client.getCount());
             out.setNbt(client.getNbt());
             out.setCustomName(new LiteralText("TEST VALUE: " + original.getNbt().getString("Test")).formatted(Formatting.WHITE));
             return out;
         }
         return client;
    }
);
```

### Making items mining calculated on server side
You can also force item's mining speed to be calculated server side 
(which happens by default to every PolymerItem).

Only thing you need to do is just listening to `PolymerBlockUtils.SERVER_SIDE_MINING_CHECK` event.

Example use:
```
PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.register(
    (player, pos, blockState) -> {
         var itemStack = player.getMainHandStack();
         return EnchantmentHelper.getLevel(MyEnchanments.SLOW_MINING, itemStack) > 0;
    }
);
```
