# Hosting Resource Packs

Some Polymer mods (or if you use PolyMc) might require or allow for optional resource packs.
Polymer has builtin module (AutoHost) to allow for automatic generation and hosting of resource packs
on server start.

## Installation / Checking if it's present
To check if it's present, start the server and check if `config/polymer/auto-host.json` exists.
If it doesn't install correct version of [Polymer from Modrinth](https://modrinth.com/mod/polymer).

## Enabling AutoHost

### For basic server setup (direct server without proxies, reusing port, 0.7.1+1.20.4 or newer) 
Start the server at least once. Then open the config file in `config/polymer/auto-host.json`
and change the value in field `"enabled"` from `false` to `true`. Then save the file and restart the server!
It should automatically apply the resource pack after it finishes generation.

### For proxy setups (reuses port, 0.7.1+1.20.4 or newer)
Do everything as above.
If your server is behind proxy, you need to change the string for `"forced_address"` from `""` to
`"http://serveraddress.net:port"` (for example `"http://server.net:25565"`). The port and address isn't required
to match server internal one, if you use http proxies.

### Custom port setup (0.4.8+1.19.4 or newer)
Start the server at least once. Then open the config file in `config/polymer/auto-host.json`
and change the value in field `"enabled"` from `false` to `true`.
Next set `"type"` to `"polymer:http_server"`. Then replace `"settings"` with
```json
{
  "port": 25567,
  "external_address": "http://localhost:25567/"
}
```
You can change port to any other you need, just make sure the external address is accessible from the outside.
Then save the file and restart the server!
It should automatically apply the resource pack after it finishes generation.
