# FluxerSRV

**FluxerSRV** is a custom, lightweight Minecraft plugin designed to bridge your Minecraft server with the [Fluxer.app](https://fluxer.app) platform. 

Built from the ground up as a standalone alternative to bulky Discord bridge plugins, FluxerSRV connects directly to the Fluxer WebSocket Gateway and REST API to provide seamless, real-time communication between your players and your Fluxer community.

## ✨ Features

* **Bidirectional Chat Bridge**: Messages sent in-game appear in your Fluxer `global` channel, and messages sent in Fluxer instantly broadcast to players in-game.
* **Live Console Streaming**: All server console logs are securely streamed directly to a designated `console` channel on Fluxer.
* **Remote Console Execution**: Server administrators can type commands directly into the Fluxer `console` channel, and the plugin will execute them as the server console.
* **Join, Quit, and Death Events**: Automatically broadcasts player logins, logouts, and death messages to the Fluxer community.
* **Lightweight & Fast**: No bloat, no unnecessary dependencies. Built purely for the Fluxer ecosystem.

## 🚀 Installation & Setup

1. **Download the Plugin**: Place the `FluxerSRV.jar` file into your Minecraft server's `plugins/` folder.
2. **Start the Server**: Run your server once to let the plugin generate its default `config.yml`.
3. **Get your Bot Token**:
   * Go to your **User Settings** in the Fluxer app.
   * Navigate to **Applications** -> **Create a New Application**.
   * Copy the **Bot Token**.
   * Use the OAuth2/Bot Invite URL generator to invite the bot to your server with `View Channels` and `Send Messages` permissions.
4. **Configure the Plugin**:
   * Open `plugins/FluxerSRV/config.yml`.
   * Paste your Fluxer bot token into the `botToken` field.
   * Set your `global` and `console` channel IDs.
5. **Restart the Server**: Restart your Minecraft server. The bot will automatically come online and bridge your server!

## ⚙️ Configuration
The `config.yml` file allows you to customize the bot token, channel bindings, and format strings for every type of message (chat, joins, quits, server startup, and server shutdown).

## 📄 License
This project is licensed under the MIT License. See the `LICENSE` file for details.
