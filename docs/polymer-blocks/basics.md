# Basics

## Creating a block.
Creating a block is mostly the same as with regular polymer ones. Only difference is implementation of `PolymerTexturedBlock`
interface, which acts as a marker.

## Supported block shapes/types
Polymer Blocks supports few types of models/shapes. However, possible amount of blocks is limited, because we can't really add fully custom 
blocks on client yet.

Every block type has its own functionality and behaviour:

- `FULL_BLOCK` - Noteblocks, have full collision and don't allow transparency, (limit: 799)
- `TRANSPARENT_BLOCK` - Mostly leaves, allow "cutout" textures, (limit: 104)
- `FARMLAND_BLOCK` - Farmland blocks, (limit: 5!)
- `VINES_BLOCK` - All centered vine blocks, Cave Vines, Twisted Vines and Weeping Vines, (limit: 100)
- `PLANT_BLOCK` - Small plant blocks, sugarcane and saplings, (limit: 21)
- `KELP_BLOCK` - Just kelp, (limit: 25)
- `CACTUS_BLOCK` - Just cactus, (limit: 15!)

- `FULL_BLOCK` - Noteblocks, have full collision and don't allow transparency, (limit: 1149)
- `TRANSPARENT_BLOCK` - Leaf blocks, allow "cutout" textures, (limit: 52)
- `TRANSPARENT_BLOCK_WATERLOGGED` - Waterlogged leaf blocks, allow "cutout" textures, (limit: 52)
- `BIOME_TRANSPARENT_BLOCK` - Biome tinted leaf blocks, (limit: 78)
- `BIOME_TRANSPARENT_BLOCK_WATERLOGGED` Waterlogged biome tinted leaf blocks- (limit: 65)
- `FARMLAND_BLOCK` - Farmland blocks, (limit: 5)
- `VINES_BLOCK` - All centered vine blocks, Cave Vines, Twisted Vines and Weeping Vines, (limit: 100)
- `PLANT_BLOCK` - Small plant blocks, mostly saplings, (limit: 7)
- `BIOME_PLANT_BLOCK` - Biome tinted plant blocks, mostly sugarcane (limit: 15)
- `KELP_BLOCK` - Just kelp, (limit: 25)
- `CACTUS_BLOCK` - Just cactus, (limit: 15)
- `SCULK_SENSOR_BLOCK` - Sculk-sensor and it's calibarated variant, half block high, allows transparency, (limit: 150)
- `SCULK_SENSOR_BLOCK_WATERLOGGED` - Waterlogged sculk-sensor and it's calibarated variant, half block high, allows transparency, (limit: 150)
- `TRIPWIRE_BLOCK` - Just Tripwire, allows transparency, (limit: 32)
- `TRIPWIRE_BLOCK_FLAT` - Flat tripwire block, allows transparency, (limit: 32)
- `TOP_SLAB` - Top-slabs, (limit: 5)
- `TOP_SLAB_WATERLOGGED` - Waterlogged top-slabs (limit: 5)
- `BOTTOM_SLAB` - Bottom slabs, don't allow transparency, (limit: 5)
- `BOTTOM_SLAB_WATERLOGGED` - Waterlogged bottom-slabs, don't allow transparency (limit: 5)
- `TOP_TRAPDOOR` - Closed top trapdoor, (limit: 20)
- `BOTTOM_TRAPDOOR` - Closed bottom trapdoor, (limit: 20)
- `NORTH_TRAPDOOR` - Open trapdoor facing north, (limit: 20)
- `EAST_TRAPDOOR` - Open trapdoor facing east, (limit: 20)
- `SOUTH_TRAPDOOR` - Open trapdoor facing south, (limit: 20)
- `WEST_TRAPDOOR` - Open trapdoor facing west, (limit: 20)
- `TOP_TRAPDOOR_WATERLOGGED` - Waterlogged closed top trapdoor, (limit: 20)
- `BOTTOM_TRAPDOOR_WATERLOGGED` - Waterlogged closed bottom top trapdoor, (limit: 20)
- `NORTH_TRAPDOOR_WATERLOGGED` - Waterlogged open trapdoor facing north, (limit: 20)
- `EAST_TRAPDOOR_WATERLOGGED` - Waterlogged open trapdoor facing east, (limit: 20)
- `SOUTH_TRAPDOOR_WATERLOGGED` - Waterlogged open trapdoor facing south, (limit: 20)
- `WEST_TRAPDOOR_WATERLOGGED` - Waterlogged open trapdoor facing west, (limit: 20)
- `NORTH_DOOR` - Door-half facing north, (limit: 160)
- `EAST_DOOR` - Door-half facing east, (limit: 160)
- `SOUTH_DOOR` - Door-half facing south, (limit: 160)
- `WEST_DOOR` - Door-half facing west, (limit: 160)

They all are accessible from `BlockModelType` enum.

## Defining a global model
First thing you do, is creating a model definition. You should do that by calling one of `PolymerBlockModel.of(...)` methods.
It takes the same argument as vanilla definition in `assets/(namespace)/blockstate/(block).json` file.

Then you need to request a model. It's as simple as calling 
`PolymerBlockResourceUtils.requestBlock(BlockModelType type, PolymerBlockModel... model)`
with one or more models. It returns a blockstate, that you need to use in your `PolymerTexturedBlock` as result of `getPolymerBlockState(...)`.
If it runs out of free BlockStates to use, it will return null instead. You can also check amount of free blocks 
with `PolymerBlockResourceUtils.getBlocksLeft(BlockModelType type)`.

If you've done everything correctly, it should now display as your model. Otherwise, you either skipped some step or didn't apply
server resource pack. Remember that you still need to register your assets with `PolymerRPUtils.addAssetSource(String modId)` method.

### Just keep in mind
Some block model types have very small amount of free BlockStates. For that reason, while making public registering blocks globally,
please allow for disabling of them and handle running out of them for best compatibility and mod support!

It is also possible to request an empty blockstate for a BlockModelType using `PolymerBlockResourceUtils.requestEmpty(BlockModelType type)`. This can be used to display custom blocks using display entities. Just be aware that display entities have a much higher performance impact on clients than normal blocks!

Empty models are shared between mods. 