package org.openlca.git.iterator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.GitConfig;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

public class DatabaseIterator extends EntryIterator {

	private final GitConfig config;
	private final PathBuilder categoryPaths;

	public DatabaseIterator(GitConfig config) {
		this(config, init(config));
	}

	private DatabaseIterator(GitConfig config, List<TreeEntry> entries) {
		super(entries);
		this.config = config;
		this.categoryPaths = Categories.pathsOf(config.database);
	}

	private DatabaseIterator(DatabaseIterator parent, GitConfig config, List<TreeEntry> entries) {
		super(parent, entries);
		this.config = config;
		this.categoryPaths = Categories.pathsOf(config.database);
	}

	private static List<TreeEntry> init(GitConfig config) {
		return Arrays.stream(ModelType.rootTypes()).filter(type -> {
			if (type == ModelType.CATEGORY)
				return false;
			var dao = new CategoryDao(config.database);
			if (!dao.getRootCategories(type).isEmpty())
				return true;
			return !Daos.root(config.database, type).getDescriptors(Optional.empty()).isEmpty();
		}).map(TreeEntry::new)
				.toList();
	}

	private static List<TreeEntry> init(GitConfig config, ModelType type) {
		var entries = new CategoryDao(config.database).getRootCategories(type)
				.stream().map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(Daos.root(config.database, type).getDescriptors(Optional.empty())
				.stream().map(d -> new TreeEntry(d))
				.toList());
		return entries;
	}

	private static List<TreeEntry> init(GitConfig config, Category category) {
		var entries = category.childCategories
				.stream().map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(Daos.root(config.database, category.modelType).getDescriptors(Optional.of(category))
				.stream().map(d -> new TreeEntry(d))
				.toList());
		return entries;
	}

	@Override
	public boolean hasId() {
		if (config.store == null)
			return false;
		var data = getEntryData();
		if (data == null)
			return false;
		if (data instanceof ModelType)
			return config.store.has((ModelType) data);
		if (data instanceof Category)
			return config.store.has((Category) data);
		return config.store.has(categoryPaths, (RootDescriptor) data);
	}

	@Override
	public byte[] idBuffer() {
		if (config.store == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		var data = getEntryData();
		if (data == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		if (data instanceof ModelType)
			return config.store.getRaw((ModelType) data);
		if (data instanceof Category)
			return config.store.getRaw((Category) data);
		return config.store.getRaw(categoryPaths, (RootDescriptor) data);
	}

	@Override
	public int idOffset() {
		return 0;
	}

	@Override
	public AbstractTreeIterator createSubtreeIterator(ObjectReader reader) {
		var data = getEntryData();
		if (data instanceof ModelType type)
			return new DatabaseIterator(this, config, init(config, type));
		if (data instanceof Category category)
			return new DatabaseIterator(this, config, init(config, category));
		return null;
	}

}
