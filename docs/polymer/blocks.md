# Blocks
!!! Note
    These docs will only take care about polymer-related part of creation of blocks.
    You might want to see [official Fabric Wiki](https://fabricmc.net/wiki/tutorial:blocks)
    for more in depth look into how to create blocks. 
    You can skip some client side specific things, as it won't take effect server side 
    (for example models and textures).

## Creation of blocks

Creation of blocks, similarly to items, is mostly the same as vanilla. Only real difference is that your blocks need to 
implement Polymer's `VirtualBlock` interface. It exposes few defaulted methods for manipulation
of client side visuals.

### Default implementation
For most basic uses, there are default implementation of `VirtualBlock`:

* `VirtualHeadBlock` - It's an interface (!), that has basic implementation of player head based blocks, you still need to apply it to your Block class,
* `BasicVirtualBlock` - Same as vanilla `Block`.

### Selecting base visual block type.
To change base block, you need to override 1 required method. There is also additional, 
optional one with more context, which you can also use.
* `Block getVirtualBlock()` - It works for everything by default, mostly when more context isn't available,
* `Block getVirtualBlock(BlockPos pos, World world)` - Used in cases where position context is available. It's only used in packets that don't contain BlockStates. By default, it redirects to `getVirtualBlock()`

Both of these methods can't return null. They can also point to other VirtualBlock instances, but keep
in mind to make validation if it's configurable by user!

Example use:

Making block look like a diamond
```
@Override
public Block getVirtualBlock() {
    return Blocks.DIAMOND_BLOCK;
}
```

### Changing client-side and collision BlockStates
If you want to change what BlockState will be used for server side collision 
and client side you need to override `BlockState getVirtualBlockState(BlockState state)` method.
You can return other BlockState of VirtualBlock, but keep in mind you can only nest them
up to 32!

Example use:

Changing BlockState to furnace with the same facing, but inverted "lit" BlockState property
```
@Override
public BlockState getVirtualBlockState(BlockState state) {
    return Blocks.FURNACE.getDefaultState()
            .with(AbstractFurnaceBlock.FACING, state.get(AbstractFurnaceBlock.FACING))
            .with(AbstractFurnaceBlock.LIT, !state.get(AbstractFurnaceBlock.LIT));
}
```

### Sending additional data (signs/heads or even custom)
In case if you want to send additional (to more customize look on client for signs/heads 
or additional data for companion mod), you need to override `sendPacketsAfterCreation(ServerPlayerEntity player, BlockPos pos, BlockState blockState)`.
Technically you can do anything there, but ideally it should be only used for packets.

Example use:

Sending data required to render player head with skin
```
@Override
public void sendPacketsAfterCreation(ServerPlayerEntity player, BlockPos pos, BlockState blockState) {
    NbtCompound main = new NbtCompound();
    NbtCompound skullOwner = SomeHelper.getSkullOwnerFor(this);
    main.putString("id", "minecraft:skull");
    main.put("SkullOwner", skullOwner);
    main.putInt("x", pos.getX());
    main.putInt("y", pos.getY());
    main.putInt("z", pos.getZ());    
    player.networkHandler.sendPacket(new BlockEntityUpdateS2CPacket(pos, 4, main));
}
```

### Using VirtualHeadBlock
`VirtualHeadBlock` is an interface extending VirtualBlock with methods prepared for 
usage of player heads as a block. To modify texture, you just need to override 
`String getVirtualHeadSkin(BlockState state)` which should return texture value.

To generate it you can use websites like ttps://mineskin.org/.

Additionally, you can override `BlockState getVirtualBlockState(BlockState state)` 
to change rotation of Player Head Block.

Example use:

Setting skin value for VirtualHeadBlock 
```
@Override
public BlockState getVirtualBlockState(BlockState state) {
    return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzk3NjcxOTAzNSwKICAicHJvZmlsZUlkIiA6ICJlZDUzZGQ4MTRmOWQ0YTNjYjRlYjY1MWRjYmE3N2U2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICI0MTQxNDE0MWgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTczNTE0YTIzMjQ1ZjE1ZGJhZDVmYjRlNjIyMTYzMDIwODY0Y2NlNGMxNWQ1NmRlM2FkYjkwZmE1YTcxMzdmZCIKICAgIH0KICB9Cn0";
}
```

## Using BlockEntities
The only thing you need to do to remove BlockEntity from being sent to client is registering its BlockEntityType with `BlockHelper.registerVirtualBlockEntity(BlockEntityType types)`.

## Limitations
While it's supported, please limit creation of VirtualBlock light sources. Because of how Minecraft
handles light updates on server/client, these can be little laggy (as it needs to be sent updates every time light changes) and not perfect, 
as client is emulating light by itself.

Similarly, client recalculates some BlockStates, which can cause some desyncs. 
