# SoundEvents

Polymer has support for creating custom sound events, that can fallback to vanilla sounds 
for players without resource packs and resource pack ones if they are present.

To use it, you just need to create new instance of `PolymerSoundEvent`. You can use it similarly to vanilla ones.

# Custom statistics

To register custom, server side statistic you just need to call `PolymerStat.registerStat(Identifier, StatFormatter)`.
Then you can use it just like vanilla ones.

# StatusEffects

To create custom, server side status effects, you just need to implement PolymerStatusEffect on your 
custom StatusEffect class. You can also override `StatusEffect getPolymerStatusEffect()` to display it
as vanilla one (otherwise they are hidden).

