package ir.doorbash.colyseus_chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.colyseus.Client;
import io.colyseus.Room;
import io.colyseus.serializer.schema.DataChange;
import io.colyseus.serializer.schema.Schema;
import ir.doorbash.colyseus_chat.classes.Message;
import ir.doorbash.colyseus_chat.classes.MyState;
import ir.doorbash.colyseus_chat.classes.User;

public class MainActivity extends AppCompatActivity {

    // Constants
    public static final String ENDPOINT = "ws://192.168.1.134:3333";

    // Views
    MessagesList messagesList;
    MessageInput inputView;
    TextView typing;

    // Variables
    String clientId;
    Client client;
    Room<MyState> room;
    MessagesListAdapter<Message> adapter;
    final HashMap<String, User> users = new HashMap<>();
    final SparseArray<Message> messages = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messagesList = findViewById(R.id.messagesList);
        inputView = findViewById(R.id.input);
        typing = findViewById(R.id.typing);

        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                if (room != null) {
                    LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                    data.put("op", "message");
                    data.put("message", input.toString());
                    room.send(data);
                }
                return true;
            }
        });

        inputView.setTypingListener(new MessageInput.TypingListener() {
            @Override
            public void onStartTyping() {
                if (room != null) {
                    LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                    data.put("op", "typing");
                    data.put("status", true);
                    room.send(data);
                }
            }

            @Override
            public void onStopTyping() {
                if (room != null) {
                    LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                    data.put("op", "typing");
                    data.put("status", false);
                    room.send(data);
                }
            }
        });

        SharedPreferences prefs = getSharedPreferences("shared_prefs", MODE_PRIVATE);
        clientId = prefs.getString("colyseus-id", null);

        connectToServer();
    }

    private void connectToServer() {
        client = new Client(ENDPOINT, clientId, new Client.Listener() {
            @Override
            public void onOpen(final String id) {
                if (clientId == null) {
                    clientId = id;
                    SharedPreferences.Editor editor = getSharedPreferences("shared_prefs", MODE_PRIVATE).edit();
                    editor.putString("colyseus-id", clientId);
                    editor.apply();
                }

                if (adapter == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new MessagesListAdapter<>(id, null);
                            messagesList.setAdapter(adapter);
                        }
                    });
                }

                room = client.join("chat", MyState.class);
                room.addListener(new Room.Listener() {
                    @Override
                    protected void onLeave() {
                        System.out.println("left chat");
                    }

                    @Override
                    protected void onError(Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    protected void onMessage(Object message) {
                        System.out.println(message);
                    }

                    @Override
                    protected void onJoin() {
                        System.out.println("joined chat");
                        room.state.users.onAdd = new Schema.MapSchema.onAddListener<User>() {
                            @Override
                            public void onAdd(User user, String key) {
                                synchronized (users) {
                                    users.put(key, user);
                                }
                                user.onChange = new Schema.onChange() {
                                    @Override
                                    public void onChange(List<DataChange> changes) {
                                        for (DataChange change : changes) {
                                            if (change.field.equals("is_typing")) updateTypingUI();
                                        }
                                    }
                                };
                            }
                        };
                        room.state.users.onRemove = new Schema.MapSchema.onRemoveListener<User>() {
                            @Override
                            public void onRemove(User value, String key) {
                                synchronized (users) {
                                    users.remove(key);
                                }
                            }
                        };
                        room.state.messages.onAdd = new Schema.ArraySchema.onAddListener<Message>() {
                            @Override
                            public void onAdd(final Message message, int key) {
                                synchronized (messages) {
                                    messages.put(key, message);
                                }
                                message.senderUser = room.state.users.get(message.sender);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.addToStart(message, true);
                                    }
                                });
                            }
                        };
                        room.state.messages.onRemove = new Schema.ArraySchema.onRemoveListener<Message>() {
                            @Override
                            public void onRemove(Message value, int key) {
                                synchronized (messages) {
                                    messages.remove(key);
                                }
                            }
                        };
                    }
                });
            }

            @Override
            public void onMessage(Object message) {
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("client closed");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTypingUI() {
        synchronized (users) {
            List<String> typingUsers = new ArrayList<>();
            for (User user : users.values()) {
                if (user == null) continue;
                if (!user.id.equals(clientId) && user.is_typing) {
                    typingUsers.add(user.name);
                }
            }
            if (typingUsers.isEmpty()) {
                typing.setText("");
                return;
            }
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < typingUsers.size(); i++) {
                if (i > 0) text.append(", ");
                text.append(typingUsers.get(i));
            }
            if (typingUsers.size() == 1) {
                text.append(" is typing...");
                typing.setText(text.toString());
            } else {
                text.append(" are typing...");
                typing.setText(text.toString());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (room != null) room.leave();
        if (client != null) client.close();
        super.onDestroy();
    }
}
