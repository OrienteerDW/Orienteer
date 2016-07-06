package org.orienteer.bpm.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.FoxFailedJobCommandFactory;

/**
 * {@link ProcessEngineConfiguration} for OrientDB implementation
 *
 */
public class OProcessEngineConfiguration extends StandaloneProcessEngineConfiguration {
	
	@Override
	protected void initPersistenceProviders() {
		addSessionFactory(new OPersistenceSessionFactory());
	}

	@Override
	public ProcessEngine buildProcessEngine() {
		super.buildProcessEngine();
		OPersistenceSession.staticInit(this);
		return processEngine;
	}

	@Override
	protected void initSqlSessionFactory() {
	}

	@Override
	protected void initDataSource() {
	}
	
	@Override
	protected void initJpa() {
	}
	
	@Override
	protected void initJobExecutor() {
		super.initJobExecutor();
		jobExecutor.setAutoActivate(true);
	}
	
	@Override
	protected void initFailedJobCommandFactory() {
		if (failedJobCommandFactory == null) {
	      failedJobCommandFactory = new FoxFailedJobCommandFactory();
	    }
	}
}
