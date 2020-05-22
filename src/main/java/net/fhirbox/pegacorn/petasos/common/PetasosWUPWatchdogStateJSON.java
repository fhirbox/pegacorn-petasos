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

import org.json.JSONObject;

import net.fhirbox.pegacorn.petasos.model.ComponentStatusEnum;
import net.fhirbox.pegacorn.petasos.model.FDN;
import net.fhirbox.pegacorn.petasos.model.PetasosWUPWatchdogState;

/**
 *
 * @author ACT Health (Mark A. Hunter)
 */
public class PetasosWUPWatchdogStateJSON {
    private JSONObject watchdogStateJSON;
    
    public PetasosWUPWatchdogStateJSON(String watchdogStateJSONString) {
        this.watchdogStateJSON = new JSONObject(watchdogStateJSONString);
    }
    
    public PetasosWUPWatchdogStateJSON(PetasosWUPWatchdogState watchdogState) {
        this.watchdogStateJSON = new JSONObject()
            .put("wupFDN", watchdogState.getWupFDN().getQualifiedFDN())
            .put("wupStatus", ComponentStatusEnum.valueOf(watchdogState.getWupStatus().getComponentWatchdogState()))
            .put("lastStatusUpdate", watchdogState.getLastStatusUpdate().toEpochMilli());
    }
    
    public PetasosWUPWatchdogState createWatchdogState() {
        return new PetasosWUPWatchdogState(new FDN(watchdogStateJSON.getString("wupFDN")),
                ComponentStatusEnum.valueOf(watchdogStateJSON.getString("wupStatus")),
                Instant.ofEpochMilli(watchdogStateJSON.getLong("lastStatusUpdate")));
    }
    
    public String toJSONString() {
        return watchdogStateJSON.toString();
    }
}
