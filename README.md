# Java Blockchain

A basic implementation of a blockchain system with message sending, currency transactions, smart contracts, and mining.

## Features

- **Message Communication**: Clients can send messages to each other.
- **Currency Transactions**: Supports transferring cryptocurrency between clients.
- **Smart Contracts**: Implements a simple smart contract system with event-driven architecture.
- **Mining**: Includes a proof-of-work mechanism for block creation.
- **Dynamic Difficulty Adjustment**: Automatically adjusts mining difficulty based on block generation time.
- **Event Notification**: Notifies registered listeners about blockchain events.

# Demonstration

```bash
git clone https://github.com/cockytrumpet/java-blockchain.git
cd java-blockchain
gradle run
```

## Output

```
Block: 0
Timestamp: 1739385015
Last block: 0
This block: d8250b827e37f93c6e806f2725359a539269d7da5113cbb5676b8521a9119558
Nonce: -1522654989606492278
Proof threshold: 1
Time mining: 3
Miner: Charlie
------------------------------
BlockChain: Initializing chain
------------------------------

Charlie submits a smart contract

Block: 1
Timestamp: 1739385016
Last block: d8250b827e37f93c6e806f2725359a539269d7da5113cbb5676b8521a9119558
This block: af8ab366f59e061466f28e363b726393f74ad7f3a821484f3bf3efc7f4d75fda
Nonce: -7274109407116674026
Proof threshold: 2
Time mining: 0
Miner: Ivy
------------------------------
BlockChain sent 10 🪙 to Charlie
SmartContract-2
------------------------------

Charlie sends 4 🪙 to Brent

Block: 2
Timestamp: 1739385016
Last block: af8ab366f59e061466f28e363b726393f74ad7f3a821484f3bf3efc7f4d75fda
This block: dec2352cad4fa9f61d472b6d81184216b2507702e7974ed4639ac86b0deb0699
Nonce: -8950109780524494627
Proof threshold: 3
Time mining: 4
Miner: Julia
------------------------------
BlockChain sent 10 🪙 to Ivy
Charlie sent 4 🪙 to Brent
------------------------------

<...>

Block: 83
Timestamp: 1739385131
Last block: beaffbbfceadfa1a71ecc354cbbe7989fdb2df6699251d2527538aec6291be28
This block: acacccfedfeabb7463307f42db427a33764a4210ec01dccd94788d7257227fd5
Nonce: -1125388868696443617
Proof threshold: 14
Time mining: 292
Miner: Bob
------------------------------
BlockChain sent 10 🪙 to Helen
Bob: message #196
Bob sent 4 🪙 to Adam
Charlie: message #197
Charlie sent 4 🪙 to Frank
David sent 4 🪙 to Julia
SmartContract-2: Brent wins!
SmartContract-2 sent 12 🪙 to Brent
------------------------------

<...>

BlockChain received shutdown command

Block: 133
Timestamp: 1739385204
Last block: edbbfbfbbacfca74311ec082b7e242432302935fa16ffcb29fea987ea90af021
This block: debfbbacffbe02dce282fb447c4b2636c5aa48df256fd0b43f2fcdb9bdc0b2a8
Nonce: -8562619345224877506
Proof threshold: 12
Time mining: 1668
Miner: Bob
------------------------------
BlockChain sent 10 🪙 to George
Eve: message #300
Eve sent 4 🪙 to Ivy
Frank: message #301
Frank sent 4 🪙 to Julia
Grace: message #302
Grace sent 4 🪙 to Brent
Helen: message #303
Helen sent 4 🪙 to Adam
Jack sent 4 🪙 to Daniel
Adam: message #304
Adam sent 4 🪙 to Crystal
Daniel sent 4 🪙 to Alice
Crystal: message #305
Crystal sent 4 🪙 to Frank
Julia sent 4 🪙 to Charlie
Brent: message #306
Brent sent 4 🪙 to Frank
George sent 4 🪙 to Eve
Alice: message #307
Alice sent 4 🪙 to David
Bob sent 4 🪙 to Brent
Charlie sent 4 🪙 to Helen
David: message #308
David sent 4 🪙 to Charlie
Eve: message #309
Eve sent 4 🪙 to Brent
Frank: message #310
Frank sent 4 🪙 to Ivy
Grace: message #311
Grace sent 4 🪙 to Bob
Helen sent 4 🪙 to Eve
Jack: message #312
Jack sent 4 🪙 to David
Adam: message #313
Adam sent 4 🪙 to Helen
Daniel sent 4 🪙 to Eve
Crystal: message #314
Crystal sent 4 🪙 to Charlie
Julia sent 4 🪙 to Crystal
Brent sent 4 🪙 to Ivy
George: message #315
George sent 4 🪙 to Grace
SmartContract-2 sent 19 🪙 to Charlie
BlockChain: SHUTDOWN
------------------------------


Average block generation time: 1293
Awards distributed: 1340
Fees collected: 969
Net change: -371
```
