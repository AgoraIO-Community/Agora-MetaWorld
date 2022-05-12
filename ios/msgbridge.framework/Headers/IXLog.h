#ifndef __COMMON_LOG_H__
#define __COMMON_LOG_H__

#include "prefix.h"
#include <memory>
#include <string>
#include <vector>

#include "spdlog/async.h"
#include "spdlog/sinks/basic_file_sink.h"
#include "spdlog/sinks/stdout_color_sinks.h"
#include "spdlog/spdlog.h"
#if defined(OS_ANDROID)
#include "spdlog/sinks/android_sink.h"
#endif

static std::shared_ptr<spdlog::logger> slogger;

static void LogInitialize(const char *logger_name, const char *filename) {
  try {
    std::vector<spdlog::sink_ptr> sinks;
#if defined(OS_ANDROID)
    auto console_sink = std::make_shared<spdlog::sinks::android_sink_mt>();
#else
    auto console_sink = std::make_shared<spdlog::sinks::stdout_color_sink_mt>();
#endif

    console_sink->set_level(spdlog::level::trace);
    sinks.push_back(console_sink);

#if !defined(OS_ANDROID) && !defined(OS_IPHONE)
    auto file_sink =
        std::make_shared<spdlog::sinks::basic_file_sink_mt>(filename, true);
    file_sink->set_level(spdlog::level::trace);
    sinks.push_back(file_sink);
#endif

    slogger.reset(new spdlog::logger(logger_name, sinks.begin(), sinks.end()));
    slogger->set_level(spdlog::level::trace);
  } catch (std::exception &exception) {
  }
}

#define XLOG_TRACE(...)                                                        \
  SPDLOG_LOGGER_CALL(slogger.get(), spdlog::level::trace, __VA_ARGS__)
#define XLOG_DEBUG(...)                                                        \
  SPDLOG_LOGGER_CALL(slogger.get(), spdlog::level::debug, __VA_ARGS__)
#define XLOG_INFO(...)                                                         \
  SPDLOG_LOGGER_CALL(slogger.get(), spdlog::level::info, __VA_ARGS__)
#define XLOG_WARN(...)                                                         \
  SPDLOG_LOGGER_CALL(slogger.get(), spdlog::level::warn, __VA_ARGS__)
#define XLOG_ERROR(...)                                                        \
  SPDLOG_LOGGER_CALL(slogger.get(), spdlog::level::err, __VA_ARGS__)

#endif
