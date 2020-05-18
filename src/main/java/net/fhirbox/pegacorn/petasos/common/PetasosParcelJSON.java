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
package net.fhirbox.pegacorn.petasos.common;

import java.time.Instant;
import java.util.HashSet;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirbox.pegacorn.petasos.model.ComponentStatusEnum;
import net.fhirbox.pegacorn.petasos.model.FDN;
import net.fhirbox.pegacorn.petasos.model.PetasosParcel;
import net.fhirbox.pegacorn.petasos.model.PetasosParcelRegistration;
import net.fhirbox.pegacorn.petasos.model.PetasosParcelStatusEnum;
import net.fhirbox.pegacorn.petasos.model.PetasosWUPWatchdogState;
import net.fhirbox.pegacorn.petasos.model.UoW;
import net.fhirbox.pegacorn.petasos.model.UoWProcessingOutcomeEnum;

/**
 *
 * @author mhunter
 */
public class PetasosParcelJSON {
    private JSONObject parcelJSON;

    private static final Logger LOG = LoggerFactory.getLogger(PetasosParcelJSON.class);

    public PetasosParcelJSON(String parcelString) {
        this.parcelJSON = new JSONObject(parcelString);

        // This is meant to support parcels from the cache, so if no registration information
        // then something is wrong, so return null
        if (!parcelJSON.has("parcelRegistration")) {
            LOG.error("Parcel JSON without registration information encountered");
        }

        // all parcels should have a uowID
        if (!parcelJSON.has("uow")) {
            LOG.error("Parcel JSON without UoW information encountered");
        }

        if (!parcelJSON.has("parcelStatus")) {
            LOG.error("Parcel JSON without status information encountered");
        }
    }    
    
    public PetasosParcelJSON(PetasosParcel petasosParcel) {
        JSONObject parcelJSON = new JSONObject();
        
        parcelJSON.put("parcelStatus", petasosParcel.getParcelStatus().getPetasosParcelStatus());

        JSONObject registrationInfo = new JSONObject().put("parcelFDN", petasosParcel.getParcelRegistration().getParcelFDN().getQualifiedFDN())
                    .put("parcelInstantiationInstant", petasosParcel.getParcelRegistration().getParcelInstantiationInstant().toEpochMilli())
                          .put("parcelExpectedCompletionInstant", petasosParcel.getParcelRegistration().getParcelExpectedCompletionInstant().toEpochMilli())
                          .put("supportingFunctionFDN", petasosParcel.getParcelRegistration().getSupportingFunctionFDN().getQualifiedFDN());

        JSONArray wupList = new JSONArray();
        if (petasosParcel.getParcelRegistration().getRegisteredWUPList() != null) {
            petasosParcel.getParcelRegistration().getRegisteredWUPList().forEach(fdn -> {
                wupList.put(fdn.getQualifiedFDN());
            });
            registrationInfo.put("registeredWUPList", wupList);
        }
        
        if (petasosParcel.getParcelRegistration().getContainedUoW() != null) {
            registrationInfo.put("containedUoW", petasosParcel.getParcelRegistration().getContainedUoW().getQualifiedFDN());
        }

        parcelJSON.put("parcelRegistration", registrationInfo);

        JSONArray ingressContent = new JSONArray();
        if (petasosParcel.getContainedUoW().getUowIngressContent() != null) {
            petasosParcel.getContainedUoW().getUowIngressContent().forEach(ingress -> {
                ingressContent.put(new JSONObject((String)ingress));
            });
        }

        JSONArray egressContent = new JSONArray();
        if (petasosParcel.getContainedUoW().getUowEgressContent() != null) {
            petasosParcel.getContainedUoW().getUowEgressContent().forEach(egress -> {
                egressContent.put(new JSONObject((String)egress));
            });
        }
  
        JSONObject uowInfo = new JSONObject().put("uowFDN", petasosParcel.getContainedUoW().getUoWFDN().getQualifiedFDN())
                .put("requiredFunctionFDN", petasosParcel.getContainedUoW().getRequiredFunctionFDN().getQualifiedFDN())
                .put("ingressContent", ingressContent).put("egressContent", egressContent)
                .put("processingOutcome", petasosParcel.getContainedUoW().getUowProcessingOutcome().getUoWProcessingOutcome());
        parcelJSON.put("uow", uowInfo);
        

        JSONArray successorParcelFDNs = new JSONArray();
        petasosParcel.getSuccessorParcelSet().forEach(successorParcelFDN -> {
            JSONObject successor = new JSONObject().put("FDN", ((FDN)successorParcelFDN).getQualifiedFDN());
            successorParcelFDNs.put(successor);
        });
        parcelJSON.put("successorParcels", successorParcelFDNs);

        JSONObject watchdogStateInfo = new JSONObject().put("wupFDN", petasosParcel.getTaskProcessorState().getWupFDN().getQualifiedFDN())
                .put("wupStatus", petasosParcel.getTaskProcessorState().getWupStatus().getComponentWatchdogState())
                .put("lastStatusUpdate", petasosParcel.getTaskProcessorState().getLastStatusUpdate().toEpochMilli());
        parcelJSON.put("watchdogStatus", watchdogStateInfo);
        
        parcelJSON.put("precursorParcelFDN", petasosParcel.getPrecursorParcel().getQualifiedFDN());
    }
    
