package com.yibo.common.monitor.endpoint;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

/**
 * Java线程转储中的火焰图
 *
 * @author yibo
 * @date 2021-05-06
 */
@ControllerEndpoint(id = "yibo-thread")
public class ThreadDumpFlameEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadDumpFlameEndPoint.class);

    private final ReentrantLock lock = new ReentrantLock();


    @GetMapping(path = "/flame", produces = "text/html")
    @SneakyThrows
    public void getThreadDumpFlame(HttpServletResponse response,
    @RequestParam(name = "prefix", defaultValue = "", required = false) String prefix) {
        final boolean locked = lock.tryLock();
        if (!locked) {
            response.getWriter().print("obtain lock failed");
            return;
        }
        try {
            final ThreadInfo[] threadInfos = ManagementFactory.getThreadMXBean()
                    .dumpAllThreads(true, true);
            Map<String, Integer> map = Maps.newHashMap();
            final Joiner joiner = Joiner.on(";");
            for (ThreadInfo threadInfo : threadInfos) {
                if (StringUtils.isNotBlank(prefix)
                        && !StringUtils.startsWith(threadInfo.getThreadName(), prefix)) {
                    continue;
                }
                List<String> stackTraceInfo = new ArrayList<>();
                for (StackTraceElement stackTraceElement : threadInfo.getStackTrace()) {
                    stackTraceInfo.add(stackTraceElement.toString()
                            .replace(" ", "-"));
                }
                final String s = joiner.join(Lists.reverse(stackTraceInfo));
                map.putIfAbsent(s, 1);
                final Integer count = map.get(s);
                map.put(s, count + 1);
            }
            response.addHeader("Content-Encoding", "gzip");
            try (final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream())) {
                try (final BufferedWriter writer = new BufferedWriter(new PrintWriter(gzipOutputStream))) {
                    List<String> data = Lists.newArrayList();
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        final String key = entry.getKey();
                        final Integer value = entry.getValue();
                        if (Strings.isNullOrEmpty(key)) {
                            continue;
                        }
                        data.add(key + " " + value);
                    }
                    FlameGraph flamegraph = new FlameGraph("thread-dump-flame", data);
                    flamegraph.write(writer);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @GetMapping(path = "/info", produces = "application/json")
    @ResponseBody
    @SneakyThrows
    public BoThreadDescriptor getThreadInfo(HttpServletResponse response, @RequestParam(name = "prefix", defaultValue = "", required = false) String prefix) {
        final boolean locked = lock.tryLock();
        if (!locked) {
            response.getWriter().print("obtain lock failed");
            return null;
        }
        try {
            final ThreadInfo[] threadInfos = ManagementFactory.getThreadMXBean()
                    .dumpAllThreads(true, true);
            // 按照状态聚合的
            Map<Thread.State, List<BoThreadInfo>> groupingByState = Maps.newHashMap();
            // 相同线程栈的
            Map<BoStackTraceElement, List<BoThreadInfo>> groupingByStackTrace = Maps.newHashMap();
            for (ThreadInfo threadInfo : threadInfos) {
                if (StringUtils.isNotBlank(prefix)
                        && !StringUtils.startsWith(threadInfo.getThreadName(), prefix)) {
                    continue;
                }
                // 按照状态聚合的
                final List<StackTraceElement> stackTraceElements = Arrays.asList(threadInfo.getStackTrace());
                final BoStackTraceElement lastStack = new BoStackTraceElement(stackTraceElements,
                        CollectionUtils.isEmpty(stackTraceElements) ? null :
                                stackTraceElements.get(stackTraceElements.size() - 1));
                final BoThreadInfo BoThreadInfo = new BoThreadInfo(lastStack, threadInfo.getThreadName(), threadInfo.getThreadState().name());
                groupingByState.putIfAbsent(threadInfo.getThreadState(), Lists.newArrayList());
                groupingByState.get(threadInfo.getThreadState()).add(BoThreadInfo);
                // 相同线程栈的
                groupingByStackTrace.putIfAbsent(lastStack, Lists.newArrayList());
                groupingByStackTrace.get(lastStack).add(BoThreadInfo);
            }
            return new BoThreadDescriptor(groupingByState, groupingByStackTrace);
        } finally {
            lock.unlock();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoThreadDescriptor {
        // 按照状态聚合的
        private Map<Thread.State, List<BoThreadInfo>> groupingByState;
        // 相同线程栈的
        private Map<BoStackTraceElement, List<BoThreadInfo>> groupingByStackTrace;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class BoThreadInfo {

        BoStackTraceElement stackTraceElement;

        String name;

        String status;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class BoStackTraceElement {

        @JsonIgnore
        @JSONField(serialize=false)
        List<StackTraceElement> stackTraceElements;

        StackTraceElement lastElement;
    }
}

