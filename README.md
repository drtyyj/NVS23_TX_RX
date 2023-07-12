# NVS23 Programmierprojekt
TX-RX-program for the nvs-project in java. Sends and receives files via udp packets over network.

## Build

Open with IntelliJ IDEA or build it directly with javac:

```bash
javac Main.java
```


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
send filename [-h destinationIP] [-p portNumber] [-d dataPacketSize] [-s sleepTime]				
```
Send file with given destination portNumber (4445 default), dataPacketSize (1000 Bytes by default, 64994 max).
and sleepTime between sending packages (5 ns default).

```bash
enableack
```

Enables the acknowledgement functionality.

```bash
disableack
```

Disables the acknowledgement functionality.

```bash
windowsize <value>
```

Sets the window size for transmission.


_NOTE_ 
The file must be in a folder called "input" located at same height as the root folder (So right outside of the project). Similarly, the received files will be stored in an "output" folder.


## Changelog

### Version 1 - Basic RX/TX
- Git commit id: a4fa272e1d2745366fbe64ec6f9a48b4b161bfa3

### Version 2 - Added acknowledgement feature
- Git commit id: 5b6279b0b063591676a891044c172bd23f1bd373
- added sleep parameter
- optional acknowledgement feature

#### Most important code changes receiver
```java
// send acknowledgement to sender after receiving packet
socket.send(new DatagramPacket(packet.getData(), 6, packet.getAddress(), packet.getPort()));
```

#### Most important code changes sender
```java
if(awaitAck) {
    try {
        socket.receive(ackPacket);
        manager.processAck(Arrays.copyOf(ackPacket.getData(), ackPacket.getLength()));
        transmissionAttempts = 0;
    } catch (SocketTimeoutException e) {
        transmissionAttempts++;
        if (transmissionAttempts >= 5)
            throw new RuntimeException("Maximum amount of transmission attempts for packet reached, a
        continue;
    }
}
```

```java
if(transmissionAttempts == 0) { 
    Thread.sleep(0, sleep); 
    DatagramPacket dataPacket = new DatagramPacket(buf, length, address, targetPort); 
    socket.send(dataPacket); 
} 
 
try { 
    socket.receive(ackPacket); 
    manager.processAck(Arrays.copyOf(ackPacket.getData(), ackPacket.getLength())); 
    transmissionAttempts = 0; 
} catch(SocketTimeoutException e) { 
    transmissionAttempts++;
    if(transmissionAttempts >= 5)
        throw new RuntimeException("Maximum amount of transmission attempts for packet reached, abor
    continue;
}

```

### Version 3 - Sliding Window
- Git commit id: c957d62f160ee3e1e61d7e1c724204dbe6daa4c5 and following
- added changeable sliding window size
- added parameter for destination ip address

#### Most important code changes receiver

```java
fileSeqNumber = manager.processReceivedData(Arrays.copyOf(packet.getData(), packet.getLength()));
if(awaitAck) {
    socket.send(new DatagramPacket(packet.getData(), 6, packet.getAddress(), packet.getPort()));
    if(windowSize > 0) {
        if(fileSeqNumber > 0) {
            byte[] cuAck = ByteBuffer.allocate(6)
                    .put(packet.getData(), 0, 2)
                    .putInt(fileSeqNumber).array();

            socket.send(new DatagramPacket(cuAck, 6, packet.getAddress(), packet.getPort()));
        }
    } else {
        socket.send(new DatagramPacket(packet.getData(), 6, packet.getAddress(), packet.getPort()));
    }
}

```

```java
protected int processReceivedData(byte[] data) throws IOException, NoSuchAlgorithmException {
        ...
        fileSequenceNumber = transmission.putData(sequenceNumber, Arrays.copyOfRange(data,6, data.length));
        ...
        return fileSequenceNumber;
        }
```
```java
public int putData(int sequenceNumber, byte[] data) {
                     
    windowBuffer.put(sequenceNumber, data);

    if(windowBuffer.size() == windowSize || sequenceNumber == maxSeqNumber) {
        for(Map.Entry<Integer, byte[]> fragment : windowBuffer.entrySet()) {
            if (fragment.getKey() - fileSeqNumber <= 1) {
                fileData.add(fragment.getKey(), fragment.getValue());
                fileSeqNumber = fragment.getKey();
            } else {
                break;
            }
        }
        windowBuffer.clear();
        return fileSeqNumber;
    }
    return 0;
}

```

#### Most important code changes sender
```java
private boolean sendPackets() throws InterruptedException, IOException {
    for(int i = 0; i < windowSize; i++) {
        int length = manager.fillBuffer(buf);
        if(length == 0) {
            return true;
        }
        Thread.sleep(0, sleep);
        DatagramPacket dataPacket = new DatagramPacket(buf, length, address, port);
        socket.send(dataPacket);
    }
    return false;
}
```

```java
public byte[] getPacketFromWindow() {
        if (currentWindowIndex >= window.length) {
        loadNextWindow();
        }
        if(window[currentWindowIndex] == null)
        return new byte[0];
        return window[currentWindowIndex++];
        }

public void loadNextWindow() {
        try{
        Arrays.fill(window, null);
        for(int i = 0; i < window.length; i++) {
        window[i] = getPacket();
        }
        } catch (IOException ignore) {}
        currentWindowIndex = 0;
        }
private void shiftWindow(int amount) {
    int firstNullIndex = 0;
    try {
        for(int i = amount; i < window.length; i++) {
            window[i - amount] = window[i];
        }
        for(int i = window.length - amount; i < window.length; i++) {
            firstNullIndex = i;
            window[i] = getPacket();
        }
    } catch (IOException e) {
        Arrays.fill(window, firstNullIndex, window.length, null);
    }
    currentWindowIndex = 0;
}

public void resetCurrentWindowIndex() {
    currentWindowIndex = 0;
}

public boolean processAck(int ackSequenceNumber){
        int shiftamount=ackSequenceNumber-currentAck;
        if(shiftamount>=0){
        shiftWindow(ackSequenceNumber-currentAck);
        currentAck=ackSequenceNumber;
        }
        return currentAck==maxSequenceNumber;
}

public void loadNextWindow() {
    factory.loadNextWindow();
}

public void resetWindow() {
    factory.resetCurrentWindowIndex();
}

```