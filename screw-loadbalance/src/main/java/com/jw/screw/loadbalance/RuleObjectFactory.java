package com.jw.screw.loadbalance;


/**
 * @author jiangw
 */
public enum RuleObjectFactory {

    /**
     * 随机算法
     */
    RANDOM("RANDOM", new RandomRule()),

    /**
     * 轮询算法
     */
    ROUND_ROBIN("ROUND_ROBIN", new RoundRobinRule()),

    /**
     * 加权随机
     */
    WEIGHT_RANDOM("WEIGHT_RANDOM", new WeightRandomRule());

    private final Rule rule;

    private final String name;

    RuleObjectFactory(String name, Rule rule) {
        this.name = name;
        this.rule = rule;
    }

    public Rule getRule() {
        return this.rule;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 根据名称获取排序的规则
     * @param name
     * @return
     */
    public static com.jw.screw.loadbalance.Rule getRuleByName(String name) {
        for (RuleObjectFactory rule : values()) {
            if (rule.getName().equals(name)) {
                return rule.getRule();
            }
        }
        return WEIGHT_RANDOM.getRule();
    }
}
