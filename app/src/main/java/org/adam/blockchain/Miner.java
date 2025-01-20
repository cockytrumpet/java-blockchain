package org.adam.blockchain;

import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attempts to mine the block extracted from a MinerEvent.
 * If it is successfull, the block is submitted to the chain.
 */
class Miner implements Runnable {
    private final Block block;
    private final MiningClient client;
    private boolean isRunning = true;

    public Miner(MinerEvent event) {
        this.client = event.getClient();
        this.block = Block.clone(event.getBlock());
        block.miner = client;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        isRunning = false;
    }

    public MiningClient getClient() {
        return client;
    }

    @Override
    public void run() {
        if (isRunning) {
            // was "^0{%d}" but i needed to try to smooth out the jump in difficulty
            Pattern pattern = Pattern.compile("^[abcdef]{%d}.*".formatted(block.proofThreshold));
            String guessHash;
            long guess;

            block.timeStamp = new Date().getTime();

            StringBuilder hashBuilder = new StringBuilder();
            hashBuilder.append(block.id)
                    .append(block.timeStamp)
                    .append(block.previousHash);

            block.entryList.stream()
                    .sorted(Comparator.comparingInt(a -> a.getId()))
                    .forEach(blockEntry -> hashBuilder.append(blockEntry.toString())
                            .append(Utils.hashFromBytes(blockEntry.getSignature())));

            while (isRunning) {
                StringBuilder guessBuilder = new StringBuilder(hashBuilder.toString());
                guess = ThreadLocalRandom.current().nextLong();
                guessBuilder.append(guess);
                guessHash = Utils.hashFromBytes(guessBuilder.toString().getBytes());
                Matcher matcher = pattern.matcher(guessHash);

                if (matcher.matches()) {
                    block.timeGenerating = new Date().getTime() - block.timeStamp;
                    block.nonce = guess;
                    block.hash = guessHash;
                    block.miner.submitBlock(block);
                    isRunning = false;
                }
            }
        }
    }
}
