package ir.doorbash.colyseus_chat.classes;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import com.stfalcon.chatkit.commons.models.IUser;

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;

public class User extends Schema implements IUser {
    @SchemaField(type = "0/string")
    public String id = "";

    @SchemaField(type = "1/string")
    public String name = "";

    @SchemaField(type = "2/boolean")
    public boolean is_typing = false;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return null;
    }
}

