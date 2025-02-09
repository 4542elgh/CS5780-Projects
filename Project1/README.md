# Project 1:  Simplified SSL (Secure Socket Layer)
No email submissions will be accepted!

**Please zip up all your source codes (java files), txt files, and document, upload only one file to Canvas. You are only allowed to upload  jar, zip or rar file.**

Note: In the doc file you submitted should contain the following information:
1. Project description
2. How to run your program
3. Screenshots of each test
4. Percentage of each member’s contribution to the project.

One submission per group.

---
Conceptually a Secure Socket Layer can be thought of as a pair of sockets between a server and a client where communication on the actual network socket is secure.
![SSL](SSL.png)
Clever implementation can actually hide the mess of encryption, decryption, and key exchange protocol entirely. As far as the server is concerned, it only wants to know if the client is authorized and receive and send data in clear-text even though the actual bytes on the network are encrypted. As far as the client is concerned, it only wants to know that it is connected to the real server and also wants to exchange data in clear-text even though the physical bytes transmitted are encrypted.

Let us investigate each point in a bit more detail.
# Server's perspective
## Authorized Client
The server stores the user's public key in the users profile. The user also stores the server's public key in its server profile. When the user connects to the server, it sends its own identity (user name) encrypted by the server's public key and its company name encrypted by its own private key. The server's users profile also contains information on the user's hash function used in data transfer. This information was communicated off-line.

Only the server can decrypt the identity of the user. The server also has the client's public key, hence it can decrypt its company information and certify the user. Unfortunately this alone does not prevent a malicious entity to connect to the server by hijacking the encrypted bitstream on the network. While he/she cannot decrypt the transmission, it can be used as a key to open a connection to the server. To circumvent such attacks, data exchange employs a hash function which requires parameters which are agreed between the client and the server but were communicated off-line. If the hashed checksums on the transmitted packets do not agree, the server immediately closes the socket.
## Secure Communication
Once the client is authorized, communication is performed using the client's unique hash function and a one-time key proposed by the client.
# Client's Perspective
## Contacting The Real Server
The server's public key is used to encode the client's identity. The matching private key is needed to decrypt the client's identity and the proposed one-time key.
## Secure Communication
In this particular model, connection is initiated by the client and he/she proposes the one-time key.
# Notation
- $KR_S$: Server's private key
- $KU_S$: Server's public key
- $KR_C$: Client's private key
- $KU_C$: Client's public key
- K: One-time key proposed by client
- h(X): Data packets obtained by appending checksums to message X using the client's hash function
- ndatabytes: number of data bytes in a packet
- ncheckbytes: number of checksum bytes in a packet
# Handshake (Authorization & Key Exchange)
The client communicates the following three pieces of information to the server
- user name
- company
- proposed one-time key

$KU_S$(user name) || $KR_C$(company) || $KU_S$(K)

The server decodes the **user name** with $KR_S$, and decodes **company** with $KU_C$ (obtained from users profile). If the **company** information sent does not match the one stored in the users profile, access is denied. On success, **K** (proposed one-time key) is decoded and stored for further communication.
## Data Transfer
Both the client and the server uses the client's hash function (h) and one-time key (K) to exchange data. Formally, message **X** is sent as **K(h(X))**.

The first step is to assemble packets which will be transmitted over the network. Fixed sized packets are much easier to work with. Use the following scheme to obtain the packets:
- chop the message into fixed sized (ndatabytes big) pieces
- calculate the checksum and append it to the message
- assemble the packet as
```
+-----+---------------------------------+-------------------+
|  n  |            data bytes           |     checksum      |
+-----+---------------------------------+-------------------+
|<-1->|<----------ndatabytes---------- >|<---ncheckbytes--->|
```

> where **n** is the number of actual data bytes used in the **data bytes** field and is one byte long. In case, less then **ndatabytes** bytes are to be transmitted, the receiving entity knows that only the first **n** bytes of the **data bytes** are meaningful. **checksum** is obtained by the users hash function and is described below. Once the packet is assembled, it is encrypted with the one-time key **K**.

**Hash Function (Calculating the checksum and Assembling packets):**
$$h(X) := [\Sigma^{ndatabytes}_{i=1} (pattern\ \&\ data\_btes[i]) * k] * mod\ 2^{8*ncheckbytes}$$
Where:
- ndatabytes is the number of bytes in the packet
- data_bytes[i] is the $i^{th}$ data byte in the packet
- ncheckbytes is the number of checkbytes
- pattern is a one byte bit pattern
- k is an odd integer
- **&** represents the bit-wise and operator.
**pattern and k are only known to the user and the server and they were communicated off line.**

Example
- ndatabytes = 3
- data_bytes = 01100101 10110101 (If less than ndatabyes, pad trailing with 0s)
- ncheckbytes = 1
- pattern = 123 (is 01111011) **Remember pattern is 1 byte, pad leading with 0s if less than 8 bits
- k = 7

From these, the packet size is 5 bytes. Even though the number of data bytes is 3 in a packet only two will be used. Hence we know the first part of the packet:
```
00000010 01100101 10110101 00000000
   n = 2     data bytes
```

The checksum is
```
    01100101 & 01111011 = 01100001 (bit wise AND)
    10110101 & 01111011 = 00110001
    00000000 & 01111011 = 00000000
 \+ -------------------------------------
                          10010010 = 146
146 * 7 (k value) = 1022 = 1111111110
```
$1022 * mod\ 2^{8*1} = 254 = 11111110$

