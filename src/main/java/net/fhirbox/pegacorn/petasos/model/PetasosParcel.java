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

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mhunter
 */
public class PetasosParcel {
    private PetasosParcelRegistration parcelRegistration;
    private UoW containedUoW;
    private ArrayList<FDN> successorParcelSet;
    private PetasosWUPWatchdogState taskProcessorState;
    private FDN precursorParcelFDN;
    
    private PetasosParcelStatusEnum petasosParcelStatus;
    

    private static final Logger LOG = LoggerFactory.getLogger(PetasosParcel.class);

    public PetasosParcel(PetasosParcelRegistration theParcelRegistration) {
        this.parcelRegistration = theParcelRegistration;
    }
    
    public PetasosParcel(PetasosParcelRegistration theParcelRegistration, UoW theUoW, FDN thePrecursorFDN, PetasosWUPWatchdogState theWUPStatus) {
        this.parcelRegistration = theParcelRegistration;
        this.containedUoW = theUoW;
        this.successorParcelSet = new ArrayList<>();
        this.precursorParcelFDN = new FDN(thePrecursorFDN);
        this.taskProcessorState = theWUPStatus;
    }
    
    public PetasosParcel( PetasosParcel originalParcel) {
        this.parcelRegistration = new PetasosParcelRegistration(originalParcel.getParcelRegistration());
        this.containedUoW = new UoW(originalParcel.getContainedUoW());
        this.successorParcelSet = new ArrayList<>();
        this.successorParcelSet.addAll(originalParcel.getSuccessorParcelSet());
        this.taskProcessorState = originalParcel.getTaskProcessorState();
        this.precursorParcelFDN = new FDN(originalParcel.getPrecursorParcel());
    }
    
    /**
     * @return the parcelRegistration
     */
    public PetasosParcelRegistration getParcelRegistration() {
        return parcelRegistration;
    }
    /**
     * @param parcelRegistration the parcelRegistration to set
     */
    public void setParcelRegistration(PetasosParcelRegistration parcelRegistration) {
        this.parcelRegistration = parcelRegistration;
    }
    /**
     * @return the containedUoW
     */
    public UoW getContainedUoW() {
        return containedUoW;
    }
    /**
     * @param containedUoW the containedUoW to set
     */
    public void setContainedUoW(UoW containedUoW) {
        this.containedUoW = new UoW(containedUoW);
    }
    /**
     * @return the successorParcels
     */
    public Collection<FDN> getSuccessorParcelSet() {
        return successorParcelSet;
    }
    /**
     * @param successorParcels the successorParcels to set
     */
    public void setSuccessorParcels(Collection<FDN> successorParcels) {
        this.successorParcelSet.clear();
        this.successorParcelSet.addAll(successorParcels);
    }
    /**
     * @return the taskProcessorState
     */
    public PetasosWUPWatchdogState getTaskProcessorState() {
        return taskProcessorState;
    }
    /**
     * @param taskProcessorState the taskProcessorState to set
     */
    public void setTaskProcessorState(PetasosWUPWatchdogState taskProcessorState) {
        this.taskProcessorState = taskProcessorState;
    }
    /**
     * @return the precursorParcel
     */
    public FDN getPrecursorParcel() {
        return precursorParcelFDN;
    }
    /**
     * @param precursorParcel the precursorParcel to set
     */
    public void setPrecursorParcel(FDN precursorParcel) {
        this.precursorParcelFDN = precursorParcel;
    }
    
    public FDN getParcelFDN() {
        return parcelRegistration.getParcelFDN();
    }
    
    public void setUoW(UoW uow) {
        this.containedUoW = uow;
    }

    public void setPetasosParcelRegistration(PetasosParcelRegistration parcelRegistration) {
        this.parcelRegistration = parcelRegistration;
    }
    
    public void setParcelStatus(PetasosParcelStatusEnum petasosParcelStatus) {
        this.petasosParcelStatus = petasosParcelStatus;
    }
    
    public PetasosParcelStatusEnum getParcelStatus() {
        return this.petasosParcelStatus;
    }
}