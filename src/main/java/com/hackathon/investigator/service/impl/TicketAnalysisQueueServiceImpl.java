package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.config.QueueProperties;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.dto.TicketJobSubmissionResponse;
import com.hackathon.investigator.entity.TicketAnalysisJob;
import com.hackathon.investigator.enums.TicketJobStatus;
import com.hackathon.investigator.exception.QueueFullException;
import com.hackathon.investigator.service.TicketAnalysisQueueService;
import com.hackathon.investigator.service.TicketAnalysisService;
import com.hackathon.investigator.service.TicketJobStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TicketAnalysisQueueServiceImpl implements TicketAnalysisQueueService {

    private final TicketAnalysisService ticketAnalysisService;
    private final TicketJobStore ticketJobStore;
    private final QueueProperties queueProperties;

    private BlockingQueue<String> workQueue;
    private ExecutorService workerPool;
    private final AtomicInteger activeWorkers = new AtomicInteger();

    public TicketAnalysisQueueServiceImpl(
            TicketAnalysisService ticketAnalysisService,
            TicketJobStore ticketJobStore,
            QueueProperties queueProperties
    ) {
        this.ticketAnalysisService = ticketAnalysisService;
        this.ticketJobStore = ticketJobStore;
        this.queueProperties = queueProperties;
    }

    @PostConstruct
    void startWorkers() {
        this.workQueue = new LinkedBlockingQueue<>(queueProperties.getCapacity());
        this.workerPool = Executors.newFixedThreadPool(
                queueProperties.getWorkers(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("ticket-analysis-worker");
                    thread.setDaemon(true);
                    return thread;
                }
        );

        for (int i = 0; i < queueProperties.getWorkers(); i++) {
            workerPool.submit(this::workerLoop);
        }

        log.info(
                "Ticket analysis queue started: capacity={}, workers={}",
                queueProperties.getCapacity(),
                queueProperties.getWorkers()
        );
    }

    @PreDestroy
    void shutdown() {
        workerPool.shutdownNow();
    }

    @Override
    public TicketJobSubmissionResponse submit(AnalyzeTicketRequest request) {
        TicketAnalysisJob job = ticketJobStore.create(request);

        try {
            boolean accepted = workQueue.offer(
                    job.getJobId(),
                    queueProperties.getEnqueueTimeoutMs(),
                    TimeUnit.MILLISECONDS
            );

            if (!accepted) {
                job.markFailed("Queue is at capacity");
                throw new QueueFullException();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            job.markFailed("Interrupted while enqueueing job");
            throw new QueueFullException();
        }

        return new TicketJobSubmissionResponse(
                job.getJobId(),
                TicketJobStatus.QUEUED,
                workQueue.size(),
                job.getSubmittedAt()
        );
    }

    @Override
    public AnalyzeTicketResponse awaitResult(String jobId, Duration timeout) {
        return ticketJobStore.awaitResult(jobId, timeout);
    }

    private void workerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String jobId = workQueue.take();
                processJob(jobId);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                log.error("Unexpected queue worker error", ex);
            }
        }
    }

    private void processJob(String jobId) {
        TicketAnalysisJob job = ticketJobStore.find(jobId).orElse(null);
        if (job == null) {
            return;
        }

        activeWorkers.incrementAndGet();
        job.markProcessing();

        try {
            AnalyzeTicketResponse response = ticketAnalysisService.analyze(job.getRequest());
            job.markCompleted(response);
        } catch (Exception ex) {
            log.error("Ticket analysis failed for job {}", jobId, ex);
            job.markFailed(ex.getMessage() != null ? ex.getMessage() : "Unknown processing error");
        } finally {
            activeWorkers.decrementAndGet();
        }
    }
}
