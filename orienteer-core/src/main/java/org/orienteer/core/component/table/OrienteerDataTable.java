package org.orienteer.core.component.table;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.behavior.UpdateOnActionPerformedEventBehavior;
import org.orienteer.core.component.ICommandsSupportComponent;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.component.meta.AbstractMetaPanel;
import org.orienteer.core.component.meta.IMetaContext;
import org.orienteer.core.component.table.navigation.OrienteerNavigationToolbar;
import org.orienteer.core.event.ActionPerformedEvent;

/**
 * Bootstrap enabled {@link DataTable}
 *
 * @param <T>
 *            the type of an table objects
 * @param <S>
 *            the type of the sorting parameter
 */
public class OrienteerDataTable<T, S> extends DataTable<T, S> implements ICommandsSupportComponent<T>
{
	/**
	 * {@link Item} that allows every row to be an {@link IMetaContext}
	 *
	 * @param <T> the type of an table objects
	 * @param <C> the type of a criteria
	 */
	public static class MetaContextItem<T, C> extends Item<T> implements IMetaContext<C>
	{

		public MetaContextItem(String id, int index, IModel<T> model)
		{
			super(id, index, model);
			setOutputMarkupId(true);
		}

		@Override
		public MarkupContainer getContextComponent() {
			return this;
		}

		@Override
		public <K extends AbstractMetaPanel<?, C, ?>> K getMetaComponent(
				C critery) {
			return AbstractMetaPanel.getMetaComponent(this, critery);
		}
		
	}
	private static final long serialVersionUID = 1L;
	protected DataTableCommandsToolbar<T> commandsToolbar;
	protected AjaxFallbackHeadersToolbar<S> headersToolbar;
	protected OrienteerNavigationToolbar navigationToolbar;
	protected NoRecordsToolbar noRecordsToolbar;
	
	private IModel<String> captionModel;
	
	public OrienteerDataTable(String id, List<? extends IColumn<T, S>> columns,
			ISortableDataProvider<T, S> dataProvider, int rowsPerPage)
	{
		super(id, columns, dataProvider, rowsPerPage);
		addTopToolbar(commandsToolbar= new DataTableCommandsToolbar<T>(this));
		addTopToolbar(headersToolbar = new AjaxFallbackHeadersToolbar<S>(this, dataProvider));
		addBottomToolbar(navigationToolbar = new OrienteerNavigationToolbar(this));
		addBottomToolbar(noRecordsToolbar = new NoRecordsToolbar(this));
		setOutputMarkupPlaceholderTag(true);
		setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
		add(UpdateOnActionPerformedEventBehavior.INSTANCE_ALL_STOP);
	}

	public DataTableCommandsToolbar<T> getCommandsToolbar() {
		return commandsToolbar;
	}
	
	public HeadersToolbar<S> getHeadersToolbar() {
		return headersToolbar;
	}
	
	public NoRecordsToolbar getNoRecordsToolbar()
	{
		return noRecordsToolbar;
	}

	@Override
	public OrienteerDataTable<T, S> addCommand(Command<T> command)
	{
		commandsToolbar.addCommand(command);
		return this;
	}
	
	@Override
	public OrienteerDataTable<T, S> removeCommand(Command<T> command) {
		commandsToolbar.removeCommand(command);
		return this;
	}

	@Override
	public String newCommandId() {
		return commandsToolbar.newCommandId();
	}

	@Override
	public void onEvent(IEvent<?> event) {
		
		if(Broadcast.BUBBLE.equals(event.getType())) {
			Object payload = event.getPayload();
			AjaxRequestTarget target=null;
			if(payload instanceof AjaxRequestTarget) target=(AjaxRequestTarget) payload;
			else if(payload instanceof ActionPerformedEvent) {
				target = ((ActionPerformedEvent<?>)payload).getTarget();
				//This is work around: wicket sometimes invoke model.getObject() before action
				//and if action change model table can display wrong information
				getDataProvider().detach();
			}
			
			/*if(target!=null) {
				target.add(this);
				onAjaxUpdate(target);
				if(target.equals(payload)) event.stop();
			}*/
		}
	}
	
	/*public void onAjaxUpdate(AjaxRequestTarget target)
	{
	}*/

	@Override
	public IModel<String> getCaptionModel() {
		if(captionModel==null)
		{
			captionModel = Model.of("");
		}
		return captionModel;
	}
	
	public OrienteerDataTable<T, S> setCaptionModel(IModel<String> captionModel) {
		get("caption").setDefaultModel(captionModel);
		this.captionModel = captionModel;
		return this;
	}

	@Override
	public void detachModels() {
		super.detachModels();
		if(captionModel!=null) captionModel.detach();
	}

	@Override
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
	{
		return new MetaContextItem<T, Object>(id, index, model);
	}
	
}
