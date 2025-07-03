package com.hsbc.transaction.repository;

import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    private TransactionRepository transactionRepository;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        transactionRepository = new TransactionRepository();
        testTransaction = new Transaction(
            null,
            "1234567890",
            new BigDecimal("100.50"),
            TransactionType.CREDIT,
            "Test transaction"
        );
    }

    @Test
    void save_ShouldSaveTransactionWithGeneratedId() {
        // When
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        // Then
        assertNotNull(savedTransaction.getId());
        assertEquals(testTransaction.getAccountNumber(), savedTransaction.getAccountNumber());
        assertEquals(testTransaction.getAmount(), savedTransaction.getAmount());
        assertEquals(testTransaction.getType(), savedTransaction.getType());
        assertEquals(testTransaction.getDescription(), savedTransaction.getDescription());
    }

    @Test
    void save_ShouldUpdateExistingTransaction() {
        // Given
        Transaction savedTransaction = transactionRepository.save(testTransaction);
        Long transactionId = savedTransaction.getId();
        
        // When
        savedTransaction.setDescription("Updated description");
        Transaction updatedTransaction = transactionRepository.save(savedTransaction);

        // Then
        assertEquals(transactionId, updatedTransaction.getId());
        assertEquals("Updated description", updatedTransaction.getDescription());
    }

    @Test
    void findById_ShouldReturnTransaction() {
        // Given
        Transaction savedTransaction = transactionRepository.save(testTransaction);
        Long transactionId = savedTransaction.getId();

        // When
        Optional<Transaction> foundTransaction = transactionRepository.findById(transactionId);

        // Then
        assertTrue(foundTransaction.isPresent());
        assertEquals(transactionId, foundTransaction.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyOptional() {
        // When
        Optional<Transaction> foundTransaction = transactionRepository.findById(999L);

        // Then
        assertFalse(foundTransaction.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllTransactions() {
        // Given
        transactionRepository.save(testTransaction);
        Transaction transaction2 = new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Another transaction"
        );
        transactionRepository.save(transaction2);

        // When
        List<Transaction> allTransactions = transactionRepository.findAll();

        // Then
        assertEquals(2, allTransactions.size());
    }

    @Test
    void findAll_WithPagination_ShouldReturnPagedResults() {
        // Given
        for (int i = 0; i < 25; i++) {
            Transaction transaction = new Transaction(
                null,
                "123456789" + i,
                new BigDecimal("100.00"),
                TransactionType.CREDIT,
                "Transaction " + i
            );
            transactionRepository.save(transaction);
        }

        // When
        List<Transaction> firstPage = transactionRepository.findAll(0, 10);
        List<Transaction> secondPage = transactionRepository.findAll(1, 10);

        // Then
        assertEquals(10, firstPage.size());
        assertEquals(10, secondPage.size());
    }

    @Test
    void findAll_WithPagination_ShouldReturnEmptyListWhenPageOutOfBounds() {
        // Given
        transactionRepository.save(testTransaction);

        // When
        List<Transaction> outOfBoundsPage = transactionRepository.findAll(10, 10);

        // Then
        assertTrue(outOfBoundsPage.isEmpty());
    }

    @Test
    void findByAccountNumber_ShouldReturnTransactionsForAccount() {
        // Given
        String accountNumber = "1234567890";
        transactionRepository.save(testTransaction);
        
        Transaction transaction2 = new Transaction(
            null,
            accountNumber,
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Another transaction"
        );
        transactionRepository.save(transaction2);

        Transaction transaction3 = new Transaction(
            null,
            "0987654321",
            new BigDecimal("300.00"),
            TransactionType.TRANSFER,
            "Different account"
        );
        transactionRepository.save(transaction3);

        // When
        List<Transaction> accountTransactions = transactionRepository.findByAccountNumber(accountNumber);

        // Then
        assertEquals(2, accountTransactions.size());
        accountTransactions.forEach(t -> assertEquals(accountNumber, t.getAccountNumber()));
    }

    @Test
    void findByType_ShouldReturnTransactionsOfType() {
        // Given
        transactionRepository.save(testTransaction);
        
        Transaction transaction2 = new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.CREDIT,
            "Another credit"
        );
        transactionRepository.save(transaction2);

        Transaction transaction3 = new Transaction(
            null,
            "1111111111",
            new BigDecimal("300.00"),
            TransactionType.DEBIT,
            "A debit"
        );
        transactionRepository.save(transaction3);

        // When
        List<Transaction> creditTransactions = transactionRepository.findByType(TransactionType.CREDIT);

        // Then
        assertEquals(2, creditTransactions.size());
        creditTransactions.forEach(t -> assertEquals(TransactionType.CREDIT, t.getType()));
    }

    @Test
    void findByAmountBetween_ShouldReturnTransactionsInRange() {
        // Given
        transactionRepository.save(testTransaction); // 100.50
        
        Transaction transaction2 = new Transaction(
            null,
            "0987654321",
            new BigDecimal("50.00"),
            TransactionType.CREDIT,
            "Low amount"
        );
        transactionRepository.save(transaction2);

        Transaction transaction3 = new Transaction(
            null,
            "1111111111",
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "High amount"
        );
        transactionRepository.save(transaction3);

        // When
        List<Transaction> rangeTransactions = transactionRepository.findByAmountBetween(
            new BigDecimal("75.00"), new BigDecimal("150.00")
        );

        // Then
        assertEquals(1, rangeTransactions.size());
        assertEquals(testTransaction.getId(), rangeTransactions.get(0).getId());
    }

    @Test
    void findByDateBetween_ShouldReturnTransactionsInDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        testTransaction.setTransactionDate(now.minusHours(2));
        transactionRepository.save(testTransaction);

        Transaction transaction2 = new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.CREDIT,
            "Recent transaction"
        );
        transaction2.setTransactionDate(now.minusMinutes(30));
        transactionRepository.save(transaction2);

        Transaction transaction3 = new Transaction(
            null,
            "1111111111",
            new BigDecimal("300.00"),
            TransactionType.DEBIT,
            "Old transaction"
        );
        transaction3.setTransactionDate(now.minusDays(1));
        transactionRepository.save(transaction3);

        // When
        List<Transaction> dateRangeTransactions = transactionRepository.findByDateBetween(
            now.minusHours(3), now.minusHours(1)
        );

        // Then
        assertEquals(1, dateRangeTransactions.size());
        assertEquals(testTransaction.getId(), dateRangeTransactions.get(0).getId());
    }

    @Test
    void findByAccountNumberAndType_ShouldReturnMatchingTransactions() {
        // Given
        String accountNumber = "1234567890";
        transactionRepository.save(testTransaction);
        
        Transaction transaction2 = new Transaction(
            null,
            accountNumber,
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Different type"
        );
        transactionRepository.save(transaction2);

        Transaction transaction3 = new Transaction(
            null,
            "0987654321",
            new BigDecimal("300.00"),
            TransactionType.CREDIT,
            "Different account"
        );
        transactionRepository.save(transaction3);

        // When
        List<Transaction> matchingTransactions = transactionRepository.findByAccountNumberAndType(
            accountNumber, TransactionType.CREDIT
        );

        // Then
        assertEquals(1, matchingTransactions.size());
        assertEquals(testTransaction.getId(), matchingTransactions.get(0).getId());
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        // Given
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        // When
        boolean exists = transactionRepository.existsById(savedTransaction.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseWhenNotExists() {
        // When
        boolean exists = transactionRepository.existsById(999L);

        // Then
        assertFalse(exists);
    }

    @Test
    void deleteById_ShouldDeleteTransaction() {
        // Given
        Transaction savedTransaction = transactionRepository.save(testTransaction);
        Long transactionId = savedTransaction.getId();

        // When
        transactionRepository.deleteById(transactionId);

        // Then
        assertFalse(transactionRepository.existsById(transactionId));
    }

    @Test
    void deleteAll_ShouldDeleteAllTransactions() {
        // Given
        transactionRepository.save(testTransaction);
        transactionRepository.save(new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Another transaction"
        ));

        // When
        transactionRepository.deleteAll();

        // Then
        assertEquals(0, transactionRepository.count());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given
        transactionRepository.save(testTransaction);
        transactionRepository.save(new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Another transaction"
        ));

        // When
        long count = transactionRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    void countByType_ShouldReturnCorrectCounts() {
        // Given
        transactionRepository.save(testTransaction); // CREDIT
        transactionRepository.save(new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.CREDIT,
            "Another credit"
        ));
        transactionRepository.save(new Transaction(
            null,
            "1111111111",
            new BigDecimal("300.00"),
            TransactionType.DEBIT,
            "A debit"
        ));

        // When
        Map<TransactionType, Long> countByType = transactionRepository.countByType();

        // Then
        assertEquals(2L, countByType.get(TransactionType.CREDIT));
        assertEquals(1L, countByType.get(TransactionType.DEBIT));
    }

    @Test
    void countByAccountNumber_ShouldReturnCorrectCounts() {
        // Given
        String account1 = "1234567890";
        String account2 = "0987654321";
        
        transactionRepository.save(testTransaction); // account1
        transactionRepository.save(new Transaction(
            null,
            account1,
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Another for account1"
        ));
        transactionRepository.save(new Transaction(
            null,
            account2,
            new BigDecimal("300.00"),
            TransactionType.CREDIT,
            "For account2"
        ));

        // When
        Map<String, Long> countByAccount = transactionRepository.countByAccountNumber();

        // Then
        assertEquals(2L, countByAccount.get(account1));
        assertEquals(1L, countByAccount.get(account2));
    }

    @Test
    void getTotalAmount_ShouldReturnCorrectTotal() {
        // Given
        transactionRepository.save(testTransaction); // 100.50
        transactionRepository.save(new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Another transaction"
        ));

        // When
        BigDecimal totalAmount = transactionRepository.getTotalAmount();

        // Then
        assertEquals(new BigDecimal("300.50"), totalAmount);
    }

    @Test
    void getTotalAmountByType_ShouldReturnCorrectTotal() {
        // Given
        transactionRepository.save(testTransaction); // CREDIT 100.50
        transactionRepository.save(new Transaction(
            null,
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.CREDIT,
            "Another credit"
        ));
        transactionRepository.save(new Transaction(
            null,
            "1111111111",
            new BigDecimal("300.00"),
            TransactionType.DEBIT,
            "A debit"
        ));

        // When
        BigDecimal totalCreditAmount = transactionRepository.getTotalAmountByType(TransactionType.CREDIT);

        // Then
        assertEquals(new BigDecimal("300.50"), totalCreditAmount);
    }

    @Test
    void getTotalAmountByAccountNumber_ShouldReturnCorrectTotal() {
        // Given
        String accountNumber = "1234567890";
        transactionRepository.save(testTransaction); // account 100.50
        transactionRepository.save(new Transaction(
            null,
            accountNumber,
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Another for same account"
        ));
        transactionRepository.save(new Transaction(
            null,
            "0987654321",
            new BigDecimal("300.00"),
            TransactionType.CREDIT,
            "Different account"
        ));

        // When
        BigDecimal totalForAccount = transactionRepository.getTotalAmountByAccountNumber(accountNumber);

        // Then
        assertEquals(new BigDecimal("300.50"), totalForAccount);
    }

    @Test
    void concurrentSave_ShouldHandleConcurrentOperations() throws InterruptedException {
        // Given
        int threadCount = 10;
        int transactionsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < transactionsPerThread; j++) {
                        Transaction transaction = new Transaction(
                            null,
                            "123456789" + threadIndex,
                            new BigDecimal("100.00"),
                            TransactionType.CREDIT,
                            "Transaction " + threadIndex + "-" + j
                        );
                        transactionRepository.save(transaction);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(threadCount * transactionsPerThread, transactionRepository.count());
    }

    @Test
    void concurrentReadWrite_ShouldHandleConcurrentOperations() throws InterruptedException {
        // Given
        Transaction savedTransaction = transactionRepository.save(testTransaction);
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    // Read operation
                    Optional<Transaction> found = transactionRepository.findById(savedTransaction.getId());
                    assertTrue(found.isPresent());
                    
                    // Write operation
                    Transaction newTransaction = new Transaction(
                        null,
                        "987654321" + threadIndex,
                        new BigDecimal("200.00"),
                        TransactionType.DEBIT,
                        "Concurrent transaction " + threadIndex
                    );
                    transactionRepository.save(newTransaction);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(threadCount + 1, transactionRepository.count()); // +1 for the initial transaction
    }
} 