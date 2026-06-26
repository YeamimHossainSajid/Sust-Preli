package com.hackathon.investigator.mapper;

import com.hackathon.investigator.dto.TransactionHistoryDto;
import com.hackathon.investigator.entity.TransactionRecord;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface InvestigationMapper {

    default List<TransactionRecord> toTransactionRecords(List<TransactionHistoryDto> history) {
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        return history.stream().map(this::toTransactionRecord).toList();
    }

    TransactionRecord toTransactionRecord(TransactionHistoryDto dto);
}
