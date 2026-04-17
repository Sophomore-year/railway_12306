package org.zys.rail_12306.framework.starter.designpattern.strategy;

import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;
import org.zys.rail_12306.framework.starter.bases.ApplicationContextHolder;
import org.zys.rail_12306.framework.starter.bases.init.ApplicationInitializingEvent;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 *策略选择器
 *
 * @author SUM
 * @date 2026/03/16
 */
public class AbstractStrategyChoose implements ApplicationListener<ApplicationInitializingEvent> {
    /**
     * 执行策略集合
     */
    private final Map<String, AbstractExecuteStrategy> abstractExecuteStrategyMap = new HashMap<>();

    /**
     * 根据 mark 查询具体策略
     *
     * @param mark          策略标识
     * @param predicateFlag 匹配范解析标识
     * @return 实际执行策略
     */
    public AbstractExecuteStrategy choose(String mark, Boolean predicateFlag) {
        if (predicateFlag != null && predicateFlag) {
            return abstractExecuteStrategyMap.values().stream()
                    // 1. 过滤掉 patternMatchMark 为空的策略
                    .filter(each -> StringUtils.hasText(each.patternMatchMark()))
                    // 2. 用正则匹配传入的 mark
                    .filter(each -> Pattern.compile(each.patternMatchMark()).matcher(mark).matches())
                    // 3. 找到第一个匹配的策略
                    .findFirst()
                    // 4. 没找到就抛异常
                    .orElseThrow(() -> new ServiceException("策略未定义"));
        }
        //拿 mark 当 key，去策略 Map 里直接取策略
        return Optional.ofNullable(abstractExecuteStrategyMap.get(mark))
                .orElseThrow(() -> new ServiceException(String.format("[%s] 策略未定义", mark)));
    }

    /**
     * 根据 mark 查询具体策略并执行
     *
     * @param mark         策略标识
     * @param requestParam 执行策略入参
     * @param <REQUEST>    执行策略入参范型
     */
    public <REQUEST> void chooseAndExecute(String mark, REQUEST requestParam) {
        AbstractExecuteStrategy executeStrategy = choose(mark, null);
        executeStrategy.execute(requestParam);
    }

    /**
     * 根据 mark 查询具体策略并执行
     *
     * @param mark          策略标识
     * @param requestParam  执行策略入参
     * @param predicateFlag 匹配范解析标识
     * @param <REQUEST>     执行策略入参范型
     */
    public <REQUEST> void chooseAndExecute(String mark, REQUEST requestParam, Boolean predicateFlag) {
        AbstractExecuteStrategy executeStrategy = choose(mark, predicateFlag);
        executeStrategy.execute(requestParam);
    }

    /**
     * 根据 mark 查询具体策略并执行，带返回结果
     *
     * @param mark         策略标识
     * @param requestParam 执行策略入参
     * @param <REQUEST>    执行策略入参范型
     * @param <RESPONSE>   执行策略出参范型
     * @return
     */
    public <REQUEST, RESPONSE> RESPONSE chooseAndExecuteResp(String mark, REQUEST requestParam) {
        //1. 根据 mark 找到对应的策略
        AbstractExecuteStrategy executeStrategy = choose(mark, null);
        // 2. 执行策略，并把返回值强转后返回给调用方
        return (RESPONSE) executeStrategy.executeResp(requestParam);
    }

    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        Map<String, AbstractExecuteStrategy> actual = ApplicationContextHolder.getBeansOfType(AbstractExecuteStrategy.class);
        actual.forEach((beanName, bean) -> {
            AbstractExecuteStrategy beanExist = abstractExecuteStrategyMap.get(bean.mark());
            if (beanExist != null) {
                throw new ServiceException(String.format("[%s] 重复执行策略", bean.mark()));
            }
            abstractExecuteStrategyMap.put(bean.mark(), bean);
        });
    }
}
