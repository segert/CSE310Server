/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 To compile on an allv machine, type
 *       javac TCPServer.java
 *
 * To run an allv machine, type
 *       java TCPServer YOUR_PORT_NUMBER
 *
 */
// applications
public class TCPServer {

    public static void main(String argv[]) throws Exception {

        String clientSentence;
        String capitalizedSentence;

        // get the port number assigned from the command line
        //int lisPort = Integer.parseInt(argv[0]);
        int lisPort = Integer.parseInt("5858");

        // create a server socket (TCP)
        ServerSocket welcomeSocket = new ServerSocket(lisPort);
        System.out.println("Socket Created Please connect to Port: "+ welcomeSocket.getLocalPort());
        // loop infinitely (process clients sequentially)
        while (true) {
            // Wait and accept client connection
            Socket connectionSocket = welcomeSocket.accept();
            System.out.println("connection accepted from "+connectionSocket.getRemoteSocketAddress());

            //create an input stream from the socket input stream
            DataInputStream inFromClient = new DataInputStream(
                    new DataInputStream(connectionSocket.getInputStream()));

            // create an output stream from the socket output stream
            DataOutputStream outToClient
                    = new DataOutputStream(connectionSocket.getOutputStream());

            // read a line form the input stream
            clientSentence = inFromClient.readUTF();
            System.out.println("command recieve: "+ clientSentence);

            String[] args = clientSentence.split(" ");
            String command = args[0];

            if (command.equals("put") && args.length == 4) {
                String record = args[1] + " " + args[2] + " " + args[3];
                put(record);

                connectionSocket.close();
            } else if (command.equals("get") && args.length == 3) {
                String name = args[1];
                String type = args[2];
                String value = get(name, type);

                outToClient.writeBytes(value);

                connectionSocket.close();
            } else if (command.equals("del") && args.length == 3) {
                String name = args[1];
                String type = args[2];
                String value = delete(name, type);

                outToClient.writeBytes(value);

                connectionSocket.close();
            } else if (command.equals("browse") && args.length == 1) {
                String[] recarray = browse();
                String result = "";
                if (recarray.length > 0) {
                    for (int i = 0; i < recarray.length; i++) {
                        result = result + "(" + recarray[i] + ") ";
                    }
                }
                else
                    result = "database is empty";

                outToClient.writeUTF(result);

                connectionSocket.close();
            } else {
                System.out.println("Unknow Command: socket closing");
                connectionSocket.close();
            }
        }

        /*
         }*/
    }

    public static String[] browse() throws FileNotFoundException, IOException {
        File fileName = new File("recorddatabase.txt");

        FileInputStream fis = new FileInputStream(fileName);
        BufferedReader b = new BufferedReader(new InputStreamReader(fis));

        String line = null;
        String value = null;

        ArrayList<String> records = new ArrayList();
        // read first blank line;
        while ((line = b.readLine()) != null) {
            // String[] record = line.split(" ");
            String[] record = new String[3];
            int i =0;
            for(String s:line.split(" ")){
              record[i]=s;
              i++;
            }
            System.out.println("record "+Arrays.deepToString(record));

            records.add(record[0] + " " + record[2]);
            System.out.println("records: ");
            for(int j=0;j<records.size();j++){
              System.out.println(records.get(j));
            }
        }

        b.close();

        String[] recarray = new String[records.size()];
        for (int i = 0; i < recarray.length; i++) {
            recarray[i] = records.get(i);
        }
        System.out.println(Arrays.deepToString(recarray));

        return recarray;
    }

    public static void put(String record) throws FileNotFoundException, IOException {
        File fileName = new File("recorddatabase.txt");

        FileWriter f = new FileWriter(fileName, true);

        f.write(record+'\n');
        f.close();

    }

    public static String get(String name, String type) throws FileNotFoundException, IOException {
        File fileName = new File("recorddatabase.txt");

        FileInputStream fis = new FileInputStream(fileName);
        BufferedReader b = new BufferedReader(new InputStreamReader(fis));

        String line = null;
        String value = null;
        while ((line = b.readLine()) != null) {
            String[] record = line.split(" ");
            if (record[0].equals(name) && record[2].equals(type)) {
                value = record[1];
            }
        }

        b.close();
        if (value.equals(null)) {
            value = "Not Found";
        }

        return value;

    }

    public static String delete(String name, String type) throws FileNotFoundException, IOException {
        File inputFile = new File("recorddatabase.txt");
        File temp = new File("myTempFile.txt");

        FileInputStream fis = new FileInputStream(inputFile);
        BufferedReader b = new BufferedReader(new InputStreamReader(fis));

        String line = null;
        String remove = null;
        while ((line = b.readLine()) != null) {
            String[] record = line.split(" ");
            if (record[0].equals(name) && record[2].equals(type)) {
                remove = line;
            }
        }

        if (remove == null) {
            remove = "Not Found";
            return remove;
        }

        b.close();

        FileInputStream fis2 = new FileInputStream(inputFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis2));
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));

        String currentLine;

        while ((currentLine = bufferedReader.readLine()) != null) {
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if (trimmedLine.equals(remove)) {
                continue;
            }
            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close();
        bufferedReader.close();

        fis2 = new FileInputStream(temp);
        bufferedReader = new BufferedReader(new InputStreamReader(fis2));
        writer = new BufferedWriter(new FileWriter(inputFile));

        while ((currentLine = bufferedReader.readLine()) != null) {

            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close();
        bufferedReader.close();

        temp.delete();

        return remove;
    }
}

/*

 * To compile on an allv machine, type
 *       javac TCPClient.java
 *
 * To run on an allv machine, when the server is run on allv25, type
 *       java TCPClient allv25.all.cs.stonybrook.edu YOUR_PORT_NUMBER
 *


 import java.io.*; // Provides for system input and output through data
 // streams, serialization and the file system
 import java.net.*; // Provides the classes for implementing networking
 // applications

 // TCP Client class
 class TCPClient {
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

 */

/*To compile on an allv machine, type
 *       javac TCPServer.java
 *
 * To run an allv machine, type
 *       java TCPServer YOUR_PORT_NUMBER
 *


 import java.io.*; // Provides for system input and output through data
 // streams, serialization and the file system
 import java.net.*; // Provides the classes for implementing networking
 // applications

 class TCPServer {
 public static void main(String argv[]) throws Exception
 {
 String clientSentence;
 String capitalizedSentence;

 // get the port number assigned from the command line
 int lisPort = Integer.parseInt(argv[0]);

 // create a server socket (TCP)
 ServerSocket welcomeSocket = new ServerSocket(lisPort);

 // loop infinitely (process clients sequentially)
 while(true) {
 // Wait and accept client connection
 Socket connectionSocket = welcomeSocket.accept();

 //create an input stream from the socket input stream
 BufferedReader inFromClient = new BufferedReader(
 new InputStreamReader(connectionSocket.getInputStream()));

 // create an output stream from the socket output stream
 DataOutputStream  outToClient =
 new DataOutputStream(connectionSocket.getOutputStream());

 // read a line form the input stream
 clientSentence = inFromClient.readLine();

 // capitalize the sentence
 capitalizedSentence = clientSentence.toUpperCase() + '\n';

 System.out.println("Hello world!");
 System.out.println("input is: " + clientSentence);
 System.out.println("output is: " + capitalizedSentence);

 // send the capitalized sentence back to the  client
 outToClient.writeBytes(capitalizedSentence);

 // close the connection socket
 connectionSocket.close();
 }
 }
 }

 */
