package de.hybris.multicountry.backoffice.populators;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.tree.model.CatalogTreeModelPopulator;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.model.ComponentModelPopulator;
import com.hybris.cockpitng.tree.node.DynamicNode;
import com.hybris.cockpitng.tree.node.DynamicNodePopulator;
import com.hybris.cockpitng.widgets.common.explorertree.model.RefreshableTreeModel;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;

import java.util.*;
import java.util.stream.Collectors;

public class ExtendedCatalogTreeModelPopulator extends CatalogTreeModelPopulator implements ComponentModelPopulator<TreeModel<TreeNode<ItemModel>>>, DynamicNodePopulator {

    private BaseStoreService baseStoreService;

    private static final Logger LOG = LoggerFactory.getLogger(ExtendedCatalogTreeModelPopulator.class);

    @Override
    public TreeModel<TreeNode<ItemModel>> createModel(CockpitContext cockpitContext) {
        ExtendedCatalogTreeModelPopulator.ExtCatalogTreeModel model = new ExtendedCatalogTreeModelPopulator.ExtCatalogTreeModel(this.getRoot(cockpitContext), cockpitContext);
        if (cockpitContext.containsParameter(MULTI_SELECT)) {
            model.setMultiple(cockpitContext.getParameterAsBoolean(MULTI_SELECT, false));
        }

        return model;
    }

    @Override
    protected List<NavigationNode> findChildrenNavigationNodes(final NavigationNode node)
    {
        final Object nodeData = node.getData();
        if (nodeData instanceof BaseStoreModel) {
            LOG.debug("findChildrenNavigationNodes for store :" + ((BaseStoreModel)nodeData).getName());
            return prepareCatalogVersionNodes(node, getAllReadableCatalogs(node.getContext(), (BaseStoreModel) nodeData));
        }
        return super.findChildrenNavigationNodes(node);
    }

    protected List<NavigationNode> prepareCatalogVersionNodes(final NavigationNode node, final Collection<CatalogModel> nodeData)
    {
        final List<NavigationNode> nodes = new ArrayList<>();
        for (final CatalogModel catalogModel : nodeData)  {
            nodes.addAll(prepareCatalogVersionNodes(node, catalogModel));
        }
        return nodes;
    }

    @Override
    public List<NavigationNode> getChildren(NavigationNode navigationNode) {
        if (!(navigationNode instanceof DynamicNode)) {
            throw new IllegalArgumentException("Only Dynamic Nodes are supported");
        } else {
            if (navigationNode.getData() instanceof CatalogModel){
                CatalogModel catalog = (CatalogModel)navigationNode.getData();
                LOG.debug("Base store count for catalog :" + catalog.getName() + " - " + catalog.getBaseStores().size());
                if (catalog.getBaseStores().size() > 1) {
                    final List baseStoreNodes = new ArrayList(catalog.getBaseStores().size());
                    for (BaseStoreModel baseStore:catalog.getBaseStores()) {

                        int index = ((DynamicNode)navigationNode).getIndexingDepth() - 1;
                        DynamicNode dynamicNode = new DynamicNode(this.createDynamicNodeId(navigationNode, baseStore.getName()), this, index);
                        dynamicNode.setData(baseStore);
                        dynamicNode.setName(baseStore.getName());
                        dynamicNode.setContext(this.createCockpitContext(navigationNode));
                        dynamicNode.setSelectable(true);

                        findChildrenNavigationNodes(dynamicNode);
                        baseStoreNodes.add(dynamicNode);
                    }
                    return baseStoreNodes;
                }
            }
            return super.getChildren(navigationNode);
        }
    }

    private Collection<CatalogModel> getAllReadableCatalogs4BaseStore(final CockpitContext context, final BaseStoreModel baseStoreModel) {
        return baseStoreModel.getCatalogs().stream().filter(this.getPermissionFacade()::canReadInstance).filter((catalog) -> {
            return this.isSupportedType(this.getTypeFacade().getType(catalog));
        }).filter((catalog) -> {
            return this.isCatalogAvailableInContext(catalog, context);
        }).collect(Collectors.toList());
    }

    public Collection<BaseStoreModel> getAllAssignedBaseStores() {
        return filterBaseStoreOnEmployee(baseStoreService.getAllBaseStores());
    }

    protected List<BaseStoreModel> filterBaseStoreOnEmployee(final Collection<BaseStoreModel> baseStores) {
        final ArrayList<BaseStoreModel> toReturn = new ArrayList<BaseStoreModel>();
        for (final BaseStoreModel baseStore : baseStores) {
            if (checkEmployeeOnBaseStore(baseStore.getEmployees(), getUserService().getCurrentUser())) {
                toReturn.add(baseStore);
            }
        }
        return toReturn;
    }

    protected boolean checkEmployeeOnBaseStore(final Collection<EmployeeModel> employees, final UserModel currentUser) {
        for (final EmployeeModel employee : employees)  {
            if (getUserService().isAdmin(currentUser) || employee.equals(currentUser)) {
                return true;
            }
        }
        return false;
    }

    public Collection<CatalogModel> getAllReadableCatalogs(final CockpitContext context, final BaseStoreModel baseStoreModel) {
        return baseStoreModel.getCatalogs().stream().filter(this.getPermissionFacade()::canReadInstance).filter((catalog) -> {
            return this.isSupportedType(this.getTypeFacade().getType(catalog));
        }).filter((catalog) -> {
            return this.isCatalogAvailableInContext(catalog, context);
        }).collect(Collectors.toList());
    }

    public class ExtCatalogTreeModel extends AbstractTreeModel<TreeNode<ItemModel>> implements RefreshableTreeModel {

        private final CockpitContext context;

        private final transient LoadingCache<CatalogModel, List<CatalogVersionModel>> catalogVersionsCache = CacheBuilder.newBuilder().build(new CacheLoader<CatalogModel, List<CatalogVersionModel>>() {
            public List<CatalogVersionModel> load(CatalogModel data) {
                return ExtendedCatalogTreeModelPopulator.this.getAllReadableCatalogVersions(data);
            }
        });

        public ExtCatalogTreeModel(TreeNode<ItemModel> root, final CockpitContext context) {
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

        public int[] getPath(TreeNode<ItemModel> child) {
            Deque<Integer> indexes = new ArrayDeque();

            TreeNode parent;
            for(TreeNode current = child; current != null; current = parent) {
                parent = current.getParent();
                if (parent != null) {
                    int indexOfNode = parent.getChildren().indexOf(current);
                    if (indexOfNode < 0) {
                        break;
                    }

                    indexes.push(indexOfNode);
                }
            }

            return indexes.stream().mapToInt(Integer::intValue).toArray();
        }

        public void refreshChildren(Object node, List children) {
            this.catalogVersionsCache.invalidate(node);
        }

        public List findNodesByData(Object data) {
            throw new UnsupportedOperationException();
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
