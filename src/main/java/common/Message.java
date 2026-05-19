package common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private String data;
    private String sender;
    private String extra;
    public Message(MessageType type, String sender, String data) {
        this.type = type;
        this.data = data;
        this.sender = sender;
    }
    public Message(MessageType type, String sender, String data, String extra) {
        this.type = type;
        this.data = data;
        this.sender = sender;
        this.extra = extra;
    }
    public MessageType getType() {
        return type;
    }
    public String getData() {
        return data;
    }
    public String getSender() {
        return sender;
    }
    public String getExtra() {
        return extra;
    }
}
