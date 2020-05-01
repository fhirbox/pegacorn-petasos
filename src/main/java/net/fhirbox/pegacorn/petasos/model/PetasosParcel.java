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

import org.jgroups.util.UUID;
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
    private UoW actualUnitOfWork;
    private Set<FDN> successorParcelFDNs;
    private PetasosWUPWatchdogState taskProcessorState;
    private FDN precursorParcelFDN;
    
    private PetasosParcelIdentifier parcelID;

    private static final Logger LOG = LoggerFactory.getLogger(PetasosParcel.class);

    // for JSON-to-object conversion where we have a full ID  
    public void setParcelID(PetasosParcelIdentifier parcelID) {
        this.parcelID = parcelID;
    }
    
    public UoW getUoW() {
        return actualUnitOfWork;
    }
    
    public void setUoW(UoW uow) {
        this.actualUnitOfWork = uow;
    }
    
    public void setTaskProcessorState(PetasosWUPWatchdogState taskProcessorState) {
        this.taskProcessorState = taskProcessorState;
    }
    
    public void addSuccessorParcel(FDN parcelFDN) {
        successorParcelFDNs.add(parcelFDN);
    }
    
    public void setSuccessorParcels(Set<FDN> successorParcelFDNs) {
        this.successorParcelFDNs = successorParcelFDNs;
    }
    
    public void setPrecursorParcel(FDN parcelFDN) {
        precursorParcelFDN = parcelFDN;
    }
    
    public FDN getPrecursorParcelFDN() {
        return precursorParcelFDN;
    }
    
    public void setParcelFDN(FDN parcelFDN) {
        parcelRegistration.setParcelFDN(parcelFDN);
    }
    
    public FDN getParcelFDN() {
        return new FDN(parcelID.getFDN());
    }
    
    public void setPetasosParcelRegistration(PetasosParcelRegistration parcelRegistration) {
        this.parcelRegistration = parcelRegistration;
    }
    
    public PetasosParcelRegistration getParcelRegistration() {
        return parcelRegistration;
    }

    public String getParcelJSON() {
        JSONObject parcelJSON = new JSONObject();

        JSONObject parcelIDInfo = new JSONObject().put("wupID", parcelID.getSourceWUPID())
                .put("uowID",  parcelID.getUowID()).put("discriminator", parcelID.getDiscriminator());
        JSONObject registrationInfo = new JSONObject().put("parcelID", parcelIDInfo).put("parcelStartDate", parcelRegistration.getParcelStartDate().toEpochMilli())
                        .put("parcelExpectedCompletionDate", parcelRegistration.getExpectedCompletionDate().toEpochMilli());
        parcelJSON.put("parcelRegistration", registrationInfo);
             
        JSONObject uowIdentifierInfo = new JSONObject().put("relativeName", actualUnitOfWork.getUowID().getRelativeName())
                .put("hierarchyFDN", actualUnitOfWork.getUowID().getComponentTypeHeirarchy().getQualifiedFDN());
        
        JSONArray ingressContent = new JSONArray();
        if (actualUnitOfWork.getUowIngressContent() != null) {
            actualUnitOfWork.getUowIngressContent().forEach(ingress -> {
                ingressContent.put(new JSONObject((String)ingress));
            });
        }

        JSONArray egressContent = new JSONArray();
        if (actualUnitOfWork.getUowEgressContent() != null) {
            actualUnitOfWork.getUowEgressContent().forEach(egress -> {
                egressContent.put(new JSONObject((String)egress));
            });
        }
        
        JSONObject uowInfo = new JSONObject().put("uowID", uowIdentifierInfo).put("ingressContent", ingressContent)
            .put("egressContent", egressContent).put("processingOutcome", actualUnitOfWork.getUowProcessingOutcome().getUoWProcessingOutcome());
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
        PetasosParcelIdentifier parcelID = new PetasosParcelIdentifier(registrationObject.getJSONObject("parcelID").getString("wupID"),
                registrationObject.getJSONObject("parcelID").getString("uowID"),
                registrationObject.getJSONObject("parcelID").getString("discriminator"));
        PetasosParcel parcel = new PetasosParcel();
        parcel.setParcelID(parcelID);
        
        PetasosParcelRegistration parcelRegistration = new PetasosParcelRegistration(new FDN(parcelID.getFDN()));
        parcelRegistration.setParcelStartDate(Instant.ofEpochMilli(registrationObject.getLong("parcelStartDate")));
        if (registrationObject.has("parcelExpectedCompletionDate")) {
            if (registrationObject.getLong("parcelExpectedCompletionDate") > 0) {
                parcelRegistration.setExpectedCompletionDate(Instant.ofEpochMilli(registrationObject.getLong("parcelStartDate")));                    
            }
        }
        parcel.setPetasosParcelRegistration(parcelRegistration);
 
        // create the UoW object
        UoW uow = new UoW();
        UoWIdentifier uowID = new UoWIdentifier(new FDN(parcelJSON.getJSONObject("uow").getString("hierarchyFDN")),
                                                parcelJSON.getJSONObject("uow").getString("relativeName"));

        uow.setUowID(uowID);
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
            HashSet<FDN> ppis = new HashSet<>();
            parcelJSON.getJSONArray("successorParcels").forEach(successor -> {
                FDN ppi  = new FDN();
                ppi.populateFDN((String)successor);
                ppis.add(ppi);
            });
            parcel.setSuccessorParcels(ppis);
        }

        if (parcelJSON.has("watchdogStatus")) {
            PetasosWUPWatchdogState watchdogState = new PetasosWUPWatchdogState(new FDN(parcelJSON.getJSONObject("watchdogStatus").getString("wupFDN")),
                    ComponentWatchdogStateEnum.valueOf(parcelJSON.getJSONObject("watchdogStatus").getString("wupStatus")),
                    Instant.ofEpochMilli(parcelJSON.getJSONObject("watchdogStatus").getLong("lastStatusUpdate")));
            parcel.setTaskProcessorState(watchdogState);
        }
       
        if (parcelJSON.has("precursorParcel")) {
            FDN precursorParcelFDN = new FDN(parcelJSON.getJSONObject("precursorParcel").getString("qualifiedFDN"));
            parcel.setPrecursorParcel(precursorParcelFDN);
        }

        return parcel;
    }
}