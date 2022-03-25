/**
 *
 */
package com.hybris.cockpitng.editor.multicountryextendedmultireferenceeditor;

import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.util.CountryManagerUtil;

import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listitem;

import com.hybris.cockpitng.editor.commonreferenceeditor.AbstractReferenceEditor;
import com.hybris.cockpitng.editor.extendedmultireferenceeditor.renderer.DefaultRowRenderer;
import com.hybris.cockpitng.editors.EditorContext;


/**
 * @author i304602
 *
 */
public class McRowRenderer<T> extends DefaultRowRenderer<T>
{

	public McRowRenderer(final AbstractReferenceEditor parentEditor, final ListModelList selectedElementsListModel,
			final String editorProperty)
	{
		super(parentEditor, selectedElementsListModel, editorProperty);
	}

	@Override
	public void render(final Listitem row, final T entry, final int rowIndex)
	{
		if (!(entry instanceof ProductAvailabilityAssignmentModel))
		{
			super.render(row, entry, rowIndex);
			return;
		}

		final ProductAvailabilityAssignmentModel availability = (ProductAvailabilityAssignmentModel) entry;
		if (CountryManagerUtil.canEditAvailabilityProduct(availability))
		{
			super.render(row, entry, rowIndex);
			return;
		}

		final EditorContext context = getContext();
		final Object oldValue = context.getParameters().get(AbstractReferenceEditor.PARAM_DISABLE_REMOVE_REFERENCE);

		context.getParameters().put(AbstractReferenceEditor.PARAM_DISABLE_REMOVE_REFERENCE, Boolean.TRUE);
		super.render(row, entry, rowIndex);

		if (oldValue == null)
		{
			context.getParameters().remove(AbstractReferenceEditor.PARAM_DISABLE_REMOVE_REFERENCE);
		}
		else
		{
			context.getParameters().put(AbstractReferenceEditor.PARAM_DISABLE_REMOVE_REFERENCE, oldValue);
		}



	}




}
