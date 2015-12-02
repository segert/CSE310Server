import java.io.*;
import java.net.*;

public class TCPClient{

  public static void main (String argv[]) throws Exception{
    //sentence for user Input
    // modifiedsentence for server return
    String sentence;
    String modifiedSentence;
    if(argv.length != 2){
      System.out.println("missing Host or port number");
      System.exit(-1);
    }
    BufferedReader inFromUser =
    new BufferedReader(new InputStreamReader(System.in));
    System.out.println("Connecting to "+argv[0]+" on port "+ Integer.parseInt(argv[1]));
    System.out.println("Connection with manager established");


    while(true){
      // try connect to server
      try{
        // create a client socket (TCP) and connect to manager application
        Socket clientSocket = new Socket(argv[0], Integer.parseInt(argv[1]));
        System.out.println("Please Enter Manager Command:");

        // create an output stream from the socket output stream
        DataOutputStream outToServer =
        new DataOutputStream(clientSocket.getOutputStream());

        // create an input stream from the socket input stream
        DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

        // read a line form the standard input
        sentence = inFromUser.readLine();
        String[] cmdLine = sentence.split(" ");
        String cmd = cmdLine[0];
        // if user input is help print out help menue
        if (cmd.equals("help") && (cmdLine.length == 1)){
          System.out.println("-------HELP MENUE ------");
          System.out.println("put: add name records to the name service database: put -name -value -type");
          System.out.println("get: send query to the server dataabse: get -name -type");
          System.out.println("del: remove a name record from server databse: del -name -type");
          System.out.println("browse: retrieve all name records from database: browse");
          System.out.println("exit : terminate client: exit");
          System.out.println("-------  end socket closing ------");
          // write to socket because at this time socket is listening for input stream
          // write to socket to avoid exception on error side
          outToServer.writeUTF("");
        }
        else if(cmd.equals("exit") && (cmdLine.length==1)){
          System.out.println("Closing socket");
          outToServer.writeUTF("");
          System.exit(0);
        }
        else if(cmd.equals("type") && (cmdLine.length==2)){
          outToServer.writeUTF(sentence);
          modifiedSentence = inFromServer.readUTF();
          if(modifiedSentence.equals("record not found")){
            System.out.println("no DNS server with Type: "  +cmdLine[1]);
          }else{

            int DNSportNumber = Integer.parseInt(modifiedSentence);
            // System.out.println(DNSportNumber);
            // now we get the port number
            // close connection with manager and connect to a DNS

            clientSocket.close();
            clientSocket = new Socket(argv[0], DNSportNumber);
            // run a sub client method for DNS server
            System.out.println("Connected to type "+ cmdLine[1]+" DNS server on port "+DNSportNumber);
            subClient(argv[0],DNSportNumber);

            System.out.println("Disconnection from DNS server, reconnect with manager");
          }

          // we get the port bumber
          //close port with manager
          clientSocket.close();

          // connect to DNS Server
          // clientSocket = new Socket()
        }else{
          System.out.println("Unknow manager command.");
        }
        // close the socket
        clientSocket.close();
      }catch(ConnectException e){
        System.out.println("Error:Connection refused, please check host name and port number system exiting");
        System.exit(0);
      }

    }
  }

  public static void subClient(String host, int port){
    String modifiedSentence;
    String sentence;
    BufferedReader inFromUser =
    new BufferedReader(new InputStreamReader(System.in));

    while(true){
      try{
        Socket clientSocket = new Socket(host, port);


        // create an output stream from the socket output stream
        DataOutputStream outToServer =
        new DataOutputStream(clientSocket.getOutputStream());

        // create an input stream from the socket input stream
        DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

        System.out.println("please enter DNS command:");
        sentence = inFromUser.readLine();
        // if user input is help print out help menue
        if (sentence.equals("help")){
          System.out.println("-------HELP MENUE ------");
          System.out.println("put: add name records to the name service database: put -name -value -type");
          System.out.println("get: send query to the server dataabse: get -name -type");
          System.out.println("del: remove a name record from server databse: del -name -type");
          System.out.println("browse: retrieve all name records from database: browse");
          System.out.println("exit : terminate client: exit");
          System.out.println("-------  end socket closing ------");
          // write to socket because at this time socket is listening for input stream
          // write to socket to avoid exception on error side
          outToServer.writeUTF("");
        }else if(sentence.equals("done")){
          System.out.println("Closing socket");
          outToServer.writeUTF("");
          return;
        }
        else{
          // send the sentence read to the server
          outToServer.writeUTF(sentence);

          // // get the reply from the server
          modifiedSentence = inFromServer.readUTF();

          // print the returned sentence
          System.out.println("FROM SERVER: " + modifiedSentence);

        }
        // close the socket
        clientSocket.close();

      }catch(Exception e){

      }
    }
  }



}
