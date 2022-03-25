/**
 *
 */
package de.hybris.multicountry.backoffice.strategies;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.HtmlBasedComponent;

import com.hybris.backoffice.cockpitng.dnd.DefaultDragAndDropStrategy;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dnd.SelectionSupplier;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class PermissionDragAndDropStrategy extends DefaultDragAndDropStrategy
{
	private PermissionFacade permissionFacade;


	@Override
	public void makeDraggable(final HtmlBasedComponent component, final Object businessObject, final CockpitContext dragContext)
	{
		if (getPermissionFacade().canChangeInstance(businessObject))
		{
			super.makeDraggable(component, businessObject, dragContext);
		}
	}

	@Override
	public void makeDraggable(final HtmlBasedComponent component, final Object businessObject, final CockpitContext dragContext,
			final SelectionSupplier selectionSupplier)
	{
		if (getPermissionFacade().canChangeInstance(businessObject))
		{
			super.makeDraggable(component, businessObject, dragContext, selectionSupplier);
		}
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}


	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}
}
