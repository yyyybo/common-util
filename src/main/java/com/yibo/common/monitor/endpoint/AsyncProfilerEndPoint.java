package com.yibo.common.monitor.endpoint;

import com.yibo.common.utils.TomcatUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

/**
 * 火焰图
 *
 * @author yibo
 * @date 2021-05-06
 */
@ControllerEndpoint(id = "async-profiler")
public class AsyncProfilerEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncProfilerEndPoint.class);

    private final ReentrantLock lock = new ReentrantLock();


    @GetMapping(path = "/", produces = "text/html")
    @SneakyThrows
    public void getProfilerInfo(HttpServletResponse response, @RequestParam(name = "duration", required = false, defaultValue = "15") String duration) {
        final boolean locked = lock.tryLock();
        String result = StringUtils.EMPTY;
        if (!locked) {
            result = "obtain lock failed";
            response.getWriter().print(result);
            return;
        }
        Process process;
        try {
            final long pid = TomcatUtil.getPid();
            if (pid <= 0) {
                LOGGER.error("get pid error");
                result = "error";
                response.getWriter().print(result);
                return;
            }
            process = Runtime.getRuntime().exec("/home/relx/tools/async-profiler/async-profiler -d " + duration + " -o svg " + pid);
            process.waitFor(5, TimeUnit.SECONDS);
            try (BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder sb = new StringBuilder(1024);
                String line = null;
                response.addHeader("Content-Encoding", "gzip");
                try(final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream())) {
                    try( PrintWriter writer = new PrintWriter(gzipOutputStream)) {
                        while ((line = stdoutReader.readLine()) != null) {
                            if(StringUtils.isBlank(line) || StringUtils.startsWith(line, "Started [cpu] profiling")) {
                                continue;
                            }
                            writer.print(line);
                            writer.print("\n");
                        }
                    }
                }

            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("get profile info failed", e);
            result =  e.getMessage();
            response.getWriter().print(result);
        } finally {
            lock.unlock();
        }
    }
}

