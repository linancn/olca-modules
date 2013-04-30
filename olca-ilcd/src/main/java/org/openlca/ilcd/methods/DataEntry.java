
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.Other;


/**
 * <p>Java class for DataEntryByType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataEntryByType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://lca.jrc.it/ILCD/Common}DataEntryByGroup1"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}referenceToConvertedOriginalDataSetFrom" minOccurs="0"/>
 *         &lt;group ref="{http://lca.jrc.it/ILCD/Common}DataEntryByGroup2"/>
 *         &lt;element name="recommendationBy" type="{http://lca.jrc.it/ILCD/LCIAMethod}RecommendationType" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataEntryByType", propOrder = {
    "timeStamp",
    "referenceToDataSetFormat",
    "referenceToConvertedOriginalDataSetFrom",
    "referenceToPersonOrEntityEnteringTheData",
    "recommendationBy",
    "other"
})
public class DataEntry
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected XMLGregorianCalendar timeStamp;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected List<DataSetReference> referenceToDataSetFormat;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected DataSetReference referenceToConvertedOriginalDataSetFrom;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected DataSetReference referenceToPersonOrEntityEnteringTheData;
    protected Recommendation recommendationBy;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Gets the value of the timeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the value of the timeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeStamp(XMLGregorianCalendar value) {
        this.timeStamp = value;
    }

    /**
     * Gets the value of the referenceToDataSetFormat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToDataSetFormat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToDataSetFormat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToDataSetFormat() {
        if (referenceToDataSetFormat == null) {
            referenceToDataSetFormat = new ArrayList<>();
        }
        return this.referenceToDataSetFormat;
    }

    /**
     * "Source data set" of the database or data set publication from which this data set has been obtained by conversion. This can cover e.g. conversion to a
     *             different format, applying a different nomenclature, mapping of flow names, conversion of units, etc. This may however not have changed or re-modeled the
     *             characterisation factors. This entry is required for converted data sets stemming originally from other LCA databases (e.g. when re-publishing LCIA characterisation
     *             factors from the database of the original impact method developer). [Note: Identically re-published data sets are identied in the field "Unchanged re-publication of:"
     *             in the section "Publication and Ownership".]
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getReferenceToConvertedOriginalDataSetFrom() {
        return referenceToConvertedOriginalDataSetFrom;
    }

    /**
     * Sets the value of the referenceToConvertedOriginalDataSetFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setReferenceToConvertedOriginalDataSetFrom(DataSetReference value) {
        this.referenceToConvertedOriginalDataSetFrom = value;
    }

    /**
     * Gets the value of the referenceToPersonOrEntityEnteringTheData property.
     * 
     * @return
     *     possible object is
     *     {@link DataSetReference }
     *     
     */
    public DataSetReference getReferenceToPersonOrEntityEnteringTheData() {
        return referenceToPersonOrEntityEnteringTheData;
    }

    /**
     * Sets the value of the referenceToPersonOrEntityEnteringTheData property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSetReference }
     *     
     */
    public void setReferenceToPersonOrEntityEnteringTheData(DataSetReference value) {
        this.referenceToPersonOrEntityEnteringTheData = value;
    }

    /**
     * Gets the value of the recommendationBy property.
     * 
     * @return
     *     possible object is
     *     {@link Recommendation }
     *     
     */
    public Recommendation getRecommendationBy() {
        return recommendationBy;
    }

    /**
     * Sets the value of the recommendationBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Recommendation }
     *     
     */
    public void setRecommendationBy(Recommendation value) {
        this.recommendationBy = value;
    }

    /**
     * Gets the value of the other property.
     * 
     * @return
     *     possible object is
     *     {@link Other }
     *     
     */
    public Other getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     * 
     * @param value
     *     allowed object is
     *     {@link Other }
     *     
     */
    public void setOther(Other value) {
        this.other = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
