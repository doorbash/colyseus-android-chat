package ir.doorbash.colyseus_chat;

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
import io.colyseus.serializer.schema.Change;
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

        inputView.setInputListener(input -> {
            if (room != null) {
                LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                data.put("op", "message");
                data.put("message", input.toString());
                room.send(data);
            }
            return true;
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

        connectToServer();
    }

    private void connectToServer() {
        Client client = new Client(ENDPOINT);
        client.joinOrCreate("chat", MyState.class, room -> {
            System.out.println("joined chat");
            this.room = room;
            runOnUiThread(() -> {
                adapter = new MessagesListAdapter<>(room.getSessionId(), null);
                messagesList.setAdapter(adapter);
            });
            room.state.users.onAdd = (user, key) -> {
                synchronized (users) {
                    users.put(key, user);
                }
                user.onChange = changes -> {
                    for (Change change : changes) {
                        if (change.field.equals("is_typing")) {
                            System.out.println(room.getSessionId() + " : " + change);
                            runOnUiThread(this::updateTypingUI);
                        }
                    }
                };
            };
            room.state.users.onRemove = (value, key) -> {
                synchronized (users) {
                    users.remove(key);
                }
            };
            room.state.messages.onAdd = (message, key) -> {
                synchronized (messages) {
                    messages.put(key, message);
                }
                message.senderUser = room.state.users.get(message.sender);
                runOnUiThread(() -> adapter.addToStart(message, true));
            };
            room.state.messages.onRemove = (value, key) -> {
                synchronized (messages) {
                    messages.remove(key);
                }
            };
            room.state.users.triggerAll();
            room.state.messages.triggerAll();
        }, Throwable::printStackTrace);
    }

    private void updateTypingUI() {
        synchronized (users) {
            List<String> typingUsers = new ArrayList<>();
            for (User user : users.values()) {
                if (user == null) continue;
                if (!user.id.equals(room.getSessionId()) && user.is_typing) {
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
        super.onDestroy();
    }
}
