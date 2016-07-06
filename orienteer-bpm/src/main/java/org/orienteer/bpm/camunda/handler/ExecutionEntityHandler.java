package org.orienteer.bpm.camunda.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryValue;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.orienteer.bpm.camunda.OPersistenceSession;
import org.orienteer.core.util.OSchemaHelper;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.Operator;
import com.github.raymanrt.orientqb.query.Query;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import ru.ydn.wicket.wicketorientdb.utils.GetODocumentFieldValueFunction;

/**
 * {@link IEntityHandler} for {@link ExecutionEntity} 
 */
public class ExecutionEntityHandler extends AbstractEntityHandler<ExecutionEntity> {

	public ExecutionEntityHandler() {
		super("BPMExecution");
	}
	
	@Override
	public void applySchema(OSchemaHelper helper) {
		super.applySchema(helper);
		helper.oProperty("processInstanceId", OType.STRING, 10)
			  .oProperty("parentId", OType.STRING, 20)
			  .oProperty("processDefinitionId", OType.STRING, 30)
			  .oProperty("businessKey", OType.STRING, 30)
			  .oProperty("superExecutionId", OType.STRING, 40)
			  .oProperty("superCaseExecutionId", OType.STRING, 50)
			  .oProperty("caseInstanceId", OType.STRING, 60)
			  .oProperty("activityInstanceId", OType.STRING, 70)
			  .oProperty("activityId", OType.STRING, 80)
			  .oProperty("active", OType.BOOLEAN, 90)
			  .oProperty("concurrent", OType.BOOLEAN, 100)
			  .oProperty("scope", OType.BOOLEAN, 120)
			  .oProperty("eventScope", OType.BOOLEAN, 120)
			  .oProperty("suspensionState", OType.INTEGER, 140)
			  .oProperty("cachedEntityState", OType.INTEGER, 150)
			  .oProperty("sequenceCounter", OType.LONG, 160);
	}
	
	/*@Override
	public void delete(ExecutionEntity entity, OPersistenceSession session) {
		super.delete(entity, session);
		if(entity.isProcessInstanceExecution()) {
			logger.info("PROCESS DELITION: "+entity.getId(), new Exception());
		}
	}*/
	
	@Statement
	public List<ExecutionEntity> selectProcessInstanceByQueryCriteria(OPersistenceSession session, ProcessInstanceQueryImpl query) {
		return  query(session, query, new IQueryMangler() {
			
			@Override
			public Query apply(Query input) {
				input.where(Clause.clause("parentId", Operator.NULL, ""));
				return input;
			}
		});
	}
	
	@Statement
	public List<ExecutionEntity> selectExecutionsByQueryCriteria(final OPersistenceSession session, final ExecutionQueryImpl query) {
		/*List<ExecutionEntity> ret =  queryList(session, "SELECT FROM BPMExecution WHERE processInstanceId = ?", query.getProcessInstanceId());
		LOG.info("Ret!!: "+ret);
		return ret;*/
		List<ExecutionEntity> ret = query(session, query, new Function<Query, Query>() {
			
			@Override
			public Query apply(Query input) {
				SuspensionState state = query.getSuspensionState();
				if(state!=null) input.where(Clause.clause("suspensionState", Operator.EQ, state.getStateCode()));
				
				String businessKey = query.getBusinessKey();
				if(businessKey!=null) {
					List<ODocument> proc = session.getDatabase()
										.query(new OSQLSynchQuery<>("select processInstanceId from "+getSchemaClass()+" where businessKey=?", 1)
												, businessKey);
					if(proc!=null && !proc.isEmpty()) {
						String processInstanceId = proc.get(0).field("processInstanceId");
						input.where(Clause.clause("processInstanceId", Operator.EQ,  processInstanceId));
					}
				}
				return input;
			}
		},"suspensionState", "businessKey");
		
		List<EventSubscriptionQueryValue> subscriptionsQueries = query.getEventSubscriptions();
		
		if(subscriptionsQueries!=null && !subscriptionsQueries.isEmpty()) {
			ret = new ArrayList<>(ret);
			for(EventSubscriptionQueryValue sub : subscriptionsQueries) {
				ListIterator<ExecutionEntity> it = ret.listIterator();
				while(it.hasNext()) {
					ExecutionEntity entity = it.next();
					List<EventSubscriptionEntity> subscriptions = entity.getEventSubscriptions();
					boolean hasMatch = false;
					for(EventSubscriptionEntity subscription: subscriptions) {
						if((sub.getEventName()==null || sub.getEventName().equals(subscription.getEventName())) 
								&& (sub.getEventType()==null || sub.getEventType().equals(subscription.getEventType()))) {
							hasMatch = true;
						}
					}
					if(!hasMatch) it.remove();
				}
			}
		}
		
		List<QueryVariableValue> queryVariableValues = query.getQueryVariableValues();
		if(queryVariableValues!=null && !queryVariableValues.isEmpty()) {
			ret = new ArrayList<>(ret);
			for(QueryVariableValue queryValue : queryVariableValues) {
				ListIterator<ExecutionEntity> it = ret.listIterator();
				while(it.hasNext()) {
					ExecutionEntity entity = it.next();
					QueryOperator operator = queryValue.getOperator();
					Object value = entity.getVariable(queryValue.getName());
					Object refValue = queryValue.getValue();
					Comparable<Object> comparable = (value instanceof Comparable && refValue instanceof Comparable)?(Comparable<Object>)value:null;
					boolean hasMatch;
					switch (operator) {
						case GREATER_THAN:
							hasMatch = comparable!=null && comparable.compareTo(refValue)>0;
						case GREATER_THAN_OR_EQUAL:
							hasMatch = comparable!=null && comparable.compareTo(refValue)>=0;
						case LESS_THAN:
							hasMatch = comparable!=null && comparable.compareTo(refValue)<0;
						case LESS_THAN_OR_EQUAL:
							hasMatch = comparable!=null && comparable.compareTo(refValue)<=0;
						case LIKE:
							hasMatch = value!=null && Pattern.matches(refValue.toString(), value.toString());
							break;
						case NOT_EQUALS:
							hasMatch = !Objects.equals(value, refValue);
							break;
						case EQUALS:
						default:
							hasMatch = Objects.equals(value, refValue);
							break;
					}
					if(!hasMatch) it.remove();
				}
			}
		}
		
		return ret;
	}
	
	@Override
	public boolean hasNeedInCache() {
		return true;
	}
	
	@Statement
	public List<ExecutionEntity> selectExecutionsByProcessInstanceId(OPersistenceSession session, ListQueryParameterObject obj) {
		return queryList(session, "select from "+getSchemaClass()+" where processInstanceId = ?", obj.getParameter());
	}
	
	@Statement
	public List<ExecutionEntity> selectExecutionsByParentExecutionId(OPersistenceSession session, ListQueryParameterObject parameter) {
		return queryList(session, "select from "+getSchemaClass()+" where parentId = ?", parameter.getParameter());
	}
	
	private static final Function<ODocument, String> GET_ID_FUNCTION = new GetODocumentFieldValueFunction<String>("id");
	
	@Statement
	public List<String> selectProcessInstanceIdsByProcessDefinitionId(OPersistenceSession session, ListQueryParameterObject parameter) {
		ODatabaseDocument db = session.getDatabase();
		List<ODocument> resultSet = db.query(new OSQLSynchQuery<>("select id from "+getSchemaClass()+" where processDefinitionId = ?"), parameter.getParameter());
		return Lists.transform(resultSet, GET_ID_FUNCTION);
	}
	
}
