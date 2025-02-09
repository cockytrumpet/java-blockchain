package org.adam.blockchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * The BlockChain class implements a blockchain that can store messages or
 * transactions. It supports adding new blocks, validation and dynamically
 * adjusting mining difficulty based on block generation time.
 *
 * A daemon thread controlls the flow of submitted entries to generated blocks.
 *
 * MiningClient registers to receive events containing the next block to be
 * mined.
 */
class BlockChain {
    private final Deque<Long> miningTimes = new ConcurrentLinkedDeque<>();;
    private final List<MinerListener> minerListeners = new ArrayList<>();
    private final List<SmartContractListener> smartContractListeners = new ArrayList<>();
    private final ConcurrentLinkedDeque<Block> chain = new ConcurrentLinkedDeque<>();
    private final BlockingQueue<BlockEntry> blockEntries = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<Client, Long> balances = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Client, Long> participationAwards = new ConcurrentHashMap<>();

    private final Lock chainLock = new ReentrantLock();
    private final Condition newEntry = chainLock.newCondition();
    private final Condition blockAdded = chainLock.newCondition();
    private final Condition finalBlockAdded = chainLock.newCondition();

    private final long STARTING_BALANCE = 1_000_000L;
    private final long MINER_COMPLETION = 10L;
    private final long MINER_PARTICIPATION = 1L;
    private final long MINING_TIME_WINDOW = 3L;
    private final long FEE = 1L;

    private volatile boolean shuttingDown = false;
    private volatile boolean running = true;
    private long feesCollected = 0L;

    private final Client chainClient;
    private Block nextBlock;

    public BlockChain() {
        chainClient = new Client("BlockChain", this);
        balances.put(chainClient, STARTING_BALANCE);
        chainClient.sendMessage("Initializing chain");
        processEntries();
    }

