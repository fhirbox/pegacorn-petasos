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
 * @author ACT Health (Mark A. Hunter)
 */
public enum PetasosParcelStatusEnum {
    PARCEL_STATUS_REGISTERED("pegacorn.petasos.parcel.status.registered"),
    PARCEL_STATUS_INITIATED("pegacorn.petasos.parcel.status.initiated"),
    PARCEL_STATUS_ACTIVE("pegacorn.petasos.parcel.status.active"),
    PARCEL_STATUS_FINISHED("pegacorn.petasos.parcel.status.finished"),
    PARCEL_STATUS_FINALISED("pegacorn.petasos.parcel.status.finalised"),
    PARCEL_STATUS_FAILED("pegacorn.petasos.agent.parcel.status.failed");
    
    private String petasosParcelStatus;
    
    private PetasosParcelStatusEnum(String petasosParcelStatus){
        this.petasosParcelStatus = petasosParcelStatus;
    }
    
    public String getPetasosParcelStatus(){
        return(this.petasosParcelStatus);
    }    
}
