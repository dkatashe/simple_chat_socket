package com.tsystems.chat.server;

import com.tsystems.network.TCPConnection;
import com.tsystems.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements TCPConnectionListener
{
  public static void main(String[] args)
  {
    new ChatServer();
  }

  //all connection to server
  private final List<TCPConnection> connections=new ArrayList<>();

  private ChatServer()
  {
    System.out.println("Server running...");

    try (ServerSocket serverSocket = new ServerSocket(8100)) {
      while (true) {
        new TCPConnection(serverSocket.accept(), this);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override public synchronized void onConnect(TCPConnection connection)
  {
    connections.add(connection);
    broadcastMessage(connection + " connected");
  }

  @Override public synchronized void onMessage(TCPConnection connection, String message)
  {
    broadcastMessage(message);
  }

  @Override public synchronized void onDisconnect(TCPConnection connection)
  {
    connections.remove(connection);
    broadcastMessage(connection + " disconnected");
  }

  @Override public synchronized void onException(TCPConnection connection, Exception e)
  {
    System.out.println("TCPConnection exception: " + e);
  }

  private void broadcastMessage (final String message) {
    for (TCPConnection connection : connections) {
      connection.sendString(message);
    }
  }
}
