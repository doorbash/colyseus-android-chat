package ir.doorbash.colyseus_chat.classes;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import com.stfalcon.chatkit.commons.models.IUser;

import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;

@SchemaClass
public class User extends Schema implements IUser {
	@SchemaField("0/string")
	public String id = "";

	@SchemaField("1/string")
	public String name = "";

	@SchemaField("2/boolean")
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

