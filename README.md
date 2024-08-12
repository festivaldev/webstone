<p align="center">
    <img src="./src/main/resources/icon.png" height="200">
</p>

<h1 align="center">Webstone  <br>
	<a href="https://modrinth.com/mod/webstone/versions#all-versions">
        <img src="https://img.shields.io/badge/Available%20for-Forge--1.20.1-green">
    </a>
	<a href="https://github.com/https://github.com/festivaldev/webstone/blob/forge-1.20.1/LICENSE">
        <img src="https://img.shields.io/github/license/festivaldev/webstone?style=flat&color=900c3f">
    </a>
	<a href="https://www.curseforge.com/minecraft/mc-mods/webstone">
        <img src="https://img.shields.io/curseforge/dt/238222?logo=CurseForge&label=&suffix=%20&color=242629&labelColor=f16436&logoColor=e9e9e9">
    </a>
    <a href="https://modrinth.com/mod/webstone">
        <img src="https://img.shields.io/modrinth/dt/webstone?logo=modrinth&label=&suffix=%20&color=242629&labelColor=5ca424&logoColor=1c1c1c">
    </a>
    <br><br>
</h1>

Remote control your Redstone contraptions using WebSockets.

## Background
This mod is inspired by a [YouTube video](https://www.youtube.com/watch?v=99Hd5Lh69T4) of Fundy, in which he briefly shows a custom Redstone block that can be controlled via a browser. This part of the video never got released as a standalone mod, so this is where Webstone comes in.

## Usage
The mod opens up a [WebSocket](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API) server on port 4321, which then lets you connect with any WebSocket client (like [this one](https://github.com/festivaldev/webstone/tree/webui) - or the hosted client at [https://webstone.festival.tf](https://webstone.festival.tf)) to toggle registered blocks or change their output signal strength.  
You can find the Webstone Remote Block under the "Redstone Blocks" tab in Creative Mode (no Survival recipe yet). To register it, right-click it with an empty hand (if successful, you'll see a message above your hotbar). To toggle it locally, right-click the block while sneaking.

## API Reference
When connecting to the WebSocket server, the connecting client receives the list of currently registered blocks, including their display name, the current powered state and the output signal strength:

```json
{
    "type": "block_list",
    "data": [
        {
            "blockId": "00000000-0000-0000-0000-000000000000",
            "name": "Example",
            "power": 15,
            "powered": false
        }
    ]
}
```

Every time a block state is changed, it will be broadcast to every connected client. There are two different types of payloads for either the powered state or the output signal strength:

#### When a block's powered state has changed

```json
{
    "type": "block_state",
    "data": {
        "blockId": "00000000-0000-0000-0000-000000000000",
        "powered": false // true or false
    }
}
```

#### When a block's output signal strength has changed.

```json
{
    "type": "block_power",
    "data": {
        "blockId": "00000000-0000-0000-0000-000000000000",
        "power": 7 // Anything between (and including) 0 and 15
    }
}
```

Clients can send the following payloads to the server:

#### Set a block's powered state:

```json
{
    "type": "block_state",
    "data": {
        "blockId": "00000000-0000-0000-0000-000000000000",
        "powered": false // Can be either true or false
    }
}
```

If successful, the server forwards the payload to all clients.

#### Set a block's output signal strength:

```json
{
    "type": "block_power",
    "data": {
        "blockId": "00000000-0000-0000-0000-000000000000",
        "power": 7 // Can be anything between 0 and 15
    }
}
```

If successful, the server forwards the payload to all clients.

#### Set the display name of a block:

```json
{
    "type": "rename_block",
    "data": {
        "blockId": "00000000-0000-0000-0000-000000000000",
        "name": "My Example Block"
    }
}
```

If successful, the server broadcasts the updated list of registered blocks to all clients.

#### Delete a block from the Web UI:

```json
{
    "type": "unregister_block",
    "data": {
        "blockId": "00000000-0000-0000-0000-000000000000"
    }
}
```

If successful, the server broadcasts the updated list of registered blocks to all clients.

## Support

Currently, only Forge is supported. Feel free to port this over to any other mod loader you like, as long as you clearly state this project as the original one.

The mod has been tested on Minecraft 1.20.1, but should also work on newer versions. It also may or may not work on older versions of Minecraft.

## Credits
Fundy - Original idea, textures (probably)  
Kaupenjoe - Creating a tutorial series that made this mod possible in the first place