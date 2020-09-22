package com.example.surl.service.impl;

import com.example.surl.repository.WorkerStatusRepository;
import com.example.surl.aspect.Retry;
import com.example.surl.exception.KeyOverFlowException;
import com.example.surl.model.WorkerStatus;
import com.example.surl.model.embedded.AllocatedCounter;
import com.example.surl.service.AllocatedRangePartitionStatusService;
import com.example.surl.service.WorkerStatusService;
import com.example.surl.util.GlobalConstants;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * service used to manage key pool allocated to each worker (service container in docker)
 *
 * @author Kalyan J
 */
@Service
public class WorkerStatusServiceImpl implements WorkerStatusService {
    private WorkerStatusRepository workerStatusRepository;
    private AllocatedRangePartitionStatusService allocatedRangePartitionStatusService;


    public WorkerStatusServiceImpl(WorkerStatusRepository workerStatusRepository, AllocatedRangePartitionStatusService allocatedRangePartitionStatusService) {
        this.workerStatusRepository = workerStatusRepository;
        this.allocatedRangePartitionStatusService = allocatedRangePartitionStatusService;
    }

    /**
     * there are 2 scenarios : 1- when workeriD (container hostname of service) is not available /or allocated range
     * exaused by the worker so new range partition should be assigned
     * 2- when workerID already in the db , and its range not exhausted yet.
     * in the 1st scenario workerId is persisted in DB(if not already) and a new Range Partition is assigned to it
     * in the 2nd scenario worker counter is incremented and that id is used to be encoded in base58 for shortUrl generation
     * through allocatedRangePartitionStatus
     * <p>
     * this method has the potential of concurrent issues,so an Optimistic Locking strategies
     * is considered to avoid problem
     * in case of optimistic locking event the system retry 10 times and if traffic is so heavy
     * system will notify the service consumer to try again later.
     *
     * @param workerId hostname of the service container in the docker
     * @return a new decimal key to be encoded in base58
     * <p>
     * * @throws KeyOverFlowException  if the system has exhausted the maximum amount of counters
     */
    @Retry(times = 10, on = org.springframework.dao.OptimisticLockingFailureException.class)
    @Override
    public Long getNewKey(String workerId) throws KeyOverFlowException {
        WorkerStatus workerStatus = Optional.ofNullable(workerStatusRepository.findByWorkerId(workerId))
                .map(c -> c)
                .orElseGet(() -> new WorkerStatus(workerId));

        Long key = workerStatus.getAllocatedRanges().stream().filter(a -> a.getExhausted() == false)
                .findFirst()
                .map(s -> {
                    Long key2 = workerStatus.getAllocatedRanges().stream().filter(a -> a.getExhausted() == false).mapToLong(a -> {
                        if (a.getCounter() == Long.MAX_VALUE) return -1; // in case of key overflow in the system
                        a.incrementCounter();
                        a.setExhausted(a.getCounter() == ((a.getRangeNumber() + 1) * GlobalConstants.KeyNumbersInPartitions - 1));
                        return a.getCounter();
                    }).findFirst().getAsLong();
                    return key2;
                })
                .orElseGet(() -> {
                    //1st scenario
                    Integer newRangePartitionNumber = allocatedRangePartitionStatusService.allocateRangePartition();

                    AllocatedCounter allocatedCounter = new AllocatedCounter();
                    allocatedCounter.setRangeNumber(newRangePartitionNumber);
                    allocatedCounter.setCounter((long) ((newRangePartitionNumber - 1) * GlobalConstants.KeyNumbersInPartitions + 1));

                    workerStatus.getAllocatedRanges().add(allocatedCounter);
                    Long key2 = allocatedCounter.getCounter();
                    return key2;
                });

        if (key < 0) throw new KeyOverFlowException();
        workerStatusRepository.save(workerStatus);
        return key;

    }
}
