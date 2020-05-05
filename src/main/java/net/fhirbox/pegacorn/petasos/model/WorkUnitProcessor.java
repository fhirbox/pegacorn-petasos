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

/**
 *
 * @author mhunter
 */
public class WorkUnitProcessor {
    private FDN processorFDN;
    private PetasosParcel activeParcel;
    private FDN supportedFunctionFDN;
    private ComponentStatusEnum processorStatus;
    
    
    public WorkUnitProcessor(FDN newProcessorFDN, FDN newSupportedFunction ) {
        this.processorFDN = newProcessorFDN;
        this.activeParcel = null;
        this.supportedFunctionFDN = newSupportedFunction;
        this.processorStatus = ComponentStatusEnum.COMPONENT_STATUS_IDLE;
    }
    
    /**
     * @return the wupFDN
     */
    public FDN getProcessorFDN() {
        return processorFDN;
    }
    /**
     * @param wupFDN the wupFDN to set
     */
    public void setProcessorFDN(FDN wupFDN) {
        this.processorFDN = wupFDN;
    }
    /**
     * @return the activeParcel
     */
    public PetasosParcel getActiveParcel() {
        return activeParcel;
    }
    /**
     * @param activeParcel the activeParcel to set
     */
    public void setActiveParcel(PetasosParcel activeParcel) {
        this.activeParcel = activeParcel;
    }
    /**
     * @return the supportedFunctionFDN
     */
    public FDN getSupportedFunctionFDN() {
        return supportedFunctionFDN;
    }
    /**
     * @param supportedFunctionFDN the supportedFunctionFDN to set
     */
    public void setSupportedFunctionFDN(FDN supportedFunctionFDN) {
        this.supportedFunctionFDN = supportedFunctionFDN;
    }

    /**
     * @return the processorStatus
     */
    public ComponentStatusEnum getProcessorStatus() {
        return processorStatus;
    }

    /**
     * @param processorStatus the processorStatus to set
     */
    public void setProcessorStatus(ComponentStatusEnum processorStatus) {
        this.processorStatus = processorStatus;
    }
}
