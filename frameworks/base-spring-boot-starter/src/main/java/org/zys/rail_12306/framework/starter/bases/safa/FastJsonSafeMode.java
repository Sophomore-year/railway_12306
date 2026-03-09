package org.zys.rail_12306.framework.starter.bases.safa;

import org.springframework.beans.factory.InitializingBean;

public class FastJsonSafeMode implements InitializingBean {

    /*
    *  设置系统属性，启用 FastJSON 2.x 的安全模式,开启后关闭类型隐式传递
    * */
    @Override
    public void afterPropertiesSet() throws Exception {
        System.setProperty("fastjson2.parser.safeMode", "true");
    }
}
