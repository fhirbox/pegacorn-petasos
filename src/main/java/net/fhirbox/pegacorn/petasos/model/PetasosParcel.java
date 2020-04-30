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
    private Set<PetasosParcelIdentifier> successorParcels;
    private PetasosWUPWatchdogState taskProcessorState;
    private PetasosParcelIdentifier precursorParcel;

    private static final Logger LOG = LoggerFactory.getLogger(PetasosParcel.class);

    public PetasosParcel(PetasosParcelIdentifier parcelID) {
        parcelRegistration = new PetasosParcelRegistration(parcelID);
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
    
    public void addSuccessorParcel(PetasosParcelIdentifier parcelID) {
        successorParcels.add(parcelID);
    }
    
    public void setSuccessorParcels(Set<PetasosParcelIdentifier> successorParcelIDs) {
        successorParcels = successorParcelIDs;
    }
    
    public void setPrecursorParcel(PetasosParcelIdentifier parcelID) {
        precursorParcel = parcelID;
    }
    
    public void setParcelID(PetasosParcelIdentifier parcelID) {
        parcelRegistration.setParcelID(parcelID);
    }
    
    public void setPetasosParcelRegistration(PetasosParcelRegistration parcelRegistration) {
        this.parcelRegistration = parcelRegistration;
    }
    
    public PetasosParcelRegistration getParcelRegistration() {
        return parcelRegistration;
    }

    public String getParcelJSON() {
        JSONObject parcelJSON = new JSONObject();
        // still need to add identifiers
        JSONObject parcelIDInfo = new JSONObject().put("wupID", parcelRegistration.getParcelID().getWUPID())
                .put("uowID", parcelRegistration.getParcelID().getUoWID()).put("discriminator", parcelRegistration.getParcelID().getDiscriminator());

        JSONObject registrationInfo = new JSONObject().put("parcelID", parcelIDInfo).put("parcelStartDate", parcelRegistration.getParcelStartDate().toEpochMilli())
                        .put("parcelExpectedCompletionDate", parcelRegistration.getExpectedCompletionDate().toEpochMilli());
        parcelJSON.put("parcelRegistration", registrationInfo);
             
        JSONObject uowIdentifier = new JSONObject().put("wupID", actualUnitOfWork.getUowID().wupID)
                .put("identifierValue", actualUnitOfWork.getUowID().identifierValue);
        JSONObject uowIdentifierInfo = new JSONObject().put("uowID", uowIdentifier);
        
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
        

        JSONArray successorParcelIDs = new JSONArray();
        successorParcels.forEach(successorParcelID -> {
            JSONObject successor = new JSONObject().put("wupID", successorParcelID.getWUPID())
                    .put("uowID", successorParcelID.getUoWID()).put("discriminator", successorParcelID.getDiscriminator());
            successorParcelIDs.put(successor);
        });
        parcelJSON.put("successorParcels", successorParcelIDs);

        // need to add identifier
        JSONObject watchdogStateInfo = new JSONObject().put("wupStatus", taskProcessorState.getWUPStatus().getComponentWatchdogState())
                .put("lastStatusUpdate", taskProcessorState.getLastStatusUpdate().toEpochMilli());
        parcelJSON.put("watchdogStatus", watchdogStateInfo);
        
        JSONObject precursorInfo = new JSONObject().put("identifierType", precursorParcel.getIdentifierType())
                .put("identifierValue", precursorParcel.getIdentifierValue()).put("discriminator", precursorParcel.getDiscriminator());
        parcelJSON.put("precursorParcel", precursorInfo);
        
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
        PetasosParcelIdentifier parcelID = new PetasosParcelIdentifier(registrationObject.getString("wupID"), registrationObject.getString("uowID"), registrationObject.getString("discriminator"));
        PetasosParcel parcel = new PetasosParcel(parcelID);
        
        PetasosParcelRegistration parcelRegistration = new PetasosParcelRegistration(parcelID);
        parcelRegistration.setParcelStartDate(Instant.ofEpochMilli(registrationObject.getLong("parcelStartDate")));
        if (registrationObject.has("parcelExpectedCompletionDate")) {
            if (registrationObject.getLong("parcelExpectedCompletionDate") > 0) {
                parcelRegistration.setExpectedCompletionDate(Instant.ofEpochMilli(registrationObject.getLong("parcelStartDate")));                    
            }
        }
        parcel.setPetasosParcelRegistration(parcelRegistration);
        
        // create the UoW object
        UoW uow = new UoW();
        UoWIdentifier uowID = new UoWIdentifier();
        uowID.identifierValue = parcelJSON.getJSONObject("uow").getString("identifierValue");
        uowID.wupID = parcelJSON.getJSONObject("uow").getString("wupID");
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
            HashSet<PetasosParcelIdentifier> ppis = new HashSet<>();
            parcelJSON.getJSONArray("successorParcels").forEach(successor -> {
                JSONObject j = (JSONObject)successor;
                PetasosParcelIdentifier ppi  = new PetasosParcelIdentifier(j.getString("wupID"), j.getString("uowID"), j.getString("discriminator"));
                ppis.add(ppi);
            });
            parcel.setSuccessorParcels(ppis);
        }

        // need to add identifier
/*        if (parcelJSON.has("watchdogStatus")) {
            PetasosWUPWatchdogState watchdogState = new PetasosWUPWatchdogState(ComponentWatchdogStateEnum.valueOf(parcelJSON.getJSONObject("watchdogStatus").getString("wupStatus")),
                    Instant.ofEpochMilli(parcelJSON.getJSONObject("watchdogStatus").getLong("lastStatusUpdate")));
            parcel.setTaskProcessorState(watchdogState);
        }
*/       
        if (parcelJSON.has("precursorParcel")) {
            PetasosParcelIdentifier precursorParcelID  = new PetasosParcelIdentifier(parcelJSON.getJSONObject("precursorParcel").getString("wupID"),
                      parcelJSON.getJSONObject("precursorParcel").getString("uowID"),
                      parcelJSON.getJSONObject("precursorParcel").getString("discriminator"));
            parcel.setPrecursorParcel(precursorParcelID);
        }

        return parcel;
    }
}