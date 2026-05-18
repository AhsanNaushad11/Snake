cmake_minimum_required(VERSION 3.22.1)
project("snake")

add_library(
        snake-native
        SHARED
        native-lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        snake-native
        ${log-lib})