    public String toJSONString() {
        return parcelJSON.toString();
    }
    
    public ComponentStatusEnum getWupStatus() {
        return ComponentStatusEnum.valueOf(parcelJSON.getJSONObject("watchdogStatus").getString("wupStatus"));
    }
    
    public void setWupStatus(ComponentStatusEnum wupStatus) {
        parcelJSON.getJSONObject("watchdogStatus").put("wupStatus", wupStatus.getComponentWatchdogState());
    }
    
    public void setWUPLastStatusUpdate(long lastStatusUpdate) {
        parcelJSON.getJSONObject("watchdogStatus").put("lastStatusUpdate", lastStatusUpdate);
    }
    
    public String getUoWFDN() {
        return parcelJSON.getJSONObject("uow").getString("uowFDN");
    }
    
    public void setParcelStatus(PetasosParcelStatusEnum parcelStatus) {
        parcelJSON.put("parcelStatus", parcelStatus.getPetasosParcelStatus());
    }
    
    public long getParcelInstantiationInstant() {
        return parcelJSON.getJSONObject("parcelRegistration").getLong("parcelInstantiationInstant");
    }
    
    public long getParcelExpectedCompletionInstant() {
        return parcelJSON.getJSONObject("parcelRegistration").getLong("parcelExpectedCompletionInstant");
    }
    
    public UoWProcessingOutcomeEnum getUoWProcessingOutcome() {
        if (parcelJSON.getJSONObject("uow").has("processingOutcome")) {
            return UoWProcessingOutcomeEnum.valueOf(parcelJSON.getJSONObject("uow").getString("processingOutcome"));            
        }
        return null;
    }
    

    public void setUoW(UoW uow) {
        JSONArray ingressContent = new JSONArray();
        if (uow.getUowIngressContent() != null) {
            uow.getUowIngressContent().forEach(ingress -> {
                ingressContent.put(new JSONObject((String)ingress));
            });
        }

        JSONArray egressContent = new JSONArray();
        if (uow.getUowEgressContent() != null) {
            uow.getUowEgressContent().forEach(egress -> {
                egressContent.put(new JSONObject((String)egress));
            });
        }
  
        JSONObject uowJSONObject = new JSONObject().put("uowFDN", uow.getUoWFDN().getQualifiedFDN())
                .put("requiredFunctionFDN", uow.getRequiredFunctionFDN().getQualifiedFDN())
                .put("ingressContent", ingressContent).put("egressContent", egressContent)
                .put("processingOutcome", uow.getUowProcessingOutcome().getUoWProcessingOutcome());
        
        parcelJSON.put("uow", uowJSONObject);
    }
    
    
    public PetasosParcel createParcel() {
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
        
        parcel.setParcelStatus(PetasosParcelStatusEnum.valueOf(parcelJSON.getString("parcelStatus")));

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