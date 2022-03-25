package de.hybris.contextualattributevaluesbackoffice.editors;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.util.ContextualAttributeValuesUtil;

import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.widgets.editorarea.renderer.EditAvailabilityProvider;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualAttributeValueEditAvailabilityProvider implements EditAvailabilityProvider<ContextualAttributeValueModel>
{
	@Override
	public boolean isAllowedToEdit( final DataAttribute dataAttribute, final ContextualAttributeValueModel contextualAttributeValueModel )
	{
		return ContextualAttributeValuesUtil.canEditContextualAttributeValue( contextualAttributeValueModel );
	}
}
