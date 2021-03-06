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
public class UoWIdentifier {
    private FDN componentTypeHeirarchy;
    private String relativeName;
    
    public UoWIdentifier(FDN componentType, String distinguishingAttribute){
        this.componentTypeHeirarchy = componentType;
        this.relativeName = distinguishingAttribute;
    }

    public FDN getComponentTypeHeirarchy() {
        return componentTypeHeirarchy;
    }

    public void setComponentTypeHeirarchy(FDN componentType) {
        this.componentTypeHeirarchy = componentType;
    }

    public String getRelativeName() {
        return relativeName;
    }

    public void setRelativeName(String relativeName) {
        this.relativeName = relativeName;
    }
    
    public String toString(){
        String fdnString = this.componentTypeHeirarchy.getQualifiedFDN() + ".UoW=" + this.relativeName;
        return(fdnString);
    }
}
