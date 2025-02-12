package org.adam.blockchain;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simulates blockchain activity to demonstrate functionality.
 *
 * MiningClients register to receive blocks in need of mining.
 * Currency is created only through mining rewards.
 * Sending messages, currency, or smart contracts incur a fee.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        AtomicReference<SmartContractListener> listenerReference = new AtomicReference<>();
        AtomicBoolean lotterySubmitted = new AtomicBoolean(false);
        LotteryClient lotteryClient = null;

        BlockChain blockChain = new BlockChain();

        // spin up some clients
        List<String> names = List.of(
                "Alice", "Bob", "Charlie", "David", "Eve",
                "Frank", "Grace", "Helen", "Ivy", "Jack",
                "Adam", "Daniel", "Crystal", "Julia", "Brent",
                "George", "Francis", "Steve", "Theresa", "Kate");
        int numberOfClients = Runtime.getRuntime().availableProcessors();
        List<MiningClient> clients = names.stream()
                .limit(numberOfClients)
                .map(name -> new MiningClient(name, blockChain))
                .toList();

        // make lottery available once instanciated
        new Thread(() -> {
            try {
                while (blockChain.getActiveContracts().isEmpty()) {
                    Thread.sleep(500);
                }
                listenerReference.set(blockChain.getActiveContracts().getFirst());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Flood the chain with submissions for DURATION millis
        long DURATION = 60000 * 3;
        Thread.sleep(250);
        long messageCount = 1;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < DURATION) {
            for (Client client : clients) {
                // whoever has currency, start a lottery smart contract
                if (client.getBalance() >= 1 && lotterySubmitted.compareAndSet(false, true)) {
                    System.out.println(client.getName() + " submits a smart contract");
                    client.sendSmartContract(LotteryClient::new);
                }

                // maybe gamble a bit
                if (lotteryClient == null) {
                    SmartContractListener listener = listenerReference.get();
                    if (listener instanceof LotteryClient foundClient) {
                        lotteryClient = foundClient;
                    }
                } else {
                    if (client.getBalance() >= 2 && random.nextInt(1, 16) == 1) {
                        System.out.println(client.getName() + " sends 1 🪙 to " + lotteryClient.getName());
                        client.sendCurrency(1L, lotteryClient);
                    }
                }

                // maybe send a message
                if (client.getBalance() >= 1 && random.nextInt(1, 3) == 1) {
                    String message = "message #" + messageCount++;
                    System.out.println(client.getName() + " says " + "\"" + message + "\"");
                    client.sendMessage(message);
                }
                // maybe send currency
                if (client.getBalance() >= 5) {
                    int thisIndex = clients.indexOf(client);
                    int nextIndex;
                    do {
                        nextIndex = random.nextInt(clients.size());
                    } while (nextIndex == thisIndex);

                    client.sendCurrency(4, clients.get(nextIndex));
                    System.out.println(client.getName() + " sends 4 🪙 to " + clients.get(nextIndex).getName());
                }
                Thread.sleep(250);
            }
        }

        blockChain.shutdown();
        blockChain.printSummary();
    }
}
