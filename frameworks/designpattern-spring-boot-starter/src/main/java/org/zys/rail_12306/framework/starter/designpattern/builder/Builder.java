package org.zys.rail_12306.framework.starter.designpattern.builder;

import java.io.Serializable;

/**
 * Builder 模式抽象接口
 * */
public interface Builder<T> extends Serializable {

    /**
     *
     *
     * @return {@link T }
     */
    T build();
}
