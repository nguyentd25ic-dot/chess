package server.entity;
import common.*;
import java.io.*;
import java.net.Socket;
public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MessageListener    listener;

    public interface MessageListener {
        void onMessage(Message msg);
        void onDisconnected();
    }
    public boolean connect (MessageListener listener){
        this.listener = listener;
        try{
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            new Thread (this::listen).start();
            return true;
        }catch(Exception e) {
            return false;
        }
    }
    public void listen () {
        try{
            while (true) {
                listener.onMessage((Message) in.readObject());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void send(Message msg){
        try{
            out.writeObject(msg);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void disconnect (){
        try{
            if(socket != null){
                socket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
