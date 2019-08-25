import { Schema, type, ArraySchema, MapSchema } from "@colyseus/schema"
import { Room, Client } from "colyseus"
var shortid = require('shortid');

class User extends Schema {
    @type("string")
    id: string;

    @type("string")
    name = "";

    @type("boolean")
    is_typing = false;
}

class Message extends Schema {
    @type("string")
    id: string = shortid.generate();

    @type("string")
    text: string;

    @type("string")
    sender: string;

    @type("int64")
    time: number = Date.now();
}

class MyState extends Schema {
    @type({ map: User })
    users = new MapSchema<User>();
    
    @type([Message])
    messages = new ArraySchema<Message>();
}

export class ChatRoom extends Room {
    maxClients = 10;
    autoDispose = false;

    onCreate?(options: any): void {
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
        message.sender = "server"
        this.state.messages.push(message);
    }

    onLeave?(client: Client, consented?: boolean): void | Promise<any>{
        console.log("onLeave(" + client.sessionId + ")");

        var user = this.state.users[client.id];

        var message: Message = new Message();
        message.text = user.name + " left.";
        message.sender = "server"
        this.state.messages.push(message);

        delete this.state.users[client.id];
    }

    onMessage(client: Client, data: any): void {
        console.log("Room received message from", client.id, ":", data);

        if (data.op == "message") {
            var message: Message = new Message();
            message.text = data.message;
            message.sender = this.state.users[client.id].name;
            this.state.messages.push(message);

            this.state.users[client.id].is_typing = false;
        } else if (data.op == "typing") {
            this.state.users[client.id].is_typing = data.status;
        }
    }

    onDispose?(): void | Promise<any> {
        console.log("Dispose Room");
    }
}