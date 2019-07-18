package ir.doorbash.colyseus_chat.classes;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;

@SchemaClass
public class Message extends Schema implements IMessage {
    @SchemaField("0/string")
    public String id = "";

    @SchemaField("1/string")
    public String text = "";

    @SchemaField("2/string")
    public String sender = "";

    @SchemaField("3/int64")
    public long time = 0;

    public User senderUser;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        if (senderUser != null)
            return senderUser;
        User user = new User();
        user.id = sender;
        return user;
    }

    @Override
    public Date getCreatedAt() {
        return new Date(time);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof User && ((User) obj).id.equals(id));
    }
}

