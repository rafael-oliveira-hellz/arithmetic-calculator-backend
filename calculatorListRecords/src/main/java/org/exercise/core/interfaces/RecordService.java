package org.exercise.core.interfaces;

import org.exercise.core.entities.Record;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface RecordService {

    public Page<Record> getRecords(String token, Integer page, Integer size);

}
