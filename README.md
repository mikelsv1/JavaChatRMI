# JAVA RMI chat

This is a simple chat application using Java RMI. Done as a part of the Distributed Systems course.

Author: Raphaël and Mikel Salvoch

## How to run

1. Compile the source code
    javac *.java

2. Start the RMI registry
    ```bash
    rmiregistry <PORT> &
    ```
    Example: rmiregistry 1099 &

3. Start the server
    ```bash
    java ChatServer <HOST> <PORT>
    ```
    Example: java ChatServer localhost 1099

4. Start the client
    ```bash
    java ChatClient <HOST> <PORT>
    ```
    Example: java ChatClient localhost 1099

5. Start chatting