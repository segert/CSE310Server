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
                String sentence; 
                String modifiedSentence; 

                // get the server port form command line
                int lisPort = Integer.parseInt(argv[1]);

                // create an input stream from the System.in
                BufferedReader inFromUser = 
                new BufferedReader(new InputStreamReader(System.in));

                // create a client socket (TCP) and connect to server
                Socket clientSocket = new Socket(argv[0], lisPort);

                // create an output stream from the socket output stream
                DataOutputStream outToServer = 
                new DataOutputStream(clientSocket.getOutputStream()); 

                // create an input stream from the socket input stream
                BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

                // read a line form the standard input
                sentence = inFromUser.readLine();

                // send the sentence read to the server
                outToServer.writeBytes(sentence + '\n');


                // get the reply from the server
                modifiedSentence = inFromServer.readLine();

                // print the returned sentence
                System.out.println("FROM SERVER: " + modifiedSentence);

                // close the socket
                clientSocket.close();
        }
}