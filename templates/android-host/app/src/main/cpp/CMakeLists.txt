cmake_minimum_required(VERSION 3.22.1)
project(mindroidnative)

add_library(mindroidnative SHARED
    mindroid_native.cpp)

find_library(log-lib log)

target_link_libraries(mindroidnative
    ${log-lib})
