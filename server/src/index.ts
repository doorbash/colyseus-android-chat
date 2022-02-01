import * as express from 'express';
import { createServer } from 'http';
import { Server } from 'colyseus';

// Import demo room handlers
import { ChatRoom } from "./rooms/chat"

const port = 3333
const app = express();
app.use(express.json());

// Attach WebSocket Server on HTTP Server.
const gameServer = new Server({
  server: createServer(app)
});

// Register PublicRoom as "chat"
gameServer.define("chat", ChatRoom);

gameServer.onShutdown(function () {
  console.log(`game server is going down.`);
});

gameServer.listen(port, '0.0.0.0');
console.log(`Listening on http://0.0.0.0:${port}`);