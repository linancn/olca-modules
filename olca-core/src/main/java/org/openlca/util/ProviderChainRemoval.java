package org.openlca.util;

import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ProviderChainRemoval {

	private final ProductSystem system;
	private final long ref;
	private final Map<Long, List<ProcessLink>> inLinks;
	private final Map<Long, List<ProcessLink>> outLinks;

	private ProviderChainRemoval(ProductSystem system) {
		this.system = system;
		this.ref = system.referenceProcess != null
				? system.referenceProcess.id
				: 0L;
		this.inLinks = new HashMap<>();
		this.outLinks = new HashMap<>();
		for (var link : system.processLinks) {
			inLinks.computeIfAbsent(
							link.processId, $ -> new ArrayList<>())
					.add(link);
			outLinks.computeIfAbsent(
							link.providerId, $ -> new ArrayList<>())
					.add(link);
		}
	}

	public static ProviderChainRemoval on(ProductSystem system) {
		return new ProviderChainRemoval(system);
	}

	/**
	 * Removes the given link from the product system and the sub-graph G with
	 * the provider of the link as root, if G is not connected to the reference
	 * process anymore. Returns the number of processes that were removed.
	 */
	public int remove(ProcessLink link) {
		if (link == null)
			return 0;
		removeLink(link);
		return link.providerId != ref
				? removeCluster(link.providerId)
				: 0;
	}

	private int removeCluster(long root) {
		var links = new ArrayList<ProcessLink>();
		var queue = new ArrayDeque<Long>();
		var handled = new HashSet<Long>();
		queue.add(root);
		while (!queue.isEmpty()) {
			var next = queue.poll();
			handled.add(next);

			// check outgoing links first because a
			// possible link to the reference process
			// could be found faster this way
			var outList = outLinks.get(next);
			if (outList != null) {
				for (var link : outList) {
					if (link.processId == ref)
						return 0;
					links.add(link);
					Long process = link.processId;
					if (!handled.contains(process)
							&& !queue.contains(process)) {
						queue.add(process);
					}
				}
			}

			var inList = inLinks.get(next);
			if (inList != null) {
				for (var link : inList) {
					if (link.providerId == ref)
						return 0;
					links.add(link);
					Long provider = link.providerId;
					if (!handled.contains(provider)
							&& !queue.contains(provider)) {
						queue.add(provider);
					}
				}
			}
		}

		system.processes.removeAll(handled);
		for (var link : links) {
			removeLink(link);
		}
		return handled.size();
	}

	private void removeLink(ProcessLink link) {
		system.processLinks.remove(link);
		var inList = inLinks.get(link.processId);
		if (inList != null) {
			inList.remove(link);
		}
		var outList = outLinks.get(link.providerId);
		if (outList != null) {
			outList.remove(link);
		}
	}
}
