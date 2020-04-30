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

import java.util.Set;

/**
 *
 * @author mhunter
 */
public class UoW {
    private UoWIdentifier uowID;
    private Set<String> uowIngressContent; // JSON Objects
    private Set<String> uowEgressContent; // JSON Objects
    private UoWProcessingOutcomeEnum uowProcessingOutcome;

    public UoWIdentifier getUowID() {
        return uowID;
    }

    public void setUowID(UoWIdentifier uowID) {
        this.uowID = uowID;
    }

    public Set<String> getUowIngressContent() {
        return uowIngressContent;
    }

    public void setUowIngressContent(Set<String> uowIngressContent) {
        this.uowIngressContent = uowIngressContent;
    }

    public Set<String> getUowEgressContent() {
        return uowEgressContent;
    }

    public void setUowEgressContent(Set<String> uowEgressContent) {
        this.uowEgressContent = uowEgressContent;
    }

    public UoWProcessingOutcomeEnum getUowProcessingOutcome() {
        return uowProcessingOutcome;
    }

    public void setUowProcessingOutcome(UoWProcessingOutcomeEnum uowProcessingOutcome) {
        this.uowProcessingOutcome = uowProcessingOutcome;
    }
}
