package de.hybris.contextualattributevaluesbackoffice.editorarea;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.util.ContextualAttributeValuesUtil;
import de.hybris.platform.core.model.product.ProductModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hybris.backoffice.editorarea.BackofficeEditorAreaLogicHandler;
import com.hybris.cockpitng.core.Executable;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualBackofficeEditorAreaLogicHandler extends BackofficeEditorAreaLogicHandler
{
	final Logger LOG = LogManager.getLogger( ContextualBackofficeEditorAreaLogicHandler.class );

	public void executeSaveWithConfirmation( WidgetInstanceManager wim, Executable save, Object currentObject )
	{
		if( currentObject instanceof ProductModel )
		{
			saveContextualAttributeValues( wim, (ProductModel) currentObject );
		}
		super.executeSaveWithConfirmation( wim, save, currentObject );
	}

	private void saveContextualAttributeValues( final WidgetInstanceManager wim, final ProductModel currentObject )
	{
		for( ContextualAttributeValueModel contextualAttributeValueModel: currentObject.getContextualAttributeValues() )
		{
			if( ContextualAttributeValuesUtil.canEditContextualAttributeValue( contextualAttributeValueModel )
					&& contextualAttributeValueModel.getItemModelContext().isDirty() )
			{
				try
				{
					super.performSave( wim, contextualAttributeValueModel );
				}
				catch( ObjectSavingException e )
				{
					LOG.error( "Cannot save ContextualAttributeValue with pk: " + contextualAttributeValueModel.getPk(), e );
				}
			}
		}
	}
}
