package com.opc.common.function;

import com.opc.common.exception.BizException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * IrrUtil
 *
 * @author 莫问
 * @date 2018/10/15
 */
public class IrrUtil {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 7634415917398642321L;

    /**
     * 最小精度
     */
    private static final BigDecimal ACCURACY = new BigDecimal(0.00000001d);

    /**
     * 最大循环次数: excel用的是20次，不过精度只到小数点后两位
     * 而且不一定一定能算出值，为了尽可能保证算出结果，我增加到100次
     */
    private static final int INIT_MAX_LOOP = 100;

    /**
     * BigDecimal: 0
     */
    private static final BigDecimal ZERO = new BigDecimal(0);

    /**
     * BigDecimal: 1
     */
    private static final BigDecimal ONE = new BigDecimal(1);

    /**
     * BigDecimal: 0.005
     */
    private static final BigDecimal Z005 = new BigDecimal(0.005d);

    /**
     * BigDecimal: 0.2
     */
    private static final BigDecimal Z2 = new BigDecimal(0.2d);

    /**
     * 内部收益率: 计算IRR公式
     *
     * @param cashFlows 计算IRR需要提供的现金流集合
     * @return 内部收益率
     */
    public static BigDecimal irr(List<Double> cashFlows) {
        try {
            // 猜测默认为0.1
            return run(transArr(cashFlows), new BigDecimal(0.1d));
        } catch (Exception e) {
            throw new BizException("计算irr出错 ", e);
        }
    }

    /**
     * 内部收益率: 计算IRR公式
     *
     * @param cashFlows 计算IRR需要提供的现金流集合
     * @param cashFlows 计算IRR需要提供的现金流集合
     * @return 内部收益率
     */
    public static BigDecimal irr(List<Double> cashFlows, Double guess) {
        try {

            // 如果为空, 猜测默认为0.1
            if (Objects.isNull(guess)) {
                guess = 0.1d;
            }

            // 猜测默认为0.1
            return run(transArr(cashFlows), trans(guess));
        } catch (Exception e) {
            throw new BizException("计算irr出错 ", e);
        }
    }

    /**
     * 将其他类型的数字转换为大数（保证精度）
     *
     * @param ele 需要转换类型的数值
     * @return 转换后的大数
     */
    private static BigDecimal trans(Object ele) {
        try {
            String val = ele.toString();
            return new BigDecimal(val);
        } catch (Exception e) {
            throw new BizException("计算irr精确计算 类型转换异常", e);
        }
    }

    /**
     * 将数组转换为大数数组
     *
     * @param in 需要转换的数值集合
     * @return 转换后的数值集合
     */
    private static List<BigDecimal> transArr(List in) {
        List<BigDecimal> rt = new ArrayList<>();
        for (Object ele : in) {
            rt.add(trans(ele));
        }
        return rt;
    }

    /**
     * 内部收益率: 计算IRR公式
     *
     * @param cashFlow 现金流集合
     * @param guess    猜测假定值
     * @return 内部收益率
     */
    private static BigDecimal run(List<BigDecimal> cashFlow, BigDecimal guess) {
        BigDecimal maxRate = initRateMax(cashFlow, guess);
        BigDecimal minRate = initRateMin(cashFlow, guess);
        for (int i = 0; i < INIT_MAX_LOOP; i++) {
            BigDecimal testRate = minRate.add(maxRate)
                .divide(new BigDecimal(2d), 16, BigDecimal.ROUND_HALF_UP);
            BigDecimal npv = npv(cashFlow, testRate);
            if (npv.abs()
                .compareTo(ACCURACY) < 0) {
                guess = testRate;
                break;
            } else if (npv.compareTo(ZERO) < 0) {
                minRate = testRate;
            } else {
                maxRate = testRate;
            }
        }
        //保留16位小数（足够精度）
        return guess.setScale(16, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 生成一个使NPV为负数的R作为内部收益率下限值
     *
     * @param cashFlow 现金流集合
     * @param guess    猜测假定值
     * @return 内部收益率下限值
     */
    private static BigDecimal initRateMin(List<BigDecimal> cashFlow, BigDecimal guess) {
        for (int i = 0; i < INIT_MAX_LOOP; i++) {
            BigDecimal npv = npv(cashFlow, guess);

            if (npv.compareTo(ZERO) < 0) {
                return guess;
            }
            BigDecimal step = guess.abs()
                .multiply(Z2);
            guess = guess.add(step.compareTo(Z005) < 0 ? Z005 : step);
        }
        return guess;
    }

    /**
     * 生成一个使NPV为正数的R作为内部收益率的上限值
     *
     * @param cashFlow 现金流集合
     * @param guess    猜测假定值
     * @return 内部收益率的上限值
     */
    private static BigDecimal initRateMax(List<BigDecimal> cashFlow, BigDecimal guess) {
        for (int i = 0; i < INIT_MAX_LOOP; i++) {
            BigDecimal npv = npv(cashFlow, guess);

            if (npv.compareTo(ZERO) > 0) {
                return guess;
            }
            BigDecimal step = guess.abs()
                .multiply(Z2);
            guess = guess.subtract(step.compareTo(Z005) < 0 ? Z005 : step);
        }
        return guess;
    }

    /**
     * 计算NPV公式
     *
     * @param cashFlow 现金流集合
     * @param rate     率
     * @return NPV
     */
    private static BigDecimal npv(List<BigDecimal> cashFlow, BigDecimal rate) {
        BigDecimal npv = ZERO;
        // (1+r)^0
        BigDecimal rpowj = ONE;
        //1+r
        BigDecimal rad = rate.add(ONE);
        for (BigDecimal valuej : cashFlow) {
            // vj / (1+r)^j
            npv = npv.add(valuej.divide(rpowj, 10, BigDecimal.ROUND_HALF_DOWN));
            // (1+r)^j
            rpowj = rpowj.multiply(rad).setScale(16, BigDecimal.ROUND_HALF_DOWN);
        }
        return npv;
    }

}
