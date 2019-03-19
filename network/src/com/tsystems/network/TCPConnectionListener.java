package com.tsystems.network;

public interface TCPConnectionListener
{
  void onConnect(TCPConnection connection);

  void onMessage(TCPConnection connection, String message);

  void onDisconnect(TCPConnection connection);

  void onException(TCPConnection connection, Exception e);
}
