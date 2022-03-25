/**
 *
 */
package de.hybris.contextualattributevaluesbackoffice.widgets;

import de.hybris.contextualattributevaluesbackoffice.models.ContextModel;
import de.hybris.platform.contextualattributevalues.daos.ContextualAttributesDao;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.contextualattributevalues.services.ContextualAttributeValuesSessionService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Toolbarbutton;

import com.hybris.backoffice.widgets.userrolechooser.model.UserRoleModel;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * @author i307088
 *
 */
public class ContextSelectorWidgetController extends DefaultWidgetController
{
	private static final long serialVersionUID = 1L;

	public static final String CONTEXTS_LIST_COMPONENT = "contextsList";
	public static final String SOCKET_UPDATED_OBJECT = "updatedObject";
	public static final String SOCKET_SET_CURRENT_OBJECT = "setCurrentObject";

	private Listbox contextsList;
	private Toolbarbutton contextChooserBtn;
	private Popup contextChooser;

	private List<ContextualAttributesContextModel> contexts;
	private Object currentObject;

	@WireVariable
	private transient ContextualAttributeValuesSessionService contextualAttributeValuesSessionService;
	@WireVariable
	private transient ContextualAttributesDao contextualAttributesDao;
	@WireVariable
	private transient ModelService modelService;
	@WireVariable
	private transient CockpitEventQueue cockpitEventQueue;


	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream stream) throws java.io.IOException
	{
		throw new NotSerializableException(getClass().getName());
	}

	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException
	{
		throw new NotSerializableException(getClass().getName());
	}

	@SocketEvent(socketId = SOCKET_SET_CURRENT_OBJECT)
	public void setCurrentObject(final Object currentObject)
	{
		this.currentObject = currentObject;
	}

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		loadAvailableContexts();
		createContextsList();
	}

	protected void loadAvailableContexts()
	{
		contexts = contextualAttributesDao.findAllAvailableContexts();
	}

	protected ContextualAttributesContextModel findContextByCode(final String code)
	{
		return contextualAttributesDao.findContextByCode(code);
	}

	protected void createContextsList()
	{
		final ListModelList<ContextModel> listModel = new ListModelList<ContextModel>();

		if (contexts != null)
		{
			final ContextModel globalContextModel = new ContextModel();
			globalContextModel.setCode("global");
			// TODO: use a label
			globalContextModel.setName("Global context");
			globalContextModel.setGlobal(true);
			globalContextModel.setSelected(getContextualAttributeValuesSessionService().getCurrentContext() == null);
			listModel.add(globalContextModel);

			for (final ContextualAttributesContextModel context : contexts)
			{
				final ContextModel contextModel = new ContextModel();
				contextModel.setCode(context.getCode());
				contextModel.setName(context.getName());

				final ContextualAttributesContextModel activeContext = getContextualAttributeValuesSessionService()
						.getCurrentContext();
				if (activeContext != null && activeContext.getCode().equals(context.getCode()))
				{
					contextModel.setSelected(true);
				}
				listModel.add(contextModel);
			}
		}
		contextsList.setModel(listModel); // NOPMD
		contextChooserBtn.setLabel(currentContextName());
	}

	protected void changeActiveContext(final ContextModel active)
	{
		ContextualAttributesContextModel context = null;
		if (!active.isGlobal())
		{
			context = findContextByCode(active.getCode());
		}
		getContextualAttributeValuesSessionService().setCurrentContext(context);
		contextChooserBtn.setLabel(currentContextName());
		contextChooser.close();

		refreshUI();
	}

	protected void sendRedirect(final String target)
	{
		Executions.sendRedirect(target);
	}

	@ViewEvent(componentID = CONTEXTS_LIST_COMPONENT, eventName = Events.ON_SELECT)
	public void onSelectionChanged(final SelectEvent<Listitem, UserRoleModel> event)
	{
		final Listitem selectedItem = event.getReference();
		final ContextModel selectedContext = selectedItem.getValue();

		changeActiveContext(selectedContext);
	}

	public String currentContextName()
	{
		final ContextualAttributesContextModel current = getContextualAttributeValuesSessionService().getCurrentContext();
		if (current != null)
		{
			return current.getName();
		}
		else
		{
			// Use label
			return "Global";
		}
	}

	public Listbox getContextsList()
	{
		return contextsList;
	}

	/**
	 * Refresh the UI, as best as possible
	 */
	protected void refreshUI()
	{
		modelService.detachAll();
		if (currentObject != null)
		{
			final DefaultCockpitEvent event = new DefaultCockpitEvent(ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, currentObject, null);
			cockpitEventQueue.publishEvent(event);
			sendOutput(SOCKET_UPDATED_OBJECT, currentObject);
		}
	}

	/**
	 * @return the contextualAttributeValuesSessionService
	 */
	public ContextualAttributeValuesSessionService getContextualAttributeValuesSessionService()
	{
		return contextualAttributeValuesSessionService;
	}
}
