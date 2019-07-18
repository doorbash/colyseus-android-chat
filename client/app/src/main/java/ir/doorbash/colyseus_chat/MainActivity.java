package ir.doorbash.colyseus_chat;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
    List<String> typingUsers = new ArrayList<>();

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

                    data = new LinkedHashMap<>();
                    data.put("op", "typing");
                    data.put("status", false);
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
                        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) message;
                        if (data.get("op").equals("typing")) {
                            String sender = (String) data.get("sender");
                            if (sender.equals(clientId)) return;
                            if ((boolean) data.get("status")) {
                                if (typingUsers.contains(sender)) return;
                                typingUsers.add(sender);
                            } else {
                                typingUsers.remove(sender);
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateTypingUI();
                            }
                        });
                    }

                    @Override
                    protected void onJoin() {
                        System.out.println("joined chat");
                        room.getState().messages.onAddListener = new Schema.ArraySchema.onAddListener<Message>() {
                            @Override
                            public void onAdd(final Message message, int key) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.addToStart(message, true);
                                    }
                                });
                            }
                        };
                        room.getState().messages.onRemoveListener = new Schema.ArraySchema.onRemoveListener<Message>() {
                            @Override
                            public void onRemove(final Message message, int key) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.delete(message);
                                    }
                                });
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

    @Override
    protected void onDestroy() {
        if (room != null) room.leave();
        if (client != null) client.close();
        super.onDestroy();
    }
}
