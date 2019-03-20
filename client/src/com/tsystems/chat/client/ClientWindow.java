package com.tsystems.chat.client;

import com.tsystems.network.TCPConnection;
import com.tsystems.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener
{
  private static final Integer WIDTH=600;
  private static final Integer HEIGHT=400;

  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(ClientWindow::new);
  }

  private final JTextArea log=new JTextArea();
  private final JTextField fieldNickname=new JTextField("John Doe");
  private final JTextField fieldInput=new JTextField();
  private final JScrollPane logScroll = new JScrollPane(log);

  private TCPConnection connection;

  private ClientWindow()
  {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(WIDTH, HEIGHT);
    setLocationRelativeTo(null);
    setTitle("Telega");

    log.setEditable(false);
    log.setLineWrap(true);
    add(logScroll, BorderLayout.CENTER);

    fieldInput.addActionListener(this);
    add(fieldInput, BorderLayout.SOUTH);
    add(fieldNickname, BorderLayout.NORTH);

    setVisible(true);

    try
    {
      connection=new TCPConnection(ClientWindow.this, "10.233.25.4", 8100);
    }
    catch (IOException e)
    {
      printMessage(e.getMessage());
    }
  }

  //to send message
  @Override public void actionPerformed(final ActionEvent e)
  {
    String msg=fieldInput.getText();
    if ("".equals(msg))
    {
      return;
    }
    fieldInput.setText(null);
    connection.sendString(fieldNickname.getText() + ": " + msg);
  }

  //for printing messages
  private synchronized void printMessage(String msg)
  {
    SwingUtilities.invokeLater(() -> {
      log.append(msg + "\n");
      log.setCaretPosition(log.getDocument().getLength());
    });
  }

  @Override public void onConnect(TCPConnection connection)
  {
    printMessage("Connected to server");
  }

  @Override public void onMessage(TCPConnection connection, String message)
  {
    printMessage(message);
  }

  @Override public void onDisconnect(TCPConnection connection)
  {
    printMessage("Disconnected from server");
  }

  @Override public void onException(TCPConnection connection, Exception e)
  {
    printMessage("Connection exception: " + e);
  }
}
