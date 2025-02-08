package org.adam.blockchain;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates blockchain activity to test BlockChain and Client.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        BlockChain blockChain = new BlockChain();

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

        Thread.sleep(250);

        /*
         * Clients register to receive blocks in need of mining.
         * Currency is obtained only through mining rewards or
         * transfer from another client.
         * Sending messages or currency incurs a fee.
         */
        long messageCount = 1;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 60000) {
            for (MiningClient client : clients) {
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
