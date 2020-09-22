package com.example.surl.service.impl;

import com.example.surl.repository.AllocatedRangePartitionStatusRepository;
import com.example.surl.aspect.Retry;
import com.example.surl.model.AllocatedRangePartitionStatus;
import com.example.surl.service.AllocatedRangePartitionStatusService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * counter allocated to the worker instances is managed through
 * this entity
 * @author  Kalyan J
 */
@Service
public class AllocatedRangePartitionStatusServiceImpl implements AllocatedRangePartitionStatusService {

    private AllocatedRangePartitionStatusRepository repository;

    public AllocatedRangePartitionStatusServiceImpl(AllocatedRangePartitionStatusRepository repository) {
        this.repository = repository;
    }


    /**
     * once worker requests to obtain new range partition
     * this method is called to increase the counter and return the result
     * to the worker .
     * this method has the potential of concurrent issues,so an Optimistic Locking strategies
     * is considered to avoid problem
     * in case of optimistic locking event the system retry 10 times and if traffic is so heavy
     * system will notify the service consumer to try again later.
     *
     * note: this collection has only one document for managing the counter
     *
     * @return allocated a new range partition to worker
     */
    @Retry(times = 10, on = org.springframework.dao.OptimisticLockingFailureException.class)
    @Override
    public Integer allocateRangePartition() {
        AllocatedRangePartitionStatus allocatedRangePartitionStatus = Optional.ofNullable(repository.findAll()).filter(l -> l.size() > 0).map(c -> c.get(0)).orElseGet(AllocatedRangePartitionStatus::new);
        allocatedRangePartitionStatus.incrementAllocatedPartitionNumber();
        repository.save(allocatedRangePartitionStatus);
        return allocatedRangePartitionStatus.getAllocatedPartitionNumber();
    }
}
