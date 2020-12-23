package org.flowable.engine.impl.variable;

import java.util.List;

import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.variable.api.types.ValueFields;
import org.flowable.variable.api.types.VariableType;

/**
 * @author Filip Hrisafov
 */
public class ParallelMultiInstanceLoopVariableType implements VariableType {

    public static final String TYPE_NAME = "bpmnParallelMultiInstanceCompleted";

    protected final ProcessEngineConfigurationImpl processEngineConfiguration;

    public ParallelMultiInstanceLoopVariableType(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public boolean isCachable() {
        return false;
    }

    @Override
    public boolean isAbleToStore(Object value) {
        return value instanceof ParallelMultiInstanceLoopVariable;
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) {
        if (value instanceof ParallelMultiInstanceLoopVariable) {
            valueFields.setTextValue(((ParallelMultiInstanceLoopVariable) value).getExecutionId());
            valueFields.setTextValue2(((ParallelMultiInstanceLoopVariable) value).getType());
        } else {
            valueFields.setTextValue(null);
            valueFields.setTextValue2(null);
        }
    }

    @Override
    public Object getValue(ValueFields valueFields) {
        String multiInstanceRootId = valueFields.getTextValue();
        String type = valueFields.getTextValue2();
        ExecutionEntity multiInstanceRootExecution = processEngineConfiguration.getExecutionEntityManager().findById(multiInstanceRootId);
        List<? extends ExecutionEntity> childExecutions = multiInstanceRootExecution.getExecutions();
        if (ParallelMultiInstanceLoopVariable.COMPLETED_INSTANCES.equals(type)) {
            // +0 for active, +1 for not active
            return (int) childExecutions.stream().filter(execution -> !execution.isActive() && !(execution.getCurrentFlowElement() instanceof BoundaryEvent)).count();
        } else if (ParallelMultiInstanceLoopVariable.ACTIVE_INSTANCES.equals(type)) {
            return (int) childExecutions.stream().filter(execution -> execution.isActive() && !(execution.getCurrentFlowElement() instanceof BoundaryEvent)).count();
        } else {
            //TODO maybe throw exception
            return 0;
        }
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
