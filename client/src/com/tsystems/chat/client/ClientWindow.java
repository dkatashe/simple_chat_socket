package com.tsystems.chat.client;

import com.tsystems.network.TCPConnection;
import com.tsystems.network.TCPConnectionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener
{
  private static final Logger LOGGER=LogManager.getLogger(ClientWindow.class);

  private static final Integer WIDTH=600;
  private static final Integer HEIGHT=400;

  public static void main(String[] args)
  {
    LOGGER.info("Application started");
    SwingUtilities.invokeLater(ClientWindow::new);
  }

  private final JTextArea log=new JTextArea();
  private final JTextField fieldNickname=new JTextField("John Doe");
  private final JTextField fieldInput=new JTextField();
  private final JScrollPane logScroll=new JScrollPane(log);

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
    LOGGER.info("Client window created");

    try
    {
      LOGGER.debug("Connecting to server...");
      connection=new TCPConnection(ClientWindow.this, "localhost", 8100);
    }
    catch (IOException e)
    {
      LOGGER.error("Connection error: ", e);
      printMessage(e.getMessage());
    }
  }

  //to send message
  @Override public void actionPerformed(final ActionEvent e)
  {
    LOGGER.trace("Enter");
    try
    {
      String msg=fieldInput.getText();
      if ("".equals(msg))
      {
        LOGGER.debug("Message is empty, nothing is sent");
        LOGGER.trace("Leave");
        return;
      }
      fieldInput.setText(null);
      connection.sendString(fieldNickname.getText() + ": " + msg);
      LOGGER.debug("Message sent: \"{}: {}\"", fieldNickname.getText(), msg);
    }
    catch (NullPointerException ex)
    {
      LOGGER.error("Error while trying to send a message: ", ex);
    }
    LOGGER.trace("Leave");
  }

  //for printing messages
  private synchronized void printMessage(String msg)
  {
    LOGGER.trace("Enter");
    SwingUtilities.invokeLater(() -> {
      log.append(msg + "\n");
      LOGGER.debug("Message printed: \"{}\"", msg);
      log.setCaretPosition(log.getDocument().getLength());
    });
    LOGGER.trace("Leave");
  }

  @Override public void onConnect(TCPConnection connection)
  {
    LOGGER.trace("Enter");
    LOGGER.debug("Connected to {}", connection);
    printMessage("Connected to server");
    LOGGER.trace("Leave");
  }

  @Override public void onMessage(TCPConnection connection, String message)
  {
    LOGGER.trace("Enter");
    LOGGER.debug("Got message from {}: \"{}\"", connection, message);
    printMessage(message);
    LOGGER.trace("Leave");
  }

  @Override public void onDisconnect(TCPConnection connection)
  {
    LOGGER.trace("Enter");
    LOGGER.debug("Disconnected from {}", connection);
    printMessage("Disconnected from server");
    LOGGER.trace("Leave");
  }

  @Override public void onException(TCPConnection connection, Exception e)
  {
    LOGGER.trace("Enter");
    LOGGER.error("Connection exception caused by " + connection + ": ", e);
    printMessage("Connection exception: " + e.getMessage());
    LOGGER.trace("Leave");
  }
}
