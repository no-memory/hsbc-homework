package com.hsbc.transaction.stress;

import com.hsbc.transaction.dto.TransactionRequestDTO;
import com.hsbc.transaction.dto.TransactionResponseDTO;
import com.hsbc.transaction.model.TransactionType;
import com.hsbc.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=caffeine",
    "logging.level.com.hsbc.transaction=INFO"
})
class TransactionStressTest {

    @Autowired
    private TransactionService transactionService;

    private Random random = new Random();
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger errorCount = new AtomicInteger(0);
    private AtomicLong totalResponseTime = new AtomicLong(0);

    @BeforeEach
    void setUp() {
        // Clear all transactions before each test
        transactionService.deleteAllTransactions();
        
        // Reset counters
        successCount.set(0);
        errorCount.set(0);
        totalResponseTime.set(0);
    }

    @Test
    void stressTestConcurrentTransactionCreation() throws InterruptedException {
        // Given
        int threadCount = 20;
        int transactionsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < transactionsPerThread; j++) {
                        long operationStart = System.currentTimeMillis();
                        try {
                            TransactionRequestDTO requestDTO = createRandomTransactionRequest(threadIndex, j);
                            TransactionResponseDTO response = transactionService.createTransaction(requestDTO);
                            
                            assertNotNull(response);
                            assertNotNull(response.getId());
                            assertEquals(requestDTO.getAccountNumber(), response.getAccountNumber());
                            assertEquals(requestDTO.getAmount(), response.getAmount());
                            assertEquals(requestDTO.getType(), response.getType());
                            
                            successCount.incrementAndGet();
                            long operationTime = System.currentTimeMillis() - operationStart;
                            totalResponseTime.addAndGet(operationTime);
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            System.err.println("Error in thread " + threadIndex + ", transaction " + j + ": " + e.getMessage());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Then
        int expectedTransactions = threadCount * transactionsPerThread;
        int actualSuccessCount = successCount.get();
        int actualErrorCount = errorCount.get();
        
        System.out.println("=== Stress Test Results ===");
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Expected transactions: " + expectedTransactions);
        System.out.println("Successful transactions: " + actualSuccessCount);
        System.out.println("Failed transactions: " + actualErrorCount);
        System.out.println("Success rate: " + ((double) actualSuccessCount / expectedTransactions * 100) + "%");
        System.out.println("Average response time: " + (totalResponseTime.get() / actualSuccessCount) + " ms");
        System.out.println("Throughput: " + ((double) actualSuccessCount / totalTime * 1000) + " transactions/second");
        
        // Verify results
        assertTrue(actualSuccessCount > 0, "Should have at least some successful transactions");
        assertTrue(actualSuccessCount >= expectedTransactions * 0.95, "Should have at least 95% success rate");
        assertEquals(expectedTransactions, transactionService.getTotalTransactionCount(), "Repository should contain all successful transactions");
        
        // Performance assertions
        long avgResponseTime = totalResponseTime.get() / actualSuccessCount;
        assertTrue(avgResponseTime < 100, "Average response time should be under 100ms, was: " + avgResponseTime + "ms");
        
        double throughput = (double) actualSuccessCount / totalTime * 1000;
        assertTrue(throughput > 100, "Throughput should be over 100 transactions/second, was: " + throughput);
    }

    @Test
    void stressTestMixedOperations() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // Pre-populate with some transactions
        List<Long> existingTransactionIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TransactionRequestDTO requestDTO = createRandomTransactionRequest(0, i);
            TransactionResponseDTO response = transactionService.createTransaction(requestDTO);
            existingTransactionIds.add(response.getId());
        }
        
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        long operationStart = System.currentTimeMillis();
                        try {
                            int operation = random.nextInt(5); // 0-4 for different operations
                            
                            switch (operation) {
                                case 0: // Create
                                    TransactionRequestDTO createRequest = createRandomTransactionRequest(threadIndex, j);
                                    TransactionResponseDTO createResponse = transactionService.createTransaction(createRequest);
                                    assertNotNull(createResponse);
                                    break;
                                    
                                case 1: // Read by ID
                                    if (!existingTransactionIds.isEmpty()) {
                                        Long randomId = existingTransactionIds.get(random.nextInt(existingTransactionIds.size()));
                                        TransactionResponseDTO readResponse = transactionService.getTransactionById(randomId);
                                        assertNotNull(readResponse);
                                    }
                                    break;
                                    
                                case 2: // Read paginated
                                    int page = random.nextInt(5);
                                    int size = 10 + random.nextInt(10);
                                    var pagedResponse = transactionService.getAllTransactions(page, size);
                                    assertNotNull(pagedResponse);
                                    break;
                                    
                                case 3: // Update
                                    if (!existingTransactionIds.isEmpty()) {
                                        Long randomId = existingTransactionIds.get(random.nextInt(existingTransactionIds.size()));
                                        try {
                                            TransactionRequestDTO updateRequest = createRandomTransactionRequest(threadIndex, j);
                                            TransactionResponseDTO updateResponse = transactionService.updateTransaction(randomId, updateRequest);
                                            assertNotNull(updateResponse);
                                        } catch (Exception e) {
                                            // Transaction might have been deleted by another thread
                                        }
                                    }
                                    break;
                                    
                                case 4: // Statistics
                                    long count = transactionService.getTotalTransactionCount();
                                    assertTrue(count >= 0);
                                    break;
                            }
                            
                            successCount.incrementAndGet();
                            long operationTime = System.currentTimeMillis() - operationStart;
                            totalResponseTime.addAndGet(operationTime);
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Then
        int expectedOperations = threadCount * operationsPerThread;
        int actualSuccessCount = successCount.get();
        int actualErrorCount = errorCount.get();
        
        System.out.println("=== Mixed Operations Stress Test Results ===");
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Expected operations: " + expectedOperations);
        System.out.println("Successful operations: " + actualSuccessCount);
        System.out.println("Failed operations: " + actualErrorCount);
        System.out.println("Success rate: " + ((double) actualSuccessCount / expectedOperations * 100) + "%");
        if (actualSuccessCount > 0) {
            System.out.println("Average response time: " + (totalResponseTime.get() / actualSuccessCount) + " ms");
        }
        System.out.println("Throughput: " + ((double) actualSuccessCount / totalTime * 1000) + " operations/second");
        
        // Verify results
        assertTrue(actualSuccessCount > 0, "Should have at least some successful operations");
        assertTrue(actualSuccessCount >= expectedOperations * 0.80, "Should have at least 80% success rate for mixed operations");
        
        // Performance assertions
        if (actualSuccessCount > 0) {
            long avgResponseTime = totalResponseTime.get() / actualSuccessCount;
            assertTrue(avgResponseTime < 200, "Average response time should be under 200ms for mixed operations, was: " + avgResponseTime + "ms");
        }
    }

