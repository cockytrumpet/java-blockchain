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
        client = event.getClient();
        block = event.getBlock();
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
            // NOTE: janky proof of work, first thing to replace if developed further
            Pattern pattern = Pattern.compile("^[abcdef]{%d}[^abcdef]{1}.*".formatted(block.proofThreshold));
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
                    if (block instanceof TerminationBlock) {
                        block.miner.submitBlock(new TerminationBlock(block));
                    } else {
                        block.miner.submitBlock(block);
                    }
                    isRunning = false;
                }
            }
        }
    }
}
