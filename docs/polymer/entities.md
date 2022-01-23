# Entities
!!! Note
    These docs will only take care about polymer-related part of creation of entities.
    You might want to see [official Fabric Wiki](https://fabricmc.net/wiki/tutorial:entity)
    for more in depth look into how to create entities. 
    You can skip most client side specific things, as it won't take effect server side (for example rendering/models)

## Creation of entities

Creation of entities is mostly the same as vanilla. You just need to implement `PolymerEntity` interface
on your entity's class. It exposes few defaulted methods for manipulation of client side visuals.

You also need to register your entity type as virtual,
by using `PolymerEntityUtils.registerType(EntityType... types)`.

### Changing client side entity.
To select visual entity type, you just need to override `EntityType<?> getPolymerEntityType()`

This method can't return null or another EntityType that points to other virtual entity, as it won't work.

Example use:

Displaying entity as zombie
```
@Override
public EntityType<?> getPolymerEntityType() {
    return EntityType.ZOMBIE;
}
```

### Modifying held items
You most likely want to modify items held by entity, to indicate its type. To do it you need to override
`List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(Map<EquipmentSlot, ItemStack> map)`.

Example use:

Displaying real items with helmet replacement.
```
@Override
public List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(Map<EquipmentSlot, ItemStack> map) {
    List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>(map.size());
    for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
        if (entry.getKey() == EquipmentSlot.HEAD) {
            continue;
        } else {
            list.add(Pair.of(entry.getKey(), entry.getValue()));
        }
    }
    list.add(new Pair<>(EquipmentSlot.HEAD, new ItemStack(Items.WITHER_SKELETON_SKULL)));

    return list;
}
```

### Modifying client-side data trackers
For more control over entity, you can modify DataTracker values send to client directly. To do it, you
need to override `void modifyTrackedData(List<DataTracker.Entry<?>> data)` method. `data` already doesn't contain any invalid data.
You should also be safe around it, as sending DataTracker.Entry's, that don't exist on client-side entity representation will cause issues and errors!

To get `TrackedData`, which is needed to create Entries, you will need to make accessors to get private static values from entity classes.

Example use:

Adding villager data to change how villager looks
```
@Override
public void modifyTrackedData(List<DataTracker.Entry<?>> data) {
    data.add(new DataTracker.Entry<>(VillagerEntityAccessor.getVillagerData(), new VillagerData(VillagerType.JUNGLE, VillagerProfession.FARMER, 3);));
}
```
