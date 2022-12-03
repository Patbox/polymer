# Updating from Polymer 0.2.x to 0.3.x

## `polymer` library got split up
The `polymer` library was broken into multiple ones, most notably:
- `polymer-common` - Common code shared between modules
- `polymer-core` - All core functionality of polymer. Contains everything required for making server side content + client mod compatibility
- `polymer-resource-pack` - Resource pack support
- `polymer-networking` - Polymer's previous networking api

All modules (excluding `polymer-reg-sync-manipulator`) also depend (and include) common module.
Core additionally depends on `polymer-networking`, which is used for client sync.

Autohost module now depends only on `polymer-networking` and `polymer-resource-pack`.

## Packages/Class name changes

With the split up, many classes changed their packages.

`polymer-blocks-ext` got renamed to `polymer-blocks`

From `polymer` (`polymer-core`, `polymer-resource-pack`, `polymer-networking`)

- `eu.pb4.polymer.api.networking` -> `eu.pb4.polymer.networking.api` (in `polymer-networking`):
  -`PolymerPacketUtils` is now `PolymerServerNetworking` 
  - Networking parts of `eu.pb4.polymer.api.client.PolymerClientUtils` are moved to `eu.pb4.polymer.api.networking.PolymerClientNetworking`
  - `eu.pb4.polymer.api.resourcepack` -> `eu.pb4.polymer.resourcepack.api` (in `polymer-resource-pack`):
    - `PolymerRPUtils` -> `PolymerResourcePackUtils`
    - `PolymerRPBuilder` -> `ResourcePackBuilder`
  - Rest of `eu.pb4.polymer.api` -> `eu.pb4.polymer.core.api` (in `polymer-core`):
    - `[...].networking.PolymerSyncUtils` -> `[...].utils.PolymerSyncUtils`
    - `PolymerUtils#getPlayer` -> `PolymerUtils#getPlayerContext`
    - Interfaces in `client` that were used on both sides were moved to `utils`
    - `[...].client.registry` -> `[...].client`
    - `PolymerEntity#modifyTrackedData` -> `PolymerEntity#modifyRawTrackedData`
  - `eu.pb4.polymer.ext.blocks` -> `eu.pb4.polymer.blocks` (in `polymer-blocks`)

Additionally, a bunch of method parameters changed to make them more consistent (making ServerPlayerEntity last argument).
Many old duplicate context-less methods got removed if they were only accessed by one with player context.

## Submodule versioning change.
All modules now use same version. This mostly makes it easier to update everything, but also makes some version bumps that don't change anything in some.

