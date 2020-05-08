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

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirbox.pegacorn.petasos.model.ComponentStatusEnum;
import net.fhirbox.pegacorn.petasos.model.FDN;
import net.fhirbox.pegacorn.petasos.model.PetasosParcel;
import net.fhirbox.pegacorn.petasos.model.PetasosParcelRegistration;
import net.fhirbox.pegacorn.petasos.model.PetasosWUPActionSuggestionEnum;
import net.fhirbox.pegacorn.petasos.model.UoW;
import net.fhirbox.pegacorn.petasos.model.UoWProcessingOutcomeEnum;
import net.fhirbox.pegacorn.petasos.node.PetasosNode;
import net.fhirbox.pegacorn.petasos.node.WatchdogEntry;

/**
 *
 * @author mhunter
 */
public class PetasosAgent implements PetasosAgentInterface{

    private static final Logger LOG = LoggerFactory.getLogger(PetasosAgent.class);
    
    private PetasosParcel parcel;
    private FDN previousParcelFDN;
    private FDN supportedFunctionFDN;
    private FDN agentFDN;

    
    @Inject
    DefaultCacheManager petasosCacheManager;   

    @Inject
    PetasosNode node;
    
    // The clustered cache
    private Cache<String, String> petasosParcelCache;
    private Cache<String, String> petasosWatchdogCache;

    @PostConstruct
    public void start() {
        // get or create the clustered cache which will hold the transactions (aka Units of Work)
        petasosParcelCache = petasosCacheManager.getCache("petasos-parcel-cache", true);
        petasosWatchdogCache = petasosCacheManager.getCache("petasos-watchdog-cache", true);
    }
    
 
    @Override
    public void registerWorkUnitProcessor(FDN myProcessorFDN, FDN mySupportedFunctionFDN) {
        // register with the local Petasos::Node
        // so for now a watchdog entry is going to be a flattened state plus supported function
        // FDN String. Petasos care about WUPs and Nodes so will limit to that as component
        // instance model too complex (recursive) to flatten onto cache.
        //Check if registered, check if function registered, if no to any of those, json-ise
        // and put on cache
        // key to watchdog cache will be component qualified fdn string
        String watchdogEntryJSON = petasosWatchdogCache.get(myProcessorFDN.getQualifiedFDN());
        WatchdogEntry watchdogEntry = new WatchdogEntry(myProcessorFDN, mySupportedFunctionFDN);
        if (watchdogEntryJSON == null) {
                petasosWatchdogCache.put(myProcessorFDN.getQualifiedFDN(), watchdogEntry.toJSONString());
        } else {
            // exists so do we add a supported function, replace, or ??
        }
        node.registerWUPWithOtherSites(watchdogEntry);
    }
    
    @Override
    public PetasosParcel registerActivity(FDN theWUPFDN, FDN theComponentFunctionFDN, UoW theUoW, FDN precursorParcelFDN){
        PetasosParcelRegistration parcelRegistration = new PetasosParcelRegistration(theWUPFDN, theUoW.getUoWFDN(), theComponentFunctionFDN, Instant.now());
        PetasosParcel parcel = new PetasosParcel(parcelRegistration);
        parcel.setUoW(theUoW);
        if (precursorParcelFDN != null) {
            parcel.setPrecursorParcel(precursorParcelFDN);
        }

        // if not in cache, the put will return null. Use UoW FDN as key as they are a consistent value across sites
        // since they are based on function FDN plus a hash
        String parcelJSON = petasosParcelCache.putIfAbsent(theUoW.getUoWFDN().getUnqualifiedFDN(), parcel.getParcelJSON());
        if (parcelJSON == null) {
            // new UoW so forward to other sites REST service points
            // possible critical path write to Hestia
            return parcel;
        } else {
            // got value off the cache so return the registration object from cached parcel since
            // it was there first
            return PetasosParcel.createParcelFromJSON(parcelJSON);
        }
    }

    @Override
    public PetasosWUPActionSuggestionEnum startActivity(FDN parcelFDN) {
        // Because this is a stub - of course we continue!
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);      
    }
    
    @Override    
    public PetasosWUPActionSuggestionEnum finishActivity(FDN parcelID, UoW theFinishedUoW, UoWProcessingOutcomeEnum theFinishedUoWOutcome) {
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }
    
    @Override
    public UoWProcessingOutcomeEnum finaliseActivity(FDN parcelFDN, UoW theFinishedUoW) {
        return(UoWProcessingOutcomeEnum.PEGACORN_UOW_OUTCOME_SUCCESS);
    }
    
    @Override
    public PetasosWUPActionSuggestionEnum updateOperationalStatus( FDN wupFDN, Long presentInstant, ComponentStatusEnum presentState ) {
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }
    
    @Override
    public PetasosWUPActionSuggestionEnum updateActivityStatus( FDN parcelFDN, Long presentInstant, ComponentStatusEnum presentState ) {
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }

    @Override
    public PetasosWUPActionSuggestionEnum getPeerActivityStatus(FDN parcelFDN) {
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }

    @Override
    public Collection<PetasosParcel> getRelevantParcels(FDN myProcessorFDN, FDN myFunctionFDN) {
        // TODO Auto-generated method stub
        return null;
    }
}
