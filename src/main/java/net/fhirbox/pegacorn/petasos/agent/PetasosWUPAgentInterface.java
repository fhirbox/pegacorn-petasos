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

import net.fhirbox.pegacorn.petasos.model.ComponentWatchdogStateEnum;
import net.fhirbox.pegacorn.petasos.model.PetasosParcelRegistration;
import net.fhirbox.pegacorn.petasos.model.PetasosWUPActionSuggestionEnum;
import net.fhirbox.pegacorn.petasos.model.UoW;
import net.fhirbox.pegacorn.petasos.model.UoWProcessingOutcomeEnum;

/**
 *
 * @author ACT Health (Mark A. Hunter)
 */
public interface PetasosWUPAgentInterface {
    public PetasosParcelRegistration registerActivity(String wupID, UoW theUoW);
    public PetasosWUPActionSuggestionEnum startActivity(String parcelID);
    public PetasosWUPActionSuggestionEnum finishActivity(String parcelID, UoW theFinishedUoW, UoWProcessingOutcomeEnum theFinishedUoWOutcome);
    public UoWProcessingOutcomeEnum finaliseActivity(String parcelID, UoW theFinishedUoW);
    public PetasosWUPActionSuggestionEnum updateOperationalStatus( String wupID, Long presentInstant, ComponentWatchdogStateEnum presentState );
    public PetasosWUPActionSuggestionEnum updateActivityStatus( String parcelID, Long presentInstant, ComponentWatchdogStateEnum presentState );
    public PetasosWUPActionSuggestionEnum getPeerActivityStatus( String parcelID );
    
}