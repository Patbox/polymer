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