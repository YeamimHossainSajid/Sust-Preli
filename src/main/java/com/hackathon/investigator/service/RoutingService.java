package com.hackathon.investigator.service;

import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;

public interface RoutingService {
    Department route(CaseType caseType, EvidenceVerdict evidenceVerdict);
}
