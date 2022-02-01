package ir.doorbash.colyseus_chat;

import android.os.Bundle;
import android.util.SparseArray;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.colyseus.Client;
import io.colyseus.Room;
import io.colyseus.serializer.schema.DataChange;
import ir.doorbash.colyseus_chat.classes.Message;
import ir.doorbash.colyseus_chat.classes.MyState;
import ir.doorbash.colyseus_chat.classes.User;

public class MainActivity extends AppCompatActivity {

    public static final String ENDPOINT = "ws://192.168.1.197:3333";

    MessagesList messagesList;
    MessageInput inputView;
    TextView typing;

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
                room.send("message", input.toString());
            }
            return true;
        });

        inputView.setTypingListener(new MessageInput.TypingListener() {
            @Override
            public void onStartTyping() {
                if (room != null) {
                    room.send("typing", true);
                }
            }

            @Override
            public void onStopTyping() {
                if (room != null) {
                    room.send("typing", false);
                }
            }
        });

        connectToServer();
    }

    private void connectToServer() {
        Client client = new Client(ENDPOINT);
        client.joinOrCreate(MyState.class,"chat", r -> {
            System.out.println("joined chat");
            this.room = r;
            runOnUiThread(() -> {
                adapter = new MessagesListAdapter<>(r.getSessionId(), null);
                messagesList.setAdapter(adapter);
            });
            r.getState().users.setOnAdd((user, key) -> {
                synchronized (users) {
                    users.put(key, user);
                }
                user.setOnChange(changes -> {
                    for (DataChange change : changes) {
                        if (change.getField().equals("is_typing")) {
                            System.out.println(r.getSessionId() + " : " + change);
                            runOnUiThread(this::updateTypingUI);
                        }
                    }
                });
            });
            r.getState().users.setOnRemove((value, key) -> {
                synchronized (users) {
                    users.remove(key);
                }
            });
            r.getState().messages.setOnAdd((message, key) -> {
                synchronized (messages) {
                    messages.put(key, message);
                }
                message.senderUser = (User) r.getState().users.get(message.sender);
                runOnUiThread(() -> adapter.addToStart(message, true));
            });
            r.getState().messages.setOnRemove((value, key) -> {
                synchronized (messages) {
                    messages.remove(key);
                }
            });
            r.getState().users.triggerAll();
            r.getState().messages.triggerAll();
        }, Throwable::printStackTrace);
    }

    private void updateTypingUI() {
        if(room == null) return;
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
