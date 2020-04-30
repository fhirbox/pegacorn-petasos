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
package net.fhirbox.pegacorn.petasos.agent;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;



import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirbox.pegacorn.petasos.model.ComponentIdentifier;
import net.fhirbox.pegacorn.petasos.model.ComponentWatchdogStateEnum;
import net.fhirbox.pegacorn.petasos.model.PetasosParcel;
import net.fhirbox.pegacorn.petasos.model.PetasosParcelIdentifier;
import net.fhirbox.pegacorn.petasos.model.PetasosParcelRegistration;
import net.fhirbox.pegacorn.petasos.model.PetasosWUPActionSuggestionEnum;
import net.fhirbox.pegacorn.petasos.model.UoW;
import net.fhirbox.pegacorn.petasos.model.UoWProcessingOutcomeEnum;

/**
 *
 * @author mhunter
 */
public class PetasosWUPAgent implements PetasosWUPAgentInterface{

    private static final Logger LOG = LoggerFactory.getLogger(PetasosWUPAgent.class);
    
    private ComponentIdentifier agentId;
    private ComponentIdentifier precursorParcelId;
    private PetasosParcel parcel;
    private PetasosParcelIdentifier  currentParcelID;
    private int parcelCount = 0;

    
    @Inject
    DefaultCacheManager cacheManager;   

    // The clustered cache
    private Cache<String, String> petasosCache;

    @PostConstruct
    public void start() {
        // get or create the clustered cache which will hold the transactions (aka Units of Work)
        petasosCache = cacheManager.getCache("mitaf-clustered-cache", true);
    }
    
    public PetasosWUPAgent(ComponentIdentifier parentId) {
//        agentId = new ComponentIdentifier(parentId.getIdAsString(), this.getClass().getName());
    }
    
    public ComponentIdentifier getAgentId() {
        return agentId;
    }
 /*   
    public ComponentIdentifier createParcel() {
        // create a parcel
        PetasosParcel parcel = new PetasosParcel(agentId);
        
        if (precursorParcelId != null) {
            parcel.setPrecursorParcelId(precursorParcelId);
        }
        // register the parcel (drop it on the cache)
        // assume for local site, if parcel on cache worked, all nodes will be aware
        // of it via cache listeners.
        petasosCache.put(parcel.getParcelId().getIdAsString(), parcel.getParcelJSON());
        // For other sites, call REST service

        return parcel.getParcelId();
    }
    
/*    
    public boolean completeParcel() {
        // do the sync write to cache, disk and Hestia and set the precursor for
        // next work, update watchdog status
        precursorParcelId = parcel.getParcelId();
        return true;
    }
   
    // called by WUP or subprocesses(?). If subprocesses need reference to Agent
    public void updateComponentStatus(ComponentIdentifier CIId,
                                        ComponentStateEnum watchdogState,
                                        long componentStatusTimestamp) {
        WatchdogState ws = new WatchdogState(CIId, watchdogState, componentStatusTimestamp);
    }

    
    public void updateComponentStatus(WatchdogState ws) {
        
    }
*/  
    // TODO: maybe add a long for expected duration
    public PetasosParcelRegistration registerActivity(String wupID, UoW theUoW) {
        PetasosParcelIdentifier parcelID = new PetasosParcelIdentifier(wupID, theUoW.getUowID().identifierValue, Integer.toString(++parcelCount)); 
        PetasosParcel parcel = new PetasosParcel(parcelID);
        parcel.setUoW(theUoW);
        if (currentParcelID != null) {
            parcel.setPrecursorParcel(currentParcelID);
        }
        currentParcelID = parcelID;

        // if not in cache, put will return null. Use UoW as key as they should be the same across sites since they
        // should be a consistent value across sites.
        // NEED JSON MARSHALL/UNMARSHALL - add JSON Objects to each parcel class
        if (petasosCache.putIfAbsent(theUoW.getUowID().identifierValue, parcel.getParcelJSON()) == null) {
            // new UoW so forward to other sites
        } else {
            // got value off the cache so return the registration object from cached parcel
        }
        return parcel.getParcelRegistration();
    }
    
    public PetasosWUPActionSuggestionEnum startActivity(String parcelID) {
         return PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE;
    }
    
    public PetasosWUPActionSuggestionEnum finishActivity(String parcelID, UoW theFinishedUoW, UoWProcessingOutcomeEnum theFinishedUoWOutcome) {
        return PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE;        
    }
    
    public UoWProcessingOutcomeEnum finaliseActivity(String parcelID, UoW theFinishedUoW) {
        return UoWProcessingOutcomeEnum.PEGACORN_UOW_OUTCOME_SUCCESS;
    }
    
    public PetasosWUPActionSuggestionEnum updateOperationalStatus( String wupID, Long presentInstant, ComponentWatchdogStateEnum presentState ) {
        return PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE;        
    }
    
    public PetasosWUPActionSuggestionEnum updateActivityStatus( String parcelID, Long presentInstant, ComponentWatchdogStateEnum presentState ) {
        return PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE;
    }
    
    public PetasosWUPActionSuggestionEnum getPeerActivityStatus( String parcelID ) {
        return PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE;
    }
}
