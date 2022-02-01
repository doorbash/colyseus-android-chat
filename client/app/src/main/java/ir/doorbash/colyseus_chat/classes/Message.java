package ir.doorbash.colyseus_chat.classes;//

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;

public class Message extends Schema implements IMessage {
    @SchemaField(type = "0/string")
    public String id = "";

    @SchemaField(type = "1/string")
    public String text = "";

    @SchemaField(type = "2/string")
    public String sender = "";

    @SchemaField(type = "3/int64")
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

