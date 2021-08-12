package com.jw.screw.storage.datax.job;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

public class JobInfoJexl implements JobInfoStrategy {

    private final static JexlEngine ENGINE = new JexlEngine();

    @Override
    public String execute(JobInfo jobInfo) {
        Expression expression = ENGINE.createExpression(jobInfo.getValue());
        Object evaluate = expression.evaluate(new MapContext());
        return evaluate.toString();
    }
}
