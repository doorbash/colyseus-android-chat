import { Schema, type, ArraySchema, MapSchema } from "@colyseus/schema"
import { Room, Client } from "colyseus"
var shortid = require('shortid');

class User extends Schema {
    @type("string")
    id: string;

    @type("string")
    name = "";
}

class Message extends Schema {
    @type("string")
    id: string = shortid.generate();

    @type("string")
    text: string;

    @type(User)
    user: User;

    @type("int64")
    time: number = Date.now();
}

class MyState extends Schema {
    @type([Message])
    messages = new ArraySchema<Message>();

    @type({ map: User })
    users = new MapSchema<User>();
}

export class ChatRoom extends Room {
    maxClients = 10;
    autoDispose = false;

    onInit(options) {
        console.log("Room created!", options);
        this.setState(new MyState());

        var user: User = new User();
        user.name = user.id = "server"
        this.state.users["server"] = user;
    }

    onJoin?(client: Client, options?: any, auth?: any): void | Promise<any> {
        console.log('onJoin(', client.id, ')', options);
        var user: User = new User();
        user.name = user.id = client.id;
        this.state.users[client.id] = user;

        var message: Message = new Message();
        message.text = user.name + " joined.";
        message.user = this.state.users["server"];
        this.state.messages.push(message);
    }

    onLeave(client) {
        console.log("onLeave(" + client.sessionId + ")");

        var user = this.state.users[client.id];

        var message: Message = new Message();
        message.text = user.name + " left.";
        message.user = this.state.users["server"];
        this.state.messages.push(message);

        delete this.state.users[client.id];

        this.broadcast({op: "typing", status: false, sender: client.id});
    }

    onMessage(client, data) {
        console.log("Room received message from", client.id, ":", data);

        if (data.op == "message") {
            var message: Message = new Message();
            message.text = data.message;
            message.user = this.state.users[client.id];
            this.state.messages.push(message);

            this.broadcast({op: "typing", status: false, sender: client.id});
        } else if (data.op == "typing") {
            data.sender = client.id;
            this.broadcast(data);
        }
    }

    onDispose() {
        console.log("Dispose Room");
    }
}