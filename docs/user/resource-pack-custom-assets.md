# Adding custom assets to Resource Pack

Polymer has builtin support for adding and overriding assets to generated resource pack. You can do it in multiple ways.

## Base folders
You can add your assets to `polymer/source_assets` and/or `polymer/override_assets` folders.
These folders might not exist by default so you will need to create them by hand.
The `source_assets` folder is applied first, before any mod. The `override_assets` folder is applied last, after generation/copying of most other files.
For most cases, you likely want to use `source_assets`. These folders both use vanilla resource pack structure, with `pack.mcmeta` and `pack.png` being optional
and replacing default Polymer-generated one.

## Adding resource packs zips or mods
To add custom resource packs, open `config/polymer/resource-pack.json` file.
Then look at list in `"include_zips"` and add paths to zips with your custom resource packs.
These paths are relative to server's root folder.
To copy assets from mods, you can either use the zip method or add their modid to `"include_mod_assets"`.
Unlike with method above, the only copied folder is included `assets` folder, ignoring anything else, making
it more suitable for merging resource packs without adding invalid metadata.

This setting will only update on server restart.