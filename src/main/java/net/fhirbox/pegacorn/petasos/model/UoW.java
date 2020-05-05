/* 
 * The MIT License
 *
 * Copyright 2020 ACT Health (Mark A. Hunter).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.fhirbox.pegacorn.petasos.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mhunter
 */
public class UoW {
    /**
     * The FDN (fully distinguished name) of the UoW. Built from a combination of
     * the required "Component Function FDN" that would be required to process it
     * plus some unique characteristic.
     */
    private FDN uowFDN;
    /**
     * The FDN (fully distinguished name) of the Component Function that is required 
     * to process this UoW. This value could be derived from the uowFDN - but for 
     * ease of utilisation and future-proofness - we have separated it here.
     */
    private FDN requiredFunctionFDN;
    /**
     * The set of (JSON) objects that represent the ingress (or starting set) of 
     * information of this UoW. 
     */
    private HashSet<String> uowIngressContent; // JSON Objects
    /**
     * The set of (JSON) objects created as part of the completion of this UoW. 
     */
    private HashSet<String> uowEgressContent; // JSON Objects
    /**
     * The (enum) outcome status of the processing of this UoW. 
     */
    private UoWProcessingOutcomeEnum uowProcessingOutcome;
    
  
    // Constructers
    
    public UoW (FDN functionFDN, Set<String> theInput) {
        String generatedInstanceValue = Long.toString(Instant.now().getNano());
        this.uowIngressContent = new HashSet<>(theInput);
        this.uowEgressContent = new HashSet<>();
        this.uowProcessingOutcome = UoWProcessingOutcomeEnum.PEGACORN_UOW_OUTCOME_NOTSTARTED;
        this.uowFDN = new FDN(functionFDN);
        RDN newRDN = new RDN("InstanceQualifier", generatedInstanceValue);
        this.uowFDN.appendRDN(newRDN); 
    }
    
    public UoW (FDN functionFDN, String uowQualifier,Set<String> theInput) {
        this.uowIngressContent = new HashSet<>(theInput);
        this.uowEgressContent = new HashSet<>();
        this.uowProcessingOutcome = UoWProcessingOutcomeEnum.PEGACORN_UOW_OUTCOME_NOTSTARTED;
        this.uowFDN = new FDN(functionFDN);
        RDN newRDN = new RDN("InstanceQualifier", uowQualifier);
        this.uowFDN.appendRDN(newRDN);
    }
    
    public UoW (UoW originalUoW) {
        this.uowFDN = new FDN(originalUoW.getUoWFDN());
        this.requiredFunctionFDN = new FDN(originalUoW.getRequiredFunctionFDN());
        this.uowIngressContent = new HashSet<>();
        this.uowIngressContent.addAll(originalUoW.getUowIngressContent());
        this.uowEgressContent = new HashSet<>();
        this.uowEgressContent.addAll(originalUoW.getUowEgressContent());
        this.uowProcessingOutcome = originalUoW.getUowProcessingOutcome();
    }
    
    public UoW (FDN uowFDN, FDN requiredFunctionFDN) {
        this.uowFDN = uowFDN;
        this.requiredFunctionFDN = requiredFunctionFDN;
    }

    public FDN getUoWFDN() {
        return uowFDN;
    }

    public void setUoWFDN(FDN uowID) {
        this.uowFDN = uowID;
    }

    public Set<String> getUowIngressContent() {
        return uowIngressContent;
    }

    public void setUowIngressContent(Set<String> uowIngressContent) {
        this.uowIngressContent.clear();
        this.uowIngressContent.addAll(uowIngressContent);
    }

    public Set<String> getUowEgressContent() {
        return uowEgressContent;
    }

    public void setUowEgressContent(Set<String> uowEgressContent) {
        this.uowEgressContent.clear();
        this.uowEgressContent.addAll(uowEgressContent);
    }

    public UoWProcessingOutcomeEnum getUowProcessingOutcome() {
        return uowProcessingOutcome;
    }

    public void setUowProcessingOutcome(UoWProcessingOutcomeEnum uowProcessingOutcome) {
        this.uowProcessingOutcome = uowProcessingOutcome;
    }

    /**
     * @return the requiredFunctionFDN
     */
    public FDN getRequiredFunctionFDN() {
        return requiredFunctionFDN;
    }

    /**
     * @param requiredFunctionFDN the requiredFunctionFDN to set
     */
    public void setRequiredFunctionFDN(FDN requiredFunction) {
        this.requiredFunctionFDN = requiredFunction;
    }
}
