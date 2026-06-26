package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.TransactionMatchAnalysis;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.mapper.InvestigationMapper;
import com.hackathon.investigator.service.AiExtractionService;
import com.hackathon.investigator.service.TransactionMatcherService;
import com.hackathon.investigator.util.ComplaintAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisContextFactory {

    private final AiExtractionService aiExtractionService;
    private final TransactionMatcherService transactionMatcherService;
    private final InvestigationMapper investigationMapper;

    public AnalysisExecutionContext create(AnalyzeTicketRequest request, boolean flagged) {
        AiExtractionResult extraction = aiExtractionService.extract(request.complaint(), request.language());
        List<TransactionRecord> transactions = investigationMapper.toTransactionRecords(request.transactionHistory());

        boolean vagueComplaint = ComplaintAnalyzer.isVagueComplaint(request.complaint());
        boolean adversarialComplaint = ComplaintAnalyzer.isAdversarialComplaint(request.complaint());

        TransactionMatchAnalysis matchAnalysis = vagueComplaint
                ? TransactionMatchAnalysis.noMatch()
                : transactionMatcherService.analyzeMatches(transactions, extraction, request.complaint());

        return AnalysisExecutionContext.builder()
                .request(request)
                .flagged(flagged)
                .extraction(extraction)
                .transactions(transactions)
                .matchAnalysis(matchAnalysis)
                .matchedTransaction(matchAnalysis.matchedTransaction())
                .vagueComplaint(vagueComplaint)
                .credentialsShared(ComplaintAnalyzer.mentionsCredentialsShared(request.complaint()))
                .emptyHistory(request.transactionHistory() == null || request.transactionHistory().isEmpty())
                .campaignContextPresent(request.hasCampaignContext())
                .ambiguousMatch(matchAnalysis.ambiguousMatch())
                .establishedRecipientPattern(matchAnalysis.establishedRecipientPattern())
                .duplicatePaymentScenario(matchAnalysis.duplicatePaymentScenario())
                .adversarialComplaint(adversarialComplaint)
                .build();
    }
}
