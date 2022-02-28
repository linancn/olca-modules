package org.openlca.jsonld.upgrades;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Upgrade2 extends Upgrade {

	private List<JsonObject> _rawImpactMethods;
	private List<JsonObject> _rawNwSets;
	private final PathBuilder categories;

	Upgrade2(JsonStoreReader reader) {
		super(reader);
		categories = PathBuilder.of(reader);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		var object = super.get(type, refId);
		if (object == null)
			return null;

		// replace category references with paths
		var categoryId = Json.getRefId(object, "category");
		if (categoryId != null) {
			var path = categories.getPath(categoryId);
			Json.put(object, "category", path);
		}

		if (type == ModelType.IMPACT_METHOD) {
			inlineNwSets(object);
		}
		if (type == ModelType.IMPACT_CATEGORY) {
			addImpactCategoryParams(object);
		}
		if (type == ModelType.PRODUCT_SYSTEM) {
			addRedefSets(object);
		}
		if (type == ModelType.PROCESS) {
			renameBool(object, "infrastructureProcess" ,"isInfrastructureProcess");
			fixExchanges(object);
		}
		if (type == ModelType.FLOW) {
			renameBool(object, "infrastructureFlow" ,"isInfrastructureFlow");
		}
		return object;
	}

	private void inlineNwSets(JsonObject methodObj) {
		if (methodObj == null)
			return;

		var nwRefs = Json.getArray(methodObj, "nwSets");
		if (nwRefs == null || nwRefs.isEmpty())
			return;

		if (_rawNwSets == null) {
			_rawNwSets = super.getFiles("nw_sets").stream()
				.map(super::getJson)
				.filter(Objects::nonNull)
				.filter(JsonElement::isJsonObject)
				.map(JsonElement::getAsJsonObject)
				.toList();
		}

		var idx = new HashMap<String, JsonObject>();
		for (var nwSet : _rawNwSets) {
			var id = Json.getString(nwSet, "@id");
			if (id == null)
				continue;
			idx.put(id, nwSet);
		}
		if (idx.isEmpty())
			return;

		var nwSets = new JsonArray();
		Json.stream(nwRefs)
			.filter(JsonElement::isJsonObject)
			.map(ref -> Json.getString(ref.getAsJsonObject(), "@id"))
			.filter(Objects::nonNull)
			.map(idx::get)
			.filter(Objects::nonNull)
			.map(JsonObject::deepCopy)
			.forEach(nwSets::add);
		methodObj.add("nwSets", nwSets);
	}

	private void addImpactCategoryParams(JsonObject object) {
		var id = Json.getString(object, "@id");
		if (id == null)
			return;

		// check if there are already parameters
		var params = Json.getArray(object, "parameters");
		if (params != null)
			return;

		// copy possible method parameters into the impact category
		var methodObj = getMethodOfImpact(id);
		if (methodObj != null) {
			var methodParams = Json.getArray(methodObj, "parameters");
			var impactParams = new JsonArray();
			if (methodParams != null) {
				// TODO: maybe update the parameters
				Json.stream(methodParams)
					.filter(JsonElement::isJsonObject)
					.map(JsonElement::getAsJsonObject)
					.map(JsonObject::deepCopy)
					.forEach(impactParams::add);
			}
			object.add("parameters", impactParams);
		}
	}

	private JsonObject getMethodOfImpact(String impactId) {

		if (_rawImpactMethods == null) {
			_rawImpactMethods = super.getRefIds(ModelType.IMPACT_METHOD)
				.stream()
				.map(methodId -> super.get(ModelType.IMPACT_METHOD, methodId))
				.filter(Objects::nonNull)
				.toList();
		}

		for (var methodObj : _rawImpactMethods) {
			var impRefs = Json.getArray(methodObj, "impactCategories");
			if (impRefs == null)
				continue;
			for (var impRef : impRefs) {
				if (!impRef.isJsonObject())
					continue;
				var impId = Json.getString(impRef.getAsJsonObject(), "@id");
				if (Objects.equals(impactId, impId))
					return methodObj;
			}
		}
		return null;
	}

	private void addRedefSets(JsonObject systemObj) {

		// check of there are already paramater sets
		var currentSets = Json.getArray(systemObj, "parameterSets");
		if (currentSets != null)
			return;

		var params = Json.getArray(systemObj, "parameterRedefs");
		if (params == null)
			return;

		var set = new JsonObject();
		set.addProperty("name", "Baseline");
		set.addProperty("isBaseline", true);
		var redefs = new JsonArray();
		Json.stream(params)
			.filter(JsonElement::isJsonObject)
			.map(JsonElement::getAsJsonObject)
			.map(JsonObject::deepCopy)
			.forEach(redefs::add);
		set.add("parameters", redefs);

		var redefSets = new JsonArray();
		redefSets.add(set);
		systemObj.add("parameterSets", redefSets);
	}

	private void fixExchanges(JsonObject object) {
		var array = Json.getArray(object, "exchanges");
		if (array == null)
			return;
		for (var elem : array) {
			if (!elem.isJsonObject())
				continue;
			var exchange = elem.getAsJsonObject();
			renameBool(exchange, "avoidedProduct", "isAvoidedProduct");
			renameBool(exchange, "input", "isInput");
			renameBool(exchange, "quantitativeReference", "isQuantitativeReference");
		}
	}

	private void renameBool(JsonObject obj, String oldName, String newName) {
		var newVal = Json.getBool(obj, newName, false);
		if (newVal)
			return; // the new field is already set to true
		var val = Json.getBool(obj, oldName, false);
		Json.put(obj, newName, val);
	}

	private record PathBuilder(
		Map<String, String> names,
		Map<String, String> parents,
		Map<String, String> paths) {


		static PathBuilder of(JsonStoreReader reader) {
			var names = new HashMap<String, String>();
			var parents = new HashMap<String, String>();
			for (var file : reader.getFiles("categories")) {
				var json = reader.getJson(file);
				if (json == null || !json.isJsonObject())
					continue;
				var obj = json.getAsJsonObject();
				var id = Json.getString(obj, "@id");
				if (id == null)
					continue;
				var name = Json.getString(obj, "name");
				if (name != null) {
					names.put(id, name);
				}
				var parentId = Json.getRefId(obj, "category");
				if (parentId != null) {
					parents.put(id, parentId);
				}
			}
			var paths = new HashMap<String, String>();
			return new PathBuilder(names, parents, paths);
		}

		String getPath(String categoryId) {
			if (categoryId == null)
				return null;
			var cached = paths.get(categoryId);
			if (cached != null)
				return cached;
			var buffer = new StringBuilder();
			var nextId = categoryId;
			do {
				var name = names.get(nextId);
				if (buffer.length() > 0) {
					buffer.insert(0, name + "/");
				} else {
					buffer.append(name);
				}
				nextId = parents.get(nextId);
			} while (nextId != null);
			if (buffer.isEmpty())
				return null;
			var path = buffer.toString();
			paths.put(categoryId, path);
			return path;
		}
	}
}
