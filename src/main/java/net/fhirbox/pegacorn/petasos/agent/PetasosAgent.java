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
import java.util.concurrent.Callable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.enterprise.concurrent.ManagedExecutorService;

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
    
    private boolean criticalWrite = false;

    // see https://docs.wildfly.org/18/Developer_Guide.html#managed-executor-service
    // see https://www.javacodegeeks.com/2014/07/java-ee-concurrency-api-tutorial.html
    @Resource(name = "DefaultManagedExecutorService")
    ManagedExecutorService executor;
    
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
        // Do we need an injection point as well?
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

        // If not in cache, the putIfAbsent will return null.
        // Use UoW FDN as key as they are a consistent value across sites since they are based
        // on function FDN (not unique across sites) plus a hash
        String parcelJSON = petasosParcelCache.putIfAbsent(theUoW.getUoWFDN().getQualifiedFDN(), parcel.getParcelJSON());
        if (parcelJSON == null) {
            // new UoW so forward to other sites REST service points
            if (criticalWrite == true) {
                // synchronous write to Hestia?
            }
            return parcel;
        } else {
            // got value off the cache so return the registration object from cached parcel since
            // it was there first
            parcel = PetasosParcel.createParcelFromJSON(parcelJSON);
        }
        
        // Add the current parcel ID to the Watchdog entry so as to provide a quick
        // lookup for failover processing (NB temporary, may not need).
