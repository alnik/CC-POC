package de.hybris.multicountry.backoffice.renderer;

import static com.hybris.multicountry.widgets.backoffice.constants.MulticountrywidgetsbackofficeConstants.Labels
		.LOCK_STATUS_PREFIX;

import de.hybris.multicountry.backoffice.services.ProductLockStatusService;
import de.hybris.platform.core.model.product.ProductModel;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Span;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class DefaultProductLockStatusRenderer extends AbstractWidgetComponentRenderer<Component, Object, ProductModel>
{
	private static final String YW_IMAGE_ATTRIBUTE_LOCK_STATUS = "yw-image-attribute-lock-status-";
	private static final String YW_IMAGE_ATTRIBUTE_ICON = "yw-image-attribute-lock-icon";
	private static final String LOCK_STATUS_LABEL = "product.tooltip.lock.status";

	private ProductLockStatusService productLockStatusService;


	@Override
	public void render(final Component parent, final Object configuration, final ProductModel data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final String sclass = YW_IMAGE_ATTRIBUTE_LOCK_STATUS.concat(getProductLockStatusService().getStatusText(data));
		final Span icon = new Span();
		UITools.addSClass(icon, sclass);
		UITools.addSClass(icon, YW_IMAGE_ATTRIBUTE_ICON);
		icon.setTooltiptext(this.getTooltipText(data));
		parent.appendChild(icon);
		this.fireComponentRendered(parent, configuration, data);
	}

	private String getTooltipText(final ProductModel data)
	{
		// get the nice descriptive label text for the lock status status
		final String statusText = Labels.getLabel(LOCK_STATUS_PREFIX.concat(getProductLockStatusService().getStatusText(data)));
		// get the complete tooltip label
		return Labels.getLabel(LOCK_STATUS_LABEL, new Object[]{ statusText });
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