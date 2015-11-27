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

public class multiserver extends Thread{
  ServerSocket m_ServerSocket;
   String serverType;
  //constructor
  multiserver(String givenType){
    try{
      m_ServerSocket=new ServerSocket(0);
    }catch(Exception e){
    }
    serverType = givenType;
  }

  public  String getport(){
    String r = String.valueOf(m_ServerSocket.getLocalPort());
    return r;
  }

  public void run() {
    System.out.println("Type " + serverType + " Server run on port "+ m_ServerSocket.getLocalPort());

    while(true){
      // socket created
      try{
        Socket clientSocket = m_ServerSocket.accept();

        System.out.println("socket accepted");
        ClientServiceThread cliThread = new ClientServiceThread(clientSocket,serverType);
        System.out.println("thread creaded");

        cliThread.start();
      }catch(Exception e){

      }
    }
  }
}

class ClientServiceThread extends Thread{
  Socket connectionSocket;
  String serverType;
  boolean running = true;

  //constructor
  ClientServiceThread(Socket s, String type){
    connectionSocket = s;
    serverType = type;
  }

  public void run() {
    String clientSentence;
    //start of run
    System.out.println("connection accepted from "+connectionSocket.getRemoteSocketAddress());
    try{
      //create an input stream from the socket input stream
      DataInputStream inFromClient = new DataInputStream(
      new DataInputStream(connectionSocket.getInputStream()));

      // create an output stream from the socket output stream
      DataOutputStream outToClient
      = new DataOutputStream(connectionSocket.getOutputStream());
      while (running) {


        // read a line form the input stream
        clientSentence = inFromClient.readUTF();
        System.out.println("command recieve: "+ clientSentence);

        String[] args = clientSentence.split(" ");
        String command = args[0];

        if (command.equals("put") && args.length == 3) {
          // record format: name ip_address type
          String record = args[1] + " " + args[2] + " " + serverType;
          put(record,serverType);
          outToClient.writeUTF("record "+record+" successfully put in database");
          connectionSocket.close();
        } else if (command.equals("get") && args.length == 2) {
          String name = args[1];
          String type = serverType;
          String value = get(name, type);

          outToClient.writeUTF(value);
          connectionSocket.close();
        } else if (command.equals("del") && args.length == 2) {
          String name = args[1];
          String type = serverType;
          String value = delete(name, type);

          outToClient.writeUTF("record: "+value+" deleted");

          connectionSocket.close();
        } else if (command.equals("browse") && args.length == 1) {
          String[] recarray = browse(serverType);
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
        } else if(command.equals("exit")&&args.length ==1) {
          running = false;
          System.out.println("Thread exit");
        }else {
          System.out.println("Unknow Command: socket closing");
          connectionSocket.close();
        }
      }


    } catch (Exception e) {
      // e.printStackTrace();
    }


    //end of run
  }
  public static void put(String record, String serverType) throws FileNotFoundException, IOException {
    File fileName = new File(serverType+".txt");
    if(!fileName.exists()){
      fileName.createNewFile();
    }
    FileWriter f = new FileWriter(fileName, true);

    f.write(record+'\n');
    f.close();

  }

  public static String get(String name, String type) throws FileNotFoundException, IOException {
    File fileName = new File(type+".txt");
    if(!fileName.exists()){
      fileName.createNewFile();
    }
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
    if (value == null) {
      value = "Error: Record Not Found";
    }

    return value;

  }

  public static String delete(String name, String type) throws FileNotFoundException, IOException {
    File inputFile = new File(type+".txt");
    File temp = new File("myTempFile.txt");
    if(!inputFile.exists()){
      inputFile.createNewFile();
    }
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
      remove = "Error: Record Not Found";
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

  public static String[] browse(String serverType) throws FileNotFoundException, IOException {
    // we use file as a database
    // data format : name value type
    // open file and establish input stream
    File fileName = new File(serverType+".txt");
    if(!fileName.exists()){
      fileName.createNewFile();
    }
    FileInputStream fis = new FileInputStream(fileName);
    BufferedReader b = new BufferedReader(new InputStreamReader(fis));

    String line = null;
    String value = null;
    // records array will holds the return value
    ArrayList<String> records = new ArrayList();

    // basically we read a line which is "name value type"
    // and we pharse the string to get name and type filed and put them in to a string

    // pharse string
    while ((line = b.readLine()) != null) {
      // String[] record = line.split(" ");
      String[] record = new String[3];
      int i =0;
      for(String s:line.split(" ")){
        record[i]=s;
        i++;
      }
      // put name and type field in to array
      records.add(record[0] + " " + record[2]);

      // for(int j=0;j<records.size();j++){
      //   System.out.println(records.get(j));
      // }
    }

    b.close();

    String[] recarray = new String[records.size()];
    for (int i = 0; i < recarray.length; i++) {
      recarray[i] = records.get(i);
    }

    return recarray;
  }

  // end of thread class
}