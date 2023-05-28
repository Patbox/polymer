# Basics
Polymer Virtual Entity API was created to simplify usage of virtual (packet-based) entities with special
visual properties. While they might require additional setup for player to be able to see them, they don't
affect on server's performance as much. Additionally, they can't be removed by other mods or commands,
making them more persistent against accidental breaking and crashes.

## VirtualElements
VirtualElements are object controlling single instances of packet entities on client. It allows you to
position, rotate and modify any supported properties of selected entities. This api provides
multiple builtin ones that are most likely to be used. They need to be used with `ElementHolder` to be visible.
See more in section below.

* BlockDisplayElement - Used for displaying blocks. Shows as `minecraft:block_display` on client,
* ItemDisplayElement - Used for displaying items. Shows as `minecraft:item_display` on client,
* TextDisplayElement - Used for displaying text. Shows as `minecraft:text_display` on client,
* InteractionElement - Used for detecting interactions. Shows as `minecraft:interaction` on client,
* MarkerElement - Used for attaching other entities (more later). Shows as `minecraft:armor_stand` with marker properties on client,
* MobAnchorElement - Similar to MarkerElement, but allows for more effects. Shows as invisible `minecraft:slime` of size 0.

There are also multiple abstract classes and main `VirtualElement` interface you can use to implement things in more flexible way.

Example usage:
```
// Creation
var element = new TextDisplayElement();
// Changing entity-specific property
element.setText(Text.literal("Hello world");
// Changing offset
element.setOffset(new Vec3d(0, 5, 0));
// Adding to holder. More info below!
holder.addElement(element);
```


## Creating a ElementHolder
ElementHolder is main object, that holds and manages ticking and sending of groups of VirtualElements.
It can be (and in many cases should be) extended, which can be used to create more dynamic elements without requirement 
of backing it with BlockEntity or Entity objects. However, you still need to attach it to make it visible and tick. 
This can be done by using attachments.
(more in section below).


Example usage:
```
var holder = new ElementHolder();

var element1 = createElement(...);
var element2 = createElement(...);
var element3 = createElement(...);

// Adding elements
holder.addElement(element1);
holder.addElement(element2);
holder.addElement(element3);

// Removing elements
holder.removeElement(element3);

/* Attach here */ 
```

## Using HolderAttachments
HolderAttachments are final element, that connects ElementHolders with their position and ticking.
There are multiple builtin ones, which different purposes:

* EntityAttachment - attaches to any entity.
   Can be created with `EntityAttachement.of(ElementHolder, Entity)`, which requires manual ticking 
   or `EntityAttachement.ofTicking(ElementHolder, Entity)` which does that automatically.
* ChunkAttachment - attaches to a chunk. It's destroyed after chunk gets unloaded.
  Can be created with `ChunkAttachment.of(ElementHolder, ServerWorld, BlockPos/Vec3d)` for manual ticking,
  or `ChunkAttachment.ofTicking(ElementHolder, ServerWorld, BlockPos/Vec3d)` to tick automatically.
* ManualAttachment - Used as a stub, doesn't handle anything by default. You need to call methods by hand.

Example usage:
```
var holder = new ElementHolder();

/* ... */

EntityAttachment.ofTicking(holder, player);
```