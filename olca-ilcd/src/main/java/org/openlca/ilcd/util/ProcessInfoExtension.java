package org.openlca.ilcd.util;

import javax.xml.namespace.QName;

import org.openlca.ilcd.processes.ProcessInformation;

public class ProcessInfoExtension {

	private ProcessInformation info;
	private final String MODEL_REF_PROCESS = "modelRefProcess";

	public ProcessInfoExtension(ProcessInformation info) {
		this.info = info;
	}

	/**
	 * Set the ID of the reference process if a product model is stored in the
	 * data set.
	 */
	public void setModelRefProcess(String uuid) {
		if (info == null)
			return;
		QName qName = Extensions.getQName(MODEL_REF_PROCESS);
		info.getOtherAttributes().put(qName, uuid);
	}

	/**
	 * Get the ID of the reference process if a product model is stored in the
	 * data set.
	 */
	public String getModelRefProcess() {
		if (info == null)
			return null;
		QName qName = Extensions.getQName(MODEL_REF_PROCESS);
		return info.getOtherAttributes().get(qName);
	}

}
