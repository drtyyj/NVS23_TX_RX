# NVS23_TX_RX
TX-RX-program for the nvs-project in java.

## Running the TX-RX Program

```bash
java Main [portnumber]    
```
run program with receiver listening to specified portnumber (4445 by default).

Afterwards the receiver will run in the background until termination and a prompt will appear, and you 
can use the following commands:

```bash
quit																
````
quits the program and terminates the receiver.

```bash
send filename [-p portNumber] [-d dataPacketSize] [-s sleepTime]				
```
Send file with given destination portNumber (4445 default), dataPacketSize (1000 Bytes by default, 64994 max).
and sleepTime between sending packages (5 ns default).

_NOTE_ 
The file must be in a folder called "input" located at same height as the root folder (So right outside of the project). Similarly, the received files will be stored in an "output" folder.