    /**
     * deamon thread to process entries upon signals
     */
    private void processEntries() {
        Thread processEntriesThread = new Thread(() -> {
            try {
                while (running) {
                    chainLock.lock();
                    try {
                        while (running && blockEntries.isEmpty()) {
                            newEntry.await();
                        }
                        if (!running)
                            break;
                        int oldChainSize = chain.size();
                        prepareNextBlock();
                        while (running && chain.size() == oldChainSize) {
                            blockAdded.await();
                        }
                    } finally {
                        chainLock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        processEntriesThread.setDaemon(true);
        processEntriesThread.start();
    }

    /**
     * Allows MiningClient to register to receive MinerEvent
     *
     * @param Client implementing MinerEventListener
     */
    public void registerMinerListener(MinerListener listener) {
        synchronized (minerListeners) {
            if (!minerListeners.contains(listener)) {
                minerListeners.add(listener);
                // if a block was already ready, send it
                if (nextBlock != null && nextBlock.id == chain.size()) {
                    MinerEvent event = new MinerEvent(this, (MiningClient) listener, nextBlock);
                    listener.onMinerEvent(event);
                }
            }
        }
    }

    /**
     * Allows MiningClient to unregister from receiving events
     *
     * @param Client implementing MinerEventListener
     */
    public void unregisterMinerListener(MinerListener listener) {
        synchronized (minerListeners) {
            minerListeners.remove(listener);
        }
    }

    /**
     * Allows SmartContractClient to register to receive SmartContractEvent
     *
     * @param Client implementing SmartContractListener
     */
    public void registerSmartContractListener(SmartContractListener listener) {
        synchronized (smartContractListeners) {
            if (!smartContractListeners.contains(listener)) {
                smartContractListeners.add(listener);
            }
        }
    }

    /**
     * Allows SmartContractClient to unregister from receiving events
     *
     * @param Client implementing SmartContractListener
     */
    public void unregisterSmartContractListener(SmartContractListener listener) {
        synchronized (smartContractListeners) {
            smartContractListeners.remove(listener);
        }
    }

    /**
     * Populate next block and send it to registered miners.
     */
    private void prepareNextBlock() {
        if (!running) {
            return;
        }

        Block newBlock = chain.size() > 0 ? new Block(chain.peekLast()) : new Block();
        newBlock.proofThreshold = calculateThreshold(newBlock);
        newBlock.entryList = getBlockEntries();
        newBlock.miner = null;

        boolean finalBlock = newBlock.entryList.stream()
                .filter(blockEntry -> blockEntry instanceof Command)
                .map(blockEntry -> (Command) blockEntry)
                .anyMatch(command -> command.text.equals("SHUTDOWN"));

        if (finalBlock) {
            System.out.println("BlockChain received shutdown command");
        }
        nextBlock = finalBlock ? new TerminationBlock(newBlock) : newBlock;

        synchronized (minerListeners) {
            Collections.shuffle(minerListeners);
            for (MinerListener listener : minerListeners) {
                MinerEvent event = new MinerEvent(this, (MiningClient) listener, nextBlock);
                listener.onMinerEvent(event);
            }
        }
    }

    /**
     * Returns a List containing all entries for the new block.
     *
     * @return List of BlockEntry
     */
    public List<BlockEntry> getBlockEntries() {
        chainLock.lock();
        try {
            List<BlockEntry> result = new ArrayList<>();

            if (blockEntries.isEmpty()) {
                result.add(new EmptyBlockEntry());
                return result;
            }

            for (Map.Entry<Client, Long> entry : participationAwards.entrySet()) {
                Transaction transaction = new Transaction(chainClient, entry.getValue(), entry.getKey());
                result.add(chainClient.sign(transaction));
            }

            participationAwards.clear();
            blockEntries.drainTo(result);

            return result;
        } finally {
            chainLock.unlock();
        }
    }

    /**
     * Attempts to maintain a target time for block generation with a moving
     * average.
     *
     * @param Block the calculation will be based on
     */
    private int calculateThreshold(Block newBlock) {
        int TARGET_TIME = 2000;
        int VARIANCE = TARGET_TIME / 10;
        int MAX_TIME = TARGET_TIME + VARIANCE;
        int MIN_TIME = TARGET_TIME - VARIANCE;

        int proofThreshold = newBlock.lastProofThreshold;

        double average = miningTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse((double) TARGET_TIME);

        if (average > MAX_TIME) {
            proofThreshold -= 2;
            if (proofThreshold < 0)
                proofThreshold = 0;
        } else if (average < MIN_TIME) {
            proofThreshold++;
        }

        return proofThreshold;
    }

    /**
     * Upon submission of a valid block:
     * - reward miner (only source of initial supply)
     * - store and update balances to reflect mined blocks
     * - adjust timing for the next block
     *
     * @param Block that was mined
     * @return boolean result of validity checks
     */
    public boolean addBlock(Block newBlock) {
        chainLock.lock();
        try {
            Block block;
            if (newBlock instanceof TerminationBlock) {
                block = newBlock;
            } else {
                block = Block.clone(newBlock);
            }
            if (isValid(block)) {
                if (block.id == chain.size()) { // next in sequence
                    System.out.println(block);
                    chain.add(block);
                    rewardMiner(block.miner, MINER_COMPLETION);
                    processTransactions(block);
                    processSmartContracts(block);
                    updateMiningTimes(block.timeGenerating);
                    if (block instanceof TerminationBlock) {
                        running = false;
                        finalBlockAdded.signalAll();
                        return true;
                    }
                    blockAdded.signalAll();
                    return true;
                } else if (block.id == chain.size() - 1) { // previous block (late submission)
                    if (!participationAwards.containsKey(block.miner)) {
                        rewardMiner(block.miner, MINER_PARTICIPATION);
                    }
                    return false;
                } else { // older block
                    return false;
                }
            } else { // invalid block
                return false;
            }
        } finally {
            chainLock.unlock();
        }
    }

    private void rewardMiner(Client client, long amount) {
        participationAwards.put(client, amount);
        balances.merge(client, amount, Long::sum);
        balances.merge(chainClient, -amount, Long::sum);
    }

    /**
     * Keep a simple moving average
     */
    private void updateMiningTimes(long latestTime) {
        miningTimes.addFirst(latestTime);
        if (miningTimes.size() > MINING_TIME_WINDOW) {
            miningTimes.removeLast();
        }
    }

    /**
     * Takes a Block, registers and executes SmartContracts
     */
    private void processSmartContracts(Block block) {
        block.entryList.stream()
                .filter(blockEntry -> blockEntry instanceof SmartContract)
                .map(blockEntry -> (SmartContract) blockEntry)
                .forEach(smartContract -> {
                    smartContractListeners.add(smartContract.register(this));
                });
        SmartContractEvent event = new SmartContractEvent(this, block);
        synchronized (smartContractListeners) {
            for (SmartContractListener listener : smartContractListeners) {
                listener.onSmartContractEvent(event);
            }
        }
    }

    /**
     * Takes a Block and applies all containing Transaction to balances.
     */
    private void processTransactions(Block block) {
        block.entryList.stream()
                .filter(blockEntry -> blockEntry instanceof Transaction)
                .filter(blockEntry -> ((Transaction) blockEntry).sourceClient != chainClient)
                .forEach(blockEntry -> {
                    long amount = 0;

                    if (blockEntry instanceof Transaction) {
                        Transaction transaction = (Transaction) blockEntry;
                        amount += transaction.amount;
                        balances.merge(transaction.destinationClient, amount, Long::sum);
                    }

                    balances.merge(blockEntry.getSourceClient(), -(amount + FEE), Long::sum);
                });
    }

    /**
     * Checks validity of Block hash, the previous hash, and the signatures of the
     * BlockEntrys.
     */
    public boolean isValid(Block block) {
        StringBuilder builder = new StringBuilder();

        builder.append(block.id)
                .append(block.timeStamp)
                .append(block.previousHash);

        boolean signaturesValid = block.entryList.stream()
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .map(blockEntry -> {
                    builder.append(blockEntry.toString())
                            .append(Utils.hashFromBytes(blockEntry.getSignature()));
                    return blockEntry.isValid();
                })
                .allMatch(Boolean::booleanValue);

        builder.append(block.nonce);

        String hash = Utils.hashFromBytes(builder.toString().getBytes());
        boolean hashValid = block.hash.equals(hash);

        boolean lastHashValid = block.id == 0 ? block.previousHash.equals("0")
                : block.previousHash.equals(chain.descendingIterator().next().hash);

        return hashValid && lastHashValid && signaturesValid;
    }

    /**
     * Submits BlockEntry for inclusion in a future block.
     * Checks for Command to events (shutdown)
     * Checks balance requirements.
     * Applies fees.
     */
    public boolean send(BlockEntry blockEntry) {
        chainLock.lock();
        try {
            if (running && !shuttingDown && blockEntry.isValid()) {
                if (blockEntry instanceof Command) {
                    Command command = (Command) blockEntry;
                    if (command.sourceClient == chainClient) {
                        // refactor if more commands are added
                        shuttingDown = command.text.equals("SHUTDOWN");
                    }
                }
                long requiredBalance = blockEntry instanceof Transaction
                        ? ((Transaction) blockEntry).amount + FEE
                        : FEE;
                long balance = balances.getOrDefault(blockEntry.getSourceClient(), 0L);
                long pendingTransactions = 0;

                for (BlockEntry entry : blockEntries) {
                    if (entry instanceof Transaction) {
                        Transaction transaction = (Transaction) entry;
                        if (transaction.sourceClient == blockEntry.getSourceClient()) {
                            pendingTransactions -= transaction.amount;
                        }
                        if (transaction.destinationClient == blockEntry.getSourceClient()) {
                            pendingTransactions += transaction.amount;
                        }
                    }
                }

                if (requiredBalance > balance + pendingTransactions) {
                    return false;
                }

                balances.merge(chainClient, FEE, Long::sum);
                feesCollected += FEE;
                blockEntries.put(blockEntry);
                newEntry.signalAll();
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            return false;
        } finally {
            chainLock.unlock();
        }
    }

    public void printBalances() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Client, Long> entry : balances.entrySet()) {
            if (entry.getKey() != chainClient) {
                builder.append(entry.getKey().name)
                        .append("=")
                        .append(entry.getValue())
                        .append("\n");
            }
        }
        System.out.println(builder.toString());
    }

    public void printChain() {
        for (Block block : chain) {
            System.out.println(block);
        }
    }

    public void printSummary() {
        long chainBalance = balances.getOrDefault(chainClient, 0L);
        StringBuilder builder = new StringBuilder();
        double averageBlockTime = chain.stream()
                .map(block -> block.timeGenerating)
                .collect(Collectors.averagingDouble(i -> i));
        builder.append("\nAverage block generation time: ").append((int) averageBlockTime)
                .append("\n")
                .append("Awards distributed: ").append(-1 * (STARTING_BALANCE - chainBalance - feesCollected))
                .append("\n")
                .append("Fees collected: ").append(feesCollected)
                .append("\n")
                .append("Net change: ").append(STARTING_BALANCE - chainBalance)
                .append("\n");
        System.out.println(builder.toString());
    }

    public long getBalance(Client client) {
        return balances.getOrDefault(client, 0L);
    }

    public int getLength() {
        return chain.size();
    }

    /**
     * Sends shutdown Command to the chain.
     *
     * - prevents further entry submissions
     */
    public void shutdown() throws InterruptedException {
        chainLock.lock();
        try {
            Command command = new Command(chainClient, "SHUTDOWN");
            send(chainClient.sign(command));

            while (running && !(chain.peekLast() instanceof TerminationBlock)) {
                finalBlockAdded.await();
            }

            running = false;
            blockAdded.signalAll(); // Notify any waiting block added observers
            newEntry.signalAll(); // Notify any waiting entry processors
        } finally {
            chainLock.unlock();
        }

    }
}
