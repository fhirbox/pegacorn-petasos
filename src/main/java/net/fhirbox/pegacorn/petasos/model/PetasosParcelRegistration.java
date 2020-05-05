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
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author ACT Health (Mark A. Hunter)
 */
public class PetasosParcelRegistration {
    private ArrayList<FDN> registeredWUPList;
    private FDN parcelFDN;
    private FDN containedUoW;
    private FDN supportingFunctionFDN;
    private Instant parcelInstantiationInstant;
    private Instant parcelExpectedCompletionInstant;


    public PetasosParcelRegistration(FDN parcelFDN) {
        this.parcelFDN = parcelFDN;
        parcelInstantiationInstant = Instant.now();
    }
    
    
    public PetasosParcelRegistration(FDN wupFDN, FDN theUoW, FDN theFunction, Instant creationTime ) {
        this.registeredWUPList = new ArrayList<>();
        this.registeredWUPList.add(wupFDN);
        this.parcelFDN = new FDN(theUoW);
        this.parcelFDN.appendRDN(new RDN("ParcelQualifier", UUID.randomUUID().toString()));
        this.supportingFunctionFDN = new FDN(theFunction);
        this.parcelInstantiationInstant = creationTime;
        this.parcelExpectedCompletionInstant = Instant.MAX;
    }
    
    public PetasosParcelRegistration( FDN wupFDN, FDN theUoW, FDN theFunction, Instant creationTime, Instant endTime ) {
        this.registeredWUPList = new ArrayList<>();
        this.registeredWUPList.add(wupFDN);
        this.parcelFDN = new FDN(theUoW);
        this.parcelFDN.appendRDN(new RDN("ParcelQualifier", UUID.randomUUID().toString()));
        this.supportingFunctionFDN = new FDN(theFunction);
        this.parcelInstantiationInstant = creationTime;
        this.parcelExpectedCompletionInstant = endTime;
    }
    
    public PetasosParcelRegistration( Collection<FDN> theWUPList, FDN theUoW, FDN theFunction, Instant creationTime, Instant endTime ) {
        this.registeredWUPList = new ArrayList<>();
        this.registeredWUPList.addAll(theWUPList);
        this.parcelFDN = new FDN(theUoW);
        this.parcelFDN.appendRDN(new RDN("ParcelQualifier", UUID.randomUUID().toString()));
        this.supportingFunctionFDN = new FDN(theFunction);
        this.parcelInstantiationInstant = creationTime;
        this.parcelExpectedCompletionInstant = endTime;
    }   
    
    public PetasosParcelRegistration(PetasosParcelRegistration originalParcelReg) {
        this.registeredWUPList = new ArrayList<>();
        this.registeredWUPList.addAll(originalParcelReg.getRegisteredWUPList());
        this.parcelFDN = new FDN(originalParcelReg.getParcelFDN());
        this.containedUoW = new FDN(originalParcelReg.getContainedUoW());
        this.supportingFunctionFDN = new FDN(originalParcelReg.getSupportingFunctionFDN());
        this.parcelInstantiationInstant = originalParcelReg.getParcelInstantiationInstant();
        this.parcelExpectedCompletionInstant = originalParcelReg.getParcelExpectedCompletionInstant();
    }
    
    /**
     * @return the registeredWUPList
     */
    public Collection<FDN> getRegisteredWUPList() {
        return registeredWUPList;
    }

    /**
     * @param registeredWUPList the registeredWUPList to set
     */
    public void setRegisteredWUPList(Collection<FDN> registeredWUPList) {
        this.registeredWUPList.clear();
        this.registeredWUPList.addAll(registeredWUPList);
    }

    /**
     * @return the containedUoW
     */
    public FDN getContainedUoW() {
        return containedUoW;
    }

    /**
     * @param containedUoW the containedUoW to set
     */
    public void setContainedUoW(FDN containedUoW) {
        this.containedUoW = containedUoW;
    }

    /**
     * @return the supportingFunctionFDN
     */
    public FDN getSupportingFunctionFDN() {
        return supportingFunctionFDN;
    }

    /**
     * @param supportingFunctionFDN the supportingFunctionFDN to set
     */
    public void setSupportingFunctionFDN(FDN supportingFunctionFDN) {
        this.supportingFunctionFDN = supportingFunctionFDN;
    }

    /**
     * @return the parcelExpectedCompletionInstant
     */
    public Instant getParcelExpectedCompletionInstant() {
        return parcelExpectedCompletionInstant;
    }

    /**
     * @param parcelExpectedCompletionInstant the parcelExpectedCompletionInstant to set
     */
    public void setParcelExpectedCompletionInstant(Instant parcelExpectedCompletionInstant) {
        this.parcelExpectedCompletionInstant = parcelExpectedCompletionInstant;
    }

    public FDN getParcelFDN() {
        return this.parcelFDN;
    }

    public void setParcelFDN(FDN parcelFDN) {
        this.parcelFDN = parcelFDN;
    }

    public Instant getParcelInstantiationInstant() {
        return parcelInstantiationInstant;
    }

    public void setParcelInstantiationInstant(Instant parcelStateDate) {
        this.parcelInstantiationInstant = parcelStateDate;
    }
}