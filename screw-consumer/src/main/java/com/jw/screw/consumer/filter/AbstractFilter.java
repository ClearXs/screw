package com.jw.screw.consumer.filter;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:18
 * @since 1.0
 */
public abstract class AbstractFilter implements Filter {

    @Override
    public Integer weight() {
        return 0;
    }

    /**
     * 权重越大，优先级越高，相同权重按照添加顺序进行添加。
     */
    @Override
    public int compareTo(Filter o) {
        Integer weight = this.weight();
        Integer compareWeight = o.weight();
        return weight > compareWeight ? 1 :
                weight.equals(compareWeight) ? 0 : -1;
    }
}
