/* 
 * The MIT License
 *
 * Copyright 2016 Mark A. Hunter.
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

import java.util.Vector;
import java.util.ListIterator;

/**
 *
 * @author markh
 */
public class FDN 
{
    private java.util.Vector<RDN> rdnElementSet;
    
    public static String RDN_ENTRY_SEPERATOR = ".";

    public FDN() 
    {
        rdnElementSet = new Vector<RDN>();
    }
    
    public FDN(Vector<RDN> rdnElement) 
    {
        this.rdnElementSet = rdnElement;
    }
    
    public FDN( String qualifiedFDN ){
        rdnElementSet = new Vector<RDN>();
        populateFDN(qualifiedFDN);
    }

    public void appendRDN( RDN pRDN)
    {
        rdnElementSet.add(rdnElementSet.size(), pRDN);
    }
    
    public void populateFDN( String qualifiedFDN )
    {
    	if( qualifiedFDN == null ) {
    		return;
    	}
    	if( qualifiedFDN.isEmpty()) {
    		return;
    	}
    	String[] qualifiedElements = qualifiedFDN.split(this.RDN_ENTRY_SEPERATOR);
    	if(qualifiedElements.length < 1 ) {
    		return;
    	}
    	for(int counter = 0; counter < qualifiedElements.length; counter += 1) {
    		RDN newRDNElement = new RDN(qualifiedElements[counter]);
    		rdnElementSet.add(counter, newRDNElement);
    	}
        return;
    }
    
	String getUnqualifiedFDN()
    {
        String lShortFDN = new String();
        if( !rdnElementSet.isEmpty() )
        {
            ListIterator<RDN> rdnIterator = rdnElementSet.listIterator();
            while( rdnIterator.hasNext() )
            {
                lShortFDN += rdnIterator.next().getTypeValue();
                if( rdnIterator.hasNext() )
                {
                    lShortFDN += new String(".");
                }
            }
        }
        return(lShortFDN);
    }
    
    String getComprehensiveFDN()
    {
        String lShortFDN = new String();
        Integer lCount = 0;
        if( !rdnElementSet.isEmpty() )
        {
            ListIterator<RDN> rdnIterator = rdnElementSet.listIterator();
            while( rdnIterator.hasNext() )
            {
                RDN lRDN = rdnIterator.next();
                lShortFDN += new String("[");
                lShortFDN += lCount.toString();
                lShortFDN += new String("]:");
                lShortFDN += lRDN.getTypeName();
                lShortFDN += new String("=");
                lShortFDN += lRDN.getTypeValue();
                if( rdnIterator.hasNext() )
                {
                    lShortFDN += new String(".");
                }
                lCount += 1;
            }
        }
        return(lShortFDN);       
    }
    
    String getQualifiedFDN()
    {
         String lShortFDN = new String();
        if( !rdnElementSet.isEmpty() )
        {
            ListIterator<RDN> rdnIterator = rdnElementSet.listIterator();
            while( rdnIterator.hasNext() )
            {
                RDN lRDN = rdnIterator.next();
                lShortFDN += lRDN.getTypeName();
                lShortFDN += new String("=");
                lShortFDN += lRDN.getTypeValue();
                if( rdnIterator.hasNext() )
                {
                    lShortFDN += new String(".");
                }
            }
        }
        return(lShortFDN);       
    } 
    
    public String toString(){
        return(getQualifiedFDN());
    }
}
