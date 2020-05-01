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

import net.fhirbox.pegacorn.petasos.model.ComponentWatchdogStateEnum;
import net.fhirbox.pegacorn.petasos.model.FDN;
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
    
    private PetasosParcel parcel;
    private FDN previousParcelFDN;

    
    @Inject
    DefaultCacheManager petasosCacheManager;   

    // The clustered cache
    private Cache<String, String> petasosParcelCache;
    private Cache<String, String> petasosWatchdogCache;

    @PostConstruct
    public void start() {
        // get or create the clustered cache which will hold the transactions (aka Units of Work)
        petasosParcelCache = petasosCacheManager.getCache("petasos-parcel-cache", true);
        petasosParcelCache = petasosCacheManager.getCache("petasos-watchdog-cache", true);
    }
    
 
    // TODO: maybe add a long for expected duration
    public PetasosParcelRegistration registerActivity(String wupID, UoW theUoW) {
        
        PetasosParcel parcel = new PetasosParcel();
        PetasosParcelIdentifier parcelID = new PetasosParcelIdentifier(wupID, theUoW.getUowID().getRelativeName(), UUID.randomUUID().toString());
        PetasosParcelRegistration parcelRegistration = new PetasosParcelRegistration(new FDN(parcelID.getFDN()));
        parcel.setPetasosParcelRegistration(parcelRegistration);
        parcel.setUoW(theUoW);

        // if not in cache, the put will return null. Use UoW id as key as they are a consistent value across sites.
        String parcelJSON = petasosParcelCache.putIfAbsent(theUoW.getUowID().getRelativeName(), parcel.getParcelJSON());
        if (parcelJSON == null) {
            // new UoW so forward to other sites REST service points
            // possible critical write to disk and Hestia
            return parcel.getParcelRegistration();
        } else {
            // got value off the cache so return the registration object from cached parcel since
            // it was there first
            return PetasosParcel.createParcelFromJSON(parcelJSON).getParcelRegistration();
        }
    }
    
    public PetasosWUPActionSuggestionEnum startActivity(String parcelID) {
         return PetasosWUPActionSuggestionEnum.WUP_ACTION_SUGGESTION_CONTINUE;
    }
    
    public PetasosWUPActionSuggestionEnum finishActivity(String parcelID, UoW theFinishedUoW, UoWProcessingOutcomeEnum theFinishedUoWOutcome) {
        if (previousParcelFDN != null) {
            parcel.setPrecursorParcel(previousParcelFDN);
        }
        previousParcelFDN = parcel.getParcelFDN();
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
