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
import java.util.ArrayList;
import java.util.Collection;


import org.json.JSONArray;
import org.json.JSONObject;
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

    
    public String getParcelJSON() {
        JSONObject parcelJSON = new JSONObject();

        JSONObject registrationInfo = new JSONObject().put("parcelFDN", parcelRegistration.getParcelFDN().getQualifiedFDN()).put("parcelInstantiationInstant", parcelRegistration.getParcelInstantiationInstant().toEpochMilli())
                          .put("parcelExpectedCompletionInstant", parcelRegistration.getParcelExpectedCompletionInstant().toEpochMilli())
                          .put("supportingFunctionFDN", parcelRegistration.getSupportingFunctionFDN().getQualifiedFDN());

        JSONArray wupList = new JSONArray();
        if (parcelRegistration.getRegisteredWUPList() != null) {
            parcelRegistration.getRegisteredWUPList().forEach(fdn -> {
                wupList.put(fdn.getQualifiedFDN());
            });
            registrationInfo.put("registeredWUPList", wupList);
        }
        
        if (parcelRegistration.getContainedUoW() != null) {
            registrationInfo.put("containedUoW", parcelRegistration.getContainedUoW().getQualifiedFDN());
        }

        parcelJSON.put("parcelRegistration", registrationInfo);

        JSONArray ingressContent = new JSONArray();
        if (containedUoW.getUowIngressContent() != null) {
            containedUoW.getUowIngressContent().forEach(ingress -> {
                ingressContent.put(new JSONObject((String)ingress));
            });
        }

        JSONArray egressContent = new JSONArray();
        if (containedUoW.getUowEgressContent() != null) {
            containedUoW.getUowEgressContent().forEach(egress -> {
                egressContent.put(new JSONObject((String)egress));
            });
        }
  
        JSONObject uowInfo = new JSONObject().put("uowFDN", containedUoW.getUoWFDN().getQualifiedFDN()).put("requiredFunctionFDN", containedUoW.getRequiredFunctionFDN().getQualifiedFDN())
                .put("ingressContent", ingressContent).put("egressContent", egressContent).put("processingOutcome", containedUoW.getUowProcessingOutcome().getUoWProcessingOutcome());
        parcelJSON.put("uow", uowInfo);
        

        JSONArray successorParcelFDNs = new JSONArray();
        successorParcelFDNs.forEach(successorParcelFDN -> {
            JSONObject successor = new JSONObject().put("FDN", ((FDN)successorParcelFDN).getQualifiedFDN());
            successorParcelFDNs.put(successor);
        });
        parcelJSON.put("successorParcels", successorParcelFDNs);

        JSONObject watchdogStateInfo = new JSONObject().put("wupFDN", taskProcessorState.getWupFDN().getQualifiedFDN())
                .put("wupStatus", taskProcessorState.getWupStatus().getComponentWatchdogState())
                .put("lastStatusUpdate", taskProcessorState.getLastStatusUpdate().toEpochMilli());
        parcelJSON.put("watchdogStatus", watchdogStateInfo);
        
        parcelJSON.put("precursorParcelFDN", precursorParcelFDN.getQualifiedFDN());
        
        return parcelJSON.toString();
    }

    public static PetasosParcel createParcelFromJSON(String jsonString) {
        JSONObject parcelJSON = new JSONObject(jsonString);

        // This is meant to support parcels from the cache, so if no registration information
        // then something is wrong, so return null
        if (!parcelJSON.has("parcelRegistration")) {
            LOG.warn("Parcel JSON without registration information encountered");
            return null;
        }

        // all parcels should have a uowID
        if (!parcelJSON.has("uow")) {
            LOG.warn("Parcel JSON without UoW information encountered");
            return null;
        }
        
        // create the parcel and registration information
        JSONObject registrationObject = parcelJSON.getJSONObject("parcelRegistration");

        PetasosParcelRegistration parcelRegistration = new PetasosParcelRegistration(new FDN(registrationObject.getString("parcelFDN")));
        parcelRegistration.setParcelInstantiationInstant(Instant.ofEpochMilli(registrationObject.getLong("parcelInstantiationInstant")));
        if (registrationObject.has("parcelExpectedCompletionInstant")) {
            if (registrationObject.getLong("parcelExpectedCompletionInstant") > 0) {
                parcelRegistration.setParcelExpectedCompletionInstant(Instant.ofEpochMilli(registrationObject.getLong("parcelExpectedCompletionInstant")));                    
            }
        }
        parcelRegistration.setSupportingFunctionFDN(new FDN(registrationObject.getString("supportingFunctionFDN")));

        if (registrationObject.has("registeredWUPList")) {
            ArrayList<FDN> wupList = new ArrayList<>();
            registrationObject.getJSONArray("registeredWUPList").forEach(wupFDN -> {
                wupList.add(new FDN((String)wupFDN));
            });
            parcelRegistration.setRegisteredWUPList(wupList);
        }

        if (registrationObject.has("containedUoW")) {
            parcelRegistration.setContainedUoW(new FDN(registrationObject.getString("containedUoW")));
        }
        
        //create the parcel
        PetasosParcel parcel = new PetasosParcel(parcelRegistration);

        // create the UoW object        
        UoW uow = new UoW(new FDN(parcelJSON.getJSONObject("uow").getString("uowFDN")), new FDN(parcelJSON.getJSONObject("uow").getString("requiredFunctionFDN")));

        if (parcelJSON.getJSONObject("uow").has("ingressContent")) {
            HashSet<String> ingressContent = new HashSet<>();
            parcelJSON.getJSONObject("uow").getJSONArray("ingressContent").forEach(ingress -> {
                ingressContent.add(((JSONObject)ingress).toString());
            });
            uow.setUowIngressContent(ingressContent);
        }
        if (parcelJSON.getJSONObject("uow").has("egressContent")) {
            HashSet<String> egressContent = new HashSet<>();
            parcelJSON.getJSONObject("uow").getJSONArray("egressContent").forEach(egress -> {
                egressContent.add(((JSONObject)egress).toString());
            });
            uow.setUowEgressContent(egressContent);
        }
        if (parcelJSON.getJSONObject("uow").has("processingOutcome")) {
            uow.setUowProcessingOutcome(UoWProcessingOutcomeEnum.valueOf(parcelJSON.getJSONObject("uow").getString("processingOutcome")));
        }
        parcel.setUoW(uow);

        if (parcelJSON.has("successorParcels")) {
            HashSet<FDN> successorParcelFDNs = new HashSet<>();
            parcelJSON.getJSONArray("successorParcels").forEach(successor -> {
                FDN successorParcelFDN  = new FDN();
                successorParcelFDN.populateFDN((String)successor);
                successorParcelFDNs.add(successorParcelFDN);
            });
            parcel.setSuccessorParcels(successorParcelFDNs);
        }

        if (parcelJSON.has("watchdogStatus")) {
            PetasosWUPWatchdogState watchdogState = new PetasosWUPWatchdogState(new FDN(parcelJSON.getJSONObject("watchdogStatus").getString("wupFDN")),
                    ComponentStatusEnum.valueOf(parcelJSON.getJSONObject("watchdogStatus").getString("wupStatus")),
                    Instant.ofEpochMilli(parcelJSON.getJSONObject("watchdogStatus").getLong("lastStatusUpdate")));
            parcel.setTaskProcessorState(watchdogState);
        }
       
        if (parcelJSON.has("precursorParcelFDN")) {
            parcel.setPrecursorParcel(new FDN(parcelJSON.getString("precursorParcelFDN")));
        }

        return parcel;
    }
}