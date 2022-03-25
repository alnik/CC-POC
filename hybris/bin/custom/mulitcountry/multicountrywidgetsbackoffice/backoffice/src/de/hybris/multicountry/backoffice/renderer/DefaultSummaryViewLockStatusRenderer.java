package de.hybris.multicountry.backoffice.renderer;

import static com.hybris.multicountry.widgets.backoffice.constants.MulticountrywidgetsbackofficeConstants.Labels
		.LOCK_STATUS_PREFIX;

import de.hybris.multicountry.backoffice.services.ProductLockStatusService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;

import com.hybris.cockpitng.config.summaryview.jaxb.Attribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.summaryview.renderer.AbstractSummaryViewItemWithIconRenderer;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class DefaultSummaryViewLockStatusRenderer extends AbstractSummaryViewItemWithIconRenderer<ItemModel>
{
	private static final String LOCK_STATUS_CLASS = "lock-status";

	private ProductLockStatusService productLockStatusService;


	@Override
	protected boolean canHandle(final ItemModel data, final DataType dataType)
	{
		return data instanceof ProductModel && dataType != null;
	}

	@Override
	protected boolean hasPermission(final ItemModel data, final DataType dataType)
	{
		return this.getPermissionFacade().canReadInstanceProperty(data, ProductModel.LOCKEDBY);
	}

	@Override
	protected String getIconStatusSClass(final HtmlBasedComponent iconContainer, final Attribute attributeConfiguration,
			final ItemModel data, final DataAttribute dataAttribute, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		return this.getIconStatusSClass(LOCK_STATUS_CLASS, getProductLockStatusService().getStatusText((ProductModel) data));
	}

	@Override
	protected void renderValue(final Div attributeContainer, final Attribute attributeConfiguration, final ItemModel data,
			final DataAttribute dataAttribute, final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		attributeContainer.appendChild(this.createLockStatusValue((ProductModel) data));
	}

	private Label createLockStatusValue(final ProductModel data)
	{
		// get the nice descriptive label text for the lock status status
		final String statusText = LOCK_STATUS_PREFIX.concat(getProductLockStatusService().getStatusText(data));
		return new Label(Labels.getLabel(statusText));
	}

	@Required
	public void setProductLockStatusService(final ProductLockStatusService productLockStatusService)
	{
		this.productLockStatusService = productLockStatusService;
	}

	public ProductLockStatusService getProductLockStatusService()
	{
		return productLockStatusService;
	}
}