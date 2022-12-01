# Blocks
!!! Note
    These docs will only take care about polymer-related part of creation of blocks.
    You might want to see [official Fabric Wiki](https://fabricmc.net/wiki/tutorial:blocks)
    for more in depth look into how to create blocks. 
    You can skip some client side specific things, as it won't take effect server side 
    (for example models and textures).

## Creation of blocks

Creation of blocks, similarly to items, is mostly the same as vanilla. Only real difference is that your blocks need to 
implement Polymer's `PolymerBlock` interface. It exposes few defaulted methods for manipulation
of client side visuals.

### Default implementation
For most basic uses, there are default implementation of `PolymerBlock`:

* `PolymerHeadBlock` - It's an interface (!), that has basic implementation of player head based blocks, you still need to apply it to your Block class,
* `SimplePolymerBlock` - Same as vanilla `Block`.

### Selecting base polymer block type.
To change base block, you need to override `Block getPolymerBlock(BlockState)` method.

You can also override `Block getPolymerBlock(ServerPlayerEntity, BlockState)` to replace blocks per player,
however keep in mind they should ideally have same collisions.

Both of these methods can't return null. They can also point to other PolymerBlock instances, but keep
in mind to make validation if it's configurable by user!

Example use:

Making block look like a diamond
```
@Override
public Block getPolymerBlock(BlockState state) {
    return Blocks.BARRIER;
}

public Block getPolymerBlock(ServerPlayerEntity player, BlockState state) {
    return Something.isRedTeam(player) ? Blocks.RED_WOOL : Blocks.BLUE_WOOL;
}
```

### Changing client-side and collision BlockStates
If you want to change what BlockState will be used for server side collision 
and client side you need to override `BlockState getPolymerBlockState(BlockState state)` method.
You can also override `BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player)` for player context,
similar to `getPolymerBlock`.
You can return other BlockState of PolymerBlock, but keep in mind you can only nest them
up to 32!

Example use:

Changing BlockState to furnace with the same facing, but inverted "lit" BlockState property
```
@Override
public BlockState getPolymerBlockState(BlockState state) {
    return Blocks.FURNACE.getDefaultState()
            .with(AbstractFurnaceBlock.FACING, state.get(AbstractFurnaceBlock.FACING))
            .with(AbstractFurnaceBlock.LIT, !state.get(AbstractFurnaceBlock.LIT));
}
```

### Sending additional data (signs/heads or even custom)
In case if you want to send additional (to more customize look on client for signs/heads 
or additional data for companion mod), you need to override `onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, ServerPlayerEntity player)`.
Technically you can do anything there, but ideally it should be only used for packets.

Example use:

Sending data required to render player head with skin
```
@Override
public void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, ServerPlayerEntity player) { 
    player.networkHandler.sendPacket(this.getPolymerHeadPacket(blockState, pos.toImmutable()));
}
```

### Using PolymerHeadBlock
`PolymerHeadBlock` is an interface extending PolymerBlock with methods prepared for 
usage of player heads as a block. To modify texture, you just need to override 
`String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity entity)` which should return texture value.

To generate it you can use websites like https://mineskin.org/.

Additionally, you can override `BlockState getPolymerBlockState(BlockState state)` 
to change rotation of Player Head Block.

Example use:

Setting skin value for PolymerHeadBlock
```
@Override
public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity entity) {
    return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzk3NjcxOTAzNSwKICAicHJvZmlsZUlkIiA6ICJlZDUzZGQ4MTRmOWQ0YTNjYjRlYjY1MWRjYmE3N2U2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICI0MTQxNDE0MWgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTczNTE0YTIzMjQ1ZjE1ZGJhZDVmYjRlNjIyMTYzMDIwODY0Y2NlNGMxNWQ1NmRlM2FkYjkwZmE1YTcxMzdmZCIKICAgIH0KICB9Cn0";
}
```

### Textured, non-player-head blocks
See [Polymer Textured Blocks extension](/polymer-blocks/basics) for more informations

## Using BlockEntities
The only thing you need to do to remove BlockEntity from being sent to client is registering its BlockEntityType with `PolymerBlockUtils.registerBlockEntity(BlockEntityType types)`.

## Getting Polymer Blocks client representation
If you want to get client-friendly representation of block, you need to call
`PolymerBlockUtils.getBlockStateSafely(PolymerBlock block, BlockState blockState)`
method. It should return block safe to use (or air in case of failure).

## Limitations
While it's supported, please limit creation of PolymerBlock light sources. Because of how Minecraft
handles light updates on server/client, these can be little laggy (as it needs to be sent updates every time light changes) and not perfect, 
as client is emulating light by itself.

Similarly, client recalculates some BlockStates, which can cause some desyncs.
