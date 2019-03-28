package com.tsystems.chat.server;

import com.tsystems.network.TCPConnection;
import com.tsystems.network.TCPConnectionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements TCPConnectionListener
{
  private final Logger LOGGER=LogManager.getLogger(ChatServer.class);

  public static void main(String[] args)
  {
    new ChatServer();
  }

  //all connection to server
  private final List<TCPConnection> connections=new ArrayList<>();

  private ChatServer()
  {
    LOGGER.info("Server is running and is listening to incoming connections");
    try (ServerSocket serverSocket=new ServerSocket(8100))
    {
      while (true)
      {
        new TCPConnection(serverSocket.accept(), this);
      }
    }
    catch (IOException e)
    {
      LOGGER.error("Connection exception: ", e);
    }
  }

  @Override public synchronized void onConnect(TCPConnection connection)
  {
    LOGGER.trace("Enter");
    connections.add(connection);
    LOGGER.info("{} connected", connection);
    broadcastMessage(connection + " connected");
    LOGGER.trace("Leave");
  }

  @Override public synchronized void onMessage(TCPConnection connection, String message)
  {
    LOGGER.trace("Enter");
    LOGGER.debug("Received message from {}: \"{}\", sending to all participants", connection, message);
    broadcastMessage(message);
    LOGGER.trace("Leave");
  }

  @Override public synchronized void onDisconnect(TCPConnection connection)
  {
    LOGGER.trace("Enter");
    connections.remove(connection);
    LOGGER.info("{} disconnected", connection);
    broadcastMessage(connection + " disconnected");
    LOGGER.trace("Leave");
  }

  @Override public synchronized void onException(TCPConnection connection, Exception e)
  {
    LOGGER.trace("Enter");
    LOGGER.error("Connection exception caused by " + connection + ": ", e);
    LOGGER.trace("Leave");
  }

  private void broadcastMessage(final String message)
  {
    LOGGER.trace("Enter");
    for (TCPConnection connection : connections)
    {
      LOGGER.debug("Sending message \"{}\" to {}", message, connection);
      connection.sendString(message);
    }
    LOGGER.trace("Leave");
  }
}
