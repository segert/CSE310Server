import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.*;

public class manager{

  public static void main(String[] args) throws Exception{
    // get runtime
    Runtime rt = Runtime.getRuntime();
    // array serverRecord for hold servertype and portbumber
    ArrayList<String> serverRecords = new ArrayList();

    // pharse maganer.in file and start servers
    System.out.println("System initiated: start reading file");
    File fileName = new File("manager.in");
    if(!fileName.exists()){
      System.out.println("manager.in file not found, System exiting");
      System.exit(-1);
    }

    FileInputStream fis = new FileInputStream(fileName);
    BufferedReader b = new BufferedReader(new InputStreamReader(fis));
    String line;

    while ((line=b.readLine()) !=null){
      // create a new process
      Process process = rt.exec("java multiserver");
      //pipe set up get server's systen.in
      DataInputStream inFromMulti = new DataInputStream(
      new DataInputStream(process.getInputStream()));
      // read from server
      String portNumber = inFromMulti.readLine();
      serverRecords.add(line +" "+portNumber);
      inFromMulti.close();
      // pupe set up send type as a string to multiserver
      DataOutputStream outToMulti
      = new DataOutputStream(process.getOutputStream());
      outToMulti.writeUTF(line);
      outToMulti.close();
    }

    System.out.println("Finished Reading Manager.io, launch manager application server");
    // manager application's unique port of 5858
    ServerSocket welcomeSocket = new ServerSocket(5858);
    System.out.println("manager application port numner :" + welcomeSocket.getLocalPort());

    // print server record
    for(int i = 0; i < serverRecords.size(); i++) {
      System.out.print(serverRecords.get(i)+'\n');
    }

    while(true){
      Socket clientSocket = welcomeSocket.accept();
      System.out.println("client connected");

      ManagerServiceThread MThread = new ManagerServiceThread(clientSocket,serverRecords);
      MThread.start();
    }
  }
}


class ManagerServiceThread extends Thread{
  Socket connectionSocket;
  ArrayList<String> serverRecords;
  boolean running = true;

  ManagerServiceThread(Socket s, ArrayList a){
    connectionSocket = s;
    serverRecords = a;
  }

  public void run(){
    String clientSentence;

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

        if (command.equals("type") && args.length == 2) {
          String portNumber = getPortNumberWithType(serverRecords, args[1]);
          System.out.println(portNumber);
          outToClient.writeUTF(portNumber);
          connectionSocket.close();
        }
        else {
          System.out.println("Unknow Command: socket closing");
          connectionSocket.close();
        }
      }


    } catch (Exception e) {
      // e.printStackTrace();
    }

  }

  public String getPortNumberWithType (ArrayList a, String type){
    String r="record not found";
    for(int i = 0; i < a.size(); i++) {
      String records = serverRecords.get(i);
      String[] record = records.split(" ");
      if(record[0].equals(type)){
        r=record[1];
      }
    }
    return r;
  }

}
