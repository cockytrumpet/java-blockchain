package org.adam.blockchain;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class SmartContractTest {
    private final Lock testLock = new ReentrantLock();
    private final Condition hasSuccess = testLock.newCondition();
    private boolean success = false;
    private BlockChain chain;
    private MiningClient client;

    public SmartContractTest() {
        chain = new BlockChain();
        client = new MiningClient("client", chain);
    }

    class DummyClient extends Client implements SmartContractListener {
        public DummyClient(String name, Client originatingClient) {
            super(name, originatingClient.blockChain);
            chain.registerSmartContractListener(this);
        }

        @Override
        public void onSmartContractEvent(SmartContractEvent event) {
            testLock.lock();
            try {
                success = true;
                hasSuccess.signalAll();
            } finally {
                testLock.unlock();
            }
        }
    }

    @Test
    void testExecution() {
        testLock.lock();
        try {
            Thread.sleep(250);

            client.sendSmartContract(DummyClient::new);

            long startTime = System.currentTimeMillis();
            long timeout = 10000;
            while (!success) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= timeout) {
                    fail("Test timed out.");
                    break;
                }
                hasSuccess.awaitNanos(1_000_000); // 1ms
            }
            assertTrue(success, "Smart contract executed successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted.");
        } finally {
            testLock.unlock();
        }
    }
}
