
package org.openlca.ilcd.processes;

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
import javax.xml.namespace.QName;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Other;


/**
 * <p>Java class for DataSetInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataSetInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}UUID"/>
 *         &lt;element name="name" type="{http://lca.jrc.it/ILCD/Process}NameType" minOccurs="0"/>
 *         &lt;element name="identifierOfSubDataSet" type="{http://lca.jrc.it/ILCD/Common}String" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}synonyms" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="complementingProcesses" type="{http://lca.jrc.it/ILCD/Process}ComplementingProcessesType" minOccurs="0"/>
 *         &lt;element name="classificationInformation" type="{http://lca.jrc.it/ILCD/Common}ClassificationInformationType" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}generalComment" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToExternalDocumentation" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "DataSetInformationType", propOrder = {
    "uuid",
    "name",
    "identifierOfSubDataSet",
    "synonyms",
    "complementingProcesses",
    "classificationInformation",
    "generalComment",
    "externalDocumentationSources",
    "other"
})
public class DataSetInformation
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "UUID", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
    protected String uuid;
    protected ProcessName name;
    protected String identifierOfSubDataSet;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected List<FreeText> synonyms;
    protected ComplementingProcesseList complementingProcesses;
    protected ClassificationInformation classificationInformation;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected List<FreeText> generalComment;
    @XmlElement(name = "referenceToExternalDocumentation")
    protected List<DataSetReference> externalDocumentationSources;
    @XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
    protected Other other;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes =  new HashMap<>();

    /**
     * Automatically generated Universally Unique Identifier of this data set. Together with the "Data set version", the UUID uniquely identifies each data
     *             set.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUUID(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessName }
     *     
     */
    public ProcessName getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessName }
     *     
     */
    public void setName(ProcessName value) {
        this.name = value;
    }

    /**
     * Gets the value of the identifierOfSubDataSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentifierOfSubDataSet() {
        return identifierOfSubDataSet;
    }

    /**
     * Sets the value of the identifierOfSubDataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentifierOfSubDataSet(String value) {
        this.identifierOfSubDataSet = value;
    }

    /**
     * Synonyms / alternative names / brands of the good, service, or process. Separated by semicolon.Gets the value of the synonyms property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the synonyms property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSynonyms().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FreeText }
     * 
     * 
     */
    public List<FreeText> getSynonyms() {
        if (synonyms == null) {
            synonyms = new ArrayList<>();
        }
        return this.synonyms;
    }

    /**
     * Gets the value of the complementingProcesses property.
     * 
     * @return
     *     possible object is
     *     {@link ComplementingProcesseList }
     *     
     */
    public ComplementingProcesseList getComplementingProcesses() {
        return complementingProcesses;
    }

    /**
     * Sets the value of the complementingProcesses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplementingProcesseList }
     *     
     */
    public void setComplementingProcesses(ComplementingProcesseList value) {
        this.complementingProcesses = value;
    }

    /**
     * Gets the value of the classificationInformation property.
     * 
     * @return
     *     possible object is
     *     {@link ClassificationInformation }
     *     
     */
    public ClassificationInformation getClassificationInformation() {
        return classificationInformation;
    }

    /**
     * Sets the value of the classificationInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassificationInformation }
     *     
     */
    public void setClassificationInformation(ClassificationInformation value) {
        this.classificationInformation = value;
    }

    /**
     * General information about the data set, including e.g. general (internal, not reviewed) quality statements as well as information sources used. (Note:
     *             Please also check the more specific fields e.g. on "Intended application", "Advice on data set use" and the fields in the "Modelling and validation" section to avoid
     *             overlapping entries.) Gets the value of the generalComment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generalComment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeneralComment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FreeText }
     * 
     * 
     */
    public List<FreeText> getGeneralComment() {
        if (generalComment == null) {
            generalComment = new ArrayList<>();
        }
        return this.generalComment;
    }

    /**
     * Gets the value of the externalDocumentationSources property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the externalDocumentationSources property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExternalDocumentationSources().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getExternalDocumentationSources() {
        if (externalDocumentationSources == null) {
            externalDocumentationSources = new ArrayList<>();
        }
        return this.externalDocumentationSources;
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
