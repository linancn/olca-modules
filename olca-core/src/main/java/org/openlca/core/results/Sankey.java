package org.openlca.core.results;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * An instance of this class contains the underlying graph data structure of
 * a Sankey diagram. It does not contain methods for rendering the diagram on
 * some output but just the data structures and functions for building the
 * graph.
 */
public class Sankey<T> {

	/**
	 * The result reference of the graph; a flow or impact category.
	 */
	public final T reference;

	/**
	 * The root (source or sink, depending on the perspective) of the graph
	 * which always describes the reference product of the underlying product
	 * system.
	 */
	public final Node root;

	/**
	 * The total number of nodes in the graph.
	 */
	public int nodeCount;

	/**
	 * Describes a single node in the graph. For a process product in the
	 * system there can be only one or no node in the resulting graph. Thus,
	 * a node can be uniquely identified by its process product (which can be
	 * also a waste input) and its index of the tech. matrix.
	 */
	public static class Node {

		/**
		 * The matrix index of corresponding process product.
		 */
		public int index;

		/**
		 * The process product of the node.
		 */
		public ProcessProduct product;

		/**
		 * The total result (upstream plus direct) of this node in the supply
		 * chain.
		 */
		public double total;

		/**
		 * The direct result of this node.
		 */
		public double direct;

		/**
		 * The absolute share of the total result of this node in relation to
		 * the total result of the root of the graph (of the reference product).
		 */
		public double share;

		/**
		 * The nodes of the providers of products or waste treatment of this
		 * node. Note that depending of the cutoffs when building the graph
		 * this list is often not complete.
		 */
		public List<Node> providers = new ArrayList<>();

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof Node))
				return false;
			var other = (Node) o;
			return index == other.index;
		}

		@Override
		public int hashCode() {
			return index;
		}
	}

	private Sankey(T reference) {
		this.reference = reference;
		this.root = new Node();
	}

	/**
	 * Creates a new builder of a graph for a Sankey diagram for the given
	 * reference (flow or impact) and result.
	 */
	public static <T> Builder<T> of(T ref, FullResult result) {
		return new Builder<>(ref, result);
	}

	/**
	 * Traverses the graph in breadth-first order starting from the root.
	 */
	public void traverse(Consumer<Node> fn) {
		if (fn == null)
			return;
		var queue = new ArrayDeque<Node>(root.providers.size());
		queue.add(root);
		var visited = new TIntHashSet(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
		visited.add(root.index);
		while (!queue.isEmpty()) {
			var node = queue.poll();
			fn.accept(node);
			for (var provider : node.providers) {
				if (visited.contains(provider.index))
					continue;
				queue.add(provider);
				visited.add(provider.index);
			}
		}
	}

	public static class Builder<T> {

		private final Sankey<T> sankey;
		private final FullResult result;

		// result references
		private IndexFlow flow;
		private ImpactCategoryDescriptor impact;

		// cutoff rules
		private double minShare = 0;
		private int maxNodes = -1;

		private final TIntObjectHashMap<Node> handled;
		private PriorityQueue<Candidate> candidates;

		private Builder(T ref, FullResult result) {
			this.sankey = new Sankey<>(ref);
			this.result = result;
			handled = new TIntObjectHashMap<>(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1);
		}

		/**
		 * The minimum share of the total result of a node in relation to the
		 * total result of the root of the graph (of the reference product) that
		 * is required for a node to be added to the graph.
		 */
		public Builder<T> withMinimumShare(double share) {
			this.minShare = Math.abs(share);
			return this;
		}

		/**
		 * The maximum number of nodes that should be added to the graph.
		 */
		public Builder<T> withMaximumNodeCount(int count) {
			this.maxNodes = count;
			return this;
		}

		/**
		 * Builds the underlying graph of a Sankey diagram.
		 */
		public Sankey<T> build() {

			// select the result reference
			// TODO: currently no support for cost-results
			if (sankey.reference instanceof IndexFlow) {
				flow = (IndexFlow) sankey.reference;
			} else if (sankey.reference instanceof ImpactCategoryDescriptor) {
				impact = (ImpactCategoryDescriptor) sankey.reference;
			}

			// create the root node of the reference product
			var root = sankey.root;
			root.product = result.techIndex.getRefFlow();
			root.index = result.techIndex.getIndex(root.product);
			root.total = getTotal(root.product);
			root.direct = getDirect(root.product);
			root.share = root.total == 0 ? 0 : 1;
			sankey.nodeCount = 1;
			handled.put(root.index, root);

			// expand the graph recursively
			if (root.total != 0 && (maxNodes < 0 || maxNodes > 1)) {
				expand(root);
			}

			return sankey;
		}

		private double getTotal(ProcessProduct product) {
			if (flow != null)
				return result.getUpstreamFlowResult(product, flow);
			if (impact != null)
				return result.getUpstreamImpactResult(product, impact);
			return 0;
		}

		private double getDirect(ProcessProduct product) {
			if (flow != null)
				return result.getDirectFlowResult(product, flow);
			if (impact != null)
				return result.getDirectImpactResult(product, impact);
			return 0;
		}

		/**
		 * Expands recursively the providers of the given node, that was already
		 * added to the graph, according to the cutoff rules of this builder.
		 */
		private void expand(Node node) {
			var colA = result.solutions.columnOfA(node.index);
			for (int i = 0; i < colA.length; i++) {
				if (i == node.index || colA[i] == 0)
					continue;
				var provider = handled.get(i);
				if (provider != null) {
					node.providers.add(provider);
					continue;
				}

				// calculate and check the share
				var product = result.techIndex.getProviderAt(i);
				var total = getTotal(product);
				if (total == 0)
					continue;
				var share = Math.abs(total / sankey.root.total);
				if (share < minShare)
					continue;

				// construct a candidate node
				provider = new Node();
				provider.index = i;
				provider.direct = getDirect(product);
				provider.total = total;
				provider.product = product;
				provider.share = share;

				// if there is no limit regarding the
				// node count, add and expand the node
				if (maxNodes < 0) {
					add(node, provider);
					expand(provider);
					continue;
				}

				// add is as a candidate
				if (candidates == null) {
					candidates = new PriorityQueue<>(
							(n1, n2) -> Double.compare(n2.share, n1.share));
				}
				candidates.add(new Candidate(node, provider));
			}

			// we add the candidate with the largest share to
			// the providers.
			if (candidates == null || candidates.isEmpty())
				return;
			var next = candidates.poll();
			add(next.handled, next.provider);
			if (sankey.nodeCount < maxNodes) {
				expand(next.provider);
			}
		}

		private void add(Node existing, Node provider) {
			existing.providers.add(provider);
			handled.put(provider.index, provider);
			sankey.nodeCount++;
		}
	}

	/**
	 * Describes a new provider candidate of a handled node that was already
	 * added to the graph. This candidate could be added in a next expansion
	 * step depending on the result share.
	 */
	private static class Candidate {

		/**
		 * The handled node that was already added to the graph.
		 */
		final Node handled;

		/**
		 * The provider node that could be added in a next expansion step.
		 */
		final Node provider;

		/**
		 * The result share of the provider candidate.
		 */
		final double share;

		Candidate(Node handled, Node provider) {
			this.handled = handled;
			this.provider = provider;
			this.share = provider.share;
		}
	}
}
