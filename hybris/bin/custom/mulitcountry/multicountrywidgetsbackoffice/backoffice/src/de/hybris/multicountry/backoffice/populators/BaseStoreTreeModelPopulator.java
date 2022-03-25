/**
 *
 */
package de.hybris.multicountry.backoffice.populators;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.tree.model.AllCatalogsTreeNode;
import com.hybris.backoffice.tree.model.CatalogTreeModelPopulator;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.tree.node.DynamicNode;
import com.hybris.cockpitng.widgets.common.explorertree.data.PartitionNodeData;



/**
 * @author cyrill.pedol@sap.com
 *
 */
public class BaseStoreTreeModelPopulator extends CatalogTreeModelPopulator
{

	private BaseStoreService baseStoreService;

	@Override
	public TreeModel<TreeNode<ItemModel>> createModel(final CockpitContext context)
	{
		final BaseStoreTreeModel model = new BaseStoreTreeModel(getRoot(context), context);
		if (context.containsParameter(MULTI_SELECT))
		{
			model.setMultiple(context.getParameterAsBoolean(MULTI_SELECT, false));
		}

		return model;
	}

	@Override
	protected List<NavigationNode> findChildrenNavigationNodes(final NavigationNode node)
	{
		final Object nodeData = node.getData();
		return nodeData instanceof BaseStoreModel
				? prepareCatalogVersionNodes(node, getAllReadableCatalogs(node.getContext(), (BaseStoreModel) nodeData))
				: (nodeData instanceof CatalogVersionModel ? prepareRootCategoryNodes(node, (CatalogVersionModel) nodeData)
						: (nodeData instanceof CategoryModel ? prepareSubcategoryNodes(node, (CategoryModel) nodeData)
								: (nodeData instanceof PartitionNodeData ? ((PartitionNodeData) nodeData).getChildren()
										: prepareCatalogNodes(node))));
	}

	protected List<NavigationNode> prepareCatalogVersionNodes(final NavigationNode node, final Collection<CatalogModel> nodeData)
	{
		final List<NavigationNode> nodes = new ArrayList<>();
		for (final CatalogModel catalogModel : nodeData)
		{
			nodes.addAll(prepareCatalogVersionNodes(node, catalogModel));
		}
		return nodes;
	}

	@Override
	protected DynamicNode prepareAllCatalogsNode(final NavigationNode rootNode)
	{
		final DynamicNode allBaseStoresNode = new DynamicNode(this.createDynamicNodeId(rootNode, "allBaseStores"), (node) -> {
			return Collections.emptyList();
		}, 1);
		allBaseStoresNode.setSelectable(true);
		allBaseStoresNode.setName(Labels.getLabel("basestoretreemodelpopulator.allbasestores"));
		return allBaseStoresNode;
	}

	@Override
	public synchronized TreeNode<ItemModel> getRoot(final CockpitContext context)
	{
		final List nodes = getAllAssignedBaseStores().stream().map(DefaultTreeNode::new).collect(Collectors.toList());
		nodes.add(new AllCatalogsTreeNode((Object) null));
		return new DefaultTreeNode((Object) null, nodes);
	}

	public Collection<BaseStoreModel> getAllAssignedBaseStores()
	{
		return filterBaseStoreOnEmployee(getBaseStoreService().getAllBaseStores());
	}

	protected List<BaseStoreModel> filterBaseStoreOnEmployee(final Collection<BaseStoreModel> baseStores)
	{
		final ArrayList<BaseStoreModel> toReturn = new ArrayList<BaseStoreModel>();
		for (final BaseStoreModel baseStore : baseStores)
		{
			if (checkEmployeeOnBaseStore(baseStore.getEmployees(), getUserService().getCurrentUser()))
			{
				toReturn.add(baseStore);
			}
		}
		return toReturn;
	}

	protected boolean checkEmployeeOnBaseStore(final Collection<EmployeeModel> employees, final UserModel currentUser)
	{
		for (final EmployeeModel employee : employees)
		{
			if (getUserService().isAdmin(currentUser) || employee.equals(currentUser))
			{
				return true;
			}
		}
		return false;
	}


	public Collection<CatalogModel> getAllReadableCatalogs(final CockpitContext context, final BaseStoreModel baseStoreModel)
	{
		return baseStoreModel.getCatalogs().stream().filter(this.getPermissionFacade()::canReadInstance).filter((catalog) -> {
			return this.isSupportedType(this.getTypeFacade().getType(catalog));
		}).filter((catalog) -> {
			return this.isCatalogAvailableInContext(catalog, context);
		}).collect(Collectors.toList());
	}


	public class BaseStoreTreeModel extends AbstractTreeModel<TreeNode<ItemModel>>
	{
		private final CockpitContext context;

		public BaseStoreTreeModel(final TreeNode<ItemModel> root, final CockpitContext context)
		{
			super(root);
			this.context = context;
		}

		@Override
		public boolean isLeaf(final TreeNode<ItemModel> node)
		{
			if (node == this.getRoot())
			{
				return node.getChildCount() == 0;
			}
			else
			{
				final ItemModel data = node.getData();

				return data instanceof BaseStoreModel ? getAllReadableCatalogs(context, (BaseStoreModel) data).stream()
						.allMatch(catalog -> getAllReadableCatalogVersions(catalog).isEmpty()) : true;
			}
		}

		@Override
		public TreeNode<ItemModel> getChild(final TreeNode<ItemModel> node, final int i)
		{
			if (node == getRoot())
			{
				return node.getChildAt(i);
			}
			else
			{
				final ItemModel data = node.getData();
				return data instanceof BaseStoreModel ? new DefaultTreeNode(getAllReadableCatalogs(context, (BaseStoreModel) data)
						.stream().map(catalog -> getAllReadableCatalogVersions(catalog)).collect(Collectors.toList()).get(i)) : null;
			}
		}

		@Override
		public int getChildCount(final TreeNode<ItemModel> node)
		{
			if (node == this.getRoot())
			{
				return node.getChildren().size();
			}
			else
			{
				final ItemModel data = node.getData();
				return data instanceof BaseStoreModel ? getAllReadableCatalogs(context, (BaseStoreModel) data).stream()
						.map(catalog -> getAllReadableCatalogVersions(catalog)).collect(Collectors.toList()).size() : 0;
			}
		}
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}
}
