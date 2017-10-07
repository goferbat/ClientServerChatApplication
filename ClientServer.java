//DECLAN HALBERT SERVER #3
//U79196431
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Arrays;

/*
 * A chat server that delivers public and private messages.
 */
public class Server {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;

  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 5;
  private static final clientThread[] threads = new clientThread[maxClientsCount];
  
  public static void main(String args[]) {

    // The default port number.
    int portNumber = 2222;
    if (args.length < 1) {
      System.out
          .println("Usage: java Server <portNumber>\n"
              + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    System.out.println("Server using port number=" + portNumber + "\n");

    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket,threads)).start();
            break;
          }
        }
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. When a client leaves the chat room this thread informs also
 * all the clients about that and terminates.
 */
class clientThread extends Thread {

  private BufferedReader is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private String name;
  private String[] friends = new String[]{"","","","",""};
  private String hasSentRequest = "";

  public clientThread(Socket clientSocket, clientThread[]threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      os = new PrintStream(clientSocket.getOutputStream());
      os.println("Enter your name.");
      String newname = is.readLine().trim();
      name = newname;
      os.println("\nWelcome " + newname
          + " to the chat room.\nCommands:\n@connect <username> - Add someone as a friend\n@friend <username> - Accept friend request\n@deny <username> - Deletes friend request\n@disconnect <username> - Get's rid of friend (must be done on both ends)\n#status - Post a status update ONLY to friends\nExit - Exits the chat\n");
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println("#newUser " + name
              + " entered the chat room!");
        }
      }

    while (true) {
        String line = is.readLine().trim();
        

        if (line.startsWith("Exit")) {
          break;
        }

        
        if (line.startsWith("@connect ")) {
          String newFriend = line.substring(9, line.length());
          for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && newFriend.startsWith(threads[i].name) && threads[i] != this) {
              threads[i].os.println("#friendme <" + name + ">");
              this.hasSentRequest = threads[i].name;
            }
          }
          continue;
        }
        
        
        if (line.startsWith("@friend ")) {
          String newFriend = line.substring(8, line.length());
          for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i].hasSentRequest != "") {
              if ((threads[i].name.contains(newFriend)) && (threads[i] != this) && (threads[i].hasSentRequest == this.name)) {
                clientThread Iwantfriend = threads[i];
                String Friendname = threads[i].name;
                for (int p = 0; p < maxClientsCount; p++) {
                  if (this.friends[p] == "") {
                    this.friends[p] = Friendname;
                    //this.os.println(Arrays.toString(this.friends));
                    break;
                  }
                }
                for (int l = 0; l < maxClientsCount; l++) {
                  if (Iwantfriend.friends[l] == ""){
                    Iwantfriend.friends[l] = this.name;
                    break;
                  }
                }
                  this.os.println("#OKfriends");
                  Iwantfriend.os.println("#OKfriends");
                  break;
              }
            if (i == 4) {
              this.os.println("No one has sent you a Friend Request!");
              break;
            }  
            }
          }
          continue;
        }
        
        


        if (line.startsWith("@deny ")) {
          String notFriend = line.substring(6, line.length());
          for (int i = 0; i < maxClientsCount; i++) {
            if ((threads[i].name.contains(notFriend)) && (threads[i] != this) && (threads[i].hasSentRequest == this.name)) {
              clientThread Iwantfriend = threads[i];
              Iwantfriend.hasSentRequest = "";
              Iwantfriend.os.println("#FriendRequestDenied <" + this.name + ">");
              break;
            }
          }
          continue;
        }
        
        /*
          ONLY DISCONNECTS ONE SIDE, NEEDS TO BE DONE ON BOTH SIDES TO REMOVE FRIENDS
        */
        if (line.startsWith("@disconnect ")) {
          int count = 0;
          for (int p = 0; p < 5; p++) {
            if (this.friends[p] == "") {
              count++;
            }
          }
          if (count == 5) {
            this.os.println("You don't have any friends!");
            continue;
          }
          String notFriend = line.substring(12, line.length());
          for (int i = 0; i < 5-count; i++) {
            if ((this.friends[i].contains(notFriend))) {
              for (int p = 0; p < 5; p++) {
                if (this.friends[p].contains(notFriend)) {
                  this.friends[p] = "";
                  this.os.println("You are not friends with " + notFriend + " anymore!");
                  }
              }
            }
            }
            continue;
        }

        
        if (line.startsWith("#status")) {
          int count = 0;
          for (int p = 0; p < 5; p++) {
            if (this.friends[p] == "") {
              count++;
            }
          }
          if (count == 5) {
            this.os.println("You don't have any friends!");
            continue;
          }
          for (int p = 0; p < 5-count; p++) {
            for (int o = 0; o < 5; o++) {
              if (threads[o].name == null) {
                break;
              }
              if ((this.friends[p].contains(threads[o].name))) {
                threads[o].os.println("#newStatus <" + name + ">" + line.substring(7, line.length()));
                break;
              }
            }
        	}
          this.os.println("#statusPosted");
          }
        


        else {
        	os.println("You must use the commands given!");
        	continue;
        }	
     

      }
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println("#Leave " + name
              + " has left the chat room!");
        }
      }
      os.println("#Bye " + name + "!");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] == this) {
          threads[i] = null;
        }
      }

      /*
       * Close.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}


// CLIENT


//DECLAN HALBERT CLIENT #3
//U79196431
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client implements Runnable {

  // The client socket
  private static Socket clientSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static BufferedReader is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args) {

    // The default port.
    int portNumber = 2222;
    // The default host.
    String host = "localhost";

    if (args.length < 2) {
      System.out
          .println("Usage: java Client <host> <portNumber>\n"
              + "Now using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    /*
     * If everything has been initialized then we want to write some data to the
     * socket we have opened a connection to on the port portNumber.
     */
    if (clientSocket != null && os != null && is != null) {
      try {

        /* Create a thread to read from the server. */
        new Thread(new Client()).start();
        while (!closed) {
          os.println(inputLine.readLine().trim());
        }
        /*
         * Close.
         */
        os.close();
        is.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }


  public void run() {
    /*
     * Keep on reading from the socket till we receive "Bye" from the
     * server. Once we received that then we want to break.
     */
    String responseLine;
    try {
      while ((responseLine = is.readLine()) != null) {
        System.out.println(responseLine);
        if (responseLine.indexOf("*** Bye") != -1)
          break;
      }
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}
