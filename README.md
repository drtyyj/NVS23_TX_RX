# NVS23_TX_RX
TX-RX-program for the nvs-project in java.
USAGE: 
java Main [portnumber]    run program with receiver listening to specified portnumber (4445 by default).

Afterwards the receiver will run in the background until termination and a prompt will appear, and you 
can use the following commands:

quit                                                    quits the program and terminates the receiver.
send filename [-p portNumber] [-d dataPacketSize]       Send file with given destinationport (4445 default) and datapacketsize 
                                                        (260 Bytes by default).
