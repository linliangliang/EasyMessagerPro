cmake_minimum_required(VERSION 3.10.0)

add_library(
			#library name
			daemon_one
			
			#set library property
			SHARED
			
			#library source path
			src/main/cpp/native-lib.cpp)