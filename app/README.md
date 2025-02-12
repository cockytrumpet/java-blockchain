# Java Blockchain Framework

A basic implementation of a blockchain system with features such as message sending, currency transactions, smart contracts, and mining.

## Features

- **Message Communication**: Clients can send messages to each other.
- **Currency Transactions**: Supports transferring cryptocurrency between clients.
- **Smart Contracts**: Implements a simple smart contract system with event-driven architecture.
- **Mining**: Includes a proof-of-work mechanism for block creation.
- **Dynamic Difficulty Adjustment**: Automatically adjusts mining difficulty based on block generation time.
- **Event Notification**: Notifies registered listeners about blockchain events.

## Setup

# Execute demo

```bash
git clone https://github.com/cockytrumpet/java-blockchain.git
cd java-blockchain
gradle run
```

## Output

```
Block: 0
Timestamp: 1739377913
Last block: 0
This block: b8e10637d0dff45c26a1263f0e813df951ce0a1ccb670854e2ed2af8eca1b834
Nonce: -1827206417184547790
Proof threshold: 1
Time mining: 2
Miner: David
------------------------------
BlockChain: Initializing chain
------------------------------

David submits a smart contract
David sends 4 🪙 to Alice

Block: 1
Timestamp: 1739377915
Last block: b8e10637d0dff45c26a1263f0e813df951ce0a1ccb670854e2ed2af8eca1b834
This block: ac95e4bc9139d47ff860adf97008d40916ae626065dee2996f1bc55c8fb01e59
Nonce: 8718197121448164435
Proof threshold: 2
Time mining: 2
Miner: Brent
------------------------------
BlockChain sent 10 🪙 to David
SmartContract-2
------------------------------


Block: 2
Timestamp: 1739377915
Last block: ac95e4bc9139d47ff860adf97008d40916ae626065dee2996f1bc55c8fb01e59
This block: aeb3ca968853690987179f51c4613851c3340631e5cc0fdcbf64cff90c6c3c7c
Nonce: 8976438812459490524
Proof threshold: 3
Time mining: 2
Miner: Helen
------------------------------
BlockChain sent 10 🪙 to Brent
David sent 4 🪙 to Alice
------------------------------

<...>

Block: 42
Timestamp: 1739377960
Last block: eebdffffdd1723706cbca00fbbc20d032f9b5bc14dfd6890de66a6014afa4b75
This block: ebbffdff0d020c2b8c1cab5733ee1dd66bf5cab0082e8afad1274ed7224c6ca4
Nonce: 8354239725875837610
Proof threshold: 8
Time mining: 17
Miner: Eve
------------------------------
BlockChain sent 10 🪙 to Jack
SmartContract-2: George wins!
SmartContract-2 sent 5 🪙 to George
------------------------------

<...>

Block: 57
Timestamp: 1739377977
Last block: efbbdddecbbfdd512ddeb197ed490f22ea206a74dec91e03a510849ac29775a3
This block: baddceebabecbd13004d7b7720a6f15836cff2ab83ee3f6ed2e422c62cabb33c
Nonce: -1914363090799768205
Proof threshold: 14
Time mining: 338
Miner: George
------------------------------
BlockChain sent 10 🪙 to Charlie
Adam: message #103
Adam sent 4 🪙 to George
Daniel: message #104
Daniel sent 4 🪙 to Charlie
Crystal: message #105
Crystal sent 4 🪙 to Alice
Julia: message #106
Julia sent 4 🪙 to George
Brent sent 4 🪙 to Frank
George sent 4 🪙 to Frank
SmartContract-2 sent 6 🪙 to David
BlockChain: SHUTDOWN
------------------------------
```
