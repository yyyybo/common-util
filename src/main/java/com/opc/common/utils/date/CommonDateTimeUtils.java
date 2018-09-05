/*
 * Copyright (c) 2013 Qunar.com. All Rights Reserved.
 */
package com.opc.common.utils.date;

import com.opc.common.exception.CusException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.Time;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author yibo.yu
 */
public class CommonDateTimeUtils {

    /**
     * 星期
     */
    private final static String[] WEEKS = new String[] {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    /**
     * 时间格式 HH:mm
     */
    public static final String HH_MM = "HH:mm";

    /**
     * 时间点的日期格式化形式 "HH:mm"
     */
    public static final FastDateFormat DATE_FORMAT_HH_MM = FastDateFormat.getInstance(HH_MM);

    /**
     * 时间格式 HH:mm:ss
     */
    public static final String HH_MM_SS = "HH:mm:ss";

    /**
     * 时间点的日期格式化形式 "HH:mm:ss"
     */
    public static final FastDateFormat DATE_FORMAT_HH_MM_SS = FastDateFormat.getInstance(HH_MM_SS);

    /**
     * 日期格式 yyyy-MM-dd
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 日期格式化形式 "yyyy-MM-dd"
     */
    public static final FastDateFormat DATE_FORMAT_YYYY_MM_DD = FastDateFormat.getInstance(YYYY_MM_DD);

    /**
     * 日期格式 yyyy-MM-dd HH:mm:ss
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期时间格式化形式 "yyyy-MM-dd HH:mm:ss"
     */
    public static final FastDateFormat DATE_FORMAT_YYYY_MM_DD_HH_MM_SS =
        FastDateFormat.getInstance(YYYY_MM_DD_HH_MM_SS);

    /**
     * 日期格式 yyyy-MM-dd HH:mm
     */
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    /**
     * 日期时间格式化形式 "yyyy-MM-dd HH:mm"
     */
    public static final FastDateFormat DATE_FORMAT_YYYY_MM_DD_HH_MM = FastDateFormat.getInstance(YYYY_MM_DD_HH_MM);

    /**
     * 日期格式 yyyyMMddHHmmss
     */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * 日期时间格式化形式 "yyyyMMddHHmmss",比如可用于支付中心的日期处理
     */
    public static final FastDateFormat DATE_FORMAT_YYYYMMDDHHMMSS = FastDateFormat.getInstance(YYYYMMDDHHMMSS);

    /**
     * 日期格式 yyyy年MM月dd日 HH:mm:ss
     */
    private static final String CHINA_FORMAT_DATETIME_PATTERN = "yyyy年MM月dd日 HH:mm:ss";

    /**
     * 日期时间格式化形式 "yyyy年MM月dd日 HH:mm:ss"
     */
    public static final FastDateFormat DATE_FORMAT_CHINA_FORMAT_DATETIME_PATTERN =
        FastDateFormat.getInstance(CHINA_FORMAT_DATETIME_PATTERN);

    /**
     * 日期格式 yyyyMMdd
     */
    private static final String YYYYMMDD = "yyyyMMdd";

    /**
     * 日期格式 yyyyMMdd
     */
    public static final FastDateFormat DATE_FORMAT_YYYYMMDD = FastDateFormat.getInstance(YYYYMMDD);

    /**
     * 计算相差天数
     * <p>时间格式 : yyyy-MM-dd || yyyy-MM-dd hh:mm:ss</p>
     *
     * @param fTime 开始时间
     * @param oTime 结束时间
     * @return 相差天数(oTime - fTime)
     */
    public static long between(String fTime, String oTime) {

        if (fTime.length() >= 10 && oTime.length() >= 10) {
            LocalDate beginDate = LocalDate.parse(fTime.substring(0, 10));
            LocalDate endDate = LocalDate.parse(oTime.substring(0, 10));
            return endDate.toEpochDay() - beginDate.toEpochDay();
        } else {
            // 指标监控--日志
            throw new CusException("日期格式有误====----【日期-开始时间 {}】【日期-结束时间 {}】----====【正确格式为:{}】", fTime, oTime,
                "yyyy-MM-dd || yyyy-MM-dd hh:mm:ss");
        }
    }

    /**
     * 获得该日期下的星期
     *
     * @param dateStr 日期字符串(一个字符串形式[yyyy-MM-dd]的日期)
     * @return 星期几
     */
    public static String strDateToWeek(String dateStr) throws ParseException {
        Date date = parseDateStrictly(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekIndex < 0) {
            weekIndex = 0;
        }
        return WEEKS[weekIndex];
    }

    /**
     * 获取规定时间 几小时前或者几小时后的时间
     *
     * @param date 字符串时间
     * @param hour 时间格式(相差的小时,可以为正负数。 负数:指定小时前,正数:指定小时后)
     * @return Date格式的时间
     */
    public static Date getDateBeforeHour(Date date, int hour) {

        if (date != null) {
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.set(Calendar.HOUR, now.get(Calendar.HOUR) + hour);
            return now.getTime();
        }
        return null;
    }

    /**
     * 获取规定时间 几天前或者几天后的时间
     *
     * @param date 字符串时间
     * @param day  时间格式(相差的天数,可以为正负数。 负数:指定日期前,正数:指定日期后)
     * @return Date格式的时间
     */
    public static Date getDateBeforeDay(Date date, int day) {

        if (date != null) {
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
            return now.getTime();
        }
        return null;
    }

    /**
     * 获取规定时间 几月前或者几月后的时间
     *
     * @param date  字符串时间
     * @param month 时间格式(相差的月数,可以为正负数。 负数:指定日期前,正数:指定日期后)
     * @return Date格式的时间
     */
    public static Date getDateBeforeMonth(Date date, int month) {

        if (date != null) {
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.set(Calendar.MONTH, now.get(Calendar.MONTH) + month);
            return now.getTime();
        }
        return null;
    }

    /**
     * 获得某个日期下的 月份的天数
     *
     * @param date 时间字符串
     * @return 该月份一共多少天
     */
    public static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 严格格式化一个字符串形式[yyyy-MM-dd HH:mm:ss]的日期为Date
     *
     * @param str 日期字符串
     * @return 格式化后的日期
     * @throws ParseException 转化异常
     */
    public static Date parseDateTimeStrictly(String str) throws ParseException {
        return DateUtils.parseDateStrictly(str, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 严格格式化一个字符串形式[yyyy-MM-dd]的日期为Date
     *
     * @param str 日期字符串
     * @return 格式化后的日期
     * @throws ParseException 转化异常
     */
    public static Date parseDateStrictly(String str) throws ParseException {
        return DateUtils.parseDateStrictly(str, YYYY_MM_DD);
    }

    /**
     * 式化一个字符串形式[yyyy-MM-dd HH:mm:ss]的日期为Date
     *
     * @param str 日期字符串
     * @return 格式化后的日期
     * @throws ParseException 转化异常
     */
    public static Date parseDateTime(String str) throws ParseException {
        return DateUtils.parseDate(str, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 式化一个字符串形式[yyyy-MM-dd]的日期为Date
     *
     * @param str 日期字符串
     * @return 格式化后的日期
     * @throws ParseException 转化异常
     */
    public static Date parseDate(String str) throws ParseException {
        return DateUtils.parseDate(str, YYYY_MM_DD);
    }

    /**
     * 判断一个日期是否是上午<br/>
     * 这个实现比
     * <p/>
     * <pre>
     *     DateUtils.getFragmentInDays(date, Calendar.DAY_OF_YEAR) < 12;
     * </pre>
     * <p/>
     * 要快一半
     *
     * @param date 日期
     * @return true: 是 false: 否
     */
    public static boolean isAM(Date date) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.AM_PM) == Calendar.AM;
    }

    /**
     * 判断一个日期时间是否为下午
     *
     * @param date 日期
     * @return true: 是 false: 否
     */
    public static boolean isPM(Date date) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.AM_PM) == Calendar.PM;
    }

    /**
     * 获取当前时间的Date形式，把分，毫秒的数据截取 比如：
     * <p/>
     * <pre>
     *  当前时间 2012-12-13 15:30:35
     *  getTodayDateHHmm返回
     *  2012-12-13 15:30:00
     * </pre>
     *
     * @return 当前时间的
     */
    public static Date getTodayDateHHmm() {
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 获取当前时间的小时分钟形式 比如:
     * <p/>
     * <pre>
     *     当前时间：2012-12-05 15:33:25返回
     *     15:33:25
     * </pre>
     * <p/>
     * 相当与 1970-01-01 15:33:25
     *
     * @return 小时分钟
     */
    public static Time getTodayHHmmss() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);
        return new Time(calendar.getTime()
            .getTime());

    }

    /**
     * 将long型时间戳转化为日期(年月日)
     *
     * @param timemillis 时间戳
     * @return 转化好的日期(年月日)
     */
    public static String formatLongToDate(long timemillis) {
        if (timemillis == 0) {
            return StringUtils.EMPTY;
        }
        return DATE_FORMAT_CHINA_FORMAT_DATETIME_PATTERN.format(new Date(timemillis));
    }
}