
package com.yibo.common.utils.number;

import com.yibo.common.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * 根据 公民身份证号码获取 年龄/性别/生日/生日年 月 日
 *
 * @author 莫问
 */
public class IdCardUtils {

    /**
     * 星座 期间判断值
     */
    private final static int[] DAY_ARR = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};

    /**
     * 中国公民 星座
     */
    private final static String[] CONSTELLATION_ARR =
            new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};

    /**
     * 中国公民 生肖
     */
    private final static String[] TWELVE_ANIMAL_ARR =
            new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};

    /**
     * 中国公民身份证号码最小长度。
     */
    private final int CHINA_ID_MIN_LENGTH = 15;

    /**
     * 中国公民身份证号码最大长度。
     */
    private final int CHINA_ID_MAX_LENGTH = 18;

    /**
     * 最低年限
     */
    private static final int MIN = 1900;

    /**
     * 根据身份证号获取年龄(截止到年)
     *
     * @param idNumber 身份证号
     * @return 年龄
     */
    public static int getAgeByIdNumberStopYear(String idNumber) {

        int iAge = 0;
        if (StringUtils.isNotBlank(idNumber)) {

            Calendar cal = Calendar.getInstance();
            String year = idNumber.substring(6, 10);
            int iCurrYear = cal.get(Calendar.YEAR);
            iAge = iCurrYear - Integer.valueOf(year);
        }

        return iAge;
    }

    /**
     * 根据身份编号获取年龄
     *
     * @param idCard 身份编号
     * @return 年龄(根据月份向取整)
     */
    public static int getAgeByMonth(String idCard) throws ParseException {
        // 获取省份证中的生日
        String time = idCard.substring(6, 12);

        // 设置格式将生日转换为日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date birthDay = null;

        try {
            birthDay = sdf.parse(time);
        } catch (ParseException e) {
            // 监控
            throw e;
        }

        //获取当前系统时间
        Calendar cal = Calendar.getInstance();

        //如果出生日期大于当前时间，则抛出异常
        if (cal.before(birthDay)) {
            throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
        }

        //取出系统当前时间的年、月、日部分
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);

        //将日期设置为出生日期
        cal.setTime(birthDay);

        //取出出生日期的年、月、日部分
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);

        //当前年份与出生年份相减，初步计算年龄
        int age = yearNow - yearBirth;

        // 当前月份与出生日期的月份相比，如果月份小于出生月份，则年龄上减1，表示不满多少周岁
        // 不满12月向下取整
        if (monthNow < monthBirth) {
            age--;
        }
        return age;
    }

    /**
     * 根据身份编号获取年龄(详细版)
     *
     * @param idCard 身份编号
     * @return 年龄2
     */
    public static int getAgeByIdCard2(String idCard) throws ParseException {

        // 获取省份证中的生日
        String time = idCard.substring(6, 14);

        // 设置格式将生日转换为日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date birthDay = null;

        try {
            birthDay = sdf.parse(time);
        } catch (ParseException e) {
            // 监控
            throw e;
        }

        //获取当前系统时间
        Calendar cal = Calendar.getInstance();

        //如果出生日期大于当前时间，则抛出异常
        if (cal.before(birthDay)) {
            throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
        }

        //取出系统当前时间的年、月、日部分
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        //将日期设置为出生日期
        cal.setTime(birthDay);

        //取出出生日期的年、月、日部分
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        //当前年份与出生年份相减，初步计算年龄
        int age = yearNow - yearBirth;

        //当前月份与出生日期的月份相比，如果月份小于出生月份，则年龄上减1，表示不满多少周岁
        if (monthNow <= monthBirth) {

            //如果月份相等，在比较日期，如果当前日，小于出生日，也减1，表示不满多少周岁
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        }
        return age;
    }

    /**
     * 根据身份编号获取生日
     *
     * @param idCard 身份编号
     * @return 生日(yyyyMMdd)
     */
    public static String getBirthByIdCard(String idCard) {
        return idCard.substring(6, 14);
    }

    /**
     * 根据身份编号获取生日年
     *
     * @param idCard 身份编号
     * @return 生日(yyyy)
     */
    public static Short getYearByIdCard(String idCard) {
        return Short.valueOf(idCard.substring(6, 10));
    }

    /**
     * 根据身份编号获取生日月
     *
     * @param idCard 身份编号
     * @return 生日(MM)
     */
    public static Short getMonthByIdCard(String idCard) {
        return Short.valueOf(idCard.substring(10, 12));
    }

    /**
     * 根据身份编号获取生日天
     *
     * @param idCard 身份编号
     * @return 生日(dd)
     */
    public static Short getDateByIdCard(String idCard) {
        return Short.valueOf(idCard.substring(12, 14));
    }

    /**
     * 根据身份编号获取性别
     *
     * @param idCard 身份编号
     * @return 性别(M 男, F 女, N 未知)
     */
    public static String getGenderByIdCard(String idCard) {
        String sGender = "未知";

        String sCardNum = idCard.substring(16, 17);
        try {
            if (Integer.parseInt(sCardNum) % 2 != 0) {
                return "男";
            } else {
                return "女";
            }
        } catch (Exception e) {
            return sGender;
        }
    }

    /**
     * 根据身份编号获取省份区域
     *
     * @param idCard 身份编号
     * @return 区域(省份)
     */
    public static String getAreaByIdCard(String idCard) {
        return idCard.substring(0, 2);
    }

    /**
     * 根据身份编号获取城市区域
     *
     * @param idCard 身份编号
     * @return 区域(城市)
     */
    public static String getCityByIdCard(String idCard) {
        return idCard.substring(0, 4);
    }

    /**
     * 根据身份证号获取星座
     *
     * @param idNumber 身份证号
     * @return 星座
     */
    public static String getConstellationByIdNumber(String idNumber) {

        if (StringUtils.isEmpty(idNumber)) {
            throw new BizException("身份证号不能为空");
        }

        // 获取生日 月份 和 天
        int month = Integer.valueOf(idNumber.substring(10, 12));
        int day = Integer.valueOf(idNumber.substring(12, 14));

        return day < DAY_ARR[month - 1] ? CONSTELLATION_ARR[month - 1] : CONSTELLATION_ARR[month];
    }

    /**
     * 判断身份证是否过期
     * <p>时间格式 : yyyy-MM-dd || yyyy-MM-dd hh:mm:ss</p>
     *
     * @param validTime 待校验日期
     * @param endTime   比较日期
     * @return true 过期; false 未过期;
     */
    public static boolean isExpired(String validTime, String endTime) {

        LocalDate validDate = LocalDate.parse(validTime.substring(0, 10));
        LocalDate endDate = LocalDate.parse(endTime.substring(0, 10));

        return validDate.isBefore(endDate);
    }

    /**
     * 根据身份证号获取生肖
     *
     * @param idNumber 身份证号
     * @return 生肖
     */
    public static String getTwelveAnimalsByIdNumber(String idNumber) {

        if (StringUtils.isEmpty(idNumber)) {
            throw new BizException("身份证号不能为空");
        }

        int year = Integer.valueOf(idNumber.substring(6, 10));
        if (year < MIN) {
            return "未知";
        }

        return TWELVE_ANIMAL_ARR[(year - MIN) % TWELVE_ANIMAL_ARR.length];
    }
}
