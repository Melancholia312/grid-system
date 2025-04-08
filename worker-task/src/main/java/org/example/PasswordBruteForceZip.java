package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


public class PasswordBruteForceZip {
    private static final int MIN_ASCII = 32; // Первый печатаемый символ: '!'
    private static final int MAX_ASCII = 126; // Последний печатаемый символ: '~'
    private static final int RANGE = MAX_ASCII - MIN_ASCII + 1;
    private static final BigInteger BIG_RANGE = BigInteger.valueOf(RANGE);
    private static final BigInteger BIG_ZERO = BigInteger.ZERO;
    private static final BigInteger BIG_ONE = BigInteger.ONE;

    private static final int numThreads = 10;

    @Solve
    public static ObjectNode start(Path zipFilePath, String startNumberString, String endNumberString) throws Exception {

        BigInteger startNumber = new BigInteger(startNumberString);
        BigInteger endNumber = new BigInteger(endNumberString);
        BigInteger range = endNumber.subtract(startNumber).add(BigInteger.ONE);
        BigInteger rangePerThread = range.divide(BigInteger.valueOf(numThreads));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicReference<String> passwordFound = new AtomicReference<>(null);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            BigInteger threadStart = startNumber.add(rangePerThread.multiply(BigInteger.valueOf(i)));
            BigInteger threadEnd;
            if (i == numThreads - 1) {
                threadEnd = endNumber;
            } else {
                threadEnd = threadStart.add(rangePerThread).subtract(BigInteger.ONE);
            }

            Callable<Void> task = () -> {
                String password = bruteForceZipPassword(threadStart, threadEnd, zipFilePath, passwordFound, executor);
                if (password != null) {
                    executor.shutdownNow();
                }
                return null;
            };
            executor.submit(task);
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        result.put("password", passwordFound.get());
        result.put("executionTime", elapsedTime);

        return result;
    }

    public static String bruteForceZipPassword(BigInteger startNumber, BigInteger endNumber, Path zipFilePath, AtomicReference<String> passwordFound, ExecutorService executor) {

        BigInteger currentNumber = startNumber;

        ZipFile zipFile = new ZipFile(zipFilePath.toFile());

        while (currentNumber.compareTo(endNumber) <= 0 && passwordFound.get() == null) {
            String password = convertNumberToAsciiPassword(currentNumber);
            try {
                zipFile.setPassword(password.toCharArray());
                zipFile.extractAll("temp");

                if(passwordFound.compareAndSet(null, password)){
                    executor.shutdownNow();
                    return password;
                } else {
                    return passwordFound.get();
                }

            } catch (ZipException e) { }

            currentNumber = currentNumber.add(BIG_ONE);
        }

        return null;
    }

    public static String convertNumberToAsciiPassword(BigInteger number) {

        if (number.equals(BIG_ZERO)) {
            return String.valueOf((char) MIN_ASCII);
        }

        StringBuilder password = new StringBuilder();
        BigInteger currentNumber = number;

        while (currentNumber.compareTo(BIG_ZERO) > 0) {
            BigInteger[] divisionResult = currentNumber.divideAndRemainder(BIG_RANGE);
            BigInteger digit = divisionResult[1];
            char asciiChar = (char) (digit.intValue() + MIN_ASCII);
            password.insert(0, asciiChar);
            currentNumber = divisionResult[0];
        }
        return password.toString();
    }

}
