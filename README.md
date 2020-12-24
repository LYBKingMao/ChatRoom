# ChatRoom
This project is a local chat room running in command line, created to help study Java web.

# Prerequisite
Java Development Kit(JDK) or JRE

# Compilation and Execution
<pre><code>javac -d ../out *.java
cd ../out
java ChatServer
java ChatClient
</code></pre>

# Release
### 2020/12/24 Release 1 Published
Features: Multi-Threads program on both server side and on client to improve program performance. 
+ Server will create three main types of thread. First, a new read thread will be created for every new connection, read thread will check illegal or empty message, assign name for each client, reading incoming messages print them on screen or transmit private messages to corresponding recepient; One thread for sending server messages to clients, it has two choices: broadcast or private message; Another thread for broadcasting server status, it will monitor clients' connect and leave regularly, send online user list to all clients.
+ Clients has two threads. One read thread to receive assigned client number and set it as client name, print messages recived from server and other clients. One send thread for sending messages to server or sending private messages with special prefix, server is responsible for distinguishing message types.

# TODO
If possible, make this local chat room program run in LAN, provide end-to-end encryption during message transmission.
Add UI for this program to make it user-friendly.
