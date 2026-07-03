package com.anwesha.optilock_ticketing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fires 50 concurrent booking requests at the exact same seat to prove
 * out the optimistic-locking behavior: exactly one request should win
 * with 201 Created, and the rest should lose the race with 409
 * Conflict rather than corrupting the seat's state.
 *
 * NOTE: replace the TOKEN placeholder with a valid JWT before running,
 * and point seatId at a seat that is currently AVAILABLE in your
 * database (see V2__seed_sample_data.sql for seed data).
 */
class BookingConcurrencyTest {

    private static final String TOKEN = "REPLACE_WITH_VALID_JWT";
    private static final String BOOKING_URL = "http://localhost:8080/api/bookings";
    private static final int THREAD_COUNT = 50;
    private static final long SEAT_ID = 2;

    @Test
    void fiftyConcurrentRequestsForSameSeat_onlyOneShouldSucceed() throws InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger otherCount = new AtomicInteger(0);

        String requestBody = "{\"seatId\": " + SEAT_ID + "}";

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    // Every thread blocks here until the main test thread
                    // releases the latch, so all 50 requests fire as
                    // close to simultaneously as the JVM allows.
                    startLatch.await();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BOOKING_URL))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + TOKEN)
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    switch (response.statusCode()) {
                        case 201 -> successCount.incrementAndGet();
                        case 409 -> conflictCount.incrementAndGet();
                        default -> {
                            otherCount.incrementAndGet();
                            System.out.println("Status: " + response.statusCode());
                            System.out.println("Body: " + response.body());
                        }
                    }
                } catch (Exception ex) {
                    otherCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();

        System.out.println("Total Requests: " + THREAD_COUNT);
        System.out.println("Successful Bookings: " + successCount.get());
        System.out.println("Conflicts: " + conflictCount.get());
        System.out.println("Others: " + otherCount.get());


        assertEquals(1, successCount.get(),
                "Exactly one booking should succeed");

        assertEquals(THREAD_COUNT - 1, conflictCount.get(),
                "All remaining requests should fail with 409 Conflict");

        assertEquals(0, otherCount.get(),
                "There should be no unexpected responses");
    }
}