Hence the packet transmitted is
```
00000010 01100101 10110101 00000000 11111110
Applying the One-time Key (Encryption)
```
Generate a one-time key which is exactly as long as a packet. If the packet is 5 bytes long a one-time key could be
```
K = 10100110 00101110 01110101 01010110 10001110
```
To encode a packet simply apply the bit-wise exclusive or (^) with a packet.
0 0 = 0
0 1 = 1
1 0 = 1
1 1 = 0
To continue with the example above, the encoded packet is:
```
    00000010 01100101 10110101 00000000 11111110
 ^  10100110 00101110 01110101 01010110 10001110
  ----------------------------------------------
    10100100 01001011 11000000 01010110 01110000
```
**Disassembling the packets**
The exclusive or operation used again with the same key results in the original data:
```
    10100100 01001011 11000000 01010110 01110000
^   10100110 00101110 01110101 01010110 10001110
  ----------------------------------------------
    00000010 01100101 10110101 00000000 11111110
```
The first byte is the number of bytes used, in this case it is 2. The next two data bytes are **01100101 10110101**. Calculate the h and compare it to the checksum byte. If it does not agree, the chances are someone is faking the user.

**Generating RSA Public and Private Keys**
Choose two large prime numbers p and q.
Calculate n = p * q
Calculate ф(n) = (p-1) * (q-1)
Find an integer e which is relative prime to фn)
Calculate d, the inverse of e modulo ф(n)
KU = {e,n}
KR = {d,n}
Plain-text M which must occupy less then n is converted to cipher-text C as
     C = $M^e$  mod n
The original message can be recovered by the private key as
     M = $C^{d\ mod}n$
## Hint
**Do not bother implementing prime number generators, you will need really big ones. Instead take a good look at the BigInteger class in the Math package. It has everything you need!**

---
# Task Breakdown and Marking Scheme
## (A) Implement the RSA key generation and ciphering algorithm [20 marks]
The class(es) should be able to generate public and private key pairs of arbitrary size and should be able to cipher and decipher short messages.
In other words, I should be able to run your program to generate a private and public key pair and use it to cipher some data and decipher it.
## (B) Implement the hash function and one-time key encryption [ 20 marks]
The classes should be able to assemble the data into packets calculate the checksums. Implement one-time key generation, use it to encode and decode data.
In other words, I should be able to run your program to verify that these modules work.
## (C) Implement the SSL layer using the classes from part A and part B and demonstrate it with a simple application described below [ 60 marks]
Implement the handshake (key exchange) described above and transfer data hashed and encrypted with the one-time key. This entails opening a network socket connection to the server, exchanging the one-time key, authorizing the client and then transferring and receiving data via the socket.

The application to demonstrate your work is very simple. The Client opens a connection to the Server. The handshake takes place, then the client sends data read from the keyboard and sends it encrypted to the server. The server examines the data and re-sends it encrypted to the client, but slightly modified: all upper case characters are converted to lower case, and all lower case characters are converted to upper case. Store the keys, the user info and the users profile in files, you are free to choose the format. I suggest you take a look at the java.util.Properties class (and its load method).

You may use the following two classes as a template. They implement the application without SSL.

```
 SimpleClient.javaDownload SimpleClient.java
 SimpleServer.javaDownload  SimpleServer.java

C:\work>javac SimpleClient.java
C:\work>javac SimpleServer.java

Open two MS-DOS prompts

In MS-DOS prompt 1
C:\work>java SimpleServer 3445

In MS-DOS prompt 2
C:\work>java SimpleClient xxx 3445  < SimpleClient.java

where xxx is the host name of your computer
```

I should be able to run the server and the client which now employs a fully functional **SSL** layer and accomplishes the same task. I will modify your users profile, change the keys and examine the data transferred through the socket to make sure it is encrypted, and you utilize every key and parameter. In the model solution I implemented the entire encryption mechanism behind the scenes by creating my own **ServerSocket, Socket, CryptoInputStream and CryptoOutputStream** classes.
## What to hand in?
- Source code
All and only the .java files.  All files I need to run your program (private key, users profile, etc ...)
## Instruction
What to type on the command line to test part A, part B and part C. (in other words, how to run your programs)

**You may not use** the built in RSA classes! (otherwise you get a 0 on part A!). **You may not use** non JDK libraries. The **JDK** API documentation is available from java.sun.com/docsLinks to an external site.. As Java is 100% portable, you may use any development platform.

---
# Model solution
These are the class files (not the source codes!). First create a new directory (for example, project). Under this directory. Now download the file  SSLproject.jar Download  SSLproject.jar to the new directory.  Follow the instructions below:

```
c:\work\project> jar -xvf SSLproject.jar

Part A
c:\work\project> java security.RSA -help
c:\work\project> java -Dprime_size=500 security.RSA -gen "hello world"

Part B
c:\work\project> java security.Hash
c:\work\project> java security.Hash 13 2 131 7 hello
c:\work\project> java security.OneTimeKey
c:\work\project> java security.OneTimeKey xyz 123abc


Part C
Open up two MS-DOS prompts

In MS-DOS prompt 1
c:\work\project> java  -Dserver.private_key=private_key.txt    -Dserver.users=users.txt -Dserver.port=3445  Server

In MS-DOS prompt 2
c:\work\project> java Client xxx 3445 mickey < users.txt

where xxx is your machine's host name
```
Your codes should work in the same way.
You may also look at the documentation of the model solution, but don't feel bound by it.

---
Acknowledgements:  Thanks Dr. István for providing the project materials!