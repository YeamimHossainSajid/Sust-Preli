package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.util.ComplaintAnalyzer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PreFlightSafetyCheck {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore\\s+(previous|your)\\s+(instructions|rules)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("reveal\\s+(system\\s+)?prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("show\\s+(your\\s+)?(system\\s+)?prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("output\\s+your\\s+system\\s+prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now", Pattern.CASE_INSENSITIVE),
            Pattern.compile("pretend\\s+you\\s+are", Pattern.CASE_INSENSITIVE),
            Pattern.compile("developer\\s+mode", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)system\\s*:")
    );

    public boolean isFlagged(String complaint) {
        if (complaint == null || complaint.isBlank()) {
            return false;
        }
        if (ComplaintAnalyzer.isAdversarialComplaint(complaint)) {
            return true;
        }
        return INJECTION_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(complaint).find());
    }
}
