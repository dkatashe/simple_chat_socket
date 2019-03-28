package com.tsystems.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

//common class for connection
public class TCPConnection
{
  private final Logger LOGGER=LogManager.getLogger(TCPConnection.class);

  //client socket
  private final Socket socket;
  //input thread
  private final Thread rxThread;
  //listener for events
  private final TCPConnectionListener eventListener;
  // reader from socket
  private final BufferedReader in;
  // writer for socket
  private final BufferedWriter out;

  //Constructor for client. Because client need to know ip and port
  public TCPConnection(final TCPConnectionListener eventListener, final String ipAddr, final int port) throws IOException
  {
    this(new Socket(ipAddr, port), eventListener);
  }

  //Constructor for server. Server need only socket
  public TCPConnection(final Socket socket, final TCPConnectionListener eventListener) throws IOException
  {
    this.socket=socket;
    this.eventListener=eventListener;

    in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    rxThread=new Thread(new Runnable()
    {
      @Override public void run()
      {
        LOGGER.trace("Enter");
        LOGGER.debug("Thread {} started", Thread.currentThread().getName());
        try
        {
          LOGGER.debug("Connected");
          eventListener.onConnect(TCPConnection.this);

          while (!rxThread.isInterrupted())
          {
            eventListener.onMessage(TCPConnection.this, in.readLine());
          }
        }
        catch (IOException e)
        {
          eventListener.onException(TCPConnection.this, e);
        }
        finally
        {
          LOGGER.debug("{} disconnected", TCPConnection.this);
          eventListener.onDisconnect(TCPConnection.this);
          LOGGER.trace("Leave");
        }
      }
    });

    rxThread.start();
    LOGGER.debug("Connection established");
  }

  //to send string
  public synchronized void sendString(final String value)
  {
    LOGGER.trace("Enter");
    try
    {
      LOGGER.debug("Sending message: \"{}\"", value);
      out.write(value + System.lineSeparator());
      out.flush();
    }
    catch (IOException e)
    {
      disconnect();
      eventListener.onException(TCPConnection.this, e);
    }
    LOGGER.trace("Leave");
  }

  //diconnection
  public synchronized void disconnect()
  {
    LOGGER.trace("Enter");
    rxThread.interrupt();
    try
    {
      LOGGER.debug("Trying to close connection...");
      socket.close();
      in.close();
      out.close();
      LOGGER.debug("Connection closed");
    }
    catch (IOException e)
    {
      eventListener.onException(TCPConnection.this, e);
    }
    LOGGER.trace("Leave");
  }

  //for logging
  @Override public String toString()
  {
    return "TCPConnection{" + socket.getInetAddress() + ":" + socket.getPort() + "}";
  }
}
