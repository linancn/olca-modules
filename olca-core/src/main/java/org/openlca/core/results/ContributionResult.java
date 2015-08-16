package org.openlca.core.results;

import java.util.List;

import org.openlca.core.math.IMatrix;
import org.openlca.core.matrix.LongPair;

/**
 * A contribution result extends a simple result and contains all single
 * contributions of processes and products to the overall inventory and impact
 * assessment results. Additionally, it contains the contributions of the single
 * inventory flows to impact category results.
 */
public class ContributionResult extends SimpleResult {

	public double[] scalingFactors;

	/**
	 * This is a matrix with single flow results where the flows are mapped to
	 * the rows and the process-products to the columns. Inputs have negative
	 * values here.
	 */
	public IMatrix singleFlowResults;

	/**
	 * The single LCIA category results in a matrix where the LCIA categories
	 * are mapped to the rows and the process-products to the columns.
	 */
	public IMatrix singleImpactResults;

	/**
	 * The single LCIA category result for each intervention flow in a matrix
	 * where the LCIA categories are mapped to the rows and the intervention
	 * flows to the columns.
	 */
	public IMatrix singleFlowImpacts;

	/**
	 * Contains the characterization factors in a matrix where the LCIA
	 * categories are mapped to rows and elementary flows to columns.
	 */
	public IMatrix impactFactors;

	/**
	 * The contributions of the product-links in the scaled product system.
	 */
	public LinkContributions linkContributions;

	/**
	 * Get the scaling factor of the given process-product.
	 */
	public double getScalingFactor(LongPair processProduct) {
		int idx = productIndex.getIndex(processProduct);
		if (idx < 0 || idx > scalingFactors.length)
			return 0;
		return scalingFactors[idx];
	}

	/**
	 * Get the sum of all scaling factors for the products of the process with
	 * the given ID.
	 */
	public double getScalingFactor(long processId) {
		double factor = 0;
		List<LongPair> productIds = productIndex.getProducts(processId);
		for (LongPair product : productIds) {
			int idx = productIndex.getIndex(product);
			if (idx < 0 || idx > scalingFactors.length)
				continue;
			factor += scalingFactors[idx];
		}
		return factor;
	}

	/**
	 * Get the single flow result of the flow with the given ID for the given
	 * process-product. Inputs have negative values here.
	 */
	public double getSingleFlowResult(LongPair processProduct, long flowId) {
		int row = flowIndex.getIndex(flowId);
		int col = productIndex.getIndex(processProduct);
		return getValue(singleFlowResults, row, col);
	}

	/**
	 * Get the sum of the single flow results of the flow with the given ID for
	 * all products of the process with the given ID. Inputs have negative
	 * values here.
	 */
	public double getSingleFlowResult(long processId, long flowId) {
		int row = flowIndex.getIndex(flowId);
		return getProcessValue(singleFlowResults, row, processId);
	}

	/**
	 * Get the single LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 */
	public double getSingleImpactResult(LongPair processProduct,
			long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		int col = productIndex.getIndex(processProduct);
		return getValue(singleImpactResults, row, col);
	}

	/**
	 * Get the sum of the single LCIA category results of the LCIA category with
	 * the given ID for all products of the process with the given ID.
	 */
	public double getSingleImpactResult(long processId, long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		return getProcessValue(singleImpactResults, row, processId);
	}

	/**
	 * Get the single LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 */
	public double getSingleFlowImpact(long flowId, long impactId) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.getIndex(impactId);
		int col = flowIndex.getIndex(flowId);
		return getValue(singleFlowImpacts, row, col);
	}

	protected double getProcessValue(IMatrix matrix, int row, long processId) {
		if (matrix == null)
			return 0;
		double colSum = 0;
		for (LongPair product : productIndex.getProducts(processId)) {
			int col = productIndex.getIndex(product);
			colSum += getValue(matrix, row, col);
		}
		return colSum;
	}

	protected double getValue(IMatrix matrix, int row, int col) {
		if (matrix == null)
			return 0d;
		if (row < 0 || row >= matrix.getRowDimension())
			return 0d;
		if (col < 0 || col >= matrix.getColumnDimension())
			return 0d;
		return matrix.getEntry(row, col);
	}

}
