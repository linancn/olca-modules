package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class ProcessWriter extends Writer<Process> {

	private Process process;

	ProcessWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	JsonObject write(Process p) {
		JsonObject obj = super.write(p);
		if (obj == null)
			return null;
		this.process = p;
		// AllocationCleanup.on(p);
		Json.put(obj, "processType", p.processType);
		Json.put(obj, "defaultAllocationMethod", p.defaultAllocationMethod);
		Json.put(obj, "isInfrastructureProcess", p.infrastructureProcess);
		Json.put(obj, "location", exp.handleRef(p.location));
		Json.put(obj, "processDocumentation", Util.mapDocOf(p, exp));
		Json.put(obj, "dqSystem", exp.handleRef(p.dqSystem));
		Json.put(obj, "dqEntry", p.dqEntry);
		Json.put(obj, "exchangeDqSystem", exp.handleRef(p.exchangeDqSystem));
		Json.put(obj, "socialDqSystem", exp.handleRef(p.socialDqSystem));
		Json.put(obj, "lastInternalId", p.lastInternalId);
		mapParameters(obj);
		mapExchanges(obj);
		mapSocialAspects(obj);
		mapAllocationFactors(obj);
		GlobalParameters.sync(p, exp);
		return obj;
	}

	private void mapParameters(JsonObject json) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : process.parameters) {
			var obj = new JsonObject();
			ParameterWriter.mapAttr(obj, p);
			parameters.add(obj);
		}
		Json.put(json, "parameters", parameters);
	}

	private void mapExchanges(JsonObject json) {
		var array = new JsonArray();
		for (var e : process.exchanges) {
			var obj = new JsonObject();
			map(e, obj);
			if (Objects.equals(process.quantitativeReference, e)) {
				Json.put(obj, "isQuantitativeReference", true);
			}
			array.add(obj);
		}
		Json.put(json, "exchanges", array);
	}

	private void mapSocialAspects(JsonObject json) {
		JsonArray aspects = new JsonArray();
		for (SocialAspect a : process.socialAspects) {
			JsonObject obj = new JsonObject();
			Json.put(obj, "socialIndicator", exp.handleRef(a.indicator));
			Json.put(obj, "comment", a.comment);
			Json.put(obj, "quality", a.quality);
			Json.put(obj, "rawAmount", a.rawAmount);
			Json.put(obj, "activityValue", a.activityValue);
			Json.put(obj, "riskLevel", a.riskLevel);
			Json.put(obj, "source", exp.handleRef(a.source));
			aspects.add(obj);
		}
		Json.put(json, "socialAspects", aspects);
	}

	private void mapAllocationFactors(JsonObject json) {
		JsonArray factors = new JsonArray();
		for (AllocationFactor f : process.allocationFactors) {
			var obj = new JsonObject();
			Json.put(obj, "allocationType", f.method);
			if (f.method == AllocationMethod.CAUSAL) {
				Json.put(obj, "exchange", createExchangeRef(f.exchange));
			}
			Json.put(obj, "product", exp.handleRef(findProduct(f.productId)));
			Json.put(obj, "value", f.value);
			Json.put(obj, "formula", f.formula);
			factors.add(obj);
		}
		Json.put(json, "allocationFactors", factors);
	}

	private Flow findProduct(long id) {
		for (Exchange e : process.exchanges)
			if (e.flow.id == id)
				return e.flow;
		return null;
	}

	private JsonObject createExchangeRef(Exchange exchange) {
		if (exchange == null)
			return null;
		JsonObject obj = new JsonObject();
		Json.put(obj, "@type", Exchange.class.getSimpleName());
		Json.put(obj, "internalId", exchange.internalId);
		Json.put(obj, "flow", exp.handleRef(exchange.flow));
		return obj;
	}

	private void map(Exchange e, JsonObject obj) {

		Json.put(obj, "@type", Exchange.class.getSimpleName());
		Json.put(obj, "isAvoidedProduct", e.isAvoided);
		Json.put(obj, "isInput", e.isInput);
		Json.put(obj, "baseUncertainty", e.baseUncertainty);
		Json.put(obj, "amount", e.amount);
		Json.put(obj, "amountFormula", e.formula);
		Json.put(obj, "dqEntry", e.dqEntry);
		Json.put(obj, "description", e.description);
		Json.put(obj, "costFormula", e.costFormula);
		Json.put(obj, "costValue", e.costs);
		Json.put(obj, "currency", exp.handleRef(e.currency));
		Json.put(obj, "internalId", e.internalId);
		Json.put(obj, "location", exp.handleRef(e.location));

		Json.put(obj, "flow", exp.handleRef(e.flow));
		Json.put(obj, "unit", Json.asRef(e.unit));
		var property = e.flowPropertyFactor != null
			? e.flowPropertyFactor.flowProperty
			: null;
		Json.put(obj, "flowProperty", exp.handleRef(property));
		Json.put(obj, "uncertainty", Uncertainties.map(e.uncertainty));

		// default provider
		if (e.defaultProviderId == 0L || exp.db == null)
			return;

		if (exp.exportProviders) {
			Json.put(obj, "defaultProvider",
				exp.handleRef(ModelType.PROCESS, e.defaultProviderId));
		} else {
			var d = new ProcessDao(exp.db).getDescriptor(e.defaultProviderId);
			Json.put(obj, "defaultProvider", Json.asRef(d));
		}
	}

}
