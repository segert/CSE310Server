/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

/**
* To compile on an allv machine, type
*       javac TCPClient.java
*
* To run on an allv machine, when the server is run on allv25, type
*       java TCPClient allv25.all.cs.stonybrook.edu YOUR_PORT_NUMBER
*

*/
import java.io.*; // Provides for system input and output through data
// streams, serialization and the file system
import java.net.*; // Provides the classes for implementing networking
// applications

// TCP Client class
public class TCPClient {

  public static void main(String argv[]) throws Exception
  {
    // error check for command input
    if (argv.length != 2) {
      System.out.println("Error: Missing Host address and/or port number");
    }else{
      String sentence;
      String modifiedSentence;

      // get the server port form command line
      int lisPort = Integer.parseInt(argv[1]);

      // create an input stream from the System.in
      BufferedReader inFromUser =
      new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Connecting to "+argv[0]+" on port "+lisPort);

      while(true){
        // try connect to server
        try{
          // create a client socket (TCP) and connect to server
          Socket clientSocket = new Socket(argv[0], lisPort);

          System.out.println("Connection established");
          System.out.println("Please Enter Command");

          // create an output stream from the socket output stream
          DataOutputStream outToServer =
          new DataOutputStream(clientSocket.getOutputStream());

          // create an input stream from the socket input stream
          DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

          // read a line form the standard input
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
          }else if(sentence.equals("exit")){
            System.out.println("Closing socket");
            outToServer.writeUTF("");
            System.exit(0);
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
        }catch(ConnectException e){
          System.out.println("Error:Connection refused, please check host name and port number");

        }

      }

    }
  }
}
