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

/**
 *
 * @author mhunter
 */
public class PetasosParcelIdentifier extends ComponentIdentifier {
    private String sourceWUPID; // the Component Identifier of the WUP as a String.
    private String uowID; // the UoW Identifier as a String.
    private String discriminator; // the unique bit for this PetasosParcel
    
    public PetasosParcelIdentifier(String sourceWUPID, String uowID, String discriminator) {
        this.sourceWUPID = sourceWUPID;
        this.uowID = uowID;
        this.discriminator = discriminator;
    }
    
    public String getWUPID() {
        return sourceWUPID;
    }
    
    public String getUoWID() {
        return uowID;
    }
    
    public String getDiscriminator() {
        return discriminator;
    }
}