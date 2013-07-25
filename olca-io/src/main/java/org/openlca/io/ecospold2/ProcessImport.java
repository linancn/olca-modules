package org.openlca.io.ecospold2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.IntermediateExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private RefDataIndex index;
	private ProcessDao dao;

	/** Exchanges that wait for a default provider: provider-id -> exchanges. */
	private HashMap<String, List<Exchange>> linkQueue = new HashMap<>();

	public ProcessImport(IDatabase database, RefDataIndex index) {
		this.database = database;
		this.index = index;
		dao = new ProcessDao(database.getEntityFactory());
	}

	public void importDataSet(DataSet dataSet) {
		try {
			if (dataSet == null) {
				log.warn("not an EcoSpold data set");
				return;
			}
			checkImport(dataSet);
		} catch (Exception e) {
			log.error("Failed to import EcoSpold 2 process", e);
		}
	}

	private void checkImport(DataSet dataSet) {
		if (!valid(dataSet)) {
			log.warn("invalid data set -> not imported");
			return;
		}
		Activity activity = dataSet.getActivity();
		try {
			boolean contains = dao.getForRefId(activity.getId()) != null;
			if (contains) {
				log.trace("process {} is already in the database",
						activity.getId());
				return;
			}
			log.trace("import process {}", activity.getName());
			runImport(dataSet);
		} catch (Exception e) {
			log.error("Failed to import process", e);
		}
	}

	private boolean valid(DataSet dataSet) {
		Activity activity = dataSet.getActivity();
		if (activity.getId() == null || activity.getName() == null)
			return false;
		IntermediateExchange refFlow = null;
		for (IntermediateExchange techFlow : dataSet.getIntermediateExchanges()) {
			if (techFlow.getOutputGroup() == null)
				continue;
			if (techFlow.getOutputGroup() != 0)
				continue;
			refFlow = techFlow;
			break;
		}
		return refFlow != null;
	}

	private void runImport(DataSet dataSet) throws Exception {
		Activity activity = dataSet.getActivity();
		Process process = new Process();
		process.setRefId(activity.getId());
		process.setName(activity.getName());
		setCategory(dataSet, process);
		createProductExchanges(dataSet, process);
		createElementaryExchanges(dataSet, process);
		database.createDao(Process.class).insert(process);
		flushLinkQueue(process);
	}

	private void flushLinkQueue(Process process) {
		List<Exchange> exchanges = linkQueue.remove(process.getRefId());
		if (exchanges == null || process.getId() == 0)
			return;
		try {
			BaseDao<Exchange> dao = database.createDao(Exchange.class);
			for (Exchange exchange : exchanges) {
				exchange.setDefaultProviderId(process.getId());
				dao.update(exchange);
			}
		} catch (Exception e) {
			log.error("failed to update default provider", e);
		}
	}

	private void createElementaryExchanges(DataSet dataSet, Process process) {
		for (ElementaryExchange e : dataSet.getElementaryExchanges()) {
			if (e.getAmount() == 0)
				continue;
			Flow flow = index.getFlow(e.getElementaryExchangeId());
			if (flow == null) {
				log.warn("could not create flow for {}",
						e.getElementaryExchangeId());
			}
			createExchange(e, flow, process);
		}
	}

	private void createProductExchanges(DataSet dataSet, Process process) {
		for (IntermediateExchange e : dataSet.getIntermediateExchanges()) {
			if (e.getAmount() == 0)
				continue;
			Flow flow = index.getFlow(e.getIntermediateExchangeId());
			if (flow == null) {
				log.warn("could not create flow for {}",
						e.getIntermediateExchangeId());
				continue;
			}
			Exchange exchange = createExchange(e, flow, process);
			if (e.getActivityLinkId() != null)
				addActivityLink(e, exchange);
			if (e.getOutputGroup() != null && e.getOutputGroup() == 0)
				process.setQuantitativeReference(exchange);
		}
	}

	private Exchange createExchange(org.openlca.ecospold2.Exchange original,
			Flow flow, Process process) {
		if (flow == null || flow.getReferenceFlowProperty() == null) {
			log.warn("invalid exchange {}; not imported", original);
			return null;
		}
		Unit unit = index.getUnit(original.getUnitId());
		Exchange exchange = new Exchange();
		exchange.setInput(original.getInputGroup() != null);
		exchange.setFlow(flow);
		exchange.setFlowPropertyFactor(flow.getReferenceFactor());
		exchange.setUnit(unit);
		exchange.getResultingAmount().setValue(original.getAmount());
		exchange.getResultingAmount().setFormula(
				Double.toString(original.getAmount()));
		process.getExchanges().add(exchange);
		return exchange;
	}

	private void addActivityLink(IntermediateExchange e, Exchange exchange) {
		String providerId = e.getActivityLinkId();
		Long processId = index.getProcessId(providerId);
		if (processId != null) {
			exchange.setDefaultProviderId(processId);
			return;
		}
		List<Exchange> exchanges = linkQueue.get(providerId);
		if (exchanges == null) {
			exchanges = new ArrayList<>();
			linkQueue.put(providerId, exchanges);
		}
		exchanges.add(exchange);
	}

	private void setCategory(DataSet dataSet, Process process) throws Exception {
		Category category = null;
		for (Classification clazz : dataSet.getClassifications()) {
			category = index.getProcessCategory(clazz.getClassificationId());
			if (category != null)
				break;
		}
		process.setCategory(category);
	}

}