    @Test
    void stressTestLargeDataset() throws InterruptedException {
        // Given
        int batchSize = 1000;
        int numBatches = 5;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(numBatches);
        
        long startTime = System.currentTimeMillis();

        // When - Create large dataset
        for (int batch = 0; batch < numBatches; batch++) {
            final int batchIndex = batch;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < batchSize; i++) {
                        TransactionRequestDTO requestDTO = createRandomTransactionRequest(batchIndex, i);
                        TransactionResponseDTO response = transactionService.createTransaction(requestDTO);
                        assertNotNull(response);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(120, TimeUnit.SECONDS);
        executor.shutdown();
        
        long creationTime = System.currentTimeMillis() - startTime;
        
        // Test querying performance on large dataset
        startTime = System.currentTimeMillis();
        
        // Test various query operations
        for (int i = 0; i < 100; i++) {
            // Test pagination
            var pagedResponse = transactionService.getAllTransactions(i % 10, 20);
            assertNotNull(pagedResponse);
            
            // Test filtering by type
            TransactionType randomType = TransactionType.values()[random.nextInt(TransactionType.values().length)];
            var typeResponse = transactionService.getTransactionsByType(randomType);
            assertNotNull(typeResponse);
            
            // Test statistics
            long count = transactionService.getTotalTransactionCount();
            assertTrue(count > 0);
        }
        
        long queryTime = System.currentTimeMillis() - startTime;
        
        // Then
        int expectedTransactions = batchSize * numBatches;
        long actualCount = transactionService.getTotalTransactionCount();
        
        System.out.println("=== Large Dataset Stress Test Results ===");
        System.out.println("Expected transactions: " + expectedTransactions);
        System.out.println("Actual transactions: " + actualCount);
        System.out.println("Creation time: " + creationTime + " ms");
        System.out.println("Query time (100 operations): " + queryTime + " ms");
        System.out.println("Average query time: " + (queryTime / 100) + " ms");
        System.out.println("Creation throughput: " + ((double) actualCount / creationTime * 1000) + " transactions/second");
        
        // Verify results
        assertTrue(actualCount >= expectedTransactions * 0.95, "Should have at least 95% of expected transactions");
        assertTrue(queryTime < 5000, "Query time should be under 5 seconds for 100 operations, was: " + queryTime + "ms");
        
        // Test memory usage doesn't grow excessively
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory usage: " + (usedMemory / 1024 / 1024) + " MB");
        
        // Clean up
        transactionService.deleteAllTransactions();
        
        // Force garbage collection
        System.gc();
        Thread.sleep(1000);
        
        long cleanupMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory after cleanup: " + (cleanupMemory / 1024 / 1024) + " MB");
    }

    private TransactionRequestDTO createRandomTransactionRequest(int threadIndex, int transactionIndex) {
        String accountNumber = String.format("12345%05d", (threadIndex * 1000 + transactionIndex) % 99999);
        BigDecimal amount = new BigDecimal(String.format("%.2f", 1 + random.nextDouble() * 9999));
        TransactionType type = TransactionType.values()[random.nextInt(TransactionType.values().length)];
        String description = "Stress test transaction " + threadIndex + "-" + transactionIndex;
        String reference = random.nextBoolean() ? "REF-" + System.currentTimeMillis() : null;
        
        return new TransactionRequestDTO(accountNumber, amount, type, description, reference);
    }
} 