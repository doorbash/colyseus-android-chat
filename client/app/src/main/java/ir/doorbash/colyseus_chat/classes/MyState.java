package ir.doorbash.colyseus_chat.classes;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;

@SchemaClass
public class MyState extends Schema {
	@SchemaField("0/array/ref")
	public ArraySchema<Message> messages = new ArraySchema<>(Message.class);

	@SchemaField("1/map/ref")
	public MapSchema<User> users = new MapSchema<>(User.class);
}

