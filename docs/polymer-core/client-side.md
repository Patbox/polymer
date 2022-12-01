# Client side features
While Polymer by itself is mainly server side api, it includes some 
client side functionality for mods to use. It allows you for example to display 
vanilla friendly item for normal clients and custom models if it's present on server.

## Keeping modded item/block on compatible client.

To keep client side model for loading, you need to implement `PolymerKeepModel` interface
on your modded object. To enable it's decoding, just add `PolymerClientDecoded` interface for it.

After that, you just need to return server side items/block 
in corresponding player-aware `getPolymerX` methods.

To "sync" presence/version of your mod you can use Polymer's handshake feature.

You can do that by registering server packet like this,

```
PolymerPacketUtils.registerServerPacket(PACKET_ID, 0, 1...);
```
Where PACKET_ID is just instance of your Identifier, and numbers after it
representing supported protocol versions (can be single or multiple).

After that you can just validate if player supports it with this check it like this
```
SomeObject getPolymerX(SomeObject serverObject, ServerPlayerEntity player) {
    if (PolymerPacketUtils.getSupportedVersion(player.networkHandler, PACKET_ID) > 0) {
        // Client state for modded
        return serverObject;
    } else {
        // Client state for vanilla
        return VanillaObjects.SOMETHING;
    }}
```


### This section is incomplete... I hope in code comments will guide you well...