package org.openlca.core.database.references;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;

public class FlowReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.FLOW;
	}

	@Override
	protected Flow createModel() {
		Flow flow = new Flow();
		flow.setCategory(addExpected(new Category()));
		flow.setLocation(addExpected(new Location()));
		flow.getFlowPropertyFactors().add(createFlowPropertyFactor());
		flow.getFlowPropertyFactors().add(createFlowPropertyFactor());
		return flow;
	}

	private FlowPropertyFactor createFlowPropertyFactor() {
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(addExpected(new FlowProperty()));
		return factor;
	}

}