/*        if (petasosWatchdogCache.containsKey(theWUPFDN.getQualifiedFDN())) {
            WatchdogEntry watchdogEntry = new WatchdogEntry(theWUPFDN.getQualifiedFDN());
            watchdogEntry.setCurrentParcelFDN(parcel.getParcelFDN());
            petasosWatchdogCache.replace(theWUPFDN.getQualifiedFDN(), watchdogEntry.toJSONString());
        } else {
            LOG.warn("Entry for "+theWUPFDN.getQualifiedFDN()+" not found on cache. Current parcel information can be added");
        }
  */    
        return parcel;
    }

    @Override
    public PetasosWUPActionSuggestionEnum startActivity(FDN parcelFDN) {
        // since we only have the parcel FDN, need to get the UoW FDN which is one
        // level up from the parcel
        PetasosParcel parcel = PetasosParcel.createParcelFromJSON(petasosParcelCache.get(parcelFDN.getParentFDN().getQualifiedFDN()));

        // if the parcel is currently being actioned, ignore it (should this be continue or pause?)
        if (parcel.getTaskProcessorState().getWupStatus() == ComponentStatusEnum.COMPONENT_STATUS_ACTIVE) {
            return PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_PAUSE;
        }
        
        // if idle, start the activity
        if (parcel.getTaskProcessorState().getWupStatus() == ComponentStatusEnum.COMPONENT_STATUS_IDLE) {
            // do we need to check that the component is on our pod??
            parcel.getTaskProcessorState().setWupStatus(ComponentStatusEnum.COMPONENT_STATUS_ACTIVE);
        }
        parcel.getTaskProcessorState().setLastStatusUpdate(Instant.now());

        // put updated parcel on the cache
        petasosParcelCache.put(parcel.getContainedUoW().getUoWFDN().getQualifiedFDN(), parcel.getParcelJSON());

        // new UoW so forward to other sites REST service points
        if (criticalWrite == true) {
            // synchronous write to Hestia?
        }
        
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }

    @Override    
    public PetasosWUPActionSuggestionEnum finishActivity(FDN parcelFDN, UoW theFinishedUoW, UoWProcessingOutcomeEnum theFinishedUoWOutcome) {
        PetasosParcel parcel = PetasosParcel.createParcelFromJSON(petasosParcelCache.get(parcelFDN.getParentFDN().getQualifiedFDN()));

        // if active, now it's finished set back to idle
        if (parcel.getTaskProcessorState().getWupStatus() == ComponentStatusEnum.COMPONENT_STATUS_ACTIVE) {
            // do we need to check that the component is on our pod??
            parcel.getTaskProcessorState().setWupStatus(ComponentStatusEnum.COMPONENT_STATUS_IDLE);
        }
        parcel.getTaskProcessorState().setLastStatusUpdate(Instant.now());
        parcel.setContainedUoW(theFinishedUoW);
        // thought the processing outcome would be set by UoW?? Should the Agent do this?
        parcel.getContainedUoW().setUowProcessingOutcome(theFinishedUoWOutcome);

        // put updated parcel on the cache
        petasosParcelCache.put(parcel.getContainedUoW().getUoWFDN().getQualifiedFDN(), parcel.getParcelJSON());

        // new UoW so forward to other sites REST service points
        if (criticalWrite == true) {
            // synchronous write to Hestia?
        }
        
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }
    
    @Override
    public UoWProcessingOutcomeEnum finaliseActivity(FDN parcelFDN, UoW theFinishedUoW) {
        // not sure what to do here??
        return(UoWProcessingOutcomeEnum.PEGACORN_UOW_OUTCOME_SUCCESS);
    }

    @Override
    public PetasosWUPActionSuggestionEnum updateOperationalStatus(FDN wupFDN, Long presentInstant, ComponentStatusEnum presentState) {
        String watchdogEntryJSON = petasosWatchdogCache.get(wupFDN.getQualifiedFDN());
        WatchdogEntry watchdogEntry = new WatchdogEntry(watchdogEntryJSON);
        watchdogEntry.getWatchdogState().setWupStatus(presentState);
        watchdogEntry.getWatchdogState().setLastStatusUpdate(Instant.ofEpochMilli(presentInstant));
        //TODO: check for success? TODO: do we need to update parcel state as well?
        node.updateCIStatus(watchdogEntry);
        // TODO: what to check for? CIStatus to see if someone has invalidated the entry? If they
        // have but this WUP is OK, then the above would have reflected that so just continue?
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }
    
    @Override
    public PetasosWUPActionSuggestionEnum updateActivityStatus(FDN parcelFDN, Long presentInstant, ComponentStatusEnum presentState) {
        PetasosParcel parcel = PetasosParcel.createParcelFromJSON(petasosParcelCache.get(parcelFDN.getParentFDN().getQualifiedFDN()));
        parcel.getTaskProcessorState().setWupStatus(presentState);
        parcel.getTaskProcessorState().setLastStatusUpdate(Instant.ofEpochMilli(presentInstant));
        // don't know what should be checked here, if the caller is OK then it makes sense to continue
        // Is the caller reporting self-problems?? Or is this just a straight 'still processing'
        // type call?
        return(PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE);
    }

    @Override
    public PetasosWUPActionSuggestionEnum getPeerActivityStatus(FDN parcelFDN) {
        PetasosWUPActionSuggestionEnum suggestedAction = PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_PAUSE;

        PetasosParcel parcel = PetasosParcel.createParcelFromJSON(petasosParcelCache.get(parcelFDN.getParentFDN().getQualifiedFDN()));

        switch (parcel.getTaskProcessorState().getWupStatus()) {
            // a WUP is processing the parcel
            case COMPONENT_STATUS_ACTIVE:
                suggestedAction = PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_PAUSE;
                break;
            // a WUP has completed the parcel
            case COMPONENT_STATUS_IDLE:
                suggestedAction = PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_HALT;
                break;
            // if unresponsive or failed tell the WUP to continue (i.e. take over?)
            case COMPONENT_STATUS_UNRESPONSIVE:
            case COMPONENT_STATUS_FAILED:
                suggestedAction = PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE;
                break;
        }
        return suggestedAction;
    }

    @Override
    public Collection<PetasosParcel> getRelevantParcels(FDN myProcessorFDN, FDN myFunctionFDN) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setCriticalWrite(boolean criticalWrite) {
        this.criticalWrite = criticalWrite;
    }
    
    
    public class ParcelForwardTask implements Callable<Integer> {
        // TBD: this will be either REST or server:port combo?
        // TODO: change the var name once connection method is known
        String connectionEndpoint;
        
        public void setConnectionEndpoint(String connectionEndpoint) {
            this.connectionEndpoint = connectionEndpoint;
        }
        
        public Integer call() {
            try {
                // connection and forwarding
            }
            catch (Exception e) {
                return new Integer(0);
            }
            return new Integer(1);
        }
    }
}