package ir.doorbash.colyseus_chat.classes;//

import io.colyseus.annotations.SchemaField;
import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.types.ArraySchema;
import io.colyseus.serializer.schema.types.MapSchema;

public class MyState extends Schema {
    @SchemaField(type = "0/map/ref", ref = User.class)
    public MapSchema<User> users = new MapSchema<>(User.class);

    @SchemaField(type = "1/array/ref", ref = Message.class)
    public ArraySchema<Message> messages = new ArraySchema<>(Message.class);
}